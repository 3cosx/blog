package cn.cosx.blog.mentor.agent.document.controller;

import cn.cosx.blog.mentor.agent.common.BaseResult;
import cn.cosx.blog.mentor.agent.document.entity.KnowledgeDocument;
import cn.cosx.blog.mentor.agent.document.service.DocumentUploadService;
import cn.cosx.blog.mentor.agent.document.service.KnowledgeDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档控制器
 * 提供文档上传、查询等接口
 */
@RestController
@RequestMapping("/document")
@Tag(name = "文档管理", description = "文档上传、查询等接口")
@Slf4j
public class DocumentController {

    @Autowired
    private DocumentUploadService documentUploadService;

    @Autowired
    private KnowledgeDocumentService documentService;

    /**
     * 上传文档
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文档", description = "上传文档到MinIO并保存文档信息")
    public BaseResult<KnowledgeDocument> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "docTitle", required = false) String docTitle,
            @RequestParam(value = "uploadUser", required = false) String uploadUser,
            @RequestParam(value = "accessibleBy", required = false) String accessibleBy
            ) {

        log.info("收到文档上传请求: fileName={}, size={}", file.getOriginalFilename(), file.getSize());

        try {
            if (file.isEmpty()) {
                return BaseResult.newError("文件不能为空");
            }

            // 设置默认值
            if (docTitle == null || docTitle.isBlank()) {
                docTitle = file.getOriginalFilename();
            }
            if (uploadUser == null || uploadUser.isBlank()) {
                uploadUser = "anonymous";
            }
            if (accessibleBy == null || accessibleBy.isBlank()) {
                accessibleBy = "public";
            }

            KnowledgeDocument document = documentUploadService.uploadDocument(file, docTitle, uploadUser, accessibleBy);
            return BaseResult.newSuccess(document);

        } catch (Exception e) {
            log.error("文档上传失败", e);
            return BaseResult.newError("文档上传失败: " + e.getMessage());
        }
    }


    /**
     * 根据文档ID获取文档信息
     */
    @GetMapping("/{docId}")
    @Operation(summary = "获取文档信息", description = "根据文档ID获取文档的基本信息")
    public BaseResult<KnowledgeDocument> getDocument(@PathVariable Long docId) {
        log.info("获取文档信息: docId={}", docId);

        try {
            KnowledgeDocument document = documentService.getById(docId);
            if (document == null) {
                return BaseResult.newError("文档不存在");
            }
            return BaseResult.newSuccess(document);

        } catch (Exception e) {
            log.error("获取文档信息失败: docId={}", docId, e);
            return BaseResult.newError("获取文档信息失败: " + e.getMessage());
        }
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{docId}")
    @Operation(summary = "删除文档", description = "根据文档ID删除文档")
    public BaseResult<String> deleteDocument(@PathVariable Long docId) {
        log.info("删除文档: docId={}", docId);

        try {
            documentService.removeById(docId);
            return BaseResult.newSuccess("文档删除成功");

        } catch (Exception e) {
            log.error("删除文档失败: docId={}", docId, e);
            return BaseResult.newError("删除文档失败: " + e.getMessage());
        }
    }

}