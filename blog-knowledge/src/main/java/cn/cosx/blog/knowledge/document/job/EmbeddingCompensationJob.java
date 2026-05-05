package cn.cosx.blog.knowledge.document.job;

import cn.cosx.blog.knowledge.document.service.KnowledgeSegmentService;
import cn.cosx.blog.knowledge.rag.service.IEmbeddingService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 向量化补偿定时任务
 * 扫描有未向量化segment的文档，进行补偿处理
 */
@Slf4j
@Component
public class EmbeddingCompensationJob {

    @Autowired
    private KnowledgeSegmentService knowledgeSegmentService;

    @Autowired
    private IEmbeddingService embeddingService;

    @XxlJob("embeddingCompensationJob")
    public void embeddingCompensationJob() {
        log.info("[EmbeddingCompensation] 开始扫描需要补偿向量的文档...");

        try {
            // 查询有未向量化segment的文档ID列表
            List<Long> documentIds = knowledgeSegmentService.listDocumentIdsWithUnvectorizedSegments();

            if (documentIds.isEmpty()) {
                log.info("[EmbeddingCompensation] 没有需要补偿的文档");
                return;
            }

            log.info("[EmbeddingCompensation] 发现{}个文档需要补偿向量", documentIds.size());

            for (Long documentId : documentIds) {
                try {
                    log.info("[EmbeddingCompensation] 开始补偿文档，docId: {}", documentId);
                    embeddingService.embedDocument(documentId);
                    log.info("[EmbeddingCompensation] 文档补偿完成，docId: {}", documentId);
                } catch (Exception e) {
                    log.error("[EmbeddingCompensation] 文档补偿失败，docId: {}", documentId, e);
                }
            }

            log.info("[EmbeddingCompensation] 补偿任务执行完成");
        } catch (Exception e) {
            log.error("[EmbeddingCompensation] 补偿任务执行异常", e);
        }
    }
}
