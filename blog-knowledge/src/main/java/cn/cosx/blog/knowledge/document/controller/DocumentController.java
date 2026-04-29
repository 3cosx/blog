package cn.cosx.blog.knowledge.document.controller;

import cn.cosx.blog.knowledge.common.BaseResult;
import cn.cosx.blog.knowledge.document.processor.DocumentServiceProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

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
     * @param accessibleBy 可见范围（可选）
     * @return 文档ID
     */
    @PostMapping("/upload")
    public BaseResult<Long> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("docTitle") String docTitle,
            @RequestParam("uploadUser") String uploadUser,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "accessibleBy", required = false) String accessibleBy) {

        log.info("[DocumentController] 接收到文档上传请求，docTitle: {}, uploadUser: {}, fileName: {}",
                docTitle, uploadUser, file.getOriginalFilename());

        try {
            Long docId = documentService.uploadAndProcessDocument(
                    file, docTitle, uploadUser, description, accessibleBy);

            Map<String, Object> data = new HashMap<>();
            data.put("docId", docId);
            data.put("message", "文档上传成功，正在处理中");

            return BaseResult.newSuccess(docId);
        } catch (Exception e) {
            log.error("[DocumentController] 文档上传失败", e);
            return BaseResult.fail(400, e.getMessage());
        }
    }
}
