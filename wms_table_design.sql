-- =====================================================================
-- 云梭智能仓储管理系统（WMS）数据库表结构设计
-- 版本：V1.0  日期：2026-10-15
-- 依据：《WMS智能仓储系统PRD V3.69》+ 可交互原型（prototype/）
-- 数据库：MySQL 8.0+  字符集 utf8mb4  引擎 InnoDB
--
-- 设计说明：
-- 1. 表按业务域划分前缀：
--    base_  基础数据（仓库/仓位/物料/字典等）
--    doc_   业务单据（出库通知/收货/码单/组托/入库/出库/移库/盘存）
--    inv_   库存台账（批次/仓位账/箱码/托盘）
--    asrs_  立库作业（仓储任务/作业任务/作业日志/扫码日志）
--    sync_  外部系统同步（ERP 引入/回写、MES 移交）
--    sys_   系统管理（用户/角色/模块/设置等）
-- 2. 通用审计字段：creator 创建人 / create_time 创建时间 /
--    updater 修改人 / update_time 修改时间（对应原型"创建人/创建时间/修改人/修改时间"列）。
-- 3. 单据同步状态既冗余在单据表上（对应原型列表"同步状态"列，便于查询），
--    同时由 sync_record 记录每次同步动作（支撑幂等重试，PRD 闭环⑤）。
-- 4. 行业计量口径（依据原型列名）：
--    标重=按筒数折算的标准重量；回潮=纱线回潮率（公定/实际）；
--    回数=码单收录的标回/实际回数；筒=纱线卷装单元；标箱=标箱数。
-- 5. 标注"(设计补充)"的字段为 PRD/原型确认的业务规则但原型列表未显式展示，
--    标注"[Assumption]"的字段为设计假设，待业务确认。
-- =====================================================================

-- =====================================================================
-- 一、基础数据域 base_
-- =====================================================================

-- 1. 仓库信息
CREATE TABLE base_warehouse (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  warehouse_no  VARCHAR(32)  NOT NULL COMMENT '仓库编码（如 CK008 / 01.01）',
  warehouse_name VARCHAR(64) NOT NULL COMMENT '仓库名称（如 立体仓库/原料仓/成品仓）',
  erp_no        VARCHAR(32)  DEFAULT NULL COMMENT 'ERP仓库编码（同步自ERP）',
  erp_name      VARCHAR(64)  DEFAULT NULL COMMENT 'ERP仓库名称（同步自ERP）',
  wh_type       VARCHAR(32)  NOT NULL DEFAULT '普通仓库' COMMENT '仓库类型（立库(自营)/普通仓库）',
  enabled       TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用（1=启用 0=停用）',
  last_sync     DATETIME     DEFAULT NULL COMMENT '最近一次与ERP同步时间',
  creator       VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater       VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_warehouse_no (warehouse_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库信息（基础数据-仓库信息）';

-- 2. 仓位信息
CREATE TABLE base_location (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  warehouse_id  BIGINT UNSIGNED NOT NULL COMMENT '所属仓库ID（base_warehouse.id）',
  erp_loc       VARCHAR(32)  DEFAULT NULL COMMENT 'ERP仓位编码（同步自ERP）',
  loc_code      VARCHAR(32)  NOT NULL COMMENT '仓位代码（如 KW0001；立库为 巷道-列-排-层 如 3-2-8-1，Y列如 Y-066）',
  loc_name      VARCHAR(64)  DEFAULT NULL COMMENT '仓位名称（如 立体库1巷道3架2层）',
  outer_name    VARCHAR(64)  DEFAULT NULL COMMENT '外文名称（多语言映射）(设计补充)',
  enabled       TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用（1=启用 0=停用）',
  last_sync     DATETIME     DEFAULT NULL COMMENT '最近一次与ERP同步时间',
  creator       VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater       VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_loc_code (loc_code),
  KEY idx_warehouse (warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓位信息（基础数据-仓位信息，ERP启用仓位管理，仓位与ERP双向同步）';

-- 3. 物料档案（ERP主数据引入，WMS只读引用 [Assumption]）
CREATE TABLE base_material (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  material_code  VARCHAR(64)  NOT NULL COMMENT '物料编码（如 5.1.2.002.017）',
  material_name  VARCHAR(128) NOT NULL COMMENT '物料名称（如 (CASHFEEL)膨体纱）',
  yarn           VARCHAR(64)  DEFAULT NULL COMMENT '纱支（如 2/30 NM、80S/2）',
  comp           VARCHAR(255) DEFAULT NULL COMMENT '原料成分（如 70% CASHFEEL 30% WOOL）',
  erp_no         VARCHAR(64)  DEFAULT NULL COMMENT 'ERP物料编码 [Assumption]',
  last_sync      DATETIME     DEFAULT NULL COMMENT '最近一次与ERP同步时间',
  creator        VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater        VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_material_code (material_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料档案（ERP主数据引入，覆盖原料/半成品/成品等多类物料）';

-- 4. 客户档案（ERP主数据引入 [Assumption]）
CREATE TABLE base_customer (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  customer_code  VARCHAR(64)  DEFAULT NULL COMMENT '客户编码（ERP）',
  customer_name  VARCHAR(128) NOT NULL COMMENT '客户名称（如 江苏阳光集团）',
  enabled        TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用（1=启用 0=停用）',
  last_sync      DATETIME     DEFAULT NULL COMMENT '最近一次与ERP同步时间',
  creator        VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater        VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_customer_name (customer_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户档案（ERP主数据引入，出库单据引用）';

-- 5. 供应商档案（ERP主数据引入 [Assumption]）
CREATE TABLE base_supplier (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  supplier_code  VARCHAR(64)  DEFAULT NULL COMMENT '供应商编码（如 VEN01344）',
  supplier_name  VARCHAR(128) NOT NULL COMMENT '供应商名称（如 云梭智能/云梭纺织(越南)）',
  enabled        TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用（1=启用 0=停用）',
  last_sync      DATETIME     DEFAULT NULL COMMENT '最近一次与ERP同步时间',
  creator        VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater        VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_supplier_name (supplier_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商档案（ERP主数据引入，收货清单引用）';

-- 6. 业务批次（基础数据-业务批次，生产/MES侧批次档案）
CREATE TABLE base_biz_batch (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  biz_batch_no   VARCHAR(64)  NOT NULL COMMENT '业务批号（如 K2610123456-8）',
  material_code  VARCHAR(64)  DEFAULT NULL COMMENT '物料编码（base_material.material_code）',
  material_name  VARCHAR(128) DEFAULT NULL COMMENT '物料名称（冗余，便于查询）',
  yarn           VARCHAR(64)  DEFAULT NULL COMMENT '纱支（冗余）',
  biz_date       DATETIME     DEFAULT NULL COMMENT '批次建立时间',
  creator        VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater        VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_biz_batch_no (biz_batch_no),
  KEY idx_material (material_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务批次档案（生产批次维度，区别于WMS库存批号）';

-- 7. 业务字典（系统管理-业务字典）
CREATE TABLE base_dict (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  dict_code    VARCHAR(64)  NOT NULL COMMENT '字典编码（如 InType/OutType/Unit/Currency）',
  dict_name    VARCHAR(128) NOT NULL COMMENT '字典名称（如 入库类型/出库类型/单位/币种）',
  remark       VARCHAR(255) DEFAULT NULL COMMENT '备注',
  creator      VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater      VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_dict_code (dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务字典（入库类型/出库类型/单位/部门等枚举定义）';

-- 8. 业务字典项
CREATE TABLE base_dict_item (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  dict_id      BIGINT UNSIGNED NOT NULL COMMENT '所属字典ID（base_dict.id）',
  seq          INT          NOT NULL DEFAULT 0 COMMENT '序号',
  display_val  VARCHAR(128) NOT NULL COMMENT '显示值（如 1月/汽运）',
  store_val    VARCHAR(128) NOT NULL COMMENT '存储值（如 1/2/12）',
  ext_val1     VARCHAR(128) DEFAULT NULL COMMENT '扩展值1',
  ext_val2     VARCHAR(128) DEFAULT NULL COMMENT '扩展值2',
  enabled      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用（1=启用 0=停用）',
  creator      VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater      VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  KEY idx_dict (dict_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务字典项（字典明细行）';

-- =====================================================================
-- 二、业务单据域 doc_（按业务链路排序）
-- =====================================================================

-- 9. 出库通知（仓储作业-出库通知，ERP发货通知引入）
CREATE TABLE doc_out_notice (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  notice_no    VARCHAR(32)  NOT NULL COMMENT '出库通知单号（BC开头）',
  status       TINYINT      NOT NULL DEFAULT 0 COMMENT '单据状态（0草稿 1制单中 2审核中 3通过 4驳回，引擎状态机）',
  notice_type  VARCHAR(32)  DEFAULT NULL COMMENT '通知来源（发货通知单/领料申请单/采购退料单/手工新建）',
  notice_date  DATE         DEFAULT NULL COMMENT '单据日期',
  erp_no       VARCHAR(32)  DEFAULT NULL COMMENT 'ERP单号（发货通知/销售订单）',
  erp_date     DATE         DEFAULT NULL COMMENT 'ERP单据日期',
  sales        VARCHAR(32)  DEFAULT NULL COMMENT '销售员',
  sales_dept   VARCHAR(64)  DEFAULT NULL COMMENT '销售部门（销售一部/销售二部/内销部/外贸部）',
  customer_id  BIGINT UNSIGNED DEFAULT NULL COMMENT '客户ID（base_customer.id）',
  customer_name VARCHAR(128) DEFAULT NULL COMMENT '客户名称（冗余）',
  supplier_name VARCHAR(128) DEFAULT NULL COMMENT '供应商名称（集团内供应主体）',
  qty          DECIMAL(18,3) DEFAULT NULL COMMENT '数量（KG）',
  creator      VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater      VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_notice_no (notice_no),
  KEY idx_status (status),
  KEY idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库通知单（ERP发货通知明细引入，数据来源A）';

-- 10. 出库通知明细
CREATE TABLE doc_out_notice_detail (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  notice_id     BIGINT UNSIGNED NOT NULL COMMENT '出库通知ID（doc_out_notice.id）',
  seq           INT          NOT NULL DEFAULT 0 COMMENT '序号',
  delive_type   VARCHAR(16)  DEFAULT NULL COMMENT '送货方式（汽运/快递/自提）',
  batch_no      VARCHAR(64)  DEFAULT NULL COMMENT '批号',
  vat_no        VARCHAR(32)  DEFAULT NULL COMMENT '缸号',
  material_code VARCHAR(64)  DEFAULT NULL COMMENT '物料编码',
  material_name VARCHAR(128) DEFAULT NULL COMMENT '物料名称',
  spec          VARCHAR(64)  DEFAULT NULL COMMENT '物料规格（纱支）',
  comp          VARCHAR(255) DEFAULT NULL COMMENT '原料明细（成分）',
  color_top     VARCHAR(16)  DEFAULT NULL COMMENT '条染打版色号',
  color_piece   VARCHAR(16)  DEFAULT NULL COMMENT '绞染打版色号',
  color_comp    VARCHAR(16)  DEFAULT NULL COMMENT '公司色号',
  color_cust    VARCHAR(16)  DEFAULT NULL COMMENT '客户色号',
  color_name    VARCHAR(32)  DEFAULT NULL COMMENT '色名（如 浅灰兰/藏青）',
  qty           DECIMAL(18,3) DEFAULT NULL COMMENT '数量',
  unit          VARCHAR(16)  DEFAULT NULL COMMENT '单位（KG）',
  warehouse_name VARCHAR(64) DEFAULT NULL COMMENT '仓库',
  loc_code      VARCHAR(32)  DEFAULT NULL COMMENT '仓位',
  creator       VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_notice (notice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库通知明细（按批号/缸号/色号行）';

-- 11. 收货清单（仓储作业-收货清单，ERP在途收料 + MES产件移交）
CREATE TABLE doc_receiving (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  receive_no     VARCHAR(32)  NOT NULL COMMENT '收货清单号（RE开头）',
  source_type    VARCHAR(16)  NOT NULL DEFAULT 'ERP' COMMENT '来源（ERP=ERP在途收料 / MES=MES产件移交）(设计补充，PRD 3.2)',
  out_date       DATE         DEFAULT NULL COMMENT '出库日期（上游出库日期）',
  supplier_id    BIGINT UNSIGNED DEFAULT NULL COMMENT '供应商ID（base_supplier.id）',
  supplier_code  VARCHAR(64)  DEFAULT NULL COMMENT '供应商编码（冗余，如 VEN01344）',
  supplier_name  VARCHAR(128) DEFAULT NULL COMMENT '供应商名称（冗余）',
  out_no         VARCHAR(32)  DEFAULT NULL COMMENT '上游出库单号（ERP出货单/OS单）',
  tran_status    VARCHAR(16)  NOT NULL DEFAULT '在途' COMMENT '在途状态（在途/已到货/已收货）',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_receive_no (receive_no),
  KEY idx_tran_status (tran_status),
  KEY idx_supplier (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货清单（ERP在途收料与MES产件移交的到货跟踪，数据来源A/B）';

-- 12. 码单收录单（仓储作业-码单收录，条码扫码收录）
CREATE TABLE doc_code_collect (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  code_no      VARCHAR(32)  NOT NULL COMMENT '码单号（MD开头）',
  status       TINYINT      NOT NULL DEFAULT 0 COMMENT '单据状态（0=制单中 3=生效，引擎 BillStatusEnum）',
  biz_type     VARCHAR(32)  NOT NULL COMMENT '业务类型（产件入库/采购入库/调拨入库/销售入库）',
  collect_date DATE         DEFAULT NULL COMMENT '收录日期',
  erp_no       VARCHAR(32)  DEFAULT NULL COMMENT '关联ERP单号',
  batch_no     VARCHAR(64)  DEFAULT NULL COMMENT '批号',
  qty          DECIMAL(18,3) DEFAULT NULL COMMENT '数量（KG）',
  auditor      BIGINT UNSIGNED DEFAULT NULL COMMENT '生效人（引擎PASS回写）(设计补充)',
  audit_time   DATETIME     DEFAULT NULL COMMENT '生效时间（引擎PASS回写）(设计补充)',
  remark       VARCHAR(255) DEFAULT NULL COMMENT '备注',
  creator      VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater      VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  auditor      BIGINT UNSIGNED DEFAULT NULL COMMENT '生效人（引擎PASS回写）',
  audit_time   DATETIME     DEFAULT NULL COMMENT '生效时间（引擎PASS回写）',
  PRIMARY KEY (id),
  UNIQUE KEY uk_code_no (code_no),
  KEY idx_biz_type (biz_type),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='码单收录单（PDA/PC扫描箱码收录，产件入库类型为MES移交箱，数据来源B/C）';

-- 13. 码单收录箱明细
CREATE TABLE doc_code_collect_detail (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  collect_id   BIGINT UNSIGNED NOT NULL COMMENT '码单ID（doc_code_collect.id）',
  seq          INT          NOT NULL DEFAULT 0 COMMENT '序号',
  box_barcode  VARCHAR(32)  NOT NULL COMMENT '箱条码（箱码）',
  batch_no     VARCHAR(64)  DEFAULT NULL COMMENT '批号（随箱核对）',
  inv_batch_no VARCHAR(64)  DEFAULT NULL COMMENT '库存批号（入账批次）',
  tubes        INT          DEFAULT NULL COMMENT '筒数',
  std_weight   DECIMAL(18,3) DEFAULT NULL COMMENT '标重（KG）',
  gross_weight DECIMAL(18,3) DEFAULT NULL COMMENT '毛重（KG）',
  net_weight   DECIMAL(18,3) DEFAULT NULL COMMENT '净重（KG）',
  tare         DECIMAL(18,3) DEFAULT NULL COMMENT '皮重（KG）',
  std_back     DECIMAL(18,3) DEFAULT NULL COMMENT '标重回数',
  real_back    DECIMAL(18,3) DEFAULT NULL COMMENT '实际回数',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_box (collect_id, box_barcode),
  KEY idx_barcode (box_barcode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='码单收录箱明细（逐箱条码与重量信息）';

-- 14. 组托单（仓储作业-组托单）
CREATE TABLE doc_pallet_order (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  pallet_order_no  VARCHAR(32)  NOT NULL COMMENT '组托单号（UT开头）',
  status           TINYINT      NOT NULL DEFAULT 0 COMMENT '单据状态（0=制单中 3=生效，引擎 BillStatusEnum）',
  pallet_type      VARCHAR(32)  NOT NULL COMMENT '组托方式（件入条码组托/传入条码组托/手动组托）',
  order_date       DATETIME     DEFAULT NULL COMMENT '单据日期',
  batch_no         VARCHAR(64)  DEFAULT NULL COMMENT '批号',
  vat_no           VARCHAR(32)  DEFAULT NULL COMMENT '缸号',
  remark           VARCHAR(255) DEFAULT NULL COMMENT '备注',
  creator          VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater          VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  auditor          BIGINT UNSIGNED DEFAULT NULL COMMENT '生效人（引擎PASS回写）',
  audit_time       DATETIME     DEFAULT NULL COMMENT '生效时间（引擎PASS回写）',
  PRIMARY KEY (id),
  UNIQUE KEY uk_pallet_order_no (pallet_order_no),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组托单（箱码组托建立托盘档案，PDA组托/PC均可发起）';

-- 15. 组托单托盘明细（编辑页-托盘明细）
CREATE TABLE doc_pallet_order_pallet (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  order_id         BIGINT UNSIGNED NOT NULL COMMENT '组托单ID（doc_pallet_order.id）',
  seq              INT          NOT NULL DEFAULT 0 COMMENT '序号',
  pallet_no        VARCHAR(32)  NOT NULL COMMENT '托盘号',
  tubes            INT          DEFAULT NULL COMMENT '筒数',
  std_weight       DECIMAL(18,3) DEFAULT NULL COMMENT '标重（KG）',
  net_weight       DECIMAL(18,3) DEFAULT NULL COMMENT '净重（KG）',
  gross_weight     DECIMAL(18,3) DEFAULT NULL COMMENT '毛重（KG）',
  tube_weight      DECIMAL(18,3) DEFAULT NULL COMMENT '单筒重量（KG）',
  real_tube        DECIMAL(18,3) DEFAULT NULL COMMENT '实测单筒重（KG）',
  std_tube         DECIMAL(18,3) DEFAULT NULL COMMENT '标准单筒重（KG）',
  create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_order (order_id),
  KEY idx_pallet (pallet_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组托单托盘明细（按托盘汇总重量）';

-- 16. 组托单箱明细（编辑页-箱明细）
CREATE TABLE doc_pallet_order_detail (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  order_id     BIGINT UNSIGNED NOT NULL COMMENT '组托单ID（doc_pallet_order.id）',
  pallet_id    BIGINT UNSIGNED DEFAULT NULL COMMENT '所属托盘明细ID（doc_pallet_order_pallet.id）',
  pallet_no    VARCHAR(32)  DEFAULT NULL COMMENT '托盘号（平挂结构下箱明细与托盘组的关联键，前端从选中托盘带出）',
  seq          INT          NOT NULL DEFAULT 0 COMMENT '序号',
  box_code     VARCHAR(32)  NOT NULL COMMENT '箱码',
  mark_out      VARCHAR(16)  DEFAULT NULL COMMENT '标记出库（原型字段 markOut）',
  box_status    VARCHAR(16)  DEFAULT NULL COMMENT '箱状态（在途/在库/已出库）',
  warehouse_name VARCHAR(64) DEFAULT NULL COMMENT '仓库',
  loc_code     VARCHAR(32)  DEFAULT NULL COMMENT '仓位代码（如 LK1234）',
  loc_name     VARCHAR(64)  DEFAULT NULL COMMENT '仓位名称',
  box_count    INT          DEFAULT NULL COMMENT '箱数',
  std_weight   DECIMAL(18,3) DEFAULT NULL COMMENT '标重（KG）',
  batch_no     VARCHAR(64)  DEFAULT NULL COMMENT '批号',
  vat_no       VARCHAR(32)  DEFAULT NULL COMMENT '缸号',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_order (order_id),
  KEY idx_box (box_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组托单箱明细（托内箱码清单）';

-- 17. 入库单（库存管理-入库单）
CREATE TABLE doc_inbound (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  inbound_no    VARCHAR(32)  NOT NULL COMMENT '入库单号（B开头）',
  status        TINYINT      NOT NULL DEFAULT 0 COMMENT '单据状态（0=制作中 3=生效，引擎 BillStatusEnum）',
  inbound_date  DATE         DEFAULT NULL COMMENT '入库日期',
  sync_status   TINYINT      NOT NULL DEFAULT 2 COMMENT 'ERP同步状态（1=已同步 2=未同步，与引擎枚举一致）',
  erp_doc_no    VARCHAR(32)  DEFAULT NULL COMMENT 'ERP单号（引入来源单号，如 FBDC77210；空则同步失败停留）',
  erp_doc_type  VARCHAR(32)  DEFAULT NULL COMMENT 'ERP单据类型（生产订单明细/收料通知单/调拨出库单/发货通知单明细/领料申请单/采购退料单）',
  erp_in_no     VARCHAR(32)  DEFAULT NULL COMMENT '回写ERP的入库单号（如 SDL123456）',
  source_type   VARCHAR(16)  NOT NULL DEFAULT 'ERP' COMMENT '数据来源（ERP=单据引入 / MES=产件移交 / MANUAL=手工录单）(设计补充，PRD 6.1)',
  reverse_flag  TINYINT      NOT NULL DEFAULT 0 COMMENT '红冲标志（1=红冲单，负向数量，Q4）',
  source_inbound_no  VARCHAR(32) DEFAULT NULL COMMENT '红冲引用的原入库单号（reverse_flag=1 时必填）',
  source_inbound_id  BIGINT UNSIGNED DEFAULT NULL COMMENT '红冲引用的原入库单ID（doc_inbound.id）',
  warehouse_id  BIGINT UNSIGNED DEFAULT NULL COMMENT '仓库ID（base_warehouse.id）',
  warehouse_name VARCHAR(64) DEFAULT NULL COMMENT '仓库名称（冗余）',
  inbound_type  VARCHAR(32)  NOT NULL COMMENT '入库类型（采购入库/产件入库/调拨入库/样品入库/退料入库/产存入库/销售退货入库）',
  work_mode     VARCHAR(16)  DEFAULT NULL COMMENT '作业方式（条码作业/批次作业）',
  batch_no      VARCHAR(64)  DEFAULT NULL COMMENT '批号',
  vat_no        VARCHAR(32)  DEFAULT NULL COMMENT '缸号',
  weight        DECIMAL(18,3) DEFAULT NULL COMMENT '重量（KG）',
  boxes         INT          DEFAULT NULL COMMENT '箱数',
  creator       VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater       VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  auditor       BIGINT UNSIGNED DEFAULT NULL COMMENT '生效人（引擎PASS回写）',
  audit_time    DATETIME     DEFAULT NULL COMMENT '生效时间（引擎PASS回写）',
  PRIMARY KEY (id),
  UNIQUE KEY uk_inbound_no (inbound_no),
  KEY idx_status (status),
  KEY idx_sync (sync_status),
  KEY idx_warehouse (warehouse_id),
  KEY idx_source_inbound (source_inbound_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单（上架确认后生成库存台账，生效后回写ERP入库单号，数据出口A；支持红冲，Q4）';

-- 18. 入库单明细
CREATE TABLE doc_inbound_detail (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  inbound_id    BIGINT UNSIGNED NOT NULL COMMENT '入库单ID（doc_inbound.id）',
  seq           INT          NOT NULL DEFAULT 0 COMMENT '序号',
  box_code      VARCHAR(32)  DEFAULT NULL COMMENT '箱码（条码作业时必填）',
  batch_no      VARCHAR(64)  DEFAULT NULL COMMENT '批号',
  vat_no        VARCHAR(32)  DEFAULT NULL COMMENT '缸号',
  loc_code      VARCHAR(32)  DEFAULT NULL COMMENT '建议/实际仓位（上架确认后回填）',
  color_code    VARCHAR(16)  DEFAULT NULL COMMENT '色号',
  color_name    VARCHAR(32)  DEFAULT NULL COMMENT '色名',
  comp          VARCHAR(255) DEFAULT NULL COMMENT '原料成分',
  material_code VARCHAR(64)  DEFAULT NULL COMMENT '物料编码',
  material_name VARCHAR(128) DEFAULT NULL COMMENT '物料名称',
  yarn          VARCHAR(64)  DEFAULT NULL COMMENT '纱支',
  tubes         INT          DEFAULT NULL COMMENT '筒数',
  std_weight    DECIMAL(18,3) DEFAULT NULL COMMENT '标重（KG）',
  gross_weight  DECIMAL(18,3) DEFAULT NULL COMMENT '毛重（KG）',
  net_weight    DECIMAL(18,3) DEFAULT NULL COMMENT '净重（KG）',
  sync_status   TINYINT      DEFAULT 2 COMMENT '行级同步状态（1=已同步 2=未同步，随主表同步回写）',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_inbound (inbound_id),
  KEY idx_box (box_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单明细（按箱/批次行，含建议仓位）';

-- 19. 出库单（库存管理-出库单）
CREATE TABLE doc_outbound (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  outbound_no   VARCHAR(32)  NOT NULL COMMENT '出库单号（OS开头）',
  status        TINYINT      NOT NULL DEFAULT 0 COMMENT '单据状态（0草稿 1制单中 2审核中 3通过 4驳回，引擎状态机）',
  outbound_date DATE         DEFAULT NULL COMMENT '出库日期',
  sync_status   TINYINT      NOT NULL DEFAULT 2 COMMENT 'ERP同步状态（1成功 2未同步）',
  erp_order_no  VARCHAR(32)  DEFAULT NULL COMMENT 'ERP订单号（引入来源，如 XSXO0000123）',
  erp_out_no    VARCHAR(32)  DEFAULT NULL COMMENT '回写ERP的出库单号（如 XSO123456）',
  customer_id   BIGINT UNSIGNED DEFAULT NULL COMMENT '客户ID（base_customer.id）',
  customer_name VARCHAR(128) DEFAULT NULL COMMENT '客户名称（冗余）',
  sales         VARCHAR(32)  DEFAULT NULL COMMENT '销售员',
  warehouse_id  BIGINT UNSIGNED DEFAULT NULL COMMENT '仓库ID（base_warehouse.id）',
  warehouse_name VARCHAR(64) DEFAULT NULL COMMENT '仓库名称（冗余）',
  outbound_type VARCHAR(32)  NOT NULL COMMENT '出库类型（销售出库/调拨出库/领料出库/直接拣货出库/复磅检出库/采购退料出库/样品出库）',
  work_mode     VARCHAR(16)  DEFAULT NULL COMMENT '作业方式（条码作业/批次作业）',
  batch_no      VARCHAR(64)  DEFAULT NULL COMMENT '批号',
  vat_no        VARCHAR(32)  DEFAULT NULL COMMENT '缸号',
  weight        DECIMAL(18,3) DEFAULT NULL COMMENT '重量（KG）',
  boxes         INT          DEFAULT NULL COMMENT '箱数',
  creator       VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  handler       VARCHAR(32)  DEFAULT NULL COMMENT '经手人（拣货完成人）',
  handle_time   DATETIME     DEFAULT NULL COMMENT '经手时间（拣货完成时间）',
  updater       VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  auditor       BIGINT UNSIGNED DEFAULT NULL COMMENT '审核人（引擎PASS写入）',
  audit_time    DATETIME     DEFAULT NULL COMMENT '审核时间',
  reverse_flag  TINYINT      NOT NULL DEFAULT 0 COMMENT '红冲标志（0正常 1红冲负向单）(设计补充，Q4红冲)',
  source_outbound_no VARCHAR(32) DEFAULT NULL COMMENT '红冲引用原单号 (设计补充，Q4红冲)',
  source_outbound_id BIGINT UNSIGNED DEFAULT NULL COMMENT '红冲引用原单ID (设计补充，Q4红冲)',
  PRIMARY KEY (id),
  UNIQUE KEY uk_outbound_no (outbound_no),
  KEY idx_status (status),
  KEY idx_sync (sync_status),
  KEY idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单（拣货完成后经同步ERP回写出库单号，数据出口A）';

-- 20. 出库单明细
CREATE TABLE doc_outbound_detail (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  outbound_id   BIGINT UNSIGNED NOT NULL COMMENT '出库单ID（doc_outbound.id）',
  seq           INT          NOT NULL DEFAULT 0 COMMENT '序号',
  erp_doc_no    VARCHAR(32)  DEFAULT NULL COMMENT 'ERP单号（行级来源）',
  erp_date      DATE         DEFAULT NULL COMMENT 'ERP单据日期',
  batch_no      VARCHAR(64)  DEFAULT NULL COMMENT '批号',
  batch_info    VARCHAR(128) DEFAULT NULL COMMENT '批次信息（物料名称冗余）',
  vat_no        VARCHAR(32)  DEFAULT NULL COMMENT '缸号',
  box_code      VARCHAR(32)  DEFAULT NULL COMMENT '箱码（条码作业时）',
  boxes         INT          DEFAULT NULL COMMENT '箱数',
  tubes         INT          DEFAULT NULL COMMENT '筒数',
  pieces        INT          DEFAULT NULL COMMENT '件数',
  std_weight    DECIMAL(18,3) DEFAULT NULL COMMENT '标重（KG）',
  gross_weight  DECIMAL(18,3) DEFAULT NULL COMMENT '毛重（KG）',
  net_weight    DECIMAL(18,3) DEFAULT NULL COMMENT '净重（KG）',
  material_code VARCHAR(64)  DEFAULT NULL COMMENT '物料编码',
  material_name VARCHAR(128) DEFAULT NULL COMMENT '物料名称',
  yarn          VARCHAR(64)  DEFAULT NULL COMMENT '纱支',
  comp          VARCHAR(255) DEFAULT NULL COMMENT '原料成分',
  color_code    VARCHAR(16)  DEFAULT NULL COMMENT '色号',
  check_status  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT 'PDA拣货核对状态（0=待核对 1=已核对）(设计补充，PDA扫码核对)',
  checker       VARCHAR(32)  DEFAULT NULL COMMENT '核对操作员 (设计补充)',
  check_time    DATETIME     DEFAULT NULL COMMENT '核对时间 (设计补充)',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_outbound (outbound_id),
  KEY idx_box (box_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单明细（拣货明细，PDA逐箱扫码核对）';

-- 21. 移库单（库存管理-移库单）
CREATE TABLE doc_stock_move (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  move_no      VARCHAR(32)  NOT NULL COMMENT '移库单号（MD开头）',
  move_date    DATE         DEFAULT NULL COMMENT '移库日期',
  status       TINYINT      NOT NULL DEFAULT 0 COMMENT '单据状态（0=制作中 3=生效，引擎 BillStatusEnum）',
  move_type    VARCHAR(32)  NOT NULL COMMENT '移库类型（托盘挪库/编码移库/批次移库/空托移库）',
  sync_status  TINYINT      NOT NULL DEFAULT 2 COMMENT 'ERP同步状态（1=已同步 2=未同步；ERP启用仓位管理，生效后须同步仓位变更）(设计补充，PRD FR-08)',
  erp_no       VARCHAR(32)  DEFAULT NULL COMMENT 'ERP调拨单号（DB开头，仓位变更回写载体）',
  batch_no     VARCHAR(64)  DEFAULT NULL COMMENT '批号（批次移库时）',
  from_warehouse_id BIGINT UNSIGNED DEFAULT NULL COMMENT '源仓库ID（base_warehouse.id）',
  from_loc_code VARCHAR(32)  DEFAULT NULL COMMENT '源仓位',
  to_warehouse_id   BIGINT UNSIGNED DEFAULT NULL COMMENT '目标仓库ID（base_warehouse.id，Q3：须与源仓库相同）',
  to_loc_code  VARCHAR(32)  NOT NULL COMMENT '目标仓位',
  remark       VARCHAR(255) DEFAULT NULL COMMENT '备注',
  reverse_flag TINYINT      NOT NULL DEFAULT 0 COMMENT '红冲标志（0=正常 1=红冲反向单，Q4：源/目标仓位对调）',
  source_move_no VARCHAR(32) DEFAULT NULL COMMENT '红冲引用的原移库单号（reverse_flag=1 时必填）',
  source_move_id BIGINT UNSIGNED DEFAULT NULL COMMENT '红冲引用的原移库单ID（doc_stock_move.id）',
  creator      VARCHAR(32)  DEFAULT NULL COMMENT '创建人（PC/PDA均可发起）',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater      VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  auditor      BIGINT UNSIGNED DEFAULT NULL COMMENT '生效人（引擎PASS回写）',
  audit_time   DATETIME     DEFAULT NULL COMMENT '生效时间（引擎PASS回写）',
  PRIMARY KEY (id),
  UNIQUE KEY uk_move_no (move_no),
  KEY idx_status (status),
  KEY idx_move_type (move_type),
  KEY idx_source_move (source_move_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='移库单（生效后更新托盘与箱码仓位归属，库存批次总量不变，仓位变更同步ERP，数据出口A；支持红冲，Q4；跨仓阻断，Q3）';

-- 22. 移库单明细
CREATE TABLE doc_stock_move_detail (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  move_id      BIGINT UNSIGNED NOT NULL COMMENT '移库单ID（doc_stock_move.id）',
  seq          INT          NOT NULL DEFAULT 0 COMMENT '序号',
  box_code     VARCHAR(32)  DEFAULT NULL COMMENT '箱码（编码移库/托盘挪库时）',
  pallet_no    VARCHAR(32)  DEFAULT NULL COMMENT '托盘号（托盘挪库/空托移库时）',
  batch_no     VARCHAR(64)  DEFAULT NULL COMMENT '批号（批次移库时）',
  vat_no       VARCHAR(32)  DEFAULT NULL COMMENT '缸号',
  to_warehouse_name VARCHAR(64) DEFAULT NULL COMMENT '目标仓库（冗余）',
  to_loc_code  VARCHAR(32)  DEFAULT NULL COMMENT '目标仓位',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_move (move_id),
  KEY idx_box (box_code),
  KEY idx_pallet (pallet_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='移库单明细（按托盘/箱/批次层级记录仓位转移，数量不变）';

-- 23. 盘存单（库存管理-盘存单）
CREATE TABLE doc_stock_count (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  count_no     VARCHAR(32)  NOT NULL COMMENT '盘存单号（CV开头）',
  status       TINYINT      NOT NULL DEFAULT 0 COMMENT '单据状态（0=制作中 3=生效，引擎 BillStatusEnum）',
  count_date   DATE         DEFAULT NULL COMMENT '盘存日期',
  work_mode    VARCHAR(16)  DEFAULT NULL COMMENT '作业方式（条码作业/批次作业）',
  warehouse_id BIGINT UNSIGNED DEFAULT NULL COMMENT '仓库ID（base_warehouse.id）',
  warehouse_name VARCHAR(64) DEFAULT NULL COMMENT '仓库名称（冗余）',
  loc_code     VARCHAR(32)  DEFAULT NULL COMMENT '盘存仓位',
  batch_no     VARCHAR(64)  DEFAULT NULL COMMENT '批号',
  vat_no       VARCHAR(32)  DEFAULT NULL COMMENT '缸号',
  real_pieces  INT          DEFAULT NULL COMMENT '实盘件数',
  real_weight  DECIMAL(18,3) DEFAULT NULL COMMENT '实盘重量（KG）',
  sync_status  TINYINT      NOT NULL DEFAULT 2 COMMENT 'ERP同步状态（1=已同步 2=未同步；主管确认后触发数量变更回写）(设计补充，PRD Q2)',
  erp_no       VARCHAR(32)  DEFAULT NULL COMMENT 'ERP调整单号（盘盈亏数量变更回写载体）(设计补充，PRD Q2)',
  adjust_state TINYINT      DEFAULT NULL COMMENT '盘盈亏调整确认状态（1=待确认 2=已确认 3=已驳回；无盈亏为空）(设计补充，PRD Q2)',
  adjust_confirm_by VARCHAR(32) DEFAULT NULL COMMENT '调整确认人（仓库主管）(设计补充，PRD Q2)',
  adjust_confirm_time DATETIME DEFAULT NULL COMMENT '调整确认时间 (设计补充，PRD Q2)',
  remark       VARCHAR(255) DEFAULT NULL COMMENT '备注',
  creator      VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater      VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  auditor      BIGINT UNSIGNED DEFAULT NULL COMMENT '生效人（引擎PASS回写）',
  audit_time   DATETIME     DEFAULT NULL COMMENT '生效时间（引擎PASS回写）',
  PRIMARY KEY (id),
  UNIQUE KEY uk_count_no (count_no),
  KEY idx_warehouse (warehouse_id),
  KEY idx_adjust (adjust_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘存单（生效生成盘盈亏调整记录，主管确认后调整台账并回写ERP数量变更，Q2）';

-- 24. 盘存单明细（盘前/实盘/盈亏三栏对照）
CREATE TABLE doc_stock_count_detail (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  count_id     BIGINT UNSIGNED NOT NULL COMMENT '盘存单ID（doc_stock_count.id）',
  seq          INT          NOT NULL DEFAULT 0 COMMENT '序号',
  loc_code     VARCHAR(32)  DEFAULT NULL COMMENT '仓位',
  loc_name     VARCHAR(64)  DEFAULT NULL COMMENT '仓位名称',
  inv_batch_no VARCHAR(64)  DEFAULT NULL COMMENT '库存批号',
  vat_no       VARCHAR(32)  DEFAULT NULL COMMENT '缸号',
  pre_tubes    INT          DEFAULT NULL COMMENT '盘前筒数（账面）',
  real_tubes   INT          DEFAULT NULL COMMENT '实盘筒数',
  diff_tubes   INT          DEFAULT NULL COMMENT '盈亏筒数（实盘-盘前）',
  pre_std      DECIMAL(18,3) DEFAULT NULL COMMENT '盘前标重（KG）',
  real_std     DECIMAL(18,3) DEFAULT NULL COMMENT '实盘标重（KG）',
  diff_std     DECIMAL(18,3) DEFAULT NULL COMMENT '盈亏标重（KG）',
  pre_gross    DECIMAL(18,3) DEFAULT NULL COMMENT '盘前毛重（KG）',
  real_gross   DECIMAL(18,3) DEFAULT NULL COMMENT '实盘毛重（KG）',
  diff_gross   DECIMAL(18,3) DEFAULT NULL COMMENT '盈亏毛重（KG）',
  pre_net      DECIMAL(18,3) DEFAULT NULL COMMENT '盘前净重（KG）',
  real_net     DECIMAL(18,3) DEFAULT NULL COMMENT '实盘净重（KG）',
  diff_net     DECIMAL(18,3) DEFAULT NULL COMMENT '盈亏净重（KG）',
  std_mr       DECIMAL(8,2) DEFAULT NULL COMMENT '标定回潮（%）',
  real_mr      DECIMAL(8,2) DEFAULT NULL COMMENT '实际回潮（%）',
  color_code   VARCHAR(16)  DEFAULT NULL COMMENT '色号',
  color_name   VARCHAR(32)  DEFAULT NULL COMMENT '色名',
  material_code VARCHAR(64) DEFAULT NULL COMMENT '物料编码',
  yarn         VARCHAR(64)  DEFAULT NULL COMMENT '纱支',
  comp         VARCHAR(255) DEFAULT NULL COMMENT '原料成分',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_count (count_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘存单明细（盘前/实盘/盈亏对照，按仓位×批次行）';

-- 25. 盘存单箱明细
CREATE TABLE doc_stock_count_box (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  count_detail_id BIGINT UNSIGNED NOT NULL COMMENT '盘存明细ID（doc_stock_count_detail.id）',
  box_code     VARCHAR(32)  NOT NULL COMMENT '箱码',
  real_std     DECIMAL(18,3) DEFAULT NULL COMMENT '实盘标重（KG）',
  real_net     DECIMAL(18,3) DEFAULT NULL COMMENT '实盘净重（KG）',
  real_gross   DECIMAL(18,3) DEFAULT NULL COMMENT '实盘毛重（KG）',
  tare         DECIMAL(18,3) DEFAULT NULL COMMENT '皮重（KG）',
  tube_weight  DECIMAL(18,3) DEFAULT NULL COMMENT '单筒重量（KG）',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_count_detail (count_detail_id),
  KEY idx_box (box_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘存单箱明细（实盘逐箱称重记录）';

-- =====================================================================
-- 三、库存台账域 inv_
-- =====================================================================

-- 26. 库存批次（批次头：批号×缸号 汇总量）
CREATE TABLE inv_batch (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  inv_batch_no  VARCHAR(64)  NOT NULL COMMENT '库存批号',
  biz_batch_no  VARCHAR(64)  DEFAULT NULL COMMENT '业务批号（关联 base_biz_batch）',
  vat_no        VARCHAR(32)  DEFAULT NULL COMMENT '缸号',
  comp          VARCHAR(255) DEFAULT NULL COMMENT '原料成分',
  total_boxes   INT          NOT NULL DEFAULT 0 COMMENT '总箱数',
  total_pieces  INT          NOT NULL DEFAULT 0 COMMENT '总件数',
  total_qty     DECIMAL(18,3) NOT NULL DEFAULT 0 COMMENT '总数量（KG）',
  creator       VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater       VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_batch_vat (inv_batch_no, vat_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存批次（批次粒度总量台账；原型"库存批次"列表的总量列）';

-- 27. 库存批次仓位账（批次×仓库×仓位 在库量）
CREATE TABLE inv_batch_stock (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  inv_batch_id  BIGINT UNSIGNED NOT NULL COMMENT '库存批次ID（inv_batch.id）',
  inv_batch_no  VARCHAR(64)  NOT NULL COMMENT '库存批号（冗余）',
  vat_no        VARCHAR(32)  DEFAULT NULL COMMENT '缸号',
  warehouse_id  BIGINT UNSIGNED DEFAULT NULL COMMENT '仓库ID（base_warehouse.id）',
  warehouse_name VARCHAR(64) DEFAULT NULL COMMENT '仓库名称（冗余）',
  loc_code      VARCHAR(32)  DEFAULT NULL COMMENT '仓位',
  pieces        INT          NOT NULL DEFAULT 0 COMMENT '件数（仓位在库）',
  boxes         INT          NOT NULL DEFAULT 0 COMMENT '箱数',
  tubes         INT          NOT NULL DEFAULT 0 COMMENT '筒数',
  std_weight    DECIMAL(18,3) DEFAULT NULL COMMENT '标重（KG）',
  gross_weight  DECIMAL(18,3) DEFAULT NULL COMMENT '毛重（KG）',
  net_weight    DECIMAL(18,3) DEFAULT NULL COMMENT '净重（KG）',
  comp          VARCHAR(255) DEFAULT NULL COMMENT '原料成分（冗余）',
  color_code    VARCHAR(16)  DEFAULT NULL COMMENT '色号',
  color_name    VARCHAR(32)  DEFAULT NULL COMMENT '色名',
  allow_qty     DECIMAL(18,3) DEFAULT NULL COMMENT '可用数量（扣减已分配/预占）(设计补充)',
  sync_status   VARCHAR(16)  DEFAULT NULL COMMENT '同步状态（成功/未同步）',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  KEY idx_batch (inv_batch_id),
  KEY idx_wh_loc (warehouse_id, loc_code),
  KEY idx_batch_no (inv_batch_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存批次仓位账（多粒度实时库存：批次×仓位；箱粒度=inv_box、条码粒度=箱码汇总）';

-- 28. 箱码信息（库存查询-箱码信息）
CREATE TABLE inv_box (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  box_code     VARCHAR(32)  NOT NULL COMMENT '箱码（最小追溯单元）',
  batch_no     VARCHAR(64)  DEFAULT NULL COMMENT '批号',
  vat_no       VARCHAR(32)  DEFAULT NULL COMMENT '缸号',
  status       VARCHAR(16)  NOT NULL DEFAULT '待入' COMMENT '箱状态（待入/在库/已出库）',
  warehouse_name VARCHAR(64) DEFAULT NULL COMMENT '仓库',
  loc_code     VARCHAR(32)  DEFAULT NULL COMMENT '仓位',
  pallet_no    VARCHAR(32)  DEFAULT NULL COMMENT '所属托盘号',
  in_time      DATETIME     DEFAULT NULL COMMENT '入库时间',
  out_time     DATETIME     DEFAULT NULL COMMENT '出库时间',
  tubes        INT          DEFAULT NULL COMMENT '筒数',
  std_weight   DECIMAL(18,3) DEFAULT NULL COMMENT '标重（KG）',
  tube_weight  DECIMAL(18,3) DEFAULT NULL COMMENT '单筒重量（KG）',
  gross_weight DECIMAL(18,3) DEFAULT NULL COMMENT '毛重（KG）',
  net_weight   DECIMAL(18,3) DEFAULT NULL COMMENT '净重（KG）',
  wait_weight  DECIMAL(18,3) DEFAULT NULL COMMENT '待入重量（KG）',
  box_total    DECIMAL(18,3) DEFAULT NULL COMMENT '箱子总重（KG）',
  unit_qty     DECIMAL(18,3) DEFAULT NULL COMMENT '单件管理量',
  real_mr      DECIMAL(8,2) DEFAULT NULL COMMENT '实际回潮（%）',
  std_mr       DECIMAL(8,2) DEFAULT NULL COMMENT '公定回潮（%）',
  qc_group     VARCHAR(32)  DEFAULT NULL COMMENT '质检组（质检三组/四组/五组）',
  qc_time      DATETIME     DEFAULT NULL COMMENT '检测时间',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_box_code (box_code),
  KEY idx_batch (batch_no),
  KEY idx_pallet (pallet_no),
  KEY idx_status (status),
  KEY idx_wh_loc (warehouse_name, loc_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='箱码信息（任一箱码可追溯批号/缸号/托盘/仓位/时间，追溯核心表）';

-- 29. 托盘信息（库存查询-托盘信息）
CREATE TABLE inv_pallet (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  pallet_no    VARCHAR(32)  NOT NULL COMMENT '托盘号',
  is_empty     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否空托（1=空托 0=载货）',
  loc_code     VARCHAR(32)  DEFAULT NULL COMMENT '当前仓位（库外时为"库外"）',
  status       VARCHAR(8)   NOT NULL DEFAULT '闲' COMMENT '托盘状态（闲=空托可用 / 货=载货）',
  asrs_status  VARCHAR(16)  DEFAULT NULL COMMENT '立库作业状态（入库中/出库中，非立库作业为空）',
  empty_qty    INT          NOT NULL DEFAULT 0 COMMENT '空托数量',
  box_count    INT          NOT NULL DEFAULT 0 COMMENT '在库箱数',
  batch_no     VARCHAR(64)  DEFAULT NULL COMMENT '批号（同托同批）',
  total_boxes  INT          DEFAULT NULL COMMENT '总箱数',
  total_weight DECIMAL(18,3) DEFAULT NULL COMMENT '总重量（KG）',
  creator      VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater      VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_pallet_no (pallet_no),
  KEY idx_loc (loc_code),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='托盘信息（托盘当前状态与归属，移库/组托/立库作业更新本表）';

-- =====================================================================
-- 四、立库作业域 asrs_（ASRS 设备执行链）
-- =====================================================================

-- 30. 仓储任务（立库作业-仓储任务，业务向任务）
CREATE TABLE asrs_wh_task (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  task_no      VARCHAR(32)  NOT NULL COMMENT '仓储任务号（AT开头）',
  status       TINYINT      NOT NULL DEFAULT 0 COMMENT '单据状态（0草稿 1制单中 2审核中 3通过 4驳回，引擎状态机）',
  task_date    DATE         DEFAULT NULL COMMENT '任务日期',
  task_type    VARCHAR(16)  NOT NULL COMMENT '任务类型（条码入库/条码出库/空托出库）',
  exec_status  VARCHAR(16)  NOT NULL DEFAULT '待执行' COMMENT '执行状态（待执行/执行中/已完成）',
  batch_no     VARCHAR(64)  DEFAULT NULL COMMENT '批号',
  material_code VARCHAR(64) DEFAULT NULL COMMENT '物料编码',
  task_batch_no VARCHAR(64) DEFAULT NULL COMMENT '任务批号',
  outbound_type VARCHAR(32) DEFAULT NULL COMMENT '出库类型',
  erp_doc_no   VARCHAR(32)  DEFAULT NULL COMMENT 'ERP单号',
  erp_doc_type VARCHAR(32)  DEFAULT NULL COMMENT 'ERP单据类型',
  std_box      INT          DEFAULT NULL COMMENT '标箱',
  boxes        INT          DEFAULT NULL COMMENT '箱数',
  creator      VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater      VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  auditor      BIGINT UNSIGNED DEFAULT NULL COMMENT '审核人（引擎PASS写入）',
  audit_time   DATETIME     DEFAULT NULL COMMENT '审核时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_task_no (task_no),
  KEY idx_exec (exec_status),
  KEY idx_batch (batch_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓储任务（立库业务任务：扫码终端展示的下架/回库任务清单）';

-- 31. 作业任务（立库作业-作业任务，设备执行任务，WCS回传）
CREATE TABLE asrs_op_task (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  wcs_task_id   VARCHAR(32)  NOT NULL COMMENT 'WCS任务ID（设备调度系统任务号）',
  wh_task_id    BIGINT UNSIGNED DEFAULT NULL COMMENT '关联仓储任务ID（asrs_wh_task.id）',
  status        TINYINT      NOT NULL DEFAULT 0 COMMENT '单据状态（0草稿 1制单中 2审核中 3通过 4驳回，引擎状态机）',
  pallet_no     VARCHAR(32)  DEFAULT NULL COMMENT '托盘号',
  from_loc      VARCHAR(32)  DEFAULT NULL COMMENT '源仓位',
  to_loc        VARCHAR(32)  DEFAULT NULL COMMENT '目标仓位（出口自动/拆托位/仓位号）',
  task_type     VARCHAR(16)  DEFAULT NULL COMMENT '任务类型（条码入库/条码出库/托盘挪库）',
  op_type       VARCHAR(16)  NOT NULL COMMENT '作业类型（入库/出库/移库）',
  port          VARCHAR(32)  DEFAULT NULL COMMENT '出入口（出口自动/月台1号回路/月台2号回路/5#自动线）',
  task_time     DATETIME     DEFAULT NULL COMMENT '任务下发时间',
  asrs_time     DATETIME     DEFAULT NULL COMMENT '立库完成时间',
  exec_status   VARCHAR(16)  NOT NULL DEFAULT '执行中' COMMENT '执行状态（执行中/成功/失败）',
  remark        VARCHAR(255) DEFAULT NULL COMMENT '备注',
  creator       VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater       VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  auditor       BIGINT UNSIGNED DEFAULT NULL COMMENT '审核人（引擎PASS写入）',
  audit_time    DATETIME     DEFAULT NULL COMMENT '审核时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_wcs_task (wcs_task_id),
  KEY idx_wh_task (wh_task_id),
  KEY idx_pallet (pallet_no),
  KEY idx_exec (exec_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业任务（堆垛机/输送线设备执行任务，连接业务单据与设备动作）';

-- 32. 作业日志（立库作业-作业日志）
CREATE TABLE asrs_op_log (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  status       TINYINT      NOT NULL DEFAULT 0 COMMENT '单据状态（0草稿 1制单中 2审核中 3通过 4驳回，引擎状态机）',
  analysis     VARCHAR(16)  NOT NULL DEFAULT '待分析' COMMENT '分析状态（待分析/已分析）',
  wcs_time     DATETIME     DEFAULT NULL COMMENT 'WCS上报时间',
  proc_time    DATETIME     DEFAULT NULL COMMENT '处理完成时间',
  op_type      VARCHAR(16)  DEFAULT NULL COMMENT '作业类型（入库/出库/移库）',
  result       VARCHAR(8)   DEFAULT NULL COMMENT '作业结果（正常/异常）',
  from_loc     VARCHAR(32)  DEFAULT NULL COMMENT '源仓位',
  from_name    VARCHAR(64)  DEFAULT NULL COMMENT '源位置名称',
  to_loc       VARCHAR(32)  DEFAULT NULL COMMENT '目标仓位',
  to_name      VARCHAR(64)  DEFAULT NULL COMMENT '目标位置名称',
  pallet_no    VARCHAR(32)  DEFAULT NULL COMMENT '托盘号',
  doc_date     DATE         DEFAULT NULL COMMENT '单据日期',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_pallet (pallet_no),
  KEY idx_result (result),
  KEY idx_wcs_time (wcs_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业日志（立库作业执行流水，异常记录待分析）';

-- 33. 扫码终端作业日志（立库扫码终端实时流水）
CREATE TABLE asrs_scan_log (
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

-- =====================================================================
-- 五、外部系统同步域 sync_（ERP 引入/回写、MES 移交）
-- =====================================================================

-- 34. 同步记录（单据与 ERP/MES 的每次同步动作，支撑幂等重试）
CREATE TABLE sync_record (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  target_system VARCHAR(8)   NOT NULL COMMENT '目标系统（ERP/MES）',
  direction     VARCHAR(8)   NOT NULL COMMENT '同步方向（引入=ERP/MES→WMS / 回写=WMS→ERP）',
  biz_type      VARCHAR(32)  NOT NULL COMMENT '单据类型（入库单/出库单/移库单-仓位变更/发货通知/收料通知等）',
  doc_id        BIGINT UNSIGNED DEFAULT NULL COMMENT '本系统单据ID',
  doc_no        VARCHAR(32)  NOT NULL COMMENT '本系统单据号',
  ext_no        VARCHAR(32)  DEFAULT NULL COMMENT '外部系统单号（ERP/MES单号）',
  status        VARCHAR(16)  NOT NULL DEFAULT '未同步' COMMENT '同步状态（成功/未同步/失败）',
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

-- =====================================================================
-- 六、系统管理域 sys_
-- =====================================================================

-- 35. 用户信息
CREATE TABLE sys_user (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  account      VARCHAR(32)  NOT NULL COMMENT '登录账号（工号）',
  user_name    VARCHAR(32)  NOT NULL COMMENT '用户名（姓名）',
  emp_id       VARCHAR(32)  DEFAULT NULL COMMENT '员工工号（HR系统）',
  dept         VARCHAR(64)  DEFAULT NULL COMMENT '部门（信息管理部/仓储物流部/纺纱车间等）',
  company      VARCHAR(128) DEFAULT NULL COMMENT '所属公司（浙江云梭纺织股份有限公司等）',
  enabled      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用（1=启用 0=停用）',
  remark       VARCHAR(255) DEFAULT NULL COMMENT '备注',
  creator      VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater      VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_account (account)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息（系统管理-用户信息）';

-- 36. 角色设置
CREATE TABLE sys_role (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  role_name    VARCHAR(64)  NOT NULL COMMENT '角色名称（如 成品库仓管员/仓库管理员）',
  remark       VARCHAR(255) DEFAULT NULL COMMENT '备注',
  creator      VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater      VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色设置（系统管理-角色设置）';

-- 37. 用户角色关联
CREATE TABLE sys_user_role (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id      BIGINT UNSIGNED NOT NULL COMMENT '用户ID（sys_user.id）',
  role_id      BIGINT UNSIGNED NOT NULL COMMENT '角色ID（sys_role.id）',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联（角色设置-角色用户）';

-- 38. 角色功能权限
CREATE TABLE sys_role_module (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  role_id      BIGINT UNSIGNED NOT NULL COMMENT '角色ID（sys_role.id）',
  module_id    BIGINT UNSIGNED NOT NULL COMMENT '功能模块ID（sys_func_module.id）',
  can_view     TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '查看权限（1=有 0=无）(设计补充)',
  can_edit     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '编辑权限（1=有 0=无）(设计补充)',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_module (role_id, module_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色功能权限（角色设置-权限树勾选）';

-- 39. 模块组别（系统管理-模块组别）
CREATE TABLE sys_module_group (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  group_code   VARCHAR(64)  NOT NULL COMMENT '组别编码（如 WMOperate/ASRSOperate/RMOperate/MDB/SystemSetting）',
  group_name   VARCHAR(64)  NOT NULL COMMENT '组别名称（仓储作业/立库作业/库存管理/基础数据/系统管理）',
  icon         VARCHAR(64)  DEFAULT NULL COMMENT '图标文件',
  sort_no      INT          NOT NULL DEFAULT 0 COMMENT '排序号',
  creator      VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater      VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_group_code (group_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模块组别（左侧菜单分组）';

-- 40. 功能模块（系统管理-功能模块）
CREATE TABLE sys_func_module (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID（模块业务ID）',
  group_id     BIGINT UNSIGNED NOT NULL COMMENT '所属组别ID（sys_module_group.id）',
  module_code  VARCHAR(64)  NOT NULL COMMENT '模块编码（如 WMS_In/WMS_Out/WMS_CodeCollect）',
  module_name  VARCHAR(64)  NOT NULL COMMENT '模块名称（如 入库单/出库单/码单收录）',
  module_cls   VARCHAR(128) DEFAULT NULL COMMENT '模块类名（如 UVMMS.WMS_In）',
  org_id       BIGINT UNSIGNED DEFAULT NULL COMMENT '组织ID',
  module_type  VARCHAR(16)  DEFAULT '列表' COMMENT '模块类型（列表/报表/终端）',
  main_table   VARCHAR(64)  DEFAULT NULL COMMENT '主表名（如 T_In）',
  status_field VARCHAR(64)  DEFAULT NULL COMMENT '状态字段名（如 PartState）',
  enabled      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用（1=启用 0=停用）',
  has_flow     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否挂接流程（1=是 0=否）',
  has_log      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否记录日志（1=是 0=否）',
  module_path  VARCHAR(255) DEFAULT NULL COMMENT '模块路径（如 UVMMS.WMS.入库单）',
  icon         VARCHAR(64)  DEFAULT NULL COMMENT '图标文件',
  sort_no      INT          NOT NULL DEFAULT 0 COMMENT '排序号',
  creator      VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater      VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_module_code (module_code),
  KEY idx_group (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='功能模块（27个功能模块注册表，权限树数据源）';

-- 41. 系统设置（系统管理-系统设置）
CREATE TABLE sys_setting (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  setting_code VARCHAR(64)  NOT NULL COMMENT '设置编码（如 ERPType/WeChatMsg/APPLanguagePackage）',
  setting_name VARCHAR(128) NOT NULL COMMENT '设置名称（如 收工方式/企业微信消息/移动端语言包）',
  content      TEXT         DEFAULT NULL COMMENT '设置内容（JSON/文本，按配置项结构存储）(设计补充)',
  creator      VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater      VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_setting_code (setting_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统设置（ERP对接/语言包/消息等全局配置）';

-- 42. 打印模板（系统管理-打印模板）
CREATE TABLE sys_print_template (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  module_name  VARCHAR(64)  NOT NULL COMMENT '适用模块（组托单/箱码信息/入库单/出库单/盘存单/移库单/托盘信息）',
  tpl_name     VARCHAR(128) NOT NULL COMMENT '模板名称（如 托盘标签/成品条码标签/出库单打印模板）',
  tpl_content  TEXT         DEFAULT NULL COMMENT '模板内容（条码打印语言/模板定义）(设计补充)',
  enabled      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用（1=启用 0=停用）',
  creator      VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater      VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  KEY idx_module (module_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打印模板（标签与单据打印模板注册）';

-- 43. 提醒设置（系统管理-提醒设置）
CREATE TABLE sys_remind_setting (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  sort_no      INT          NOT NULL DEFAULT 0 COMMENT '排序号',
  module_name  VARCHAR(64)  NOT NULL COMMENT '所属模块（采购管理/销售管理/仓储作业）',
  remind_name  VARCHAR(128) NOT NULL COMMENT '提醒名称（如 采购送货单 待完善/销售发货 待出库）',
  pk_field     VARCHAR(32)  DEFAULT 'ID' COMMENT '主键字段',
  is_public    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否公共提醒（1=公共 0=按用户授权）',
  allow_users  VARCHAR(512) DEFAULT NULL COMMENT '授权用户ID串（逗号分隔，如 533,541,542）',
  enabled      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用（1=启用 0=停用）',
  creator      VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater      VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  KEY idx_module (module_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒设置（待办事项提醒规则）';

-- 44. 自动编码设置（系统管理-编码规则，支撑取号）
CREATE TABLE sys_auto_code_set (
  id            BIGINT UNSIGNED NOT NULL COMMENT '主键ID（手工指定，非自增）',
  function_name VARCHAR(64)  NOT NULL COMMENT '功能编码（=sys_func_module.module_code，如 WMS_CODE_COLLECT）',
  is_auto_code  TINYINT      NOT NULL DEFAULT 1 COMMENT '是否自动取号（1=是 0=否）',
  prefix        VARCHAR(32)  DEFAULT NULL COMMENT '编号前缀模板（支持占位符 [yyyy]/[yy]/[mm]/[dd]，如 MD[yyyy][mm][dd]）',
  serial_len    INT          NOT NULL DEFAULT 4 COMMENT '流水号长度（按天重置，前缀+日期+流水，如 MD202610230001）',
  creator       VARCHAR(32)  DEFAULT NULL COMMENT '创建人',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updater       VARCHAR(32)  DEFAULT NULL COMMENT '修改人',
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  KEY idx_function (function_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自动编码设置（按功能配置取号前缀与流水长度，M3 取号数据源）';

-- =====================================================================
-- 附：核心表关系说明（逻辑外键，不建物理外键约束）
--   doc_inbound_detail.box_code        → inv_box.box_code          （上架回填）
--   inv_box.pallet_no                  → inv_pallet.pallet_no      （箱→托盘归属）
--   inv_pallet.loc_code                → base_location.loc_code    （托盘→仓位）
--   inv_batch_stock.inv_batch_id       → inv_batch.id              （仓位账→批次）
--   asrs_op_task.wh_task_id            → asrs_wh_task.id           （设备任务→业务任务）
--   asrs_wh_task.task_no / doc_no      → 各单据表                  （任务与单据关联）
--   sync_record.(biz_type, doc_no)     → 各单据表单据号            （同步状态关联）
--   sys_role_module.module_id          → sys_func_module.id        （角色权限）
--   sys_user_role.(user_id, role_id)   → sys_user / sys_role       （用户角色）
-- =====================================================================

-- =====================================================================
-- 附：引擎工作流配置表（基线表，xinao-crm 沿用）
-- AbstractDynamicsFormService.isIgnoreOa 依赖：无配置行时提交直通生效（WMS 全模块语义）。
-- WMS 不配置任何行；表必须存在，否则提交生效报错。
-- =====================================================================
CREATE TABLE wf_config (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  code         VARCHAR(32)  DEFAULT NULL COMMENT '配置编号',
  pact_date    DATE         DEFAULT NULL COMMENT '配置日期',
  status       TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（引擎 BillStatusEnum）',
  sql_main     TEXT         DEFAULT NULL COMMENT '主表查询SQL',
  child_db_name VARCHAR(64) DEFAULT NULL COMMENT '子表库名',
  sql_child    TEXT         DEFAULT NULL COMMENT '子表查询SQL',
  function_name VARCHAR(64) DEFAULT NULL COMMENT '功能模块（FunctionEnum）',
  wf_id        VARCHAR(64) DEFAULT NULL COMMENT '工作流ID（OA）',
  auditor      BIGINT UNSIGNED DEFAULT NULL COMMENT '审核人',
  audit_time   DATETIME    DEFAULT NULL COMMENT '审核时间',
  enabled      TINYINT     NOT NULL DEFAULT 1 COMMENT '是否启用',
  create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_function (function_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='引擎工作流配置（无行=提交直通生效；CRM OA 流程遗留结构，WMS 仅保表）';
