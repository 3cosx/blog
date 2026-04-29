package cn.cosx.blog.article.domain.service;

import cn.cosx.blog.api.article.vo.ArticleDetailInfo;
import cn.cosx.blog.api.article.vo.ArticleListInfo;
import cn.cosx.blog.article.domain.entity.Article;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ArticleService extends IService<Article> {

    /**
     * 根据游标分页查询已发布的文章
     *
     * @param lastId   上一次查询的最大ID，null表示第一页
     * @param pageSize 每页数量
     * @return 文章列表
     */
    List<ArticleListInfo> pageQuery(Long lastId, Integer pageSize);

    /**
     * 获取文章详情
     *
     * @param articleId 文章ID
     * @return 文章详情
     */
    ArticleDetailInfo getDetail(Long articleId);

    /**
     * 创建文章
     *
     * @param article 文章实体
     * @return 创建的文章
     */
    Article createArticle(Article article);

    /**
     * 更新文章
     *
     * @param article 文章实体
     * @return 更新是否成功
     */
    Boolean updateArticle(Article article);

    /**
     * 删除文章（逻辑删除）
     *
     * @param articleId 文章ID
     * @return 删除是否成功
     */
    Boolean deleteArticle(Long articleId);

    /**
     * 点赞文章
     *
     * @param articleId 文章ID
     * @param userId    用户ID
     * @return 点赞是否成功
     */
    Boolean likeArticle(Long articleId, Long userId);

    /**
     * 收藏文章
     *
     * @param articleId 文章ID
     * @param userId    用户ID
     * @return 收藏是否成功
     */
    Boolean collectArticle(Long articleId, Long userId);

    /**
     * 获取用户收藏的文章列表
     *
     * @param userId 用户ID
     * @return 收藏的文章列表
     */
    List<ArticleListInfo> getCollectedArticles(Long userId);
}