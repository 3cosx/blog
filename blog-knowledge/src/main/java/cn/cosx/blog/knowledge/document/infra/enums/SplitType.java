package cn.cosx.blog.knowledge.document.infra.enums;

import lombok.Getter;

@Getter
public enum SplitType {

    MARKDOWN_HEADER("TITLE", "按Markdown标题层级分割"),

    LENGTH("LENGTH", "按固定大小分割"),

    REGEX("REGEX", "按正则表达式分割"),

    SEPARATOR("SEPARATOR", "按分隔符分割");

    private final String value;
    private final String desc;

    SplitType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static SplitType getSplitType(String value) {
        for (SplitType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("不支持的分割类型: " + value);
    }
}
