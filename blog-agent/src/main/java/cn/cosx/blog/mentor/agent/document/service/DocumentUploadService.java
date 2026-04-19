package cn.cosx.blog.mentor.agent.document.service;

import cn.cosx.blog.mentor.agent.document.entity.KnowledgeDocument;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档上传服务接口
 */
public interface DocumentUploadService {

    /**
     * 处理文档上传
     *
     * @param file         上传的文件
     * @param docTitle     文档标题
     * @param uploadUser   上传用户
     * @param accessibleBy 访问权限
     * @return 上传后的文档信息
     */
    KnowledgeDocument uploadDocument(MultipartFile file, String docTitle, String uploadUser, String accessibleBy);
}