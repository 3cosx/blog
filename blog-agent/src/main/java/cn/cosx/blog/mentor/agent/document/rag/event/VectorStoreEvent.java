package cn.cosx.blog.mentor.agent.document.rag.event;

import org.springframework.context.ApplicationEvent;

/**
 * 向量存储事件
 * 当文档切分完成后发布此事件，触发向量存储操作
 */
public class VectorStoreEvent extends ApplicationEvent {

    /**
     * 文档ID
     */
    private final Long documentId;

    public VectorStoreEvent(Object source, Long documentId) {
        super(source);
        this.documentId = documentId;
    }

    public Long getDocumentId() {
        return documentId;
    }
}
