package cn.cosx.blog.api.product.response;

import cn.cosx.blog.api.product.vo.ProductInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 商品查询响应
 *
 * @author cosx
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductQueryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品信息
     */
    private ProductInfo productInfo;

    /**
     * 商品列表
     */
    private List<ProductInfo> productInfoList;
}
