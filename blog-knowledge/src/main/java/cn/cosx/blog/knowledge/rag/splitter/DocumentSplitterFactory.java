package cn.cosx.blog.knowledge.rag.splitter;

import cn.cosx.blog.knowledge.document.infra.enums.SplitType;
import cn.cosx.blog.knowledge.document.infra.param.DocumentSplitParam;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentByRegexSplitter;
import dev.langchain4j.data.document.splitter.DocumentByWordSplitter;
import org.springframework.stereotype.Component;

@Component
public class DocumentSplitterFactory {

    public static DocumentSplitter getDocumentSplitter(DocumentSplitParam param) {
        String splitType = param.getSplitType();
        if (SplitType.MARKDOWN_HEADER.getValue().equals(splitType)) {
            return new MarkdownHeaderParentTextSplitter(param.getChunkSize(),param.getOverlapSize());
        }

        if (SplitType.LENGTH.getValue().equals(splitType)) {
            return new DocumentByWordSplitter(param.getChunkSize(), param.getOverlapSize());
        }

        if (SplitType.REGEX.getValue().equals(splitType)) {
            return new DocumentByRegexSplitter(param.getRegex(), "\\n\\n", param.getChunkSize(),param.getOverlapSize());
        }

        if (SplitType.SEPARATOR.getValue().equals(splitType)) {
            return new DocumentByRegexSplitter(param.getRegex(), "\\n\\n", param.getChunkSize(),param.getOverlapSize());
        } else {
            throw new IllegalArgumentException("Invalid split type: " + splitType);
        }

    }
}
