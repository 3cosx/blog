package cn.cosx.blog.knowledge.rag.listener;

import cn.cosx.blog.knowledge.document.entity.KnowledgeDocument;
import cn.cosx.blog.knowledge.document.enums.DocumentStatus;
import cn.cosx.blog.knowledge.document.service.IKnowledgeDocumentService;
import cn.cosx.blog.knowledge.rag.service.IEmbeddingService;
import cn.cosx.blog.knowledge.rag.event.DocumentEmbeddingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 文档向量存储监听器
 * 监听文档切分完成事件，触发向量存储操作
 */
@Slf4j
@Component
public class DocumentEmbeddingListener {

    @Autowired
    private IEmbeddingService embeddingService;


    @Async("documentTaskExecutor")
    @EventListener
    public void handleDocumentEmbeddingEvent(DocumentEmbeddingEvent event) {
        Long documentId = event.getDocumentId();
        log.info("[DocumentEmbedding] 收到向量存储事件，docId: {}", documentId);

        try {
            embeddingService.embedDocument(documentId);
            log.info("[DocumentEmbedding] 向量存储处理完成，docId: {}", documentId);
        } catch (Exception e) {
            log.error("[DocumentEmbedding] 向量存储处理失败，docId: {}", documentId, e);
            throw e;
        }
    }
}
