package cn.cosx.blog.knowledge.document.infra.enums;

import lombok.Getter;

@Getter
public enum UseTypeEnums {

    DOCUMENT_SEARCH,

    DATA_QUERY,


    ;



    public static UseTypeEnums getEnumByValue(String value) {
        for (UseTypeEnums useTypeEnums : UseTypeEnums.values()) {
            if (useTypeEnums.name().equals(value)) {
                return useTypeEnums;
            }
        }
        return null;
    }
}
