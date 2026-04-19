package cn.cosx.blog.mentor.agent.document.listener;

import cn.cosx.blog.mentor.agent.document.entity.KnowledgeDocument;
import cn.cosx.blog.mentor.agent.document.rag.event.DocumentChunkEvent;
import cn.cosx.blog.mentor.agent.document.rag.event.VectorStoreEvent;
import cn.cosx.blog.mentor.agent.document.service.DocumentChunkService;
import cn.cosx.blog.mentor.agent.document.service.KnowledgeDocumentService;
import cn.cosx.blog.mentor.agent.document.service.VectorStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 文档事件监听器
 * 监听文档处理相关的事件并异步处理
 */
@Component
@Slf4j
public class DocumentEventListener {

    @Autowired
    private DocumentChunkService chunkService;

    @Autowired
    private VectorStoreService vectorStoreService;

    @Autowired
    private KnowledgeDocumentService documentService;

    /**
     * 监听文档切分事件
     * 使用文档处理线程池异步处理文档切分操作
     */
    @Async("documentTaskExecutor")
    @EventListener
    public void handleDocumentChunkEvent(DocumentChunkEvent event) {
        log.info("收到文档切分事件: docId={}",event);

        try {
            KnowledgeDocument document = documentService.getById(event.getDocumentId());
            chunkService.chunkDocument(document);
            log.info("文档切分事件处理完成: docId={}", event.getDocumentId());
        } catch (Exception e) {
            log.error("文档切分事件处理失败: docId={}", event.getDocumentId(), e);
            // 这里可以添加失败处理逻辑，比如记录失败状态、发送告警等
        }
    }

    /**
     * 监听向量存储事件
     * 使用向量存储专用线程池异步处理向量存储操作
     */
    @Async("vectorStoreTaskExecutor")
    @EventListener
    public void handleVectorStoreEvent(VectorStoreEvent event) {
        Long documentId = event.getDocumentId();
        log.info("收到向量存储事件: documentId={}", documentId);

        try {
            KnowledgeDocument document = documentService.getById(documentId);
            if (document == null) {
                log.error("根据documentId查询文档失败: documentId={}", documentId);
                return;
            }
            vectorStoreService.storeDocumentVectors(document);
            log.info("向量存储事件处理完成: docId={}", documentId);
        } catch (Exception e) {
            log.error("向量存储事件处理失败: documentId={}", documentId, e);
            // 这里可以添加失败处理逻辑，比如记录失败状态、发送告警等
        }
    }
}
