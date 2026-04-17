package cn.cosx.blog.mentor.agent.document.service;

import cn.cosx.blog.mentor.agent.document.mapper.SegmentMapper;
import cn.cosx.blog.mentor.agent.document.entity.SegmentEntity;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 文档分片服务实现类
 */
@Service
public class SegmentServiceImpl extends ServiceImpl<SegmentMapper, SegmentEntity> implements SegmentService {
}
