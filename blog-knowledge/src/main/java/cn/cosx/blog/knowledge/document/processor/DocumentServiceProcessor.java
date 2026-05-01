package cn.cosx.blog.knowledge.document.processor;

import cn.cosx.blog.knowledge.document.domain.entity.KnowledgeDocument;
import cn.cosx.blog.knowledge.document.factory.FileProcessServiceFactory;
import cn.cosx.blog.knowledge.document.factory.file.FileProcessService;
import cn.cosx.blog.knowledge.document.infra.enums.DocumentStatus;
import cn.cosx.blog.knowledge.document.infra.enums.FileType;
import cn.cosx.blog.knowledge.document.infra.enums.UseTypeEnums;
import cn.cosx.blog.knowledge.document.infra.param.DocumentSplitParam;
import cn.cosx.blog.knowledge.rag.service.IDocumentChunkService;
import cn.cosx.blog.knowledge.document.service.IKnowledgeDocumentService;
import cn.cosx.blog.knowledge.document.infra.file.MinioUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class DocumentServiceProcessor {

    @Autowired
    private IKnowledgeDocumentService knowledgeDocumentService;

    @Autowired
    private IDocumentChunkService documentChunkService;

    @Autowired
    private MinioUtils minioUtils;

    @Autowired
    private FileProcessServiceFactory fileProcessServiceFactory;

    /**
     * 上传并处理文档
     */
    public Long uploadAndProcessDocument(MultipartFile file, String docTitle, String uploadUser,
                                         String description, String accessibleBy, String useType) {
        try {
            String originalFilename = file.getOriginalFilename();
            FileType fileType = resolveFileType(file);

            // 1. 创建文档实体
            KnowledgeDocument document = new KnowledgeDocument();
            document.setDocTitle(docTitle);
            document.setUploadUser(uploadUser);
            document.setDescription(description);
            document.setAccessibleBy(accessibleBy);
            document.setStatus(DocumentStatus.INIT);
            document.setUseType(UseTypeEnums.getEnumByValue(useType));
            document.setFileType(fileType);
            document.setOriginalFilename(originalFilename);

            KnowledgeDocument savedDocument = knowledgeDocumentService.saveDocument(document);
            Long docId = savedDocument.getDocId();
            log.info("[Document] 创建文档记录成功，docId: {}", docId);

            // 2. 上传原始文件到MinIO
            String objectName = String.format("documents/%d/%s", docId, originalFilename);
            String docUrl = minioUtils.uploadFile(file, objectName);

            knowledgeDocumentService.updateDocUrl(docId, docUrl);
            log.info("[Document] 文档上传成功，docId: {}, url: {}", docId, docUrl);

            // 3. 通过工厂获取处理器，按文件类型和用途分发处理
            FileProcessService processor = fileProcessServiceFactory.get(fileType, savedDocument.getUseType());
            if (processor != null){
                processor.processDocument(savedDocument, file);

            }

            if(UseTypeEnums.getEnumByValue(useType) == UseTypeEnums.DOCUMENT_SEARCH){
                knowledgeDocumentService.updateStatus(savedDocument.getDocId(), DocumentStatus.CONVERTED);
            }else{
                knowledgeDocumentService.updateStatus(savedDocument.getDocId(), DocumentStatus.STORED);
            }



            return docId;
        } catch (Exception e) {
            log.error("[Document] 上传文档失败", e);
            throw new RuntimeException("上传文档失败", e);
        }
    }

    /**
     * 文档分割
     */
    public Boolean splitDocument(DocumentSplitParam param) {
        documentChunkService.chunkDocument(param);
        return true;
    }

    /**
     * 根据ID获取文档
     */
    public KnowledgeDocument getDocument(Long docId) {
        return knowledgeDocumentService.getByDocId(docId);
    }

    private static final Tika TIKA = new Tika();

    private FileType resolveFileType(MultipartFile file) {
        try {
            String mimeType = TIKA.detect(file.getInputStream(), file.getOriginalFilename());
            log.info("[Document] Tika 检测文件类型，filename: {}, mimeType: {}", file.getOriginalFilename(), mimeType);
            return mimeToFileType(mimeType);
        } catch (Exception e) {
            log.warn("[Document] Tika 检测失败，回退到扩展名匹配", e);
            return resolveFileTypeByExtension(file.getOriginalFilename());
        }
    }

    private FileType mimeToFileType(String mimeType) {
        if (mimeType == null) {
            return FileType.TXT;
        }
        switch (mimeType) {
            case "application/pdf":
                return FileType.PDF;
            case "application/msword":
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return FileType.DOC;
            case "application/vnd.ms-excel":
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                return FileType.EXCEL;
            case "text/csv":
                return FileType.CSV;
            case "text/html":
            case "application/xhtml+xml":
                return FileType.HTML;
            case "text/markdown":
            case "text/x-markdown":
                return FileType.MARKDOWN;
            default:
                if (mimeType.startsWith("text/")) {
                    return FileType.TXT;
                }
                return FileType.TXT;
        }
    }

    private FileType resolveFileTypeByExtension(String filename) {
        if (filename == null) {
            return FileType.TXT;
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return FileType.PDF;
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) return FileType.DOC;
        if (lower.endsWith(".xls") || lower.endsWith(".xlsx")) return FileType.EXCEL;
        if (lower.endsWith(".csv")) return FileType.CSV;
        if (lower.endsWith(".md") || lower.endsWith(".markdown")) return FileType.MARKDOWN;
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return FileType.HTML;
        return FileType.TXT;
    }
}
