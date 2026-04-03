package cn.cosx.blog.api.product.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 商品分类信息VO
 *
 * @author cosx
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductCategoryInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private String id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类ID
     */
    private String parentId;

    /**
     * 排序值
     */
    private Integer sort;

    /**
     * 子分类列表
     */
    private List<ProductCategoryInfo> children;
}
