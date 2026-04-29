package cn.cosx.blog.api.article.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 发布评论请求
 */
@Getter
@Setter
@NoArgsConstructor
public class CommentCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 根评论ID（一级评论不需要，二级评论必填）
     */
    private Long topId;

    /**
     * 直接回复的评论ID（一级评论不需要，二级评论必填）
     */
    private Long pid;
}