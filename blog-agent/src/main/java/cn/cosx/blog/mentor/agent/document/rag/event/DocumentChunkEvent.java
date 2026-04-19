package cn.cosx.blog.mentor.agent.document.rag.event;

import cn.cosx.blog.mentor.agent.document.entity.KnowledgeDocument;
import org.springframework.context.ApplicationEvent;

/**
 * 文档切分事件
 * 当文档转换完成后发布此事件，触发文档切分操作
 */
public class DocumentChunkEvent extends ApplicationEvent {

    /**
     * 需要切分的文档
     */
    private final Long documentId;


    public DocumentChunkEvent(Object source, Long documentId) {
        super(source);
        this.documentId = documentId;
    }

    public Long getDocumentId() {
        return documentId;
    }

}
