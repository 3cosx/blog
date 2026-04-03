package cn.cosx.blog.product.domain.service.impl;

import cn.cosx.blog.api.product.enums.ProductStreamTypeEnum;
import cn.cosx.blog.api.product.request.ProductPageQueryRequest;
import cn.cosx.blog.api.product.vo.ProductInfo;
import cn.cosx.blog.base.exception.BizException;
import cn.cosx.blog.base.exception.RepoErrorCode;
import cn.cosx.blog.lock.DistributeLock;
import cn.cosx.blog.product.domain.entity.Product;
import cn.cosx.blog.product.domain.entity.ProductCategory;
import cn.cosx.blog.product.domain.entity.ProductStream;
import cn.cosx.blog.product.domain.service.ProductCategoryService;
import cn.cosx.blog.product.domain.service.ProductService;
import cn.cosx.blog.product.domain.service.ProductStreamService;
import cn.cosx.blog.product.infrastructure.mapper.ProductMapper;
import cn.cosx.blog.product.converter.ProductConverter;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品主表 Service 实现类
 *
 * @author cosx
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private ProductStreamService productStreamService;

    @Resource
    private ProductCategoryService productCategoryService;

    @Override
    public Product findById(String productId) {
        return productMapper.selectById(productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLock(keyExpression = "#product.identifier", scene = "PRODUCT_CREATE")
    public Product createProduct(Product product) {
        // 初始化商品
        product.init();
        boolean flag = this.save(product);
        Assert.isTrue(flag, () -> new BizException(RepoErrorCode.INSERT_FAILED));

        // 记录入库流水
        if (product.getTotalStock() != null && product.getTotalStock() > 0) {
            productStreamService.insertStream(product, ProductStreamTypeEnum.IN, product.getTotalStock());
        }

        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateProduct(Product product) {
        Product existProduct = productMapper.selectById(product.getId());
        Assert.notNull(existProduct, () -> new BizException(RepoErrorCode.DATA_NOT_FOUND));

        if (!updateById(product)) {
            throw new BizException(RepoErrorCode.UPDATE_FAILED);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean onShelf(String productId) {
        Product product = productMapper.selectById(productId);
        Assert.notNull(product, () -> new BizException(RepoErrorCode.DATA_NOT_FOUND));

        product.onShelf();
        if (!updateById(product)) {
            throw new BizException(RepoErrorCode.UPDATE_FAILED);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean offShelf(String productId) {
        Product product = productMapper.selectById(productId);
        Assert.notNull(product, () -> new BizException(RepoErrorCode.DATA_NOT_FOUND));

        product.offShelf();
        if (!updateById(product)) {
            throw new BizException(RepoErrorCode.UPDATE_FAILED);
        }
        return true;
    }

    @Override
    public List<ProductInfo> pageQueryProductInfo(ProductPageQueryRequest request) {
        // 获取该分类及其子分类的ID列表
        List<String> categoryIds = getCategoryIds(request.getCategoryId());
        
        if (categoryIds.isEmpty()) {
            return List.of();
        }

        // 使用游标分页查询商品
        List<Product> products = productMapper.selectByCategoryIdsWithCursor(
                categoryIds,
                request.getStatus(),
                request.getLastProductId(),
                request.getPageSize()
        );

        // 转换为ProductInfo
        return products.stream()
                .map(ProductConverter.INSTANCE::product2ProductInfo)
                .collect(Collectors.toList());
    }

    /**
     * 获取分类ID及其所有子分类ID
     */
    private List<String> getCategoryIds(String categoryId) {
        List<String> categoryIds = new ArrayList<>();
        if (categoryId == null) {
            return categoryIds;
        }
        
        // 添加当前分类ID
        categoryIds.add(categoryId);
        
        // 递归获取子分类ID
        loadChildrenCategoryIds(categoryId, categoryIds);
        
        return categoryIds;
    }

    /**
     * 递归加载子分类ID
     */
    private void loadChildrenCategoryIds(String parentId, List<String> categoryIds) {
        List<ProductCategory> children = productCategoryService.findByParentId(parentId);
        if (children == null || children.isEmpty()){
            return ;
        }
        for (ProductCategory child : children) {
            categoryIds.add(child.getId());
            loadChildrenCategoryIds(child.getId(), categoryIds);
        }
    }
}
