package cn.cosx.blog.base.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PageRequest extends BaseRequest {
    private static final long serialVersionUID = 1L;

    /**
     * 当前页
     */
    private int currentPage = 1;
    /**
     * 每页结果数
     */
    private int pageSize = 10;
}