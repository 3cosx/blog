package cn.cosx.blog.api.product.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品流水类型枚举
 *
 * @author cosx
 */
@Getter
@AllArgsConstructor
public enum ProductStreamTypeEnum {

    /**
     * 入库
     */
    IN("IN", "入库"),

    /**
     * 出库
     */
    OUT("OUT", "出库"),

    /**
     * 锁定
     */
    LOCK("LOCK", "锁定"),

    /**
     * 释放
     */
    RELEASE("RELEASE", "释放"),

    /**
     * 冻结
     */
    FROZEN("FROZEN", "冻结"),

    /**
     * 解冻
     */
    UNFROZEN("UNFROZEN", "解冻");

    private final String code;
    private final String desc;

    public static ProductStreamTypeEnum getByCode(String code) {
        for (ProductStreamTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
