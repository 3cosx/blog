package cn.cosx.blog.api.product.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品更新请求
 *
 * @author cosx
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductUpdateRequest implements Serializable {

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
     * 商品封面图
     */
    private String cover;

    /**
     * 商品分类ID
     */
    private String categoryId;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品详情
     */
    private String detail;

    /**
     * 发售时间
     */
    private Date saleTime;
}
