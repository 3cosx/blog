package cn.cosx.blog.article.domain.entity;

import cn.cosx.blog.database.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@TableName("article_collect")
public class ArticleCollect extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableField("article_id")
    private Long articleId;

    @TableField("user_id")
    private Long userId;
}