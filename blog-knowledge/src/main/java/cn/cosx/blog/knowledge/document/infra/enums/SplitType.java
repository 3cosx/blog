package cn.cosx.blog.knowledge.document.infra.enums;

import lombok.Getter;

@Getter
public enum SplitType {

    MARKDOWN_HEADER("markdown_header", "按Markdown标题层级分割"),

    FIXED_SIZE("fixed_size", "按固定大小分割"),

    REGEX("regex", "按正则表达式分割"),

    SEPARATOR("separator", "按分隔符分割");

    private final String value;
    private final String desc;

    SplitType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static SplitType fromValue(String value) {
        for (SplitType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("不支持的分割类型: " + value);
    }
}
