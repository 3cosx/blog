package cn.cosx.blog.article.infrastructure.mapper;

import cn.cosx.blog.article.domain.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 获取文章下所有一级评论（分页）
     */
    List<Comment> selectRootComments(@Param("articleId") Long articleId, @Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 根据topId获取该楼所有子评论（一次性查询，不递归）
     */
    List<Comment> selectCommentsByTopId(@Param("topId") Long topId);

    /**
     * 批量更新一级评论的回复数量
     */
    void batchUpdateReplyCount(@Param("topId") Long topId);
}