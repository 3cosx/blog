package cn.cosx.blog.api.article.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 创建文章请求
 */
@Getter
@Setter
@NoArgsConstructor
public class ArticleCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 封面图URL
     */
    private String coverImageUrl;

    /**
     * 文章状态（0=草稿，1=已发布）
     */
    private Integer status = 0;
}