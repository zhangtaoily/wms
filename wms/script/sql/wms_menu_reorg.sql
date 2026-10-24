-- =====================================================================
-- WMS 菜单重组脚本（依据 D:\prj\wms\img\仓储任务列表.png 原型菜单结构）
-- 目标结构（组序/组内序/名称规范，OCR 修正：唯单收录→码单收录、拣托单→组托单、
--   盘库单→盘存单、辅码信息→箱码信息、库位信息→仓位信息、数据字典→业务字典）：
--   仓储作业：出库通知、码单收录、组托单、收货清单
--   立库作业：仓储任务、作业任务、作业日志、立库扫码终端（新增，H5 终端入口）
--   库存管理：入库单、出库单、盘存单、移库单
--   基础数据：业务批次、库存批次、箱码信息、托盘信息、仓位信息、仓库信息（物料管理为PRD外已交付项，排组尾）
--   系统管理：业务字典、系统设置（+管理必需项：用户信息、角色设置、字段配置、打印模板、提醒设置、操作日志）
--   同步对账：同步监控、对账报表（M7 运维组，置末）
-- 按图不建（说明见执行汇报）：
--   外部单据组（K3入库/出库/盘库/测试）——PRD 1.2 明确排除，业务已停用
--   决策分析——帆软报表集成已随 CRM 清理移除，无实现
--   模块组别/功能模块菜单——无独立维护页面（经 角色设置-权限树 维护，M1 交付形态）
-- 幂等：可重复执行。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 一、归组与排序（module_id 不变，角色授权 sys_role_module 不受影响）
-- ---------------------------------------------------------------------
-- 1. 仓储作业（group sort=1）
UPDATE sys_func_module SET group_id=(SELECT id FROM sys_module_group WHERE group_name='仓储作业'), sort_no=1 WHERE module_code='WMS_OUT_NOTICE';      -- 出库通知
UPDATE sys_func_module SET group_id=(SELECT id FROM sys_module_group WHERE group_name='仓储作业'), sort_no=2 WHERE module_code='WMS_CODE_COLLECT';   -- 码单收录
UPDATE sys_func_module SET group_id=(SELECT id FROM sys_module_group WHERE group_name='仓储作业'), sort_no=3 WHERE module_code='WMS_PALLET_ORDER';   -- 组托单
UPDATE sys_func_module SET group_id=(SELECT id FROM sys_module_group WHERE group_name='仓储作业'), sort_no=4 WHERE module_code='WMS_RECEIVE';       -- 收货清单

-- 2. 立库作业（group sort=2）
UPDATE sys_func_module SET group_id=(SELECT id FROM sys_module_group WHERE group_name='立库作业'), sort_no=1 WHERE module_code='ASRS_WH_TASK';       -- 仓储任务
UPDATE sys_func_module SET group_id=(SELECT id FROM sys_module_group WHERE group_name='立库作业'), sort_no=2 WHERE module_code='ASRS_OP_TASK';      -- 作业任务
UPDATE sys_func_module SET group_id=(SELECT id FROM sys_module_group WHERE group_name='立库作业'), sort_no=3 WHERE module_code='ASRS_OP_LOG';       -- 作业日志

-- 3. 库存管理（group sort=3）：按图序 入库/出库/盘存/移库
UPDATE sys_func_module SET group_id=(SELECT id FROM sys_module_group WHERE group_name='库存管理'), sort_no=1 WHERE module_code='WMS_INBOUND';   -- 入库单
UPDATE sys_func_module SET group_id=(SELECT id FROM sys_module_group WHERE group_name='库存管理'), sort_no=2 WHERE module_code='WMS_OUTBOUND';  -- 出库单
UPDATE sys_func_module SET sort_no=3 WHERE module_code='WMS_STOCK_COUNT';   -- 盘存单
UPDATE sys_func_module SET sort_no=4 WHERE module_code='WMS_STOCK_MOVE';    -- 移库单

-- 4. 基础数据（group sort=4）：按图序，物料管理（PRD 6 项外已交付）排组尾
UPDATE sys_func_module SET group_id=(SELECT id FROM sys_module_group WHERE group_name='基础数据'), sort_no=1 WHERE module_code='BIZ_BATCH';    -- 业务批次
UPDATE sys_func_module SET group_id=(SELECT id FROM sys_module_group WHERE group_name='基础数据'), sort_no=2 WHERE module_code='INV_BATCH';    -- 库存批次
UPDATE sys_func_module SET group_id=(SELECT id FROM sys_module_group WHERE group_name='基础数据'), sort_no=3 WHERE module_code='BOX_INFO';     -- 箱码信息
UPDATE sys_func_module SET group_id=(SELECT id FROM sys_module_group WHERE group_name='基础数据'), sort_no=4 WHERE module_code='PALLET_INFO';  -- 托盘信息
UPDATE sys_func_module SET group_id=(SELECT id FROM sys_module_group WHERE group_name='基础数据'), sort_no=5 WHERE module_code='LOCATION';     -- 仓位信息
UPDATE sys_func_module SET group_id=(SELECT id FROM sys_module_group WHERE group_name='基础数据'), sort_no=6 WHERE module_code='WAREHOUSE';    -- 仓库信息
UPDATE sys_func_module SET sort_no=7 WHERE module_code='MATERIAL';          -- 物料管理

-- 5. 系统管理（group sort=5）：按图序（业务字典、系统设置），管理必需项排后
UPDATE sys_func_module SET sort_no=1 WHERE module_code='SYS_Dict';          -- 业务字典
UPDATE sys_func_module SET sort_no=2 WHERE module_code='SYS_Setting';       -- 系统设置
UPDATE sys_func_module SET sort_no=3 WHERE module_code='SYS_User';          -- 用户信息
UPDATE sys_func_module SET sort_no=4 WHERE module_code='SYS_Role';          -- 角色设置
UPDATE sys_func_module SET sort_no=5 WHERE module_code='SYS_FieldConfig';   -- 字段配置
UPDATE sys_func_module SET sort_no=7 WHERE module_code='REMIND_SETTING';    -- 提醒设置
UPDATE sys_func_module SET sort_no=8 WHERE module_code='SYS_OperLog';       -- 操作日志

-- 6. 同步对账（M7 运维组，置末）
UPDATE sys_func_module SET sort_no=1 WHERE module_code='SYNC_RECORD';
UPDATE sys_func_module SET sort_no=2 WHERE module_code='SYNC_RECONCILE';

-- ---------------------------------------------------------------------
-- 二、名称规范化（PRD/图示用语）
-- ---------------------------------------------------------------------
UPDATE sys_func_module SET module_name='仓库信息' WHERE module_code='WAREHOUSE'   AND module_name='仓库管理';
UPDATE sys_func_module SET module_name='仓位信息' WHERE module_code='LOCATION'    AND module_name='仓位管理';
UPDATE sys_func_module SET module_name='箱码信息' WHERE module_code='BOX_INFO'    AND module_name='箱信息';
UPDATE sys_func_module SET module_name='业务字典' WHERE module_code='SYS_Dict'    AND module_name='数据字典';
UPDATE sys_func_module SET module_name='用户信息' WHERE module_code='SYS_User'    AND module_name='用户管理';

-- ---------------------------------------------------------------------
-- 三、新增菜单
-- ---------------------------------------------------------------------
-- 3.1 立库扫码终端（H5 终端 PC 入口；module_path 非 demo/ 前缀 → 前端组件 terminal/scan/index）
INSERT INTO sys_func_module (id, group_id, module_code, module_name, enabled, has_flow, has_log, module_path, icon, sort_no, creator, create_time)
SELECT 5310, g.id, 'TERMINAL_SCAN', '立库扫码终端', 1, 0, 0, 'terminal/scan', 'Scan_16x16.png', 4, 'admin', NOW()
FROM sys_module_group g
WHERE g.group_name = '立库作业'
  AND NOT EXISTS (SELECT 1 FROM sys_func_module WHERE module_code = 'TERMINAL_SCAN');

-- 3.2 打印模板（M2 交付模块，此前无菜单入口；补入系统管理组）
INSERT INTO sys_func_module (id, group_id, module_code, module_name, enabled, has_flow, has_log, module_path, icon, sort_no, creator, create_time)
SELECT 5311, g.id, 'PRINT_TEMPLATE', '打印模板', 1, 0, 0, '/system/print-template', 'Print_16x16.png', 6, 'admin', NOW()
FROM sys_module_group g
WHERE g.group_name = '系统管理'
  AND NOT EXISTS (SELECT 1 FROM sys_func_module WHERE module_code = 'PRINT_TEMPLATE');
