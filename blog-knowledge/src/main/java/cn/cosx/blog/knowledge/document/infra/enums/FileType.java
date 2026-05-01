package cn.cosx.blog.knowledge.document.infra.enums;

import lombok.Getter;

@Getter
public enum FileType {

    PDF("pdf"),
    DOC("doc"),
    TXT("txt"),
    HTML("html"),
    MARKDOWN("markdown"),
    CSV("csv"),
    EXCEL("excel");

    private final String type;

    FileType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
