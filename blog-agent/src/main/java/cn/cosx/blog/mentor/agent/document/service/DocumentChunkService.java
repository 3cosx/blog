package cn.cosx.blog.mentor.agent.document.service;

import cn.cosx.blog.mentor.agent.document.entity.KnowledgeDocument;

/**
 * 文档切分服务接口
 */
public interface DocumentChunkService {

    /**
     * 对文档进行切分
     *
     * @param document 需要切分的文档
     */
    void chunkDocument(KnowledgeDocument document);
}
