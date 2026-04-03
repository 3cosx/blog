package cn.cosx.blog.api.product.request;

import cn.cosx.blog.base.request.PageRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductPageQueryRequest extends PageRequest {

    private static final long serialVersionUID = 1L;

    /**
     * 商品状态
     */
    private String status;

    private Long categoryId;

    private Long lastProductId;
}
