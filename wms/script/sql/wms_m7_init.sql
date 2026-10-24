-- =====================================================================
-- WMS M7 同步对账与部署收口 初始化脚本
-- 依据：WMS开发计划.html 3.7（同步工程化/Q1 可配置重试/对账报表/部署）+ PRD 闭环⑤
-- 内容：
--   一、sync_record 建表（存量库补建；全新库 wms_table_design.sql 已含）
--   二、菜单：同步对账组（同步监控 / 对账报表）
--   三、同步监控列表字段配置（SYNC_RECORD）
--   四、系统设置：同步重试配置 SYNC_RETRY（Q1：次数/间隔可配置）
-- 说明：sys_field_config.id 手工号段 5300+；sys_setting.id 5310+，冲突请顺延。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 一、sync_record 建表（幂等键 uk_doc(biz_type, doc_no, direction)，验收①）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sync_record (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  target_system VARCHAR(8)   NOT NULL COMMENT '目标系统（ERP/MES）',
  direction     VARCHAR(8)   NOT NULL COMMENT '同步方向（引入=ERP/MES→WMS / 回写=WMS→ERP）',
  biz_type      VARCHAR(32)  NOT NULL COMMENT '单据类型（入库单/出库单/移库单/盘存单）',
  doc_id        BIGINT UNSIGNED DEFAULT NULL COMMENT '本系统单据ID',
  doc_no        VARCHAR(32)  NOT NULL COMMENT '本系统单据号',
  ext_no        VARCHAR(32)  DEFAULT NULL COMMENT '外部系统单号（ERP/MES单号）',
  status        VARCHAR(16)  NOT NULL DEFAULT '未同步' COMMENT '同步状态（成功/未同步/失败；失败=超限转人工）',
  retry_count   INT          NOT NULL DEFAULT 0 COMMENT '重试次数',
  last_msg      VARCHAR(255) DEFAULT NULL COMMENT '最近同步消息（成功确认号/失败原因）',
  last_time     DATETIME     DEFAULT NULL COMMENT '最近同步时间',
  creator       VARCHAR(32)  DEFAULT NULL COMMENT '创建人（触发同步的操作员或系统）',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_doc (biz_type, doc_no, direction),
  KEY idx_status (status),
  KEY idx_ext_no (ext_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外部系统同步记录（PC列表同步状态列+PDA同步状态查询+未同步幂等重试的数据支撑，PRD 闭环⑤）';

-- ---------------------------------------------------------------------
-- 二、菜单：同步对账组（同步监控走引擎动态列表 demo/ 前缀；对账报表为专用页面）
-- ---------------------------------------------------------------------
INSERT INTO sys_module_group (id, group_code, group_name, icon, sort_no, creator, create_time)
SELECT '5301', 'SyncOperate', '同步对账', 'Sync_16x16.png', 60, 'admin', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_module_group WHERE group_name = '同步对账');

INSERT INTO sys_func_module (id, group_id, module_code, module_name, enabled, has_flow, has_log, module_path, icon, sort_no, creator, create_time)
SELECT '5302', g.id, 'SYNC_RECORD', '同步监控', 1, 0, 0, 'demo/sync-record-monitor', 'Sync_16x16.png', 70, 'admin', NOW()
FROM sys_module_group g
WHERE g.group_name = '同步对账'
  AND NOT EXISTS (SELECT 1 FROM sys_func_module WHERE module_code = 'SYNC_RECORD');

INSERT INTO sys_func_module (id, group_id, module_code, module_name, enabled, has_flow, has_log, module_path, icon, sort_no, creator, create_time)
SELECT '5303', g.id, 'SYNC_RECONCILE', '对账报表', 1, 0, 0, 'sync/reconcile', 'Report_16x16.png', 71, 'admin', NOW()
FROM sys_module_group g
WHERE g.group_name = '同步对账'
  AND NOT EXISTS (SELECT 1 FROM sys_func_module WHERE module_code = 'SYNC_RECONCILE');

-- 角色授权（sys_role_module）：按需给目标角色开通
-- INSERT INTO sys_role_module (id, role_id, module_id) SELECT 5304, <角色ID>, 5302;
-- INSERT INTO sys_role_module (id, role_id, module_id) SELECT 5305, <角色ID>, 5303;

-- ---------------------------------------------------------------------
-- 三、同步监控列表字段配置（SYNC_RECORD；引擎动态列表渲染）
-- ---------------------------------------------------------------------
INSERT INTO sys_field_config
  (id, module_group, name, column_name, sql_column, dynamic, header_text, search, sort_index, sort, field_type, sql_query, column_width, default_value, order_by_index, checked, align, fixed_column, column_sort, popover, is_show) VALUES
  (5311, '同步监控', 'SYNC_RECORD', 'docNo',       'doc_no',       0, '单据号',      1, 1,  NULL, 'VARCHAR', '', '150', NULL, NULL, 1, 0, NULL, 1, 0, 1),
  (5312, '同步监控', 'SYNC_RECORD', 'bizType',     'biz_type',     0, '单据类型',    1, 2,  NULL, 'VARCHAR', '', '100', NULL, NULL, 1, 0, NULL, 1, 0, 1),
  (5313, '同步监控', 'SYNC_RECORD', 'direction',   'direction',    0, '方向',        0, 3,  NULL, 'VARCHAR', '', '80',  NULL, NULL, 0, 0, NULL, 0, 0, 1),
  (5314, '同步监控', 'SYNC_RECORD', 'targetSystem','target_system', 0, '目标系统',   0, 4,  NULL, 'VARCHAR', '', '90',  NULL, NULL, 0, 0, NULL, 0, 0, 1),
  (5315, '同步监控', 'SYNC_RECORD', 'status',      'status',       0, '同步状态',    1, 5,  NULL, 'VARCHAR', '', '110', NULL, NULL, 1, 0, NULL, 1, 0, 1),
  (5316, '同步监控', 'SYNC_RECORD', 'extNo',       'ext_no',       0, 'ERP单号',     0, 6,  NULL, 'VARCHAR', '', '140', NULL, NULL, 0, 0, NULL, 1, 0, 1),
  (5317, '同步监控', 'SYNC_RECORD', 'retryCount',  'retry_count',  0, '重试次数',    0, 7,  NULL, 'NUMBER',  '', '90',  NULL, NULL, 0, 2, NULL, 1, 0, 1),
  (5318, '同步监控', 'SYNC_RECORD', 'lastMsg',     'last_msg',     0, '最近消息',    0, 8,  NULL, 'VARCHAR', '', '220', NULL, NULL, 0, 0, NULL, 0, 0, 1),
  (5319, '同步监控', 'SYNC_RECORD', 'lastTime',    'last_time',    0, '最近同步时间', 0, 9, NULL, 'DATE',    '', '160', NULL, NULL, 0, 0, NULL, 1, 0, 1),
  (5320, '同步监控', 'SYNC_RECORD', 'creator',     'creator',      0, '创建人',      0, 10, NULL, 'VARCHAR', '', '100', NULL, NULL, 0, 0, NULL, 0, 0, 1),
  (5321, '同步监控', 'SYNC_RECORD', 'updateTime',  'update_time',  0, '更新时间',    0, 11, NULL, 'DATE',    '', '160', NULL, NULL, 0, 0, NULL, 0, 0, 1);

-- ---------------------------------------------------------------------
-- 四、系统设置：同步重试配置（Q1：自动重试次数/间隔可配置，超限转人工）
--     content JSON：maxRetry=最大自动重试次数；baseIntervalSec=退避基准间隔秒（实际间隔=基准×2^已重试次数）
-- ---------------------------------------------------------------------
INSERT INTO sys_setting (setting_code, setting_name, content, creator, create_time)
SELECT 'SYNC_RETRY', 'ERP同步自动重试配置', '{"maxRetry":5,"baseIntervalSec":60}', 'admin', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_setting WHERE setting_code = 'SYNC_RETRY');
