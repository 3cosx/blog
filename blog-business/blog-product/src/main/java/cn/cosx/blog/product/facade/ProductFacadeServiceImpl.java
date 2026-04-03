package cn.cosx.blog.product.facade;

import cn.cosx.blog.api.product.request.ProductCategoryQueryRequest;
import cn.cosx.blog.api.product.request.ProductCreateRequest;
import cn.cosx.blog.api.product.request.ProductQueryRequest;
import cn.cosx.blog.api.product.request.ProductUpdateRequest;
import cn.cosx.blog.api.product.response.ProductCategoryQueryResponse;
import cn.cosx.blog.api.product.service.ProductFacadeService;
import cn.cosx.blog.api.product.vo.ProductCategoryInfo;
import cn.cosx.blog.api.product.vo.ProductInfo;
import cn.cosx.blog.base.response.Response;
import cn.cosx.blog.product.converter.ProductConverter;
import cn.cosx.blog.product.domain.entity.Product;
import cn.cosx.blog.product.domain.entity.ProductCategory;
import cn.cosx.blog.product.domain.service.ProductCategoryService;
import cn.cosx.blog.product.domain.service.ProductService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务 Dubbo 实现类
 *
 * @author cosx
 */
@DubboService(version = "1.0.0")
public class ProductFacadeServiceImpl implements ProductFacadeService {

    @Resource
    private ProductService productService;

    @Resource
    private ProductCategoryService productCategoryService;

    @Override
    public Response<ProductInfo> queryProductById(ProductQueryRequest request) {
        Product product = productService
                .lambdaQuery()
                .eq(request.getProductId() != null, Product::getId, request.getProductId())
                .one();
        ProductInfo productInfo = ProductConverter.INSTANCE.product2ProductInfo(product);
        return Response.of(productInfo);
    }

    @Override
    public Response<ProductInfo> createProduct(ProductCreateRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setCover(request.getCover());
        product.setCategoryId(request.getCategoryId());
        product.setPrice(request.getPrice());
        product.setTotalStock(request.getTotalStock());
        product.setDetail(request.getDetail());
        product.setSaleTime(request.getSaleTime());
        product.setIdentifier(request.getIdentifier());

        Product createdProduct = productService.createProduct(product);
        ProductInfo productInfo = ProductConverter.INSTANCE.product2ProductInfo(createdProduct);
        return Response.of(productInfo);
    }

    @Override
    public Response<Boolean> updateProduct(ProductUpdateRequest request) {
        Product product = new Product();
        product.setId(request.getProductId());
        product.setName(request.getName());
        product.setCover(request.getCover());
        product.setCategoryId(request.getCategoryId());
        product.setPrice(request.getPrice());
        product.setDetail(request.getDetail());
        product.setSaleTime(request.getSaleTime());

        Boolean result = productService.updateProduct(product);
        return Response.of(result);
    }

    @Override
    public Response<Boolean> onShelf(Long productId) {
        Boolean result = productService.onShelf(productId);
        return Response.of(result);
    }

    @Override
    public Response<Boolean> offShelf(Long productId) {
        Boolean result = productService.offShelf(productId);
        return Response.of(result);
    }

    @Override
    public Response<ProductCategoryInfo> queryCategoryById(ProductCategoryQueryRequest request) {
        ProductCategory category = productCategoryService
                .lambdaQuery()
                .eq(request.getCategoryId() != null, ProductCategory::getId, request.getCategoryId())
                .one();
        ProductCategoryInfo categoryInfo = ProductConverter.INSTANCE.category2CategoryInfo(category);
        return Response.of(categoryInfo);
    }

    @Override
    public Response<ProductCategoryQueryResponse> querySubCategories(ProductCategoryQueryRequest request) {
        Long parentId = request.getParentId() != null ? request.getParentId() : 0L;
        List<ProductCategory> categories = productCategoryService.findByParentId(parentId);
        
        List<ProductCategoryInfo> categoryInfoList = categories.stream()
                .map(ProductConverter.INSTANCE::category2CategoryInfo)
                .collect(Collectors.toList());

        ProductCategoryQueryResponse response = new ProductCategoryQueryResponse();
        response.setCategoryInfoList(categoryInfoList);
        return Response.of(response);
    }
}
