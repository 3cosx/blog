package cn.cosx.blog.knowledge.rag.service.impl;

import cn.cosx.blog.knowledge.document.entity.KnowledgeDocument;
import cn.cosx.blog.knowledge.document.entity.KnowledgeSegment;
import cn.cosx.blog.knowledge.document.enums.DocumentStatus;
import cn.cosx.blog.knowledge.document.enums.SegmentStatus;
import cn.cosx.blog.knowledge.document.file.MinioUtils;
import cn.cosx.blog.knowledge.rag.event.DocumentEmbeddingEvent;
import cn.cosx.blog.knowledge.rag.splitter.MarkdownHeaderParentTextSplitter;
import cn.cosx.blog.knowledge.document.service.IKnowledgeDocumentService;
import cn.cosx.blog.knowledge.document.service.IKnowledgeSegmentService;
import cn.cosx.blog.knowledge.rag.service.IDocumentChunkService;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档切分Service实现类
 */
@Slf4j
@Service
public class DocumentChunkServiceImpl implements IDocumentChunkService {

    private static final int CHUNK_SIZE = 500;
    private static final int OVERLAP = 50;

    @Autowired
    private IKnowledgeDocumentService knowledgeDocumentService;

    @Autowired
    private IKnowledgeSegmentService knowledgeSegmentService;

    @Autowired
    private MinioUtils minioUtils;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void chunkDocument(Long documentId) {
        log.info("[DocumentChunk] 开始处理文档切分，docId: {}", documentId);

        KnowledgeDocument document = knowledgeDocumentService.getByDocId(documentId);
        if (document == null) {
            throw new IllegalArgumentException("文档不存在: " + documentId);
        }

        if(document.getStatus() == DocumentStatus.CHUNKED){
            return ;
        }

        if(document.getStatus() != DocumentStatus.CONVERTED){
            throw new IllegalArgumentException("文档状态非CONVERTED，无法切分: " + documentId);
        }
        chunkDocument(document);

        //抛出一个事件做embedding
        eventPublisher.publishEvent(new DocumentEmbeddingEvent(this, documentId));
    }

    @Override
    @Transactional
    public void chunkDocument(KnowledgeDocument document) {
        Long documentId = document.getDocId();
        log.info("[DocumentChunk] 开始切分文档，docId: {}, docTitle: {}", documentId, document.getDocTitle());

        try {
            // 1. 从文档信息中获取convertedDocUrl，下载文件内容
            String markdownContent = downloadMarkdownContent(document);

            // 2. 使用MarkdownHeaderParentTextSplitter对文件内容进行切分
            MarkdownHeaderParentTextSplitter splitter = new MarkdownHeaderParentTextSplitter(CHUNK_SIZE, OVERLAP);
            Map<String, Object> baseMetadata = new HashMap<>();
            baseMetadata.put("documentId", documentId.toString());
            List<TextSegment> segments = splitter.splitText(markdownContent, baseMetadata);
            log.info("[DocumentChunk] 文档切分完成，docId: {}, 切分数量: {}", documentId, segments.size());

            // 3. 保存切分片段到KnowledgeSegment
            List<KnowledgeSegment> knowledgeSegments = new ArrayList<>();
            for (int i = 0; i < segments.size(); i++) {
                TextSegment segment = segments.get(i);
                KnowledgeSegment knowledgeSegment = new KnowledgeSegment();
                knowledgeSegment.setDocumentId(documentId);
                knowledgeSegment.setText(segment.text());
                knowledgeSegment.setChunkId(segment.metadata().getString("chunkId"));
                knowledgeSegment.setChunkOrder(i);
                knowledgeSegment.setStatus(SegmentStatus.STORED);

                Integer skipEmbedding = segment.metadata().getInteger("skipEmbedding");
                knowledgeSegment.setSkipEmbedding(skipEmbedding != null ? skipEmbedding : 0);

                Map<String, Object> metadata = segment.metadata().toMap();

                knowledgeSegment.setMetadata(convertMetadataToJson(metadata));

                knowledgeSegments.add(knowledgeSegment);
            }

            if (!knowledgeSegments.isEmpty()) {
                knowledgeSegmentService.saveSegments(knowledgeSegments);
                log.info("[DocumentChunk] 切分片段保存成功，docId: {}, 保存数量: {}", documentId, knowledgeSegments.size());
            }

            // 4. 更新文档状态为CHUNKED
            knowledgeDocumentService.updateStatus(documentId, DocumentStatus.CHUNKED);
            log.info("[DocumentChunk] 文档状态已更新为CHUNKED，docId: {}", documentId);

        } catch (Exception e) {
            log.error("[DocumentChunk] 文档切分失败，docId: {}", documentId, e);
            throw new RuntimeException("文档切分失败", e);
        }
    }

    private String downloadMarkdownContent(KnowledgeDocument document) {
        String convertedUrl = document.getConvertedDocUrl();
        if (convertedUrl == null || convertedUrl.isEmpty()) {
            throw new RuntimeException("文档转换后的URL为空，docId: " + document.getDocId());
        }

        String objectName = extractObjectNameFromUrl(convertedUrl);
        byte[] mdBytes = minioUtils.downloadFileAsBytes(objectName);
        return new String(mdBytes, StandardCharsets.UTF_8);
    }

    private String extractObjectNameFromUrl(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            int bucketIndex = path.indexOf("/");
            if (bucketIndex > 0) {
                path = path.substring(bucketIndex + 1);
            }
            return path;
        } catch (Exception e) {
            log.error("[DocumentChunk] 解析MinIO URL失败: {}", url, e);
            return url;
        }
    }

    private String convertMetadataToJson(Map<String, Object> metadata) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else if (value instanceof Number) {
                json.append(value);
            } else if (value instanceof Boolean) {
                json.append(value);
            } else {
                json.append("\"").append(value.toString()).append("\"");
            }
            first = false;
        }
        json.append("}");
        return json.toString();
    }
}
