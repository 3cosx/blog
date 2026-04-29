package cn.cosx.blog.article.domain.entity;

import cn.cosx.blog.database.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 评论表实体类（楼中楼扁平化设计）
 * 一级评论：pid=0, topId=自身id, level=1
 * 子评论：pid=被回复的评论id, topId=一级评论id, level=父评论level+1
 */
@Setter
@Getter
@TableName("comment")
public class Comment extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    @TableField("article_id")
    private Long articleId;

    /**
     * 评论者ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 父评论ID，0表示一级评论
     */
    @TableField("pid")
    private Long pid;

    /**
     * 根评论ID，一级评论的topId为自身id
     */
    @TableField("top_id")
    private Long topId;

    /**
     * 评论层级，1=一级评论
     */
    @TableField("level")
    private Integer level;

    /**
     * 回复数量（一级评论专用）
     */
    @TableField("reply_count")
    private Integer replyCount;
}