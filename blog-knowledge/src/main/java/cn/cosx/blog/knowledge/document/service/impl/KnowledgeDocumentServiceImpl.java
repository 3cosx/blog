package cn.cosx.blog.knowledge.document.service.impl;

import cn.cosx.blog.knowledge.document.entity.KnowledgeDocument;
import cn.cosx.blog.knowledge.document.enums.DocumentStatus;
import cn.cosx.blog.knowledge.document.mapper.KnowledgeDocumentMapper;
import cn.cosx.blog.knowledge.document.service.IKnowledgeDocumentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文档实体Service实现类
 * 只负责对knowledge_document表的CRUD操作
 */
@Service
public class KnowledgeDocumentServiceImpl extends ServiceImpl<KnowledgeDocumentMapper, KnowledgeDocument> implements IKnowledgeDocumentService {

    @Override
    @Transactional
    public KnowledgeDocument saveDocument(KnowledgeDocument document) {
        document.setStatus(DocumentStatus.INIT);
        this.save(document);
        return document;
    }

    @Override
    @Transactional
    public boolean updateStatus(Long docId, DocumentStatus status) {
        KnowledgeDocument document = this.getById(docId);
        if (document == null) {
            throw new IllegalArgumentException("文档不存在: " + docId);
        }
        document.setStatus(status);
        return this.updateById(document);
    }

    @Override
    @Transactional
    public boolean updateDocUrl(Long docId, String docUrl) {
        KnowledgeDocument document = this.getById(docId);
        if (document == null) {
            throw new IllegalArgumentException("文档不存在: " + docId);
        }
        document.setDocUrl(docUrl);
        document.setStatus(DocumentStatus.UPLOADED);
        return this.updateById(document);
    }

    @Override
    @Transactional
    public boolean updateConvertedDocUrl(Long docId, String convertedDocUrl) {
        KnowledgeDocument document = this.getById(docId);
        if (document == null) {
            throw new IllegalArgumentException("文档不存在: " + docId);
        }
        document.setConvertedDocUrl(convertedDocUrl);
        return this.updateById(document);
    }

    @Override
    public KnowledgeDocument getByDocId(Long docId) {
        return this.getById(docId);
    }

    @Override
    public List<KnowledgeDocument> listByStatus(DocumentStatus status) {
        LambdaQueryWrapper<KnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDocument::getStatus, status);
        return this.list(wrapper);
    }
}
