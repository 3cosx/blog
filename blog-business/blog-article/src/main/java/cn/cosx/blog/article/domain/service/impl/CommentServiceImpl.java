package cn.cosx.blog.article.domain.service.impl;

import cn.cosx.blog.api.article.vo.CommentInfo;
import cn.cosx.blog.api.user.request.UserQueryRequest;
import cn.cosx.blog.api.user.service.UserFacadeService;
import cn.cosx.blog.api.user.vo.UserInfo;
import cn.cosx.blog.article.domain.entity.Article;
import cn.cosx.blog.article.domain.entity.Comment;
import cn.cosx.blog.article.domain.service.CommentService;
import cn.cosx.blog.article.infrastructure.mapper.ArticleMapper;
import cn.cosx.blog.article.infrastructure.mapper.CommentMapper;
import cn.cosx.blog.base.response.Response;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 *
 * @author cosx
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final UserFacadeService userFacadeService;

    @Override
    public List<CommentInfo> getComments(Long articleId) {
        // 查询文章的所有评论
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getArticleId, articleId).orderByAsc(Comment::getCreateTime);
        List<Comment> comments = commentMapper.selectList(queryWrapper);

        if (CollectionUtils.isEmpty(comments)) {
            return new ArrayList<>();
        }

        // 获取所有评论的用户信息
        List<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, UserInfo> userInfoMap = getUserInfoMap(userIds);

        // 分隔顶层评论和子评论
        List<Comment> topLevelComments = new ArrayList<>();
        List<Comment> childComments = new ArrayList<>();
        for (Comment comment : comments) {
            if (comment.getParentId() == null) {
                topLevelComments.add(comment);
            } else {
                childComments.add(comment);
            }
        }

        // 构建子评论映射（parentId -> replies）
        Map<Long, List<CommentInfo>> childCommentMap = childComments.stream()
                .map(comment -> convertToCommentInfo(comment, userInfoMap))
                .collect(Collectors.groupingBy(CommentInfo::getParentId));

        // 构建顶层评论列表
        return topLevelComments.stream()
                .map(comment -> {
                    CommentInfo info = convertToCommentInfo(comment, userInfoMap);
                    info.setReplies(childCommentMap.getOrDefault(comment.getId(), new ArrayList<>()));
                    return info;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Comment addComment(Long articleId, Long userId, String content, Long parentId) {
        // 创建评论
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId);
        commentMapper.insert(comment);

        // 更新文章的评论数
        Article article = articleMapper.selectById(articleId);
        if (article != null) {
            article.setCommentCount(article.getCommentCount() == null ? 1 : article.getCommentCount() + 1);
            articleMapper.updateById(article);
        }

        return comment;
    }

    /**
     * 将Comment实体转换为CommentInfo VO
     */
    private CommentInfo convertToCommentInfo(Comment comment, Map<Long, UserInfo> userInfoMap) {
        CommentInfo info = new CommentInfo();
        info.setId(comment.getId());
        info.setArticleId(comment.getArticleId());
        info.setUserId(comment.getUserId());
        info.setContent(comment.getContent());
        info.setParentId(comment.getParentId());
        info.setCreateTime(comment.getCreateTime());

        UserInfo userInfo = userInfoMap.get(comment.getUserId());
        if (userInfo != null) {
            info.setUserNickName(userInfo.getNickName());
            info.setUserAvatar(userInfo.getProfilePhotoUrl());
        }

        return info;
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