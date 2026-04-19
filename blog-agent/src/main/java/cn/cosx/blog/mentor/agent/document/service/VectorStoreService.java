package cn.cosx.blog.mentor.agent.document.service;

import cn.cosx.blog.mentor.agent.document.entity.KnowledgeDocument;

/**
 * 向量存储服务接口
 */
public interface VectorStoreService {

    /**
     * 将文档的切分片段存储到向量数据库
     *
     * @param document 已切分的文档
     */
    void storeDocumentVectors(KnowledgeDocument document);
}
