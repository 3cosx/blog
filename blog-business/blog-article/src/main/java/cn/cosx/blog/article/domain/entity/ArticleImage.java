package cn.cosx.blog.article.domain.entity;

import cn.cosx.blog.database.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@TableName("article_image")
public class ArticleImage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableField("article_id")
    private Long articleId;

    @TableField("image_url")
    private String imageUrl;
}