package cn.cosx.blog.knowledge.document.factory.file.impl;

import cn.cosx.blog.knowledge.document.infra.enums.FileType;
import cn.cosx.blog.knowledge.document.infra.enums.UseTypeEnums;
import org.springframework.stereotype.Service;

@Service
public class PdfFileProcessServiceImpl extends MineruFileProcessServiceImpl {


    @Override
    public boolean supports(FileType fileType, UseTypeEnums useTypeEnums) {
        return fileType == FileType.PDF;
    }
}
