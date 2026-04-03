package cn.cosx.blog.product.domain.entity;

import cn.cosx.blog.api.product.enums.ProductStreamTypeEnum;
import cn.cosx.blog.database.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 商品流水表实体类
 *
 * @author cosx
 */
@Setter
@Getter
@TableName("product_stream")
public class ProductStream extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    @TableField("product_id")
    private String productId;

    /**
     * 流水类型：入库/出库/锁定/释放等
     */
    @TableField("stream_type")
    private String streamType;

    /**
     * 变动数量
     */
    @TableField("quantity")
    private Long quantity;

    /**
     * 变动前库存
     */
    @TableField("before_stock")
    private Long beforeStock;

    /**
     * 变动后库存
     */
    @TableField("after_stock")
    private Long afterStock;

    /**
     * 幂等号
     */
    @TableField("identifier")
    private String identifier;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建入库流水
     */
    public static ProductStream createInStream(String productId, Long quantity, Long beforeStock, String identifier) {
        ProductStream stream = new ProductStream();
        stream.setProductId(productId);
        stream.setStreamType(ProductStreamTypeEnum.IN.getCode());
        stream.setQuantity(quantity);
        stream.setBeforeStock(beforeStock);
        stream.setAfterStock(beforeStock + quantity);
        stream.setIdentifier(identifier);
        return stream;
    }

    /**
     * 创建出库流水
     */
    public static ProductStream createOutStream(String productId, Long quantity, Long beforeStock, String identifier) {
        ProductStream stream = new ProductStream();
        stream.setProductId(productId);
        stream.setStreamType(ProductStreamTypeEnum.OUT.getCode());
        stream.setQuantity(quantity);
        stream.setBeforeStock(beforeStock);
        stream.setAfterStock(beforeStock - quantity);
        stream.setIdentifier(identifier);
        return stream;
    }

    /**
     * 创建锁定流水
     */
    public static ProductStream createLockStream(String productId, Long quantity, Long beforeStock, String identifier) {
        ProductStream stream = new ProductStream();
        stream.setProductId(productId);
        stream.setStreamType(ProductStreamTypeEnum.LOCK.getCode());
        stream.setQuantity(quantity);
        stream.setBeforeStock(beforeStock);
        stream.setAfterStock(beforeStock - quantity);
        stream.setIdentifier(identifier);
        return stream;
    }
}
