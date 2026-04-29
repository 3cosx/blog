package cn.cosx.blog.article.domain.service.impl;

import cn.cosx.blog.api.article.vo.ArticleDetailInfo;
import cn.cosx.blog.api.article.vo.ArticleListInfo;
import cn.cosx.blog.api.user.request.UserQueryRequest;
import cn.cosx.blog.api.user.service.UserFacadeService;
import cn.cosx.blog.api.user.vo.UserInfo;
import cn.cosx.blog.article.domain.entity.Article;
import cn.cosx.blog.article.domain.entity.ArticleCollect;
import cn.cosx.blog.article.domain.entity.ArticleImage;
import cn.cosx.blog.article.domain.entity.ArticleLike;
import cn.cosx.blog.article.domain.service.ArticleService;
import cn.cosx.blog.article.infrastructure.mapper.ArticleCollectMapper;
import cn.cosx.blog.article.infrastructure.mapper.ArticleImageMapper;
import cn.cosx.blog.article.infrastructure.mapper.ArticleLikeMapper;
import cn.cosx.blog.article.infrastructure.mapper.ArticleMapper;
import cn.cosx.blog.base.response.Response;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文章服务实现类
 *
 * @author cosx
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleCollectMapper articleCollectMapper;
    private final ArticleImageMapper articleImageMapper;
    private final UserFacadeService userFacadeService;

    @Override
    public Page<ArticleListInfo> pageQuery(Integer pageNum, Integer pageSize) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getStatus, 1).orderByDesc(Article::getCreateTime);

        Page<Article> articlePage = articleMapper.selectPage(page, queryWrapper);
        Page<ArticleListInfo> resultPage = new Page<>(articlePage.getCurrent(), articlePage.getSize(), articlePage.getTotal());

        if (CollectionUtils.isEmpty(articlePage.getRecords())) {
            return resultPage;
        }

        // 获取作者信息
        List<Long> authorIds = articlePage.getRecords().stream()
                .map(Article::getAuthorId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, UserInfo> userInfoMap = getUserInfoMap(authorIds);

        // 转换VO
        List<ArticleListInfo> articleListInfos = articlePage.getRecords().stream()
                .map(article -> {
                    ArticleListInfo info = new ArticleListInfo();
                    info.setId(article.getId());
                    info.setTitle(article.getTitle());
                    info.setCoverImageUrl(article.getCoverImageUrl());
                    info.setViewCount(article.getViewCount());
                    info.setLikeCount(article.getLikeCount());
                    info.setCommentCount(article.getCommentCount());
                    info.setCreateTime(article.getCreateTime());

                    UserInfo userInfo = userInfoMap.get(article.getAuthorId());
                    if (userInfo != null) {
                        info.setAuthorNickName(userInfo.getNickName());
                        info.setAuthorAvatar(userInfo.getProfilePhotoUrl());
                    }
                    return info;
                })
                .collect(Collectors.toList());

        resultPage.setRecords(articleListInfos);
        return resultPage;
    }

    @Override
    public ArticleDetailInfo getDetail(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            return null;
        }

        // 增加浏览量
        article.setViewCount(article.getViewCount() == null ? 1 : article.getViewCount() + 1);
        articleMapper.updateById(article);

        // 获取作者信息
        UserInfo userInfo = getUserInfo(article.getAuthorId());

        // 获取文章图片列表
        LambdaQueryWrapper<ArticleImage> imageQueryWrapper = new LambdaQueryWrapper<>();
        imageQueryWrapper.eq(ArticleImage::getArticleId, articleId);
        List<ArticleImage> articleImages = articleImageMapper.selectList(imageQueryWrapper);
        List<String> imageUrls = articleImages.stream()
                .map(ArticleImage::getImageUrl)
                .collect(Collectors.toList());

        // 转换VO
        ArticleDetailInfo info = new ArticleDetailInfo();
        info.setId(article.getId());
        info.setTitle(article.getTitle());
        info.setContent(article.getContent());
        info.setCoverImageUrl(article.getCoverImageUrl());
        info.setViewCount(article.getViewCount());
        info.setLikeCount(article.getLikeCount());
        info.setCommentCount(article.getCommentCount());
        info.setAuthorId(article.getAuthorId());
        info.setCreateTime(article.getCreateTime());
        info.setUpdateTime(article.getUpdateTime());
        info.setImageUrls(imageUrls);

        if (userInfo != null) {
            info.setAuthorNickName(userInfo.getNickName());
            info.setAuthorAvatar(userInfo.getProfilePhotoUrl());
        }

        return info;
    }

    @Override
    public Article createArticle(Article article) {
        if (article.getStatus() == null) {
            article.setStatus(0);
        }
        articleMapper.insert(article);
        return article;
    }

    @Override
    public Boolean updateArticle(Article article) {
        return articleMapper.updateById(article) > 0;
    }

    @Override
    public Boolean deleteArticle(Long articleId) {
        return articleMapper.deleteById(articleId) > 0;
    }

    @Override
    public Boolean likeArticle(Long articleId, Long userId) {
        // 检查是否已经点赞
        LambdaQueryWrapper<ArticleLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleLike::getArticleId, articleId).eq(ArticleLike::getUserId, userId);
        Long count = articleLikeMapper.selectCount(queryWrapper);

        if (count > 0) {
            return false;
        }

        // 创建点赞记录
        ArticleLike articleLike = new ArticleLike();
        articleLike.setArticleId(articleId);
        articleLike.setUserId(userId);
        articleLikeMapper.insert(articleLike);

        // 更新文章点赞数
        Article article = articleMapper.selectById(articleId);
        if (article != null) {
            article.setLikeCount(article.getLikeCount() == null ? 1 : article.getLikeCount() + 1);
            articleMapper.updateById(article);
        }

        return true;
    }

    @Override
    public Boolean collectArticle(Long articleId, Long userId) {
        // 检查是否已经收藏
        LambdaQueryWrapper<ArticleCollect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleCollect::getArticleId, articleId).eq(ArticleCollect::getUserId, userId);
        Long count = articleCollectMapper.selectCount(queryWrapper);

        if (count > 0) {
            return false;
        }

        // 创建收藏记录
        ArticleCollect articleCollect = new ArticleCollect();
        articleCollect.setArticleId(articleId);
        articleCollect.setUserId(userId);
        articleCollectMapper.insert(articleCollect);

        return true;
    }

    @Override
    public List<ArticleListInfo> getCollectedArticles(Long userId) {
        // 查询用户收藏的文章ID列表
        LambdaQueryWrapper<ArticleCollect> collectQueryWrapper = new LambdaQueryWrapper<>();
        collectQueryWrapper.eq(ArticleCollect::getUserId, userId);
        List<ArticleCollect> collects = articleCollectMapper.selectList(collectQueryWrapper);

        if (CollectionUtils.isEmpty(collects)) {
            return new ArrayList<>();
        }

        List<Long> articleIds = collects.stream()
                .map(ArticleCollect::getArticleId)
                .collect(Collectors.toList());

        // 查询文章列表
        LambdaQueryWrapper<Article> articleQueryWrapper = new LambdaQueryWrapper<>();
        articleQueryWrapper.in(Article::getId, articleIds);
        List<Article> articles = articleMapper.selectList(articleQueryWrapper);

        // 获取作者信息
        List<Long> authorIds = articles.stream()
                .map(Article::getAuthorId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, UserInfo> userInfoMap = getUserInfoMap(authorIds);

        // 转换VO
        return articles.stream()
                .map(article -> {
                    ArticleListInfo info = new ArticleListInfo();
                    info.setId(article.getId());
                    info.setTitle(article.getTitle());
                    info.setCoverImageUrl(article.getCoverImageUrl());
                    info.setViewCount(article.getViewCount());
                    info.setLikeCount(article.getLikeCount());
                    info.setCommentCount(article.getCommentCount());
                    info.setCreateTime(article.getCreateTime());

                    UserInfo userInfo = userInfoMap.get(article.getAuthorId());
                    if (userInfo != null) {
                        info.setAuthorNickName(userInfo.getNickName());
                        info.setAuthorAvatar(userInfo.getProfilePhotoUrl());
                    }
                    return info;
                })
                .collect(Collectors.toList());
    }

    /**
     * 批量获取用户信息
     */
    private Map<Long, UserInfo> getUserInfoMap(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Map.of();
        }

        List<UserInfo> userInfos = new ArrayList<>();
        for (Long userId : userIds) {
            UserInfo userInfo = getUserInfo(userId);
            if (userInfo != null) {
                userInfos.add(userInfo);
            }
        }

        return userInfos.stream()
                .collect(Collectors.toMap(
                        info -> Long.parseLong(info.getUserId()),
                        info -> info,
                        (v1, v2) -> v1
                ));
    }

    /**
     * 获取单个用户信息
     */
    private UserInfo getUserInfo(Long userId) {
        try {
            UserQueryRequest request = new UserQueryRequest();
            request.setUserId(String.valueOf(userId));
            Response<UserInfo> response = userFacadeService.queryUserById(request);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("获取用户信息失败, userId: {}", userId, e);
        }
        return null;
    }
}