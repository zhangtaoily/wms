-- =====================================================================
-- WMS 存量库 CRM 遗留数据清理脚本（配合 2026-10-24 代码清理）
-- 背景：FunctionEnum 已移除 SAL_*(销售订单/生产通知单/销售合同/意向合同) 与 WRITE_BACK_LOG；
--       存量库 sys_field_config / sys_func_module / sys_role_module 若残留对应行，
--       动态列表请求 FunctionEnum.fromCode 会抛 IllegalArgumentException，须先清理。
-- 执行时机：部署清理后的版本前执行；全新库（仅跑 wms_table_design.sql + m5/m6/m7 init）无需执行。
-- =====================================================================

-- 1. 字段配置：CRM 模块列配置行
DELETE FROM sys_field_config WHERE name IN
  ('SAL_ORDER', 'SAL_PRDNOTICE', 'SAL_CONT', 'SAL_INT_CONT', 'WRITE_BACK_LOG');

-- 2. 功能模块菜单：CRM 模块入口（含变更单后缀 _C 系列）
DELETE FROM sys_func_module WHERE module_code IN
  ('SAL_ORDER', 'SAL_PRDNOTICE', 'SAL_CONT', 'SAL_INT_CONT',
   'SAL_ORDER_C', 'SAL_PRDNOTICE_C', 'SAL_CONT_C', 'SAL_INT_CONT_C',
   'SAL_NOTICE', 'SAL_OUTSTOCK', 'SAL_RESTOCK', 'SAL_RENOTICE',
   'WRITE_BACK_LOG');

-- 3. 角色授权：随模块删除的授权行
DELETE FROM sys_role_module WHERE module_id NOT IN (SELECT id FROM sys_func_module);

-- 4. 编号规则：CRM 单据自动编码
DELETE FROM sys_auto_code_set WHERE function_name IN
  ('SAL_ORDER', 'SAL_PRDNOTICE', 'SAL_CONT', 'SAL_INT_CONT', 'WRITE_BACK_LOG');

-- 说明：sal_* 业务表/触发器仅存在于 CRM 参考库（crm数据库仅结构脚本），
--       WMS 库（wms_table_design.sql 建库）无这些表，无需处理。
