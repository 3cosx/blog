package cn.cosx.blog.api.product.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品信息VO
 *
 * @author cosx
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

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
     * 可销售库存
     */
    private Long saleableStock;

    /**
     * 已占用库存
     */
    private Long occupiedStock;

    /**
     * 冻结库存
     */
    private Long frozenStock;

    /**
     * 商品详情
     */
    private String detail;

    /**
     * 商品状态
     */
    private String status;

    /**
     * 发售时间
     */
    private Date saleTime;
}
