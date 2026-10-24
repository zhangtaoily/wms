
-- ===== 菜单图标分配（86 个 svg 图标集内取值，修复 *.png 无效值 + 补空值）=====
-- 组图标
UPDATE sys_module_group SET icon='form'    WHERE group_name='仓储作业';   -- 单据作业
UPDATE sys_module_group SET icon='monitor' WHERE group_name='立库作业';   -- 设备监控（保留）
UPDATE sys_module_group SET icon='chart'   WHERE group_name='库存管理';   -- 台账
UPDATE sys_module_group SET icon='build'   WHERE group_name='基础数据';   -- 基础档案（保留）
UPDATE sys_module_group SET icon='tool'    WHERE group_name='系统管理';   -- 修复 sys-tool 无效值
UPDATE sys_module_group SET icon='link'    WHERE group_name='同步对账';   -- 系统间同步
-- 仓储作业
UPDATE sys_func_module SET icon='message'    WHERE module_code='WMS_OUT_NOTICE';    -- 出库通知
UPDATE sys_func_module SET icon='input'      WHERE module_code='WMS_CODE_COLLECT';  -- 码单收录
UPDATE sys_func_module SET icon='zip'        WHERE module_code='WMS_PALLET_ORDER';  -- 组托单（码放打包）
UPDATE sys_func_module SET icon='clipboard'  WHERE module_code='WMS_RECEIVE';       -- 收货清单
-- 立库作业
UPDATE sys_func_module SET icon='job'        WHERE module_code='ASRS_WH_TASK';      -- 仓储任务
UPDATE sys_func_module SET icon='nested'     WHERE module_code='ASRS_OP_TASK';      -- 作业任务
UPDATE sys_func_module SET icon='log'        WHERE module_code='ASRS_OP_LOG';       -- 作业日志
UPDATE sys_func_module SET icon='validCode'  WHERE module_code='TERMINAL_SCAN';     -- 立库扫码终端（扫码）
-- 库存管理
UPDATE sys_func_module SET icon='upload'     WHERE module_code='WMS_INBOUND';       -- 入库单（入）
UPDATE sys_func_module SET icon='download'   WHERE module_code='WMS_OUTBOUND';      -- 出库单（出）
UPDATE sys_func_module SET icon='search'     WHERE module_code='WMS_STOCK_COUNT';   -- 盘存单（盘点查账）
UPDATE sys_func_module SET icon='switch'     WHERE module_code='WMS_STOCK_MOVE';    -- 移库单（仓位切换）
-- 同步对账
UPDATE sys_func_module SET icon='link'       WHERE module_code='SYNC_RECORD';       -- 同步监控
UPDATE sys_func_module SET icon='chart'      WHERE module_code='SYNC_RECONCILE';    -- 对账报表
-- 系统管理
UPDATE sys_func_module SET icon='documentation' WHERE module_code='PRINT_TEMPLATE'; -- 打印模板

-- 出库单图标换为购物车（发货语义，醒目）；相关项联动调整避免语义重复
UPDATE sys_func_module SET icon='shopping'   WHERE module_code='WMS_OUTBOUND';   -- 出库单：购物车=发货出库
UPDATE sys_func_module SET icon='component'  WHERE module_code='MATERIAL';       -- 物料管理：拼图=物料构件（让出购物车）
UPDATE sys_func_module SET icon='server'     WHERE module_code='WAREHOUSE';      -- 仓库信息：库房货架（让出拼图）
