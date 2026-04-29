package cn.cosx.blog.api.article.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 文章列表查询请求
 */
@Getter
@Setter
@NoArgsConstructor
public class ArticleListRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 上一次查询的最大ID，null表示第一页
     */
    private Long lastId;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;
}