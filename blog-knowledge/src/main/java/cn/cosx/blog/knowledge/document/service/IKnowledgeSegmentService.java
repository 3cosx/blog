package cn.cosx.blog.knowledge.document.service;

import cn.cosx.blog.knowledge.common.Page;
import cn.cosx.blog.knowledge.document.domain.entity.KnowledgeSegment;
import cn.cosx.blog.knowledge.document.infra.enums.SegmentStatus;

import java.io.Serializable;
import java.util.List;

/**
 * 文档分片Service接口
 * 只负责对knowledge_segment表的CRUD操作
 */
public interface IKnowledgeSegmentService {

    public String getTextByChunkId(Serializable chunkId) ;
        /**
         * 批量保存分片
         */
    boolean saveSegments(List<KnowledgeSegment> segments);

    /**
     * 根据ID查询分片
     */
    KnowledgeSegment getById(Long segmentId);


    /**
     * 根据文档ID和分页信息查询还未向量化的分片列表（游标分页）
     */
    List<KnowledgeSegment> listByDocumentIdAndNotVectorStoredWithCursor(Long documentId, Page page);

    /**
     * 批量更新分段状态
     */
    boolean batchUpdateStatus(List<Long> segmentIds, SegmentStatus status);

    /**
     * 批量更新分段的向量ID和状态
     */
    boolean batchUpdateEmbeddingIdAndStatus(List<Long> segmentIds, List<String> embeddingIds, SegmentStatus status);

    /**
     * 查询有未向量化segment的文档ID列表（状态为CHUNKED但segments未VECTOR_STORED）
     */
    List<Long> listDocumentIdsWithUnvectorizedSegments();
}
