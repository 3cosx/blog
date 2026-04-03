package cn.cosx.blog.api.product.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品状态枚举
 *
 * @author cosx
 */
@Getter
@AllArgsConstructor
public enum ProductStatusEnum {

    /**
     * 草稿
     */
    DRAFT("DRAFT", "初始化"),

    /**
     * 已上架
     */
    ON_SHELF("ON_SHELF", "已上架"),

    /**
     * 已下架
     */
    OFF_SHELF("OFF_SHELF", "已下架"),

    /**
     * 已售罄
     */
    SOLD_OUT("SOLD_OUT", "已售罄");

    private final String code;
    private final String desc;

    public static ProductStatusEnum getByCode(String code) {
        for (ProductStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
}
