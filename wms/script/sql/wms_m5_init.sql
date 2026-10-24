-- =====================================================================
-- WMS M5 库内管理（移库单/盘存单）初始化脚本
-- 依据：WMS开发计划.html 3.5（M5 范围/验收标准）+ Q2/Q3/Q4 建议决策
-- 内容：
--   一、DDL 增量（存量库升级：doc_stock_move/doc_stock_count 对齐引擎状态机与 Q2/Q4 决策）
--   二、单据编号规则（sys_auto_code_set：移库 MD 6位、盘存 CV 5位）
--   三、菜单（sys_module_group/sys_func_module：库存管理组下两个模块）
--   四、列表字段配置（sys_field_config：两模块列表列/查询列）
-- 说明：全新库直接执行 wms_table_design.sql 后仅需执行 二/三/四 部分。
--       sys_field_config.id 为手工号段（5000+），如冲突请顺延调整。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 一、DDL 增量（存量库升级）
-- ---------------------------------------------------------------------

-- 21. 移库单：状态列对齐引擎 BillStatusEnum（TINYINT），补红冲列（Q4）与生效人
ALTER TABLE doc_stock_move
  MODIFY COLUMN status      TINYINT     NOT NULL DEFAULT 0 COMMENT '单据状态（0=制作中 3=生效，引擎 BillStatusEnum）',
  MODIFY COLUMN sync_status TINYINT     NOT NULL DEFAULT 2 COMMENT 'ERP同步状态（1=已同步 2=未同步）',
  ADD COLUMN reverse_flag   TINYINT     NOT NULL DEFAULT 0 COMMENT '红冲标志（0=正常 1=红冲反向单，Q4：源/目标仓位对调）' AFTER remark,
  ADD COLUMN source_move_no VARCHAR(32) DEFAULT NULL COMMENT '红冲引用的原移库单号（reverse_flag=1 时必填）' AFTER reverse_flag,
  ADD COLUMN source_move_id BIGINT UNSIGNED DEFAULT NULL COMMENT '红冲引用的原移库单ID（doc_stock_move.id）' AFTER source_move_no,
  ADD COLUMN auditor        BIGINT UNSIGNED DEFAULT NULL COMMENT '生效人（引擎PASS回写）' AFTER update_time,
  ADD COLUMN audit_time     DATETIME    DEFAULT NULL COMMENT '生效时间（引擎PASS回写）' AFTER auditor,
  ADD INDEX idx_source_move (source_move_no);

-- 23. 盘存单：状态列对齐引擎，补同步轴与盘盈亏确认列（Q2）
ALTER TABLE doc_stock_count
  MODIFY COLUMN status        TINYINT     NOT NULL DEFAULT 0 COMMENT '单据状态（0=制作中 3=生效，引擎 BillStatusEnum）',
  MODIFY COLUMN work_mode     VARCHAR(16) DEFAULT NULL COMMENT '作业方式（条码作业/批次作业）',
  ADD COLUMN sync_status      TINYINT     NOT NULL DEFAULT 2 COMMENT 'ERP同步状态（1=已同步 2=未同步；主管确认后触发数量变更回写）' AFTER real_weight,
  ADD COLUMN erp_no           VARCHAR(32) DEFAULT NULL COMMENT 'ERP调整单号（盘盈亏数量变更回写载体）' AFTER sync_status,
  ADD COLUMN adjust_state     TINYINT     DEFAULT NULL COMMENT '盘盈亏调整确认状态（1=待确认 2=已确认 3=已驳回；无盈亏为空）' AFTER erp_no,
  ADD COLUMN adjust_confirm_by VARCHAR(32) DEFAULT NULL COMMENT '调整确认人（仓库主管）' AFTER adjust_state,
  ADD COLUMN adjust_confirm_time DATETIME DEFAULT NULL COMMENT '调整确认时间' AFTER adjust_confirm_by,
  ADD COLUMN auditor          BIGINT UNSIGNED DEFAULT NULL COMMENT '生效人（引擎PASS回写）' AFTER update_time,
  ADD COLUMN audit_time       DATETIME    DEFAULT NULL COMMENT '生效时间（引擎PASS回写）' AFTER auditor,
  ADD INDEX idx_adjust (adjust_state);

-- ---------------------------------------------------------------------
-- 二、单据编号规则（AbstractM3DocService.fetchCode 按 FunctionEnum 取前缀）
-- ---------------------------------------------------------------------
INSERT INTO sys_auto_code_set (id, function_name, is_auto_code, prefix, serial_len) VALUES
  ('5001', 'WMS_STOCK_MOVE',  1, 'MD', 6),
  ('5002', 'WMS_STOCK_COUNT', 1, 'CV', 5);

-- ---------------------------------------------------------------------
-- 三、菜单：库存管理组下新增 移库单/盘存单（Q3：与出库单/入库单同组）
-- ---------------------------------------------------------------------
INSERT INTO sys_module_group (id, group_code, group_name, icon, sort_no, creator, create_time)
SELECT '5003', 'RMOperate', '库存管理', 'Send_16x16.png', 40, 'admin', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_module_group WHERE group_name = '库存管理');

INSERT INTO sys_func_module (id, group_id, module_code, module_name, enabled, has_flow, has_log, module_path, icon, sort_no, creator, create_time)
SELECT '5004', g.id, 'WMS_STOCK_MOVE', '移库单', 1, 0, 1, 'demo/wms-stock-move', 'Move_16x16.png', 50, 'admin', NOW()
FROM sys_module_group g
WHERE g.group_name = '库存管理'
  AND NOT EXISTS (SELECT 1 FROM sys_func_module WHERE module_code = 'WMS_STOCK_MOVE');

INSERT INTO sys_func_module (id, group_id, module_code, module_name, enabled, has_flow, has_log, module_path, icon, sort_no, creator, create_time)
SELECT '5005', g.id, 'WMS_STOCK_COUNT', '盘存单', 1, 0, 1, 'demo/wms-stock-count', 'Count_16x16.png', 51, 'admin', NOW()
FROM sys_module_group g
WHERE g.group_name = '库存管理'
  AND NOT EXISTS (SELECT 1 FROM sys_func_module WHERE module_code = 'WMS_STOCK_COUNT');

-- 角色授权（sys_role_module）：按需给目标角色开通，以下为管理员角色放开示例
-- INSERT INTO sys_role_module (id, role_id, module_id) SELECT 5006, <角色ID>, 5004;
-- INSERT INTO sys_role_module (id, role_id, module_id) SELECT 5007, <角色ID>, 5005;

-- ---------------------------------------------------------------------
-- 四、列表字段配置（sys_field_config，name=FunctionEnum；id 为 5000+ 手工号段）
-- ---------------------------------------------------------------------

-- 4.1 移库单列表
INSERT INTO sys_field_config
  (id, module_group, name, column_name, sql_column, dynamic, header_text, search, sort_index, sort, field_type, sql_query, column_width, default_value, order_by_index, checked, align, fixed_column, column_sort, popover, is_show) VALUES
  (5101, '移库单', 'WMS_STOCK_MOVE', 'moveNo',      'move_no',      0, '移库单号',    1, 1,  NULL, 'VARCHAR', '', '150', NULL, NULL, 1, 0, NULL, 1, 0, 1),
  (5102, '移库单', 'WMS_STOCK_MOVE', 'status',      'CASE status WHEN 0 THEN ''制作中'' WHEN 3 THEN ''生效'' ELSE ''制作中'' END', 0, '状态', 0, 2,  NULL, 'VARCHAR', '', '90',  NULL, NULL, 0, 0, NULL, 0, 0, 1),
  (5103, '移库单', 'WMS_STOCK_MOVE', 'moveType',    'move_type',    0, '移库类型',    1, 3,  NULL, 'VARCHAR', '', '110', NULL, NULL, 1, 0, NULL, 1, 0, 1),
  (5104, '移库单', 'WMS_STOCK_MOVE', 'moveDate',    'move_date',    0, '移库日期',    1, 4,  NULL, 'DATE',    '', '110', NULL, NULL, 1, 0, NULL, 1, 0, 1),
  (5105, '移库单', 'WMS_STOCK_MOVE', 'batchNo',     'batch_no',     0, '批号',        1, 5,  NULL, 'VARCHAR', '', '120', NULL, NULL, 0, 0, NULL, 1, 0, 1),
  (5106, '移库单', 'WMS_STOCK_MOVE', 'fromLocCode', 'from_loc_code', 0, '源仓位',     1, 6,  NULL, 'VARCHAR', '', '100', NULL, NULL, 0, 0, NULL, 1, 0, 1),
  (5107, '移库单', 'WMS_STOCK_MOVE', 'toLocCode',   'to_loc_code',  0, '目标仓位',    0, 7,  NULL, 'VARCHAR', '', '100', NULL, NULL, 0, 0, NULL, 1, 0, 1),
  (5108, '移库单', 'WMS_STOCK_MOVE', 'syncStatus',  'CASE sync_status WHEN 1 THEN ''同步成功'' ELSE ''未同步'' END', 0, '同步状态', 0, 8, NULL, 'VARCHAR', '', '100', NULL, NULL, 0, 0, NULL, 0, 0, 1),
  (5109, '移库单', 'WMS_STOCK_MOVE', 'erpNo',       'erp_no',       0, 'ERP调拨单号', 0, 9,  NULL, 'VARCHAR', '', '130', NULL, NULL, 0, 0, NULL, 1, 0, 1),
  (5110, '移库单', 'WMS_STOCK_MOVE', 'remark',      'remark',       0, '备注',        0, 10, NULL, 'VARCHAR', '', '160', NULL, NULL, 0, 0, NULL, 0, 0, 1),
  (5111, '移库单', 'WMS_STOCK_MOVE', 'creator',     'creator',      0, '创建人',      0, 11, NULL, 'VARCHAR', '', '100', NULL, NULL, 0, 0, NULL, 0, 0, 1),
  (5112, '移库单', 'WMS_STOCK_MOVE', 'createTime',  'create_time',  0, '创建时间',    0, 12, NULL, 'DATE',    '', '150', NULL, NULL, 0, 0, NULL, 0, 0, 1);

-- 4.2 盘存单列表
INSERT INTO sys_field_config
  (id, module_group, name, column_name, sql_column, dynamic, header_text, search, sort_index, sort, field_type, sql_query, column_width, default_value, order_by_index, checked, align, fixed_column, column_sort, popover, is_show) VALUES
  (5151, '盘存单', 'WMS_STOCK_COUNT', 'countNo',      'count_no',      0, '盘存单号',  1, 1,  NULL, 'VARCHAR', '', '150', NULL, NULL, 1, 0, NULL, 1, 0, 1),
  (5152, '盘存单', 'WMS_STOCK_COUNT', 'status',       'CASE status WHEN 0 THEN ''制作中'' WHEN 3 THEN ''生效'' ELSE ''制作中'' END', 0, '状态', 0, 2, NULL, 'VARCHAR', '', '90',  NULL, NULL, 0, 0, NULL, 0, 0, 1),
  (5153, '盘存单', 'WMS_STOCK_COUNT', 'countDate',    'count_date',    0, '盘存日期',  1, 3,  NULL, 'DATE',    '', '110', NULL, NULL, 1, 0, NULL, 1, 0, 1),
  (5154, '盘存单', 'WMS_STOCK_COUNT', 'workMode',     'work_mode',     0, '作业方式',  1, 4,  NULL, 'VARCHAR', '', '100', NULL, NULL, 0, 0, NULL, 1, 0, 1),
  (5155, '盘存单', 'WMS_STOCK_COUNT', 'warehouseName', 'warehouse_name', 0, '仓库',    1, 5,  NULL, 'VARCHAR', '', '120', NULL, NULL, 0, 0, NULL, 1, 0, 1),
  (5156, '盘存单', 'WMS_STOCK_COUNT', 'locCode',      'loc_code',      0, '仓位',      1, 6,  NULL, 'VARCHAR', '', '100', NULL, NULL, 0, 0, NULL, 1, 0, 1),
  (5157, '盘存单', 'WMS_STOCK_COUNT', 'batchNo',      'batch_no',      0, '批号',      1, 7,  NULL, 'VARCHAR', '', '120', NULL, NULL, 0, 0, NULL, 1, 0, 1),
  (5158, '盘存单', 'WMS_STOCK_COUNT', 'vatNo',        'vat_no',        0, '缸号',      0, 8,  NULL, 'VARCHAR', '', '90',  NULL, NULL, 0, 0, NULL, 1, 0, 1),
  (5159, '盘存单', 'WMS_STOCK_COUNT', 'realPieces',   'real_pieces',   0, '实盘件数',  0, 9,  NULL, 'NUMBER',  '', '100', NULL, NULL, 0, 2, NULL, 1, 0, 1),
  (5160, '盘存单', 'WMS_STOCK_COUNT', 'realWeight',   'real_weight',   0, '实盘标重(KG)', 0, 10, NULL, 'NUMBER', '', '120', NULL, NULL, 0, 2, NULL, 1, 0, 1),
  (5161, '盘存单', 'WMS_STOCK_COUNT', 'adjustState',  'CASE adjust_state WHEN 1 THEN ''待确认'' WHEN 2 THEN ''已确认'' WHEN 3 THEN ''已驳回'' ELSE '''' END', 0, '盈亏确认', 0, 11, NULL, 'VARCHAR', '', '100', NULL, NULL, 0, 0, NULL, 0, 0, 1),
  (5162, '盘存单', 'WMS_STOCK_COUNT', 'syncStatus',   'CASE sync_status WHEN 1 THEN ''同步成功'' ELSE ''未同步'' END', 0, '同步状态', 0, 12, NULL, 'VARCHAR', '', '100', NULL, NULL, 0, 0, NULL, 0, 0, 1),
  (5163, '盘存单', 'WMS_STOCK_COUNT', 'erpNo',        'erp_no',        0, 'ERP调整单号', 0, 13, NULL, 'VARCHAR', '', '130', NULL, NULL, 0, 0, NULL, 1, 0, 1),
  (5164, '盘存单', 'WMS_STOCK_COUNT', 'creator',      'creator',       0, '创建人',    0, 14, NULL, 'VARCHAR', '', '100', NULL, NULL, 0, 0, NULL, 0, 0, 1),
  (5165, '盘存单', 'WMS_STOCK_COUNT', 'createTime',   'create_time',   0, '创建时间',  0, 15, NULL, 'DATE',    '', '150', NULL, NULL, 0, 0, NULL, 0, 0, 1);
