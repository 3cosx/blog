package cn.cosx.blog.api.article.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 文章列表信息VO
 *
 * @author cosx
 */
@Getter
@Setter
@NoArgsConstructor
public class ArticleListInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 封面图URL
     */
    private String coverImageUrl;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 作者昵称
     */
    private String authorNickName;

    /**
     * 作者头像
     */
    private String authorAvatar;

    /**
     * 创建时间
     */
    private Date createTime;
}