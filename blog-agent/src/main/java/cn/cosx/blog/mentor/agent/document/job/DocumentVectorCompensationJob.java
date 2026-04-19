package cn.cosx.blog.mentor.agent.document.job;

import cn.cosx.blog.mentor.agent.document.entity.KnowledgeDocument;
import cn.cosx.blog.mentor.agent.document.enums.DocumentStatus;
import cn.cosx.blog.mentor.agent.document.service.DocumentChunkService;
import cn.cosx.blog.mentor.agent.document.service.KnowledgeDocumentService;
import cn.cosx.blog.mentor.agent.document.service.VectorStoreService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文档向量补偿任务
 * 用于处理未完成分块向量化的文档，从数据库扫描非CHUNKED状态的文件进行补偿处理
 */
@Component
@Slf4j
public class DocumentVectorCompensationJob {

    @Autowired
    private KnowledgeDocumentService documentService;

    @Autowired
    private DocumentChunkService documentChunkService;

    @Autowired
    private VectorStoreService vectorStoreService;

    /**
     * 补偿任务入口
     * 扫描数据库中不是CHUNKED状态且不是VECTOR_STORED状态的文档，进行分块和向量化
     */
    @XxlJob("documentVectorCompensationJob")
    public void documentVectorCompensation() {
        log.info("========================================");
        log.info("【XXL-JOB】开始执行文档向量补偿任务");
        log.info("========================================");
        System.out.println("===== 任务被调用了！ =====");

        int successCount = 0;
        int failCount = 0;

        try {
            // 1. 查询所有未完成分块的文档（状态为CONVERTED的文档）
            LambdaQueryWrapper<KnowledgeDocument> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(KnowledgeDocument::getStatus, DocumentStatus.CONVERTED);

            List<KnowledgeDocument> documents = documentService.list(queryWrapper);
            log.info("查询到待处理的文档数量: {}", documents.size());

            if (documents.isEmpty()) {
                log.info("没有需要补偿的文档");
                XxlJobHelper.handleSuccess("没有需要补偿的文档");
                return;
            }

            for (KnowledgeDocument document : documents) {
                try {
                    log.info("开始处理文档: docId={}, docTitle={}, status={}",
                            document.getDocId(), document.getDocTitle(), document.getStatus());
                    // 3. 执行向量存储
                    vectorStoreService.storeDocumentVectors(document);
                    log.info("文档向量化完成: docId={}", document.getDocId());

                    successCount++;
                    log.info("文档补偿处理成功: docId={}, 累计成功={}, 累计失败={}",
                            document.getDocId(), successCount, failCount);

                } catch (Exception e) {
                    failCount++;
                    log.error("文档补偿处理失败: docId={}, docTitle={}, error={}",
                            document.getDocId(), document.getDocTitle(), e.getMessage(), e);
                }
            }

            log.info("========================================");
            log.info("【XXL-JOB】文档向量补偿任务执行完成, 成功={}, 失败={}", successCount, failCount);
            log.info("========================================");

            if (failCount > 0) {
                XxlJobHelper.handleFail("执行完成，失败文档数: " + failCount);
            } else {
                XxlJobHelper.handleSuccess("执行完成，成功处理文档数: " + successCount);
            }

        } catch (Exception e) {
            log.error("文档向量补偿任务执行异常", e);
            XxlJobHelper.handleFail("任务执行异常: " + e.getMessage());
        }
    }
}
