package cn.cosx.blog.knowledge.rag.service;

import cn.cosx.blog.knowledge.document.entity.KnowledgeDocument;

/**
 * 文档切分Service接口
 */
public interface IDocumentChunkService {

    /**
     * 切分文档
     *
     * @param documentId 文档ID
     */
    void chunkDocument(Long documentId);

    /**
     * 切分文档
     *
     * @param document 文档实体
     */
    void chunkDocument(KnowledgeDocument document);
}
