package cn.cosx.blog.knowledge.rag.service.impl;

import cn.cosx.blog.knowledge.document.domain.entity.KnowledgeDocument;
import cn.cosx.blog.knowledge.document.domain.entity.KnowledgeSegment;
import cn.cosx.blog.knowledge.document.infra.enums.DocumentStatus;
import cn.cosx.blog.knowledge.document.infra.enums.FileType;
import cn.cosx.blog.knowledge.document.infra.enums.SegmentStatus;

import cn.cosx.blog.knowledge.document.infra.file.MinioUtils;
import cn.cosx.blog.knowledge.document.infra.param.DocumentSplitParam;
import cn.cosx.blog.knowledge.document.service.KnowledgeSegmentService;
import cn.cosx.blog.knowledge.rag.event.DocumentEmbeddingEvent;
import cn.cosx.blog.knowledge.rag.splitter.DocumentSplitterFactory;
import cn.cosx.blog.knowledge.rag.splitter.ExcelSplitter;
import cn.cosx.blog.knowledge.rag.splitter.MarkdownHeaderParentTextSplitter;
import cn.cosx.blog.knowledge.document.service.IKnowledgeDocumentService;
import cn.cosx.blog.knowledge.rag.service.IDocumentChunkService;
import com.alibaba.fastjson2.JSON;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.cosx.blog.knowledge.rag.constant.MetadataKeyConstant.*;

@Slf4j
@Service
public class DocumentChunkServiceImpl implements IDocumentChunkService {

    private static final int DEFAULT_CHUNK_SIZE = 500;
    private static final int DEFAULT_OVERLAP = 50;

    @Autowired
    private IKnowledgeDocumentService knowledgeDocumentService;

    @Autowired
    private KnowledgeSegmentService knowledgeSegmentService;

    @Autowired
    private MinioUtils minioUtils;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void chunkDocument(DocumentSplitParam param) {
        log.info("[DocumentChunk] 开始处理文档切分，param: {}", param);

        KnowledgeDocument document = knowledgeDocumentService.getByDocId(param.getDocId());
        if (document == null) {
            throw new IllegalArgumentException("文档不存在: " + param.getDocId());
        }

        if (document.getStatus() == DocumentStatus.CHUNKED) {
            log.info("[DocumentChunk] 文档已切分，跳过，docId: {}", param.getDocId());
            return;
        }

        if (document.getStatus() != DocumentStatus.CONVERTED) {
            throw new IllegalArgumentException("文档状态非CONVERTED，无法切分: " + param.getDocId());
        }

        doChunk(document, param);

        eventPublisher.publishEvent(new DocumentEmbeddingEvent(this, param.getDocId()));
    }

    private void doChunk(KnowledgeDocument document, DocumentSplitParam param) {
        Long documentId = document.getDocId();
        log.info("[DocumentChunk] 开始切分文档，docId: {}, docTitle: {}", documentId, document.getDocTitle());

        try {

            List<TextSegment> segments ;
            try(InputStream inputStream = downloadMarkdownContent(document)){
                if(FileType.EXCEL.equals(document.getFileType()) || FileType.CSV.equals(document.getFileType())){
                    ExcelSplitter excelSplitter = new ExcelSplitter();
                    segments = excelSplitter.split(inputStream.readAllBytes());
                }else{

                    DocumentSplitter documentSplitter = DocumentSplitterFactory.getDocumentSplitter(param);
                    Document doc = Document.from(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
                    segments = documentSplitter.split(doc);
                }
            }

            log.info("[DocumentChunk] 文档切分完成，docId: {}, 切分数量: {}", documentId, segments.size());

            List<KnowledgeSegment> knowledgeSegments = new ArrayList<>();
            for (int i = 0; i < segments.size(); i++) {
                TextSegment segment = segments.get(i);
                KnowledgeSegment knowledgeSegment = new KnowledgeSegment();

                //todo metadata统一处理(权限相关、多版本相关）
                Metadata metadata = segment.metadata();
                knowledgeSegment.setDocumentId(documentId);
                knowledgeSegment.setText(segment.text());
                knowledgeSegment.setChunkId(metadata.getString(CHUNK_ID));
                knowledgeSegment.setChunkOrder(i);
                knowledgeSegment.setStatus(SegmentStatus.STORED);


                Integer skipEmbedding = metadata.getInteger(SKIP_EMBEDDING);
                knowledgeSegment.setSkipEmbedding(skipEmbedding != null ? skipEmbedding : 0);


                metadata.put(DOC_ID, documentId);
                metadata.put(FILE_NAME, document.getDocTitle());
                metadata.put(URL, document.getDocUrl());
                knowledgeSegment.setMetadata(JSON.toJSONString(metadata));

                knowledgeSegments.add(knowledgeSegment);
            }

            if (!knowledgeSegments.isEmpty()) {
                knowledgeSegmentService.saveSegments(knowledgeSegments);
                log.info("[DocumentChunk] 切分片段保存成功，docId: {}, 保存数量: {}", documentId, knowledgeSegments.size());
            }

            knowledgeDocumentService.updateStatus(documentId, DocumentStatus.CHUNKED);
            log.info("[DocumentChunk] 文档状态已更新为CHUNKED，docId: {}", documentId);

        } catch (Exception e) {
            log.error("[DocumentChunk] 文档切分失败，docId: {}", documentId, e);
            throw new RuntimeException("文档切分失败", e);
        }
    }

    private InputStream downloadMarkdownContent(KnowledgeDocument document) {
        String convertedUrl = document.getConvertedDocUrl();
        if (convertedUrl == null || convertedUrl.isEmpty()) {
            throw new RuntimeException("文档转换后的URL为空，docId: " + document.getDocId());
        }

        String objectName = extractObjectNameFromUrl(convertedUrl);
        InputStream mdStream = minioUtils.downloadFileAsInputStream(objectName);
        return mdStream;
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

}
