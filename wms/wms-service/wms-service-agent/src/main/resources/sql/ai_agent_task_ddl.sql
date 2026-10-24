-- AI Agent 任务表
CREATE TABLE IF NOT EXISTS `ai_agent_task` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `session_id`  BIGINT       COMMENT '关联会话ID',
    `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
    `title`       VARCHAR(200) NOT NULL COMMENT '任务标题',
    `description` TEXT         COMMENT '任务描述',
    `status`      VARCHAR(20)  DEFAULT 'pending' COMMENT '状态：pending/in_progress/completed',
    `result`      TEXT         COMMENT '任务结果',
    `due_date`    VARCHAR(20)  COMMENT '截止日期',
    `create_by`   VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Agent 任务';
