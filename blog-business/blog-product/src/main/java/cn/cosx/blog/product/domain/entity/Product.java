package cn.cosx.blog.product.domain.entity;

import cn.cosx.blog.api.product.enums.ProductStatusEnum;
import cn.cosx.blog.database.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品主表实体类
 *
 * @author cosx
 */
@Setter
@Getter
@TableName("product")
public class Product extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 商品名称
     */
    @TableField("name")
    private String name;

    /**
     * 商品封面图
     */
    @TableField("cover")
    private String cover;

    /**
     * 商品分类ID
     */
    @TableField("category_id")
    private String categoryId;

    /**
     * 商品价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 总库存
     */
    @TableField("total_stock")
    private Long totalStock;

    /**
     * 可销售库存
     */
    @TableField("saleable_stock")
    private Long saleableStock;

    /**
     * 已占用库存
     */
    @TableField("occupied_stock")
    private Long occupiedStock;

    /**
     * 冻结库存
     */
    @TableField("frozen_stock")
    private Long frozenStock;

    /**
     * 商品详情
     */
    @TableField("detail")
    private String detail;

    /**
     * 商品状态
     */
    @TableField("status")
    private String status;

    /**
     * 发售时间
     */
    @TableField("sale_time")
    private Date saleTime;

    /**
     * 幂等号
     */
    @TableField("identifier")
    private String identifier;

    /**
     * 创建人
     */
    @TableField("create_by")
    private String createBy;

    /**
     * 初始化商品
     */
    public void init() {
        this.status = ProductStatusEnum.DRAFT.getCode();
        if (this.totalStock == null) {
            this.totalStock = 0L;
        }
        this.saleableStock = this.totalStock;
        this.occupiedStock = 0L;
        this.frozenStock = 0L;
    }

    /**
     * 上架
     */
    public void onShelf() {
        this.status = ProductStatusEnum.ON_SHELF.getCode();
    }

    /**
     * 下架
     */
    public void offShelf() {
        this.status = ProductStatusEnum.OFF_SHELF.getCode();
    }
}
