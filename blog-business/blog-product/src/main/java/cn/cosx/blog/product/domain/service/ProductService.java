package cn.cosx.blog.product.domain.service;

import cn.cosx.blog.api.product.request.ProductPageQueryRequest;
import cn.cosx.blog.api.product.vo.ProductInfo;
import cn.cosx.blog.product.domain.entity.Product;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品主表 Service 接口
 *
 * @author cosx
 */
public interface ProductService extends IService<Product> {

    /**
     * 根据ID查询商品
     *
     * @param productId 商品ID
     * @return 商品实体
     */
    Product findById(String productId);

    /**
     * 创建商品
     *
     * @param product 商品实体
     * @return 创建后的商品
     */
    Product createProduct(Product product);

    /**
     * 更新商品
     *
     * @param product 商品实体
     * @return 更新结果
     */
    Boolean updateProduct(Product product);

    /**
     * 商品上架
     *
     * @param productId 商品ID
     * @return 上架结果
     */
    Boolean onShelf(String productId);

    /**
     * 商品下架
     *
     * @param productId 商品ID
     * @return 下架结果
     */
    Boolean offShelf(String productId);

    List<ProductInfo> pageQueryProductInfo(ProductPageQueryRequest request);
}
