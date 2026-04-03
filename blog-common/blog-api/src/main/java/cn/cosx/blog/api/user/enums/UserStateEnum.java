package cn.cosx.blog.api.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举
 * 对应 users 表中的 state 字段
 */
@Getter
@AllArgsConstructor // 必须加：自动生成全参构造
public enum UserStateEnum {

    /**
     * 初始化/创建成功
     */
    INIT("INIT", "创建成功"),

    /**
     * 已实名认证
     */
    AUTH("AUTH", "实名认证"),

    /**
     * 冻结/禁用
     */
    FROZEN("FROZEN", "冻结");

    /**
     * 状态编码（对应数据库值）
     */
    private final String code;

    /**
     * 状态描述
     */
    private final String message;

}