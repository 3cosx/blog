package cn.cosx.blog.api.product.service;

import cn.cosx.blog.api.product.request.ProductCategoryQueryRequest;
import cn.cosx.blog.api.product.request.ProductCreateRequest;
import cn.cosx.blog.api.product.request.ProductQueryRequest;
import cn.cosx.blog.api.product.request.ProductUpdateRequest;
import cn.cosx.blog.api.product.response.ProductCategoryQueryResponse;
import cn.cosx.blog.api.product.response.ProductQueryResponse;
import cn.cosx.blog.api.product.vo.ProductCategoryInfo;
import cn.cosx.blog.api.product.vo.ProductInfo;
import cn.cosx.blog.base.response.Response;

/**
 * 商品服务 Dubbo 接口
 *
 * @author cosx
 */
public interface ProductFacadeService {

    /**
     * 根据商品ID查询商品信息
     *
     * @param request 商品查询请求
     * @return 商品信息响应
     */
    Response<ProductInfo> queryProductById(ProductQueryRequest request);

    /**
     * 创建商品
     *
     * @param request 商品创建请求
     * @return 商品信息
     */
    Response<ProductInfo> createProduct(ProductCreateRequest request);

    /**
     * 更新商品信息
     *
     * @param request 商品更新请求
     * @return 更新结果
     */
    Response<Boolean> updateProduct(ProductUpdateRequest request);

    /**
     * 商品上架
     *
     * @param productId 商品ID
     * @return 上架结果
     */
    Response<Boolean> onShelf(String productId);

    /**
     * 商品下架
     *
     * @param productId 商品ID
     * @return 下架结果
     */
    Response<Boolean> offShelf(String productId);

    /**
     * 根据分类ID查询分类信息
     *
     * @param request 分类查询请求
     * @return 分类信息
     */
    Response<ProductCategoryInfo> queryCategoryById(ProductCategoryQueryRequest request);

    /**
     * 查询子分类列表
     *
     * @param request 分类查询请求
     * @return 分类列表
     */
    Response<ProductCategoryQueryResponse> querySubCategories(ProductCategoryQueryRequest request);
}
