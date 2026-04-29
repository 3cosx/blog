-- 楼中楼评论表（扁平化设计）
-- 设计特点：使用 topId 字段实现楼中楼，一次查询获取整楼评论，不递归

DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `content` VARCHAR(1000) NOT NULL COMMENT '评论内容',
    `pid` BIGINT NOT NULL DEFAULT 0 COMMENT '父评论ID，0表示一级评论',
    `top_id` BIGINT NOT NULL DEFAULT 0 COMMENT '根评论ID，一级评论的top_id为自身id，子评论的top_id为一级评论id',
    `level` TINYINT NOT NULL DEFAULT 1 COMMENT '评论层级，1=一级评论，2=二级评论，3=三级评论...',
    `reply_count` INT DEFAULT 0 COMMENT '回复数量（一级评论专用）',
    `deleted` INT DEFAULT 0 COMMENT '是否删除',
    `lock_version` INT DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_article_id` (`article_id`),
    INDEX `idx_top_id` (`top_id`),
    INDEX `idx_pid` (`pid`),
    INDEX `idx_article_top` (`article_id`, `top_id`),
    INDEX `idx_article_create` (`article_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表（楼中楼扁平化设计）';