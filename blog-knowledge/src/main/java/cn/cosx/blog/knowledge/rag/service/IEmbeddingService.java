package cn.cosx.blog.knowledge.rag.service;

import cn.cosx.blog.knowledge.document.entity.KnowledgeDocument;

/**
 * 向量化Service接口
 */
public interface IEmbeddingService {

    /**
     * 对文档进行向量化
     *
     * @param documentId 文档ID
     */
    void embedDocument(Long documentId);

    /**
     * 对文档进行向量化
     *
     * @param document 文档实体
     */
    void embedDocument(KnowledgeDocument document);
}
