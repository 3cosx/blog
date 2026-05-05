package cn.cosx.blog.knowledge.document.controller;

import cn.cosx.blog.knowledge.common.BaseResult;
import cn.cosx.blog.knowledge.document.infra.param.DocumentSplitParam;
import cn.cosx.blog.knowledge.document.processor.DocumentServiceProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * 文档处理Controller
 */
@Slf4j
@RestController
@RequestMapping("/knowledge/documents")
public class DocumentController {

    @Autowired
    private DocumentServiceProcessor documentService;

    /**
     * 上传并处理文档
     *
     * @param file         上传的文件
     * @param docTitle     文档标题
     * @param uploadUser   上传用户
     * @param description  文档描述（可选）
     * @param useType      用途类型（DOCUMENT_SEARCH / DATA_QUERY）
     * @param tableName    表名（DATA_QUERY 模式必填）
     * @param accessibleBy 可见范围（可选）
     */
    @PostMapping("/upload")
    public BaseResult<Long> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("docTitle") String docTitle,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("uploadUser") String uploadUser,
            @RequestParam("useType") String useType,
            @RequestParam(value = "tableName", required = false) String tableName,
            @RequestParam(value = "accessibleBy", required = false) String accessibleBy) {

        log.info("[DocumentController] 接收到文档上传请求，docTitle: {}, uploadUser: {}, useType: {}, fileName: {}",
                docTitle, uploadUser, useType, file.getOriginalFilename());

        // 数据查询模式必须提供表名
        if ("DATA_QUERY".equals(useType) && (tableName == null || tableName.isBlank())) {
            return BaseResult.fail(400, "数据查询模式下，表名（tableName）不能为空");
        }

        try {
            Long docId = documentService.uploadAndProcessDocument(
                    file, docTitle, uploadUser, description, accessibleBy, useType, tableName);

            return BaseResult.newSuccess(docId);
        } catch (Exception e) {
            log.error("[DocumentController] 文档上传失败", e);
            return BaseResult.fail(400, e.getMessage());
        }
    }

    /**
     * 文档分割
     *
     * @param documentId 文档ID（路径参数）
     * @param splitParam 分割参数（请求体，docId会被路径参数覆盖）
     * @return 分割结果
     */
    @PostMapping("/split/{documentId}")
    public BaseResult<Boolean> splitDocument(
            @PathVariable("documentId") Long documentId,
            @RequestBody DocumentSplitParam splitParam) {

        splitParam.setDocId(documentId);
        log.info("[DocumentController] 接收到文档分割请求，param: {}", splitParam);

        try {
            return BaseResult.newSuccess(documentService.splitDocument(splitParam));
        } catch (Exception e) {
            log.error("[DocumentController] 文档分割失败，documentId: {}", documentId, e);
            return BaseResult.fail(400, e.getMessage());
        }
    }



}
