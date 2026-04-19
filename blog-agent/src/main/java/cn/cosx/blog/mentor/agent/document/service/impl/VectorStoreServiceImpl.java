package cn.cosx.blog.mentor.agent.document.service.impl;

import cn.cosx.blog.mentor.agent.document.entity.KnowledgeDocument;
import cn.cosx.blog.mentor.agent.document.entity.KnowledgeSegment;
import cn.cosx.blog.mentor.agent.document.enums.DocumentStatus;
import cn.cosx.blog.mentor.agent.document.enums.SegmentStatus;
import cn.cosx.blog.mentor.agent.document.service.KnowledgeDocumentService;
import cn.cosx.blog.mentor.agent.document.service.KnowledgeSegmentService;
import cn.cosx.blog.mentor.agent.document.service.VectorStoreService;
import cn.cosx.blog.mentor.agent.utils.DynamicPgVectorStoreFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 向量存储服务实现类
 */
@Service
@Slf4j
public class VectorStoreServiceImpl implements VectorStoreService {

    @Autowired
    private KnowledgeDocumentService documentService;

    @Autowired
    private KnowledgeSegmentService segmentService;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private DynamicPgVectorStoreFactory pgVectorStoreFactory;

    /**
     * 向量表名称
     */
    @Value("${vector.store.table-name:vector_knowledge_document}")
    private String vectorTableName;

    private PgVectorStore pgVectorStore;

    @PostConstruct
    public void init() {
        pgVectorStore = pgVectorStoreFactory.createPgVectorStore(vectorTableName);
        log.info("向量存储服务初始化完成, tableName={}", vectorTableName);
    }

    @Override
    @Transactional
    public void storeDocumentVectors(KnowledgeDocument document) {
        log.info("开始存储文档向量: docId={}, docTitle={}", document.getDocId(), document.getDocTitle());

        if(document.getStatus() == DocumentStatus.CHUNKED){
            return ;
        }
        // 1. 判断文档状态，只有 CONVERTED 状态才能进行向量化
        if (document.getStatus() != DocumentStatus.CONVERTED) {
            log.warn("文档状态不是CONVERTED，跳过向量化: docId={}, status={}", document.getDocId(), document.getStatus());
            return;
        }

        // 2. 查询该文档的所有切分片段，排除标记为跳过embedding的片段
        LambdaQueryWrapper<KnowledgeSegment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KnowledgeSegment::getDocumentId, document.getDocId())
                .eq(KnowledgeSegment::getStatus, SegmentStatus.STORED)
                .eq(KnowledgeSegment::getSkipEmbedding, 0)  // 只查询需要向量化的片段
                .isNull(KnowledgeSegment::getEmbeddingId)
                .orderByAsc(KnowledgeSegment::getChunkOrder);

        // 3. 使用分页查询，每批处理一页
        long current = 1;
        long size = 100;
        Page<KnowledgeSegment> page = new Page<>(current, size);
        List<KnowledgeSegment> allSegments = new ArrayList<>();

        while (true) {
            Page<KnowledgeSegment> segmentPage = segmentService.page(page, queryWrapper);
            List<KnowledgeSegment> records = segmentPage.getRecords();

            // 只有首页或者有下一页时才继续处理
            if (page.getCurrent() == 1 || segmentPage.hasNext()) {
                allSegments.addAll(records);
            } else {
                break;
            }

            if (!segmentPage.hasNext()) {
                break;
            }
            page = new Page<>(page.getCurrent() + 1, size);
        }

        log.info("查询到待向量化的片段数量: docId={}, count={}", document.getDocId(), allSegments.size());
        if (allSegments.isEmpty()) {
            log.warn("未找到待向量化的片段: docId={}", document.getDocId());
            // 即使没有需要向量化的片段，也要更新文档状态
            document.setStatus(DocumentStatus.VECTOR_STORED);
            documentService.updateById(document);
            return;
        }
        // 4. 将KnowledgeSegment转换为Spring AI Document
        List<Document> springAiDocuments = new ArrayList<>();
        for (KnowledgeSegment segment : allSegments) {
            Map<String, Object> metadata = Map.of("segmentId", segment.getId(), "chunkId", segment.getChunkId(), "documentId", document.getDocId(), "chunkOrder", segment.getChunkOrder()
            );

            Document springAiDoc = new Document(segment.getText(), metadata);
            springAiDocuments.add(springAiDoc);
        }
        // 5. 生成embedding并存储到pgvector
        log.info("开始生成embedding并存储到pgvector: docId={}, 文档数量={}", document.getDocId(), springAiDocuments.size());

        // 分批存储，每批9个文档
        int batchSize = 9;
        for (int i = 0; i < springAiDocuments.size(); i += batchSize) {
            List<Document> batch = springAiDocuments.subList(i, Math.min(i + batchSize, springAiDocuments.size()));
            pgVectorStore.doAdd(batch);
        }

        log.info("向量存储完成: docId={}", document.getDocId());
        // 6. 更新所有片段的状态为 VECTOR_STORED
        for (KnowledgeSegment segment : allSegments) {
            segment.setStatus(SegmentStatus.VECTOR_STORED);
            // 生成embeddingId（这里使用segment的id作为embeddingId）
            segment.setEmbeddingId(segment.getId().toString());
        }
        segmentService.updateBatchById(allSegments);
        log.info("片段状态已更新为VECTOR_STORED: docId={}, 更新数量={}", document.getDocId(), allSegments.size());
        // 7. 更新文档状态为 VECTOR_STORED
        document.setStatus(DocumentStatus.VECTOR_STORED);
        boolean updateResult = documentService.updateById(document);
        if (!updateResult) {
            throw new RuntimeException("更新文档状态为VECTOR_STORED失败: docId=" + document.getDocId());
        }
        log.info("文档状态已更新为VECTOR_STORED: docId={}", document.getDocId());

    }
}
