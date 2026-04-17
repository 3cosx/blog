package cn.cosx.blog.mentor.agent.document.mapper;

import cn.cosx.blog.mentor.agent.document.entity.DocumentEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档信息 Mapper 接口
 */
@Mapper
public interface DocumentMapper extends BaseMapper<DocumentEntity> {
}
