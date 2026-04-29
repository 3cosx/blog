package cn.cosx.blog.knowledge.rag.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 文档向量存储事件
 * 当文档切分完成后发布此事件，触发向量存储操作
 */
@Getter
public class DocumentEmbeddingEvent extends ApplicationEvent {

    private final Long documentId;

    public DocumentEmbeddingEvent(Object source, Long documentId) {
        super(source);
        this.documentId = documentId;
    }
}
