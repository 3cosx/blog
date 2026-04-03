package cn.cosx.blog.product.domain.entity;

import cn.cosx.blog.database.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 商品分类表实体类
 *
 * @author cosx
 */
@Setter
@Getter
@TableName("product_category")
public class ProductCategory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 分类名称
     */
    @TableField("name")
    private String name;

    /**
     * 父分类ID，0=顶级分类
     */
    @TableField("parent_id")
    private String parentId;

    /**
     * 排序值
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 子分类列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<ProductCategory> children;

    /**
     * 判断是否为顶级分类
     */
    public boolean isTopCategory() {
        return "0".equals(parentId) || parentId == null;
    }

}
