package cn.cosx.blog.knowledge.document.service;

import cn.cosx.blog.knowledge.document.domain.entity.KnowledgeDocument;
import cn.cosx.blog.knowledge.document.infra.enums.DocumentStatus;

import java.util.List;

/**
 * 文档实体Service接口
 * 只负责对knowledge_document表的CRUD操作
 */
public interface IKnowledgeDocumentService {

    /**
     * 保存文档，状态初始化为INIT
     */
    KnowledgeDocument saveDocument(KnowledgeDocument document);

    /**
     * 更新文档状态
     */
    boolean updateStatus(Long docId, DocumentStatus status);

    /**
     * 更新文档URL
     */
    boolean updateDocUrl(Long docId, String docUrl);

    /**
     * 更新转换后的文档URL
     */
    boolean updateConvertedDocUrl(Long docId, String convertedDocUrl);

    /**
     * 根据ID查询文档
     */
    KnowledgeDocument getByDocId(Long docId);

    /**
     * 根据状态查询文档列表
     */
    List<KnowledgeDocument> listByStatus(DocumentStatus status);

    /**
     * 根据ID更新文档
     */
    boolean updateById(KnowledgeDocument document);
}
