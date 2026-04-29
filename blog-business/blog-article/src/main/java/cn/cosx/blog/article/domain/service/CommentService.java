package cn.cosx.blog.article.domain.service;

import cn.cosx.blog.api.article.vo.CommentInfo;
import cn.cosx.blog.article.domain.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CommentService extends IService<Comment> {

    /**
     * 获取文章的评论列表（树形结构）
     *
     * @param articleId 文章ID
     * @return 评论列表（顶层评论及其子评论）
     */
    List<CommentInfo> getComments(Long articleId);

    /**
     * 添加评论
     *
     * @param articleId 文章ID
     * @param userId    用户ID
     * @param content   评论内容
     * @param parentId  父评论ID（可为null）
     * @return 评论信息
     */
    Comment addComment(Long articleId, Long userId, String content, Long parentId);
}