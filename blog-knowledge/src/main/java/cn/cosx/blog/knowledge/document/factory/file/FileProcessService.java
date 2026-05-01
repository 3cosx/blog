package cn.cosx.blog.knowledge.document.factory.file;

import cn.cosx.blog.knowledge.document.domain.entity.KnowledgeDocument;
import cn.cosx.blog.knowledge.document.infra.enums.FileType;
import cn.cosx.blog.knowledge.document.infra.enums.UseTypeEnums;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileProcessService {


    void processDocument(KnowledgeDocument document, MultipartFile file);

    boolean supports(FileType fileType, UseTypeEnums useTypeEnums);
}
