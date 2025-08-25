CREATE TABLE `capture_record`
(
    `id`          bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_name`   varchar(255) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '使用用户(前期淘宝账户, 后期应该是后台用户)',
    `uuid`        int                              DEFAULT NULL COMMENT '生成的uuid',
    `keyword`     varchar(128) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '关键词',
    `sort`        int                              DEFAULT NULL COMMENT '排序规则',
    `start_page`  int                              DEFAULT NULL COMMENT '起始页',
    `sales`       int                              DEFAULT NULL COMMENT '销量',
    `complete`    int                              DEFAULT NULL COMMENT '是否已完成',
    `download`    int                              DEFAULT NULL COMMENT '是否已下载',
    `create_by`   varchar(128) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建人',
    `create_time` datetime                         DEFAULT NULL COMMENT '创建时间',
    `update_by`   varchar(128) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新人',
    `update_time` datetime                         DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;