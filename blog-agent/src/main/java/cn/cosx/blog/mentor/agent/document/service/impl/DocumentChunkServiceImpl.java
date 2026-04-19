package cn.cosx.blog.mentor.agent.document.service.impl;

import cn.cosx.blog.mentor.agent.document.entity.KnowledgeDocument;
import cn.cosx.blog.mentor.agent.document.entity.KnowledgeSegment;
import cn.cosx.blog.mentor.agent.document.enums.DocumentStatus;
import cn.cosx.blog.mentor.agent.document.enums.SegmentStatus;
import cn.cosx.blog.mentor.agent.document.rag.event.VectorStoreEvent;
import cn.cosx.blog.mentor.agent.document.rag.splitter.MarkdownHeaderParentTextSplitter;
import cn.cosx.blog.mentor.agent.document.service.DocumentChunkService;
import cn.cosx.blog.mentor.agent.document.service.KnowledgeDocumentService;
import cn.cosx.blog.mentor.agent.document.service.KnowledgeSegmentService;
import cn.cosx.blog.mentor.agent.service.FileManageService;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.TransactionCallback;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.cosx.blog.mentor.agent.document.rag.constant.MetadataKeyConstant.SKIP_EMBEDDING;

/**
 * 文档切分服务实现类
 */
@Service
@Slf4j
public class DocumentChunkServiceImpl implements DocumentChunkService {

    @Autowired
    private KnowledgeDocumentService documentService;

    @Autowired
    private KnowledgeSegmentService segmentService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private FileManageService fileManageService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * 每块最大字符数
     */
    private static final int CHUNK_SIZE = 500;

    /**
     * 相邻块之间重叠字符数
     */
    private static final int OVERLAP = 50;

    //todo 事务优化
    @Override
    @Transactional
    public void chunkDocument(KnowledgeDocument document) {
        log.info("开始切分文档: docId={}, docTitle={}", document.getDocId(), document.getDocTitle());

        try {
            // 1. 从MinIO下载Markdown内容
            String markdownContent = downloadMarkdownContent(document);

            // 2. 创建Markdown文档切分器（基于标题层级 + 字符大小切分）
            MarkdownHeaderParentTextSplitter splitter = new MarkdownHeaderParentTextSplitter(CHUNK_SIZE, OVERLAP);

            // 3. 执行文档切分
            List<TextSegment> segments = splitter.splitText(markdownContent);
            log.info("文档切分完成: docId={}, 切分数量={}", document.getDocId(), segments.size());

            // 4. 保存切分片段到数据库
            List<KnowledgeSegment> knowledgeSegments = new ArrayList<>();
            for (int i = 0; i < segments.size(); i++) {
                TextSegment segment = segments.get(i);
                
                KnowledgeSegment knowledgeSegment = new KnowledgeSegment();
                knowledgeSegment.setDocumentId(document.getDocId());
                knowledgeSegment.setText(segment.text());
                
                // 从元数据中获取chunkId，如果没有则生成新的
                String chunkId = segment.metadata().getString("chunk_id");

                knowledgeSegment.setChunkId(chunkId);
                
                knowledgeSegment.setChunkOrder(i);
                
                // 设置状态：标记为跳过embedding的片段状态仍为STORED，但后续不会进行向量化
                Integer skipEmbedding = segment.metadata().getInteger(SKIP_EMBEDDING);
                knowledgeSegment.setSkipEmbedding(skipEmbedding);
                knowledgeSegment.setStatus(SegmentStatus.STORED);
                
                // 设置元数据（转换为JSON字符串）
                Map<String, Object> metadata = segment.metadata().toMap();
                if (metadata != null && !metadata.isEmpty()) {
                    // 将元数据转换为JSON字符串存储
                    String metadataJson = convertMetadataToJson(metadata);
                    knowledgeSegment.setMetadata(metadataJson);
                }
                
                knowledgeSegments.add(knowledgeSegment);
            }

            transactionTemplate.execute(obk -> {
                // 5. 批量保存切分片段
                if (!knowledgeSegments.isEmpty()) {
                    segmentService.saveBatch(knowledgeSegments);
                    log.info("切分片段保存成功: docId={}, 保存数量={}", document.getDocId(), knowledgeSegments.size());
                }

                // 6. 更新文档状态为 CHUNKED
                document.setStatus(DocumentStatus.CHUNKED);
                boolean updateResult = documentService.updateById(document);
                if (!updateResult) {
                    throw new RuntimeException("更新文档状态为CHUNKED失败: docId=" + document.getDocId());
                }
                log.info("文档状态已更新为CHUNKED: docId={}", document.getDocId());

                // 7. 发布向量存储事件
                eventPublisher.publishEvent(new VectorStoreEvent(this, document.getDocId()));
                log.info("已发布向量存储事件: docId={}", document.getDocId());
                return null;
            });


        } catch (Exception e) {
            log.error("文档切分失败: docId={}, docTitle={}", document.getDocId(), document.getDocTitle(), e);
            throw new RuntimeException("文档切分失败", e);
        }
    }

    /**
     * 从MinIO下载Markdown内容
     */
    private String downloadMarkdownContent(KnowledgeDocument document) {
        try {
            String convertedUrl = document.getConvertedDocUrl();
            if (convertedUrl == null || convertedUrl.isEmpty()) {
                throw new RuntimeException("文档转换后的URL为空: docId=" + document.getDocId());
            }

            String objectName = extractObjectNameFromUrl(convertedUrl);
            byte[] mdBytes = fileManageService.downloadFileAsBytes(objectName);
            return new String(mdBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("从MinIO下载Markdown内容失败: docId={}", document.getDocId(), e);
            throw new RuntimeException("从MinIO下载Markdown内容失败", e);
        }
    }

    /**
     * 从URL中提取ObjectName
     */
    private String extractObjectNameFromUrl(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            // URL格式为: endpoint/bucketName/objectName
            int bucketIndex = path.indexOf("/");
            if (bucketIndex > 0) {
                path = path.substring(bucketIndex + 1);
            }
            return path;
        } catch (Exception e) {
            log.error("解析MinIO URL失败: url={}", url, e);
            return url;
        }
    }

    /**
     * 将元数据Map转换为JSON字符串
     */
    private String convertMetadataToJson(Map<String, Object> metadata) {
        try {
            // 使用简单的JSON转换，避免引入额外的依赖
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
        } catch (Exception e) {
            log.warn("元数据转换为JSON失败: {}", e.getMessage());
            return null;
        }
    }
}
