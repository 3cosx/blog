package cn.cosx.blog.api.article.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 文章评论列表查询请求
 */
@Getter
@Setter
@NoArgsConstructor
public class CommentListRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 偏移量
     */
    private Integer offset = 0;

    /**
     * 每页数量
     */
    private Integer limit = 10;
}