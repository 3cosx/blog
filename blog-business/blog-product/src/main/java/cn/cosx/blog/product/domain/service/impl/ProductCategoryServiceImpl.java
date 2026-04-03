package cn.cosx.blog.product.domain.service.impl;

import cn.cosx.blog.product.domain.entity.ProductCategory;
import cn.cosx.blog.product.domain.service.ProductCategoryService;
import cn.cosx.blog.product.infrastructure.mapper.ProductCategoryMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品分类表 Service 实现类
 *
 * @author cosx
 */
@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {

    @Resource
    private ProductCategoryMapper productCategoryMapper;

    @Override
    public ProductCategory findById(String categoryId) {
        return productCategoryMapper.selectById(categoryId);
    }

    @Override
    public List<ProductCategory> findByParentId(String parentId) {
        return this.lambdaQuery()
                .eq(ProductCategory::getParentId, parentId)
                .orderByAsc(ProductCategory::getSort)
                .list();
    }

    @Override
    public List<ProductCategory> findAll() {
        return this.lambdaQuery()
                .orderByAsc(ProductCategory::getSort)
                .list();
    }

    @Override
    public List<ProductCategory> findAllCategoryTree() {
        // 查询所有分类
        List<ProductCategory> allCategories = productCategoryMapper.selectList(null);
        if(CollectionUtils.isEmpty(allCategories)){
            return Lists.newArrayList();
        }
        // 按父ID分组
        Map<String, List<ProductCategory>> categoryMap = allCategories.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? "0" : c.getParentId()));

        // 获取顶级分类
        List<ProductCategory> topCategories = categoryMap.getOrDefault("0", new ArrayList<>());

        // 递归设置子分类
        setChildren(topCategories, categoryMap);

        return topCategories;
    }

    /**
     * 递归设置子分类
     */
    private void setChildren(List<ProductCategory> categories, Map<String, List<ProductCategory>> categoryMap) {
        if (categories == null || categories.isEmpty()) {
            return;
        }
        for (ProductCategory category : categories) {
            List<ProductCategory> children = categoryMap.get(category.getId());
            if (children != null && !children.isEmpty()) {
                // 设置子分类
                category.setChildren(children);
                // 递归设置子分类的子分类
                setChildren(children, categoryMap);
            }
        }
    }

}
