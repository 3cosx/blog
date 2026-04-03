package cn.cosx.blog.api.product.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 商品分类查询请求
 *
 * @author cosx
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductCategoryQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 分类名称
     */
    private String name;
}
