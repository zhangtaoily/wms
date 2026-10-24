-- =====================================================================
-- WMS M6 立库终端与 PDA 初始化脚本
-- 依据：WMS开发计划.html 3.6（M6 范围/关键交付物/验收标准）+ FR-07 + PDA 引擎增强
-- 内容：
--   一、PDA 引擎三列扩展（sys_field_config 追加 mobile_show/scan_input/mobile_seq，M6 增强）
--   二、asrs_scan_log 建表（存量库补建；全新库 wms_table_design.sql 已含）
--   三、PDA 引擎示例配置（码单收录扫箱字段 scan_input；入库/出库移动列 mobile_show）
-- 说明：
--   1. 终端/PDA 入口为免登 H5 URL（不走 sys_func_module 菜单）：
--      立库扫码终端  http://<host>/terminal/scan
--      PDA 工作台    http://<host>/pda
--      （后端通用接口 /common/terminal/* 均为 SaIgnore；前端路由已加入免登白名单）
--   2. sys_field_config.id 为手工号段（5200+），如冲突请顺延调整。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 一、PDA 引擎三列扩展（M6 增强：PDA 列表/表单按声明渲染，scan_input 字段唤起扫码）
-- ---------------------------------------------------------------------
ALTER TABLE sys_field_config
  ADD COLUMN mobile_show INT(1) DEFAULT 0 COMMENT 'PDA引擎：移动端是否显示（1=显示，M6）',
  ADD COLUMN scan_input  INT(1) DEFAULT 0 COMMENT 'PDA引擎：扫码输入字段（1=PDA端渲染为扫码输入框唤起扫码，M6）',
  ADD COLUMN mobile_seq  INT(11) DEFAULT NULL COMMENT 'PDA引擎：移动端列序（按 mobile_seq 升序渲染，M6）';

-- ---------------------------------------------------------------------
-- 二、asrs_scan_log 建表（存量库补建；wms_table_design.sql 第 33 节已含同结构 DDL）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS asrs_scan_log (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  scan_time    DATETIME     NOT NULL COMMENT '扫码时间',
  action       VARCHAR(16)  NOT NULL COMMENT '动作（入库请求/入库完成/出库请求/出库完成/拣选确认）',
  loc_code     VARCHAR(32)  DEFAULT NULL COMMENT '仓位',
  pallet_no    VARCHAR(32)  DEFAULT NULL COMMENT '托盘号',
  result       VARCHAR(8)   NOT NULL DEFAULT '成功' COMMENT '结果（成功/失败）',
  msg          VARCHAR(255) DEFAULT NULL COMMENT '消息（失败原因等）',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_pallet (pallet_no),
  KEY idx_scan_time (scan_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='扫码终端作业日志（立库扫码终端 ASRS SCAN TERMINAL 实时流水）';

-- ---------------------------------------------------------------------
-- 三、PDA 引擎示例配置（mobile_show/scan_input 标记；列缺失则补行）
--     说明：PC 端 sys_field_config 为活库数据，以下按 name+column_name 幂等维护。
-- ---------------------------------------------------------------------

-- 3.1 码单收录：箱条码列为扫码输入字段（PDA 码单收录扫箱唤起扫码）
UPDATE sys_field_config
SET scan_input = 1, mobile_show = 1, mobile_seq = 1
WHERE name = 'WMS_CODE_COLLECT' AND column_name = 'boxBarcode';

-- 条码号为子表明细列，主表列表以标量子查询取首箱码（原 b.box_barcode 别名在单表查询不可解析）
INSERT INTO sys_field_config
  (id, module_group, name, column_name, sql_column, dynamic, header_text, search, sort_index, field_type, column_width, align, is_show, mobile_show, scan_input, mobile_seq)
SELECT 5201, '码单收录', 'WMS_CODE_COLLECT', 'boxBarcode', '(SELECT d.box_barcode FROM doc_code_collect_detail d WHERE d.collect_id = doc_code_collect.id LIMIT 1)', 0, '条码号', 1, 1, 'VARCHAR', '160', 0, 1, 1, 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_field_config WHERE name = 'WMS_CODE_COLLECT' AND column_name = 'boxBarcode');

-- 3.2 入库单：移动端展示列（PDA 入库待办卡片列序）
UPDATE sys_field_config SET mobile_show = 1, mobile_seq = 1 WHERE name = 'WMS_INBOUND'  AND column_name = 'code';
UPDATE sys_field_config SET mobile_show = 1, mobile_seq = 2 WHERE name = 'WMS_INBOUND'  AND column_name = 'warehouseName';
UPDATE sys_field_config SET mobile_show = 1, mobile_seq = 3 WHERE name = 'WMS_INBOUND'  AND column_name = 'boxes';
UPDATE sys_field_config SET mobile_show = 1, mobile_seq = 4 WHERE name = 'WMS_INBOUND'  AND column_name = 'weight';

-- 3.3 出库单：移动端展示列（PDA 出库待办卡片列序）
UPDATE sys_field_config SET mobile_show = 1, mobile_seq = 1 WHERE name = 'WMS_OUTBOUND' AND column_name = 'code';
UPDATE sys_field_config SET mobile_show = 1, mobile_seq = 2 WHERE name = 'WMS_OUTBOUND' AND column_name = 'customerName';
UPDATE sys_field_config SET mobile_show = 1, mobile_seq = 3 WHERE name = 'WMS_OUTBOUND' AND column_name = 'boxes';
UPDATE sys_field_config SET mobile_show = 1, mobile_seq = 4 WHERE name = 'WMS_OUTBOUND' AND column_name = 'weight';
