package cn.cosx.blog.article.domain.entity;

import cn.cosx.blog.database.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@TableName("article")
public class Article extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableField("author_id")
    private Long authorId;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("cover_image_url")
    private String coverImageUrl;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("comment_count")
    private Integer commentCount;

    @TableField("status")
    private Integer status;
}