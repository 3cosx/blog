package cn.cosx.blog.knowledge.rag.service;

import cn.cosx.blog.knowledge.document.domain.entity.KnowledgeDocument;
import cn.cosx.blog.knowledge.document.infra.param.DocumentSplitParam;

/**
 * 文档切分Service接口
 */
public interface IDocumentChunkService {

    /**
     * 切分文档
     *
     * @param documentId 文档ID
     */
    void chunkDocument(DocumentSplitParam documentSplitParam);

}
