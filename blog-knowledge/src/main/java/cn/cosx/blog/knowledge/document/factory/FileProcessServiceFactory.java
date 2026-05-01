package cn.cosx.blog.knowledge.document.factory;

import cn.cosx.blog.knowledge.document.factory.file.FileProcessService;
import cn.cosx.blog.knowledge.document.infra.enums.FileType;
import cn.cosx.blog.knowledge.document.infra.enums.UseTypeEnums;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FileProcessServiceFactory {

    @Autowired
    private List<FileProcessService> fileProcessServices;

    public FileProcessService get(FileType fileProcessType, UseTypeEnums useTypeEnums) {
        return fileProcessServices.stream()
                .filter(service -> service.supports(fileProcessType, useTypeEnums))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "未找到支持的文件处理服务: fileType=" + fileProcessType + ", useType=" + useTypeEnums));
    }
}
