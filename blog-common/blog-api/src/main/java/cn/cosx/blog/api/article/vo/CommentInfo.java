package cn.cosx.blog.api.article.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 评论信息VO（楼中楼扁平化设计）
 */
@Getter
@Setter
@NoArgsConstructor
public class CommentInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String userNickName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID，0表示一级评论
     */
    private Long pid;

    /**
     * 根评论ID
     */
    private Long topId;

    /**
     * 评论层级，1=一级评论
     */
    private Integer level;

    /**
     * 回复数量（一级评论专用）
     */
    private Integer replyCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 子评论列表
     */
    private List<CommentInfo> replies;
}