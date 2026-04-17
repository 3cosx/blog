package cn.cosx.blog.mentor.agent.document.service;

import cn.cosx.blog.mentor.agent.document.mapper.DocumentMapper;
import cn.cosx.blog.mentor.agent.document.entity.DocumentEntity;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 文档服务实现类
 */
@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, DocumentEntity> implements DocumentService {
}
