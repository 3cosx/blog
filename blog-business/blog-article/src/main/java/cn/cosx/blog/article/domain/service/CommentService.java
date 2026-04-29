package cn.cosx.blog.article.domain.service;

import cn.cosx.blog.api.article.vo.CommentInfo;
import cn.cosx.blog.article.domain.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CommentService extends IService<Comment> {

    /**
     * 获取文章下所有一级评论（分页）
     *
     * @param articleId 文章ID
     * @param offset    偏移量
     * @param limit     每页数量
     * @return 一级评论列表
     */
    List<CommentInfo> getRootComments(Long articleId, Integer offset, Integer limit);

    /**
     * 根据topId获取该楼所有评论（一次性查询，不递归）
     *
     * @param topId 根评论ID
     * @return 该楼所有评论（按时间正序）
     */
    List<CommentInfo> getCommentsByTopId(Long topId);

    /**
     * 发布一级评论
     */
    Comment addRootComment(Long articleId, Long userId, String content);

    /**
     * 回复评论
     *
     * @param topId      根评论ID
     * @param pid        直接回复的评论ID
     * @param userId     回复用户ID
     * @param content    评论内容
     */
    Comment addReply(Long topId, Long pid, Long userId, String content);
}