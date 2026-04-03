package cn.cosx.blog.api.product.response;

import cn.cosx.blog.api.product.vo.ProductCategoryInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 商品分类查询响应
 *
 * @author cosx
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductCategoryQueryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类信息
     */
    private ProductCategoryInfo categoryInfo;

    /**
     * 分类列表
     */
    private List<ProductCategoryInfo> categoryInfoList;
}
