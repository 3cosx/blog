package cn.cosx.blog.knowledge.rag.service.impl;

import cn.cosx.blog.knowledge.common.Page;
import cn.cosx.blog.knowledge.common.lock.DistributeLock;
import cn.cosx.blog.knowledge.document.entity.KnowledgeDocument;
import cn.cosx.blog.knowledge.document.entity.KnowledgeSegment;
import cn.cosx.blog.knowledge.document.enums.DocumentStatus;
import cn.cosx.blog.knowledge.document.enums.SegmentStatus;
import cn.cosx.blog.knowledge.document.service.IKnowledgeDocumentService;
import cn.cosx.blog.knowledge.document.service.IKnowledgeSegmentService;
import cn.cosx.blog.knowledge.rag.service.IEmbeddingService;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.apache.groovy.util.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 向量化Service实现类
 */
@Slf4j
@Service
public class EmbeddingServiceImpl implements IEmbeddingService {

    private static final int DEFAULT_PAGE_SIZE = 100;

    @Autowired
    private OpenAiEmbeddingModel openAiEmbeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private IKnowledgeDocumentService knowledgeDocumentService;

    @Autowired
    private IKnowledgeSegmentService knowledgeSegmentService;

    @Override
    @Transactional
    public void embedDocument(Long documentId) {
        log.info("[Embedding] 开始处理文档向量化，docId: {}", documentId);

        KnowledgeDocument document = knowledgeDocumentService.getByDocId(documentId);
        if(document == null) {
            return;
        }
        if(document.getStatus() == DocumentStatus.VECTOR_STORED){
            return ;
        }
        if(document.getStatus() != DocumentStatus.CHUNKED){
            throw new IllegalArgumentException("文档状态非CHUNKED，无法存储向量: " + documentId);
        }

        embedDocument(document);
    }

    @Override
    @Transactional
    @DistributeLock(scene = "embedDocument", key = "#documentId", waitTime = 0)
    public void embedDocument(KnowledgeDocument document) {
        Long documentId = document.getDocId();
        log.info("[Embedding] 开始向量化文档，docId: {}, docTitle: {}", documentId, document.getDocTitle());

        try {
            Page page = new Page(1, DEFAULT_PAGE_SIZE);

            while (page.isFirst() || page.isHasNext()) {
                if (!page.isFirst()) {
                    page.setCur(page.getCur() + 1);
                }

                List<KnowledgeSegment> segments = knowledgeSegmentService.listByDocumentIdAndNotVectorStoredWithCursor(documentId, page);
                if (segments.isEmpty()) {
                    log.info("[Embedding] 没有需要向量化的分段，docId: {}", documentId);
                    break;
                }
                log.info("[Embedding] 第{}页查询到需要向量化的分段数量: {}, docId: {}", page.getCur(), segments.size(), documentId);

                List<KnowledgeSegment> toEmbedSegments = new ArrayList<>();
                for (KnowledgeSegment segment : segments) {
                    if (segment.getSkipEmbedding() != null && segment.getSkipEmbedding() == 1) {
                        continue;
                    }
                    toEmbedSegments.add(segment);
                }

                if (toEmbedSegments.isEmpty()) {
                    log.info("[Embedding] 没有需要向量化的分段，docId: {}", documentId);
                    if (!page.isHasNext()) {
                        break;
                    }
                    continue;
                }

                // 构建TextSegment列表
                List<TextSegment> textSegments = new ArrayList<>();
                for (KnowledgeSegment segment : toEmbedSegments) {
                    Map<String, String> embeddingMetadata = new HashMap<>();
                    embeddingMetadata.put("documentId", String.valueOf(documentId));
                    embeddingMetadata.put("segmentId", String.valueOf(segment.getId()));
                    embeddingMetadata.put("chunkId", segment.getChunkId() != null ? segment.getChunkId() : "");
                    embeddingMetadata.put("chunkOrder", String.valueOf(segment.getChunkOrder() != null ? segment.getChunkOrder() : 0));

                    // 从segment的metadata中解析parentChunkId
                    if (segment.getMetadata() != null && !segment.getMetadata().isEmpty()) {
                        try {
                            Map<String, Object> segmentMetadata = com.alibaba.fastjson2.JSON.parseObject(segment.getMetadata());
                            Object parentChunkId = segmentMetadata.get("parentChunkId");
                            if (parentChunkId != null) {
                                embeddingMetadata.put("parentChunkId", parentChunkId.toString());
                            }
                        } catch (Exception e) {
                            log.warn("[Embedding] 解析segment metadata失败，segmentId: {}", segment.getId(), e);
                        }
                    }

                    Metadata metadata = Metadata.from(embeddingMetadata);
                    textSegments.add(TextSegment.from(segment.getText(), metadata));
                }

                // 批量生成向量
                List<Embedding> embeddings = openAiEmbeddingModel.embedAll(textSegments).content();
                // 批量存储
                List<String> embeddingIds = embeddingStore.addAll(embeddings, textSegments);
                log.info("[Embedding] 批量存储完成，待更新数量: {}, embeddingIds数量: {}, docId: {}",
                        toEmbedSegments.size(), embeddingIds.size(), documentId);
                if (embeddingIds.size() != toEmbedSegments.size()) {
                    log.error("[Embedding] embeddingIds数量与segment数量不匹配！segments: {}, ids: {}, docId: {}",
                            toEmbedSegments.size(), embeddingIds.size(), documentId);
                }

                // 收集成功的segmentIds并批量更新
                List<Long> successSegmentIds = toEmbedSegments.stream().map(KnowledgeSegment::getId).toList();
                if (!successSegmentIds.isEmpty()) {
                    knowledgeSegmentService.batchUpdateEmbeddingIdAndStatus(successSegmentIds, embeddingIds, SegmentStatus.VECTOR_STORED);
                }


                if (!page.isHasNext()) {
                    break;
                }
            }

            knowledgeDocumentService.updateStatus(documentId, DocumentStatus.VECTOR_STORED);
            log.info("[Embedding] 文档向量化完成，docId: {}", documentId);

        } catch (Exception e) {
            log.error("[Embedding] 文档向量化失败，docId: {}", documentId, e);
            throw new RuntimeException("文档向量化失败", e);
        }
    }
}
