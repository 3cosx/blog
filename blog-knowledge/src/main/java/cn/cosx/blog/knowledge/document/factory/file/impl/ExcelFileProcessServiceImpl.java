package cn.cosx.blog.knowledge.document.factory.file.impl;

import cn.cosx.blog.knowledge.document.domain.entity.KnowledgeDocument;
import cn.cosx.blog.knowledge.document.factory.file.FileProcessService;
import cn.cosx.blog.knowledge.document.infra.enums.FileType;
import cn.cosx.blog.knowledge.document.infra.enums.UseTypeEnums;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ExcelFileProcessServiceImpl implements FileProcessService {


    @Override
    public void processDocument(KnowledgeDocument document, MultipartFile file) {

    }

    @Override
    public boolean supports(FileType fileType, UseTypeEnums useTypeEnums) {
        /**
         * 只有Excel和CSV文件，并且知识库类型为数据查询时支持
         */
        if(FileType.EXCEL.equals(fileType) || FileType.CSV.equals(fileType)){
            return useTypeEnums == UseTypeEnums.DATA_QUERY;
        }
        return false;    }
}
