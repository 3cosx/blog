package cn.cosx.blog.api.product.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品创建请求
 *
 * @author cosx
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

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
    private Long categoryId;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 总库存
     */
    private Long totalStock;

    /**
     * 商品详情
     */
    private String detail;

    /**
     * 发售时间
     */
    private Date saleTime;

    /**
     * 幂等号
     */
    private String identifier;
}
