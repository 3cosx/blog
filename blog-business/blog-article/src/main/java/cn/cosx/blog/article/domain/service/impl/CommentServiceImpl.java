package cn.cosx.blog.article.domain.service.impl;

import cn.cosx.blog.api.article.vo.CommentInfo;
import cn.cosx.blog.api.user.request.UserQueryRequest;
import cn.cosx.blog.api.user.service.UserFacadeService;
import cn.cosx.blog.api.user.vo.UserInfo;
import cn.cosx.blog.article.domain.entity.Comment;
import cn.cosx.blog.article.domain.service.CommentService;
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
 * 评论服务实现类（楼中楼扁平化设计）
 *
 * 核心设计：
 * - 一级评论：pid=0, topId=自身id, level=1
 * - 子评论：pid=直接回复的评论id, topId=一级评论id, level=父评论level+1
 * - 获取某楼评论：SELECT * FROM comment WHERE top_id = ? ORDER BY create_time ASC（一次查询，不递归）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final CommentMapper commentMapper;
    private final UserFacadeService userFacadeService;

    @Override
    public List<CommentInfo> getRootComments(Long articleId, Integer offset, Integer limit) {
        // 查询一级评论
        List<Comment> rootComments = commentMapper.selectRootComments(articleId, offset, limit);

        if (CollectionUtils.isEmpty(rootComments)) {
            return new ArrayList<>();
        }

        // 批量获取用户信息
        Map<Long, UserInfo> userInfoMap = getUserInfoMap(
                rootComments.stream().map(Comment::getUserId).collect(Collectors.toList())
        );

        return rootComments.stream()
                .map(comment -> convertToCommentInfo(comment, userInfoMap))
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentInfo> getCommentsByTopId(Long topId) {
        // 一次性查询该楼所有评论（不递归）
        List<Comment> comments = commentMapper.selectCommentsByTopId(topId);

        if (CollectionUtils.isEmpty(comments)) {
            return new ArrayList<>();
        }

        // 批量获取用户信息
        Map<Long, UserInfo> userInfoMap = getUserInfoMap(
                comments.stream().map(Comment::getUserId).distinct().collect(Collectors.toList())
        );

        // 转换为VO
        return comments.stream()
                .map(comment -> convertToCommentInfo(comment, userInfoMap))
                .collect(Collectors.toList());
    }

    @Override
    public Comment addRootComment(Long articleId, Long userId, String content) {
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setPid(0L);           // 一级评论 pid = 0
        comment.setLevel(1);          // 一级评论 level = 1
        comment.setReplyCount(0);
        commentMapper.insert(comment);

        // 回写 topId = 自身id
        comment.setTopId(comment.getId());
        commentMapper.updateById(comment);

        return comment;
    }

    @Override
    public Comment addReply(Long topId, Long pid, Long userId, String content) {
        // 查询父评论，获取 level 和 articleId
        Comment parentComment = commentMapper.selectById(pid);
        if (parentComment == null) {
            throw new RuntimeException("父评论不存在");
        }

        Comment comment = new Comment();
        comment.setArticleId(parentComment.getArticleId());
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setPid(pid);
        comment.setTopId(topId);                     // 子评论 topId = 一级评论id
        comment.setLevel(parentComment.getLevel() + 1);  // level = 父评论level + 1
        commentMapper.insert(comment);

        // 更新一级评论的回复数
        updateReplyCount(topId);

        return comment;
    }

    /**
     * 更新一级评论的回复数量
     */
    private void updateReplyCount(Long topId) {
        Comment topComment = commentMapper.selectById(topId);
        if (topComment != null && topComment.getPid() == 0) {
            Long count = lambdaQuery().eq(Comment::getTopId, topId)
                    .ne(Comment::getPid, 0)
                    .count();
            topComment.setReplyCount(count.intValue());
            commentMapper.updateById(topComment);
        }
    }

    /**
     * 转换为CommentInfo VO
     */
    private CommentInfo convertToCommentInfo(Comment comment, Map<Long, UserInfo> userInfoMap) {
        CommentInfo info = new CommentInfo();
        info.setId(comment.getId());
        info.setArticleId(comment.getArticleId());
        info.setUserId(comment.getUserId());
        info.setContent(comment.getContent());
        info.setPid(comment.getPid());
        info.setTopId(comment.getTopId());
        info.setLevel(comment.getLevel());
        info.setReplyCount(comment.getReplyCount());
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

        try {
            // 批量查询用户信息（一次RPC调用）
            List<String> stringIds = userIds.stream().map(String::valueOf).collect(Collectors.toList());
            Response<List<UserInfo>> response = userFacadeService.queryUserByIds(stringIds);

            if (response != null && response.getSuccess() && response.getData() != null) {
                return response.getData().stream()
                        .collect(Collectors.toMap(
                                info -> Long.parseLong(info.getUserId()),
                                info -> info,
                                (v1, v2) -> v1
                        ));
            }
        } catch (Exception e) {
            log.error("批量获取用户信息失败, userIds: {}", userIds, e);
        }

        return Map.of();
    }

    private UserInfo getUserInfo(Long userId) {
        try {
            UserQueryRequest request = new UserQueryRequest();
            request.setUserId(String.valueOf(userId));
            Response<UserInfo> response = userFacadeService.queryUserById(request);
            if (response != null && response.getSuccess() && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("获取用户信息失败, userId: {}", userId, e);
        }
        return null;
    }
}