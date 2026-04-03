package cn.cosx.blog.api.product.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 商品查询请求
 *
 * @author cosx
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品分类ID
     */
    private String categoryId;

    /**
     * 商品状态
     */
    private String status;
}
