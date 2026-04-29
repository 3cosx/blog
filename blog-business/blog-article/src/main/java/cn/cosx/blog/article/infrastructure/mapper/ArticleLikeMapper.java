package cn.cosx.blog.article.infrastructure.mapper;

import cn.cosx.blog.article.domain.entity.ArticleLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleLikeMapper extends BaseMapper<ArticleLike> {
}