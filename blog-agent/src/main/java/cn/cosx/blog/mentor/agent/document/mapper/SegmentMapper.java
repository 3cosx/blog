package cn.cosx.blog.mentor.agent.document.mapper;

import cn.cosx.blog.mentor.agent.document.entity.KnowledgeSegment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档分片 Mapper 接口
 */
@Mapper
public interface SegmentMapper extends BaseMapper<KnowledgeSegment> {
}
