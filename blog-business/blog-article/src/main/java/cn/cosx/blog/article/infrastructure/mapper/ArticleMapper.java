package cn.cosx.blog.article.infrastructure.mapper;

import cn.cosx.blog.article.domain.entity.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 根据游标分页查询文章
     *
     * @param lastId    上一次查询的最大ID，null表示第一页
     * @param pageSize  每页数量
     * @param status    文章状态
     * @return 文章列表
     */
    List<Article> selectByCursor(@Param("lastId") Long lastId, @Param("pageSize") Integer pageSize, @Param("status") Integer status);
}