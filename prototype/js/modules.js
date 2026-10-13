/* ============ 模块定义(列/工具栏/编辑表单) ============ */
var D = WMS_DATA;

/* 字段构造器 */
function F(l, k, t, o) { return { l: l, k: k, t: t || "text", ro: !!(o && o.ro), opts: o && o.opts || null, req: !!(o && o.req), w: o && o.w }; }
function SEL(l, k, opts, o) { return F(l, k, "select", Object.assign({ opts: opts }, o || {})); }
function CHK(l, k) { return { l: l, k: k, t: "check" }; }
function TA(l, k) { return { l: l, k: k, t: "textarea" }; }

var EDIT_TOOLBAR_DEFAULT = ["新建", "保存", "生效", "取消", "刷新", "打印", "联动更新", "关闭"];
var EDIT_TOOLBAR_SIMPLE = ["新建", "保存", "删除", "联动更新", "关闭"];

/* ============ 模块注册表 ============ */
var MODULES = {

  /* ---------- 仓储作业 ---------- */
  outNotice: {
    title: "出库通知", dateField: "单据日期",
    toolbar: ["新建", "查看", "删除", "导入发货通知", "导入领料申请", "导入采购退料", "查询选项", "查询", "高级查询"],
    columns: [
      { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "单据编号", k: "no", w: 125, cls: "bold c-blue" },
      { t: "单据状态", k: "status", w: 60, cls: "ctr", cmap: { "生效": "c-green", "制单中": "c-red" } },
      { t: "单据日期", k: "date", w: 80 }, { t: "ERP单号", k: "erpNo", w: 110 },
      { t: "ERP日期", k: "erpDate", w: 80 }, { t: "销售员", k: "sales", w: 60 }, { t: "销售部门", k: "salesDept", w: 70 },
      { t: "客户", k: "customer", w: 130 }, { t: "供应商", k: "supplier", w: 110 }, { t: "总件量", k: "qty", w: 90, cls: "num" },
      { t: "创建人", k: "creator", w: 60 }, { t: "创建时间", k: "cTime", w: 90 },
      { t: "修改人", k: "editor", w: 60 }, { t: "修改时间", k: "eTime", w: 90 }
    ],
    gen: D.outNotice,
    edit: {
      toolbar: ["删除", "保存", "制单中", "联动更新", "关闭"],
      header: [
        [F("申请单号", "no", "text", { ro: true }), F("单据日期", "date", "date"), SEL("申请类型", "_applyType", ["销售出库", "领料出库", "采购退料", "调拨出库"]), F("ERP单号", "erpNo"), F("ERP日期", "erpDate", "date")]
      ],
      tabs: [
        {
          title: "物料批发明细", toolbar: ["引入通知单", "调拨订单引入", "删除", "引入", "扫描引入", "移除"],
          columns: [
            { t: "发货类型", k: "deliveType", w: 60, cls: "ctr" }, { t: "批号", k: "batch", w: 130 },
            { t: "缸号", k: "vat", w: 110 }, { t: "物料编号", k: "material", w: 105 }, { t: "物料名称", k: "materialName", w: 140 },
            { t: "物料规格", k: "spec", w: 70 }, { t: "原料明细", k: "comp", w: 130 },
            { t: "条染打版色号", k: "colorTop", w: 85, cls: "ctr" }, { t: "统染打版色号", k: "colorPiece", w: 85, cls: "ctr" },
            { t: "公司色号", k: "colorComp", w: 70, cls: "ctr" }, { t: "客户色号", k: "colorCust", w: 70, cls: "ctr" },
            { t: "颜色", k: "color", w: 55 }, { t: "标重", k: "qty", w: 80, cls: "num" }, { t: "单位", k: "unit", w: 40, cls: "ctr" },
            { t: "仓库", k: "wh", w: 75 }, { t: "仓位", k: "loc", w: 80 }
          ], gen: "noticeDetail"
        },
        { title: "更多业务详情", remarkOnly: true }
      ]
    }
  },

  outboundOrder: {
    title: "出库单", dateField: "出库日期",
    toolbar: ["新增", "编辑", "查看", "删除", "提交", "同步ERP", "查询选项", "查询", "高级查询", "刷新", "打印", "导出"],
    columns: [
      { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "出库单号", k: "no", w: 130, cls: "bold c-blue" },
      { t: "状态", k: "status", w: 55, cls: "ctr", cmap: { "生效": "c-green", "制作中": "c-red" } },
      { t: "ERP订单单号", k: "erpOrder", w: 110 }, { t: "客户", k: "customer", w: 120 }, { t: "销售员", k: "sales", w: 60 },
      { t: "同步状态", k: "sync", w: 60, cls: "ctr", cmap: { "成功": "c-green", "未同步": "c-red" } },
      { t: "ERP出库单号", k: "erpOutNo", w: 110 }, { t: "出库日期", k: "date", w: 80 }, { t: "仓库", k: "wh", w: 80 },
      { t: "出库类型", k: "outType", w: 90, cls: "ctr" }, { t: "作业方式", k: "workMode", w: 70, cls: "ctr" },
      { t: "库存批号", k: "batch", w: 125 }, { t: "缸号", k: "vat", w: 105 }, { t: "标重", k: "weight", w: 85, cls: "num" },
      { t: "箱数", k: "boxes", w: 50, cls: "num" }, { t: "创建人", k: "creator", w: 60 }, { t: "创建时间", k: "cTime", w: 88 },
      { t: "经办人", k: "handler", w: 60 }, { t: "经办时间", k: "hTime", w: 88 }
    ],
    gen: D.outbound,
    edit: {
      header: [
        [F("出库单号", "no", "text", { ro: true }), F("出库日期", "date", "date"), SEL("出库类型", "outType", D.OUT_TYPES), SEL("作业方式", "workMode", D.WORK_MODES), SEL("数据模式", "_dataMode", ["本地手动", "作业日志", "在线同步"])],
        [SEL("仓库编号", "_whNo", D.WAREHOUSES.map(function (w) { return w.no; })), F("仓库名称", "wh"), SEL("仓库类型", "_whType", ["普通仓库", "立库(自营)"]), F("ERP仓库名", "_erpWh"), F("客户", "customer"), F("ERP订单单号", "erpOrder")]
      ],
      tabs: [
        {
          title: "出库明细", toolbar: ["新增", "引出通知", "设置批次", "业务批次", "库存批次", "删除"],
          columns: [
            { t: "ERP单号", k: "erpNo", w: 100 }, { t: "ERP日期", k: "erpDate", w: 78 },
            { t: "业务批号", k: "batch", w: 125 }, { t: "批次信息", k: "batchInfo", w: 120 }, { t: "缸号", k: "vat", w: 105 },
            { t: "箱数", k: "boxes", w: 45, cls: "num" }, { t: "筒数", k: "tubes", w: 45, cls: "num" }, { t: "件数", k: "pieces", w: 45, cls: "num" },
            { t: "标重", k: "stdWeight", w: 80, cls: "num" }, { t: "毛重", k: "grossWeight", w: 80, cls: "num" }, { t: "净重", k: "netWeight", w: 80, cls: "num" },
            { t: "产品编号", k: "material", w: 100 }, { t: "品名", k: "materialName", w: 130 }, { t: "纱支", k: "yarn", w: 65 },
            { t: "原料明细", k: "comp", w: 120 }, { t: "色号", k: "colorCode", w: 60, cls: "ctr" }
          ], gen: "outDetail"
        },
        { title: "源单详细信息", remarkOnly: true }
      ],
      lowerTabs: [
        {
          title: "库存批次明细", columns: [
            { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "同步状态", k: "sync", w: 60, cls: "ctr" }, { t: "仓位", k: "loc", w: 80 },
            { t: "库存批号", k: "invBatch", w: 125 }, { t: "缸号", k: "vat", w: 105 }, { t: "件数", k: "pieces", w: 50, cls: "num" },
            { t: "箱数", k: "boxes", w: 45, cls: "num" }, { t: "筒数", k: "tubes", w: 45, cls: "num" },
            { t: "标重", k: "stdWeight", w: 80, cls: "num" }, { t: "毛重", k: "grossWeight", w: 80, cls: "num" }, { t: "净重", k: "netWeight", w: 80, cls: "num" },
            { t: "原料成分", k: "comp", w: 120 }, { t: "色号", k: "colorCode", w: 60, cls: "ctr" }, { t: "色名", k: "colorName", w: 60 },
            { t: "AllowQty", k: "allowQty", w: 85, cls: "num" }
          ], gen: "invBatchDetail"
        },
        { title: "作业任务详情", columns: [
          { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "任务单号", k: "taskNo", w: 120 }, { t: "状态", k: "status", w: 55, cls: "ctr" },
          { t: "原始库位", k: "fromLoc", w: 80 }, { t: "目标库位", k: "toLoc", w: 80 }, { t: "出库口", k: "port", w: 90 },
          { t: "任务时间", k: "tTime", w: 130 }, { t: "执行状态", k: "exec", w: 60, cls: "ctr" }
        ], gen: "taskDetail" }
      ]
    }
  },

  receiving: {
    title: "收货清单", dateField: "出库日期",
    toolbar: ["查看", "刷新", "查询选项", "查询", "高级查询"],
    columns: [
      { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "收货单号", k: "no", w: 130, cls: "bold c-blue" },
      { t: "出库日期", k: "outDate", w: 85 }, { t: "供应商", k: "supplier", w: 120 }, { t: "供应商编号", k: "supplierCode", w: 90 },
      { t: "出库单号", k: "outNo", w: 120 }, { t: "运输状态", k: "tranStatus", w: 70, cls: "ctr", cmap: { "在途": "c-blue", "已到货": "c-green", "已收货": "c-green" } },
      { t: "创建时间", k: "cTime", w: 88 }, { t: "修改时间", k: "eTime", w: 88 }
    ],
    gen: D.receiving,
    viewOnly: true,
    edit: {
      toolbar: ["刷新", "打印", "关闭"],
      header: [
        [F("收货单号", "no"), F("出库日期", "outDate", "date"), F("运输状态", "tranStatus"), F("供应商", "supplier"), F("供应商编号", "supplierCode")]
      ],
      tabs: [{ title: "收货明细", toolbar: ["刷新"], columns: [
        { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "业务批号", k: "batch", w: 125 }, { t: "缸号", k: "vat", w: 105 },
        { t: "筒数", k: "tubes", w: 50, cls: "num" }, { t: "标重", k: "stdWeight", w: 80, cls: "num" },
        { t: "净重", k: "netWeight", w: 80, cls: "num" }, { t: "收货状态", k: "status", w: 60, cls: "ctr" }
      ], gen: "inDetail" }]
    }
  },

  palletOrder: {
    title: "组托单", dateField: "组托日期",
    toolbar: ["新建", "编辑", "查看", "删除", "查询选项", "查询", "高级查询", "导出"],
    columns: [
      { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "组托单号", k: "no", w: 125, cls: "bold c-blue" },
      { t: "状态", k: "status", w: 55, cls: "ctr", cmap: { "生效": "c-green" } },
      { t: "组托日期", k: "date", w: 135 }, { t: "组托类型", k: "pType", w: 95, cls: "ctr" },
      { t: "托号", k: "pallet", w: 85 }, { t: "箱号", k: "box", w: 120 }, { t: "库存批号", k: "batch", w: 125 },
      { t: "缸号", k: "vat", w: 105 }, { t: "备注", k: "remark", w: 80 }, { t: "创建人", k: "creator", w: 60 }, { t: "最后修改人", k: "editor", w: 75 }
    ],
    gen: D.palletOrder,
    edit: {
      toolbar: ["新建", "删除", "保存", "生效", "联动更新", "关闭"],
      header: [
        [F("组托单号", "no", "text", { ro: true }), SEL("组托类型", "pType", D.PALLET_TYPES), F("组托时间", "date")]
      ],
      tabs: [
        {
          title: "条码组托明细", twoPanel: true,
          leftPanel: {
            title: "托盘信息", toolbar: ["添加", "移除"],
            columns: [{ t: "托号", k: "pallet", w: 100 }, { t: "托状态", k: "status", w: 70, cls: "ctr" }],
            rows: [{ pallet: "00023012", status: "非库存" }]
          },
          rightPanel: {
            title: "箱子明细", toolbar: ["引入", "移除"],
            columns: [
              { t: "箱号", k: "box", w: 115 }, { t: "标出", k: "markOut", w: 45, cls: "ctr" }, { t: "箱状态", k: "boxStatus", w: 55, cls: "ctr" },
              { t: "仓库", k: "wh", w: 75 }, { t: "库位编号", k: "locCode", w: 80 }, { t: "库位名称", k: "locName", w: 80 },
              { t: "箱子数", k: "boxCount", w: 55, cls: "num" }, { t: "标重", k: "stdWeight", w: 70, cls: "num" },
              { t: "批号", k: "batch", w: 120 }, { t: "缸号", k: "vat", w: 105 }
            ], gen: "unionDetail"
          }
        },
        { title: "备注信息", remarkOnly: true }
      ]
    }
  },

  codeCollect: {
    title: "码单收录", dateField: "日期",
    toolbar: ["新建", "编辑", "查看", "删除", "查询选项", "查询", "高级查询"],
    columns: [
      { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "单据编号", k: "no", w: 125, cls: "bold c-blue" },
      { t: "状态", k: "status", w: 55, cls: "ctr", cmap: { "生效": "c-green", "制单中": "c-red" } },
      { t: "日期", k: "date", w: 80 }, { t: "ERP订单号", k: "erpNo", w: 110 }, { t: "业务类型", k: "bizType", w: 75, cls: "ctr" },
      { t: "批号", k: "batch", w: 125 }, { t: "数量", k: "qty", w: 85, cls: "num" },
      { t: "创建者", k: "creator", w: 60 }, { t: "创建时间", k: "cTime", w: 88 },
      { t: "修改人", k: "editor", w: 60 }, { t: "修改时间", k: "eTime", w: 88 }, { t: "备注", k: "remark", w: 80 }
    ],
    gen: D.codeCollect,
    edit: {
      toolbar: ["新增", "保存", "提交", "生效", "打印", "联查", "刷新", "关闭"],
      header: [
        [F("单据编号", "no", "text", { ro: true }), F("单据日期", "date", "date"), SEL("业务类型", "bizType", ["调拨入库", "销售入库", "采购入库", "产件入库", "退料入库"])]
      ],
      tabs: [
        {
          title: "条码数据明细", toolbar: ["引入通知单", "调拨订单引入", "删除", "引入", "扫描引入", "移除"],
          columns: [
            { t: "行号", k: "seq", w: 40, cls: "ctr" }, { t: "条码号", k: "barcode", w: 130 }, { t: "业务批号", k: "batch", w: 130 },
            { t: "库存批号", k: "invBatch", w: 130 }, { t: "筒数", k: "tubes", w: 45, cls: "num" },
            { t: "标重", k: "stdWeight", w: 70, cls: "num" }, { t: "毛重", k: "grossWeight", w: 80, cls: "num" },
            { t: "净重", k: "netWeight", w: 80, cls: "num" }, { t: "皮重", k: "tare", w: 65, cls: "num" },
            { t: "标重回数", k: "stdBack", w: 70, cls: "num" }, { t: "实际回数", k: "realBack", w: 70, cls: "num" }
          ], gen: "codeDetail"
        },
        {
          title: "更多详情", toolbar: ["引入通知单", "删除"],
          columns: [
            { t: "业务单号", k: "bizNo", w: 110 }, { t: "业务批号", k: "bizBatch", w: 130 }, { t: "库存批号", k: "invBatch", w: 130 },
            { t: "筒数", k: "tubes", w: 50, cls: "num" }, { t: "标重", k: "stdWeight", w: 80, cls: "num" }
          ], gen: "noticeSource"
        }
      ]
    }
  },

  inboundOrder: {
    title: "入库单", dateField: "入库日期",
    toolbar: ["新增", "编辑", "查看", "删除", "提交", "同步ERP", "查询", "高级查询", "刷新", "打印", "导出", "导入模板", "日志查询", "关闭"],
    columns: [
      { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "入库单号", k: "no", w: 130, cls: "bold c-blue" },
      { t: "入库日期", k: "date", w: 80 }, { t: "生效状态", k: "status", w: 60, cls: "ctr", cmap: { "生效": "c-green", "制作中": "c-red" } },
      { t: "同步状态", k: "sync", w: 60, cls: "ctr", cmap: { "成功": "c-green", "未同步": "c-red" } },
      { t: "ERP单号", k: "erpNo", w: 105 }, { t: "单据类型", k: "docType", w: 100, cls: "ctr" }, { t: "ERP入库单", k: "erpInNo", w: 100 },
      { t: "仓库", k: "wh", w: 80 }, { t: "入库类型", k: "inType", w: 75, cls: "ctr" }, { t: "作业方式", k: "workMode", w: 70, cls: "ctr" },
      { t: "库存批次", k: "batch", w: 125 }, { t: "缸号", k: "vat", w: 105 }, { t: "标重", k: "weight", w: 85, cls: "num" },
      { t: "箱数", k: "boxes", w: 45, cls: "num" }, { t: "创建人", k: "creator", w: 60 }, { t: "创建时间", k: "cTime", w: 88 },
      { t: "修改人", k: "editor", w: 60 }, { t: "修改时间", k: "eTime", w: 88 }
    ],
    gen: D.inbound,
    edit: {
      header: [
        [F("入库单号", "no", "text", { ro: true }), F("入库日期", "date", "date"), SEL("入库类型", "inType", D.IN_TYPES), SEL("作业方式", "workMode", D.WORK_MODES), SEL("数据模式", "_dataMode", ["作业日志", "本地手动", "在线同步"])],
        [SEL("仓库编号", "_whNo", D.WAREHOUSES.map(function (w) { return w.no; })), F("仓库名称", "wh"), SEL("仓库类型", "_whType", ["立库(自营)", "普通仓库"]), F("ERP仓库", "_erpWh"), F("ERP单号", "erpNo"), F("单据类型", "docType")]
      ],
      tabs: [
        {
          title: "显示详细信息", toolbar: ["增加", "删除", "复制行", "设置库位", "移除"],
          columns: [
            { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "业务批号", k: "batch", w: 125 }, { t: "筒数", k: "tubes", w: 45, cls: "num" },
            { t: "标重", k: "stdWeight", w: 80, cls: "num" }, { t: "毛重", k: "grossWeight", w: 80, cls: "num" }, { t: "净重", k: "netWeight", w: 80, cls: "num" },
            { t: "库存批号", k: "batch", w: 125 }, { t: "缸号", k: "vat", w: 105 }, { t: "仓位号", k: "loc", w: 80 },
            { t: "色位名", k: "colorName", w: 60 }, { t: "色名", k: "colorName", w: 60 }, { t: "原料明细", k: "comp", w: 120 },
            { t: "同步状态", k: "sync", w: 60, cls: "ctr" }, { t: "条码", k: "barcode", w: 125 }
          ], gen: "inDetail"
        },
        {
          title: "显示作业日志", toolbar: ["刷新"],
          columns: [
            { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "时间", k: "time", w: 140 }, { t: "动作", k: "action", w: 80, cls: "ctr" },
            { t: "库位", k: "loc", w: 80 }, { t: "托盘号", k: "pallet", w: 90 }, { t: "结果", k: "result", w: 50, cls: "ctr", cmap: { "成功": "c-green", "失败": "c-red" } },
            { t: "消息", k: "msg", w: 120 }
          ], gen: "asrsLog"
        }
      ],
      lowerTabs: [
        {
          title: "按明细大小添加", columns: [
            { t: "编号", k: "seq", w: 36, cls: "ctr" }, { t: "托号", k: "pallet", w: 90 }, { t: "筒数", k: "tubes", w: 45, cls: "num" },
            { t: "标重", k: "stdWeight", w: 80, cls: "num" }, { t: "净重", k: "netWeight", w: 80, cls: "num" },
            { t: "毛重", k: "grossWeight", w: 80, cls: "num" }, { t: "筒重管", k: "tubeWeight", w: 60, cls: "num" },
            { t: "实际筒重", k: "realTube", w: 70, cls: "num" }, { t: "标定筒重", k: "stdTube", w: 70, cls: "num" }
          ], gen: "palletDetail"
        },
        { title: "按箱添加", columns: [{ t: "箱号", k: "barcode", w: 130 }, { t: "批号", k: "batch", w: 125 }, { t: "筒数", k: "tubes", w: 45, cls: "num" }, { t: "标重", k: "stdWeight", w: 80, cls: "num" }], gen: "inDetail" },
        { title: "按托添加", columns: [{ t: "托号", k: "pallet", w: 100 }, { t: "箱数", k: "boxCount", w: 50, cls: "num" }, { t: "总重", k: "stdWeight", w: 80, cls: "num" }], gen: "palletDetail" }
      ]
    }
  },

  /* ---------- 立库作业 ---------- */
  whTask: {
    title: "仓储任务", dateField: "任务日期",
    toolbar: ["新建", "编辑", "查看", "删除", "查询选项", "查询", "高级查询", "导出"],
    columns: [
      { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "任务编号", k: "no", w: 125, cls: "bold c-blue" },
      { t: "单据状态", k: "status", w: 60, cls: "ctr", cmap: { "生效": "c-green" } },
      { t: "任务日期", k: "date", w: 80 }, { t: "任务类型", k: "tType", w: 75, cls: "ctr" },
      { t: "执行状态", k: "execStatus", w: 60, cls: "ctr", cmap: { "待执行": "c-red", "执行中": "c-blue", "已完成": "c-green" } },
      { t: "库存批号", k: "batch", w: 125 }, { t: "料号", k: "material", w: 100 }, { t: "任务批号", k: "taskBatch", w: 125 },
      { t: "出库类型", k: "outType", w: 85, cls: "ctr" }, { t: "ERP单号", k: "erpNo", w: 105 }, { t: "ERP单据类型", k: "erpType", w: 105, cls: "ctr" },
      { t: "标箱", k: "stdBox", w: 45, cls: "num" }, { t: "箱数", k: "boxes", w: 45, cls: "num" }
    ],
    gen: D.whTask,
    edit: {
      header: [
        [F("任务编号", "no", "text", { ro: true }), F("任务日期", "date", "date"), SEL("任务类型", "tType", ["条码出库", "条码入库", "空托出库"]), SEL("执行状态", "execStatus", ["待执行", "执行中", "已完成"])],
        [F("库存批号", "batch"), F("料号", "material"), SEL("出库类型", "outType", D.OUT_TYPES), F("ERP单号", "erpNo"), F("ERP单据类型", "erpType")]
      ],
      tabs: [
        {
          title: "任务批次明细", toolbar: ["手动添加", "删除"],
          columns: [
            { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "批次", k: "batch", w: 130 }, { t: "缸号", k: "vat", w: 105 },
            { t: "筒数", k: "tubes", w: 45, cls: "num" }, { t: "标重", k: "stdWeight", w: 80, cls: "num" },
            { t: "库存数", k: "allowQty", w: 80, cls: "num" }
          ], gen: "inDetail"
        }
      ]
    }
  },

  opTask: {
    title: "作业任务", dateField: "任务时间",
    toolbar: ["新建", "复制", "编辑", "查看", "删除", "重试", "查询选项", "查询", "高级查询", "导出", "打印"],
    columns: [
      { t: "ID", k: "id", w: 50, cls: "ctr" }, { t: "单号", k: "docId", w: 60, cls: "ctr" },
      { t: "单据状态", k: "status", w: 60, cls: "ctr", cmap: { "生效": "c-green" } },
      { t: "托盘号", k: "pallet", w: 85 }, { t: "原始库位", k: "fromLoc", w: 80 }, { t: "目标库位", k: "toLoc", w: 80 },
      { t: "任务类型", k: "tType", w: 75, cls: "ctr" }, { t: "作业类型", k: "opType", w: 55, cls: "ctr" },
      { t: "出库口", k: "port", w: 90 }, { t: "任务时间", k: "tTime", w: 135 }, { t: "立库接收时间", k: "asrsTime", w: 135 },
      { t: "执行状态", k: "execStatus", w: 60, cls: "ctr", cmap: { "执行中": "c-blue", "成功": "c-green", "失败": "c-red" } },
      { t: "备注", k: "remark", w: 80 }
    ],
    gen: D.opTask,
    edit: {
      header: [
        [F("ID", "id", "text", { ro: true }), F("托盘号", "pallet"), F("原始库位", "fromLoc"), F("目标库位", "toLoc")],
        [SEL("任务类型", "tType", ["条码出库", "条码入库", "托盘挪库", "空托出库"]), SEL("作业类型", "opType", ["出库", "入库", "移库"]), F("出库口", "port"), SEL("执行状态", "execStatus", ["执行中", "成功", "失败"])]
      ],
      tabs: [{ title: "任务明细", toolbar: ["刷新"], columns: [
        { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "任务单号", k: "taskNo", w: 120 }, { t: "状态", k: "status", w: 55, cls: "ctr" },
        { t: "原始库位", k: "fromLoc", w: 80 }, { t: "目标库位", k: "toLoc", w: 80 }, { t: "出库口", k: "port", w: 90 },
        { t: "任务时间", k: "tTime", w: 130 }, { t: "执行状态", k: "exec", w: 60, cls: "ctr" }
      ], gen: "taskDetail" }]
    }
  },

  opLog: {
    title: "作业日志", dateField: "单据日期",
    toolbar: ["新建", "复制", "编辑", "查看", "删除", "查询选项", "查询", "高级查询"],
    columns: [
      { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "生效状态", k: "status", w: 60, cls: "ctr", cmap: { "生效": "c-green" } },
      { t: "分析状态", k: "analysis", w: 60, cls: "ctr", cmap: { "待分析": "c-red", "已分析": "c-green" } },
      { t: "WCS时间", k: "wcsTime", w: 140 }, { t: "处理时间", k: "procTime", w: 140 },
      { t: "作业类型", k: "opType", w: 55, cls: "ctr" }, { t: "作业结果", k: "result", w: 55, cls: "ctr", cmap: { "正常": "c-green", "异常": "c-red" } },
      { t: "原始库位号", k: "fromLoc", w: 85 }, { t: "原始库位名称", k: "fromName", w: 90 },
      { t: "目标库位号", k: "toLoc", w: 85 }, { t: "目标库位名称", k: "toName", w: 90 },
      { t: "托盘号", k: "pallet", w: 90 }, { t: "单据日期", k: "docDate", w: 80 }
    ],
    gen: D.opLog,
    edit: {
      header: [
        [F("生效状态", "status"), F("分析状态", "analysis"), SEL("作业类型", "opType", ["入库", "出库", "移库"]), SEL("作业结果", "result", ["正常", "异常"])],
        [F("原始库位号", "fromLoc"), F("目标库位号", "toLoc"), F("托盘号", "pallet"), F("单据日期", "docDate", "date")]
      ],
      tabs: [{ title: "日志明细", toolbar: ["刷新"], columns: [
        { t: "时间", k: "time", w: 140 }, { t: "动作", k: "action", w: 80, cls: "ctr" }, { t: "库位", k: "loc", w: 80 },
        { t: "托盘号", k: "pallet", w: 90 }, { t: "结果", k: "result", w: 50, cls: "ctr" }, { t: "消息", k: "msg", w: 150 }
      ], gen: "asrsLog" }]
    }
  },

  /* ---------- 库存管理 ---------- */
  stockCount: {
    title: "盘存单", dateField: "盘存日期",
    toolbar: ["新增", "编辑", "查看", "删除", "查询选项", "查询", "高级查询", "刷新", "打印", "导出"],
    columns: [
      { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "盘存单号", k: "no", w: 125, cls: "bold c-blue" },
      { t: "状态", k: "status", w: 55, cls: "ctr", cmap: { "生效": "c-green" } },
      { t: "盘存日期", k: "date", w: 80 }, { t: "作业方式", k: "workMode", w: 60, cls: "ctr" }, { t: "仓库名", k: "wh", w: 75 },
      { t: "仓位", k: "loc", w: 70 }, { t: "批号", k: "batch", w: 125 }, { t: "缸号", k: "vat", w: 105 },
      { t: "实盘件数", k: "realPieces", w: 60, cls: "num" }, { t: "实盘标重", k: "realWeight", w: 75, cls: "num" },
      { t: "箱条码", k: "boxBarcode", w: 120 }, { t: "箱实件数", k: "boxPieces", w: 60, cls: "num" }, { t: "箱实重量", k: "boxWeight", w: 70, cls: "num" },
      { t: "备注", k: "remark", w: 70 }, { t: "创建人", k: "creator", w: 60 }, { t: "创建时间", k: "cTime", w: 88 },
      { t: "编辑人", k: "editor", w: 60 }, { t: "编辑时间", k: "eTime", w: 88 }
    ],
    gen: D.stockCount,
    edit: {
      toolbar: ["新建", "保存", "删除", "打印", "生效", "联动更新", "关闭"],
      header: [
        [F("盘存单号", "no", "text", { ro: true }), F("盘存日期", "date", "date"), SEL("作业方式", "workMode", ["条码作业", "批次作业"]), SEL("数据方式", "_dataMode", ["本地手动", "作业日志"])],
        [F("仓库编号", "_whNo", "text", { ro: true }), F("仓库类型", "_whType", "text", { ro: true }), F("仓库名称", "wh"), F("ERP仓库名", "_erpWh", "text", { ro: true })]
      ],
      tabs: [
        {
          title: "条码盘存作业", toolbar: ["添加", "删除", "刷新库存"],
          columns: [
            { t: "仓位号", k: "loc", w: 70 }, { t: "仓位名", k: "locName", w: 70 }, { t: "存货批号", k: "invBatch", w: 125 },
            { t: "缸号", k: "vat", w: 105 }, { t: "盘前筒数", k: "preTubes", w: 55, cls: "num" }, { t: "实盘筒数", k: "realTubes", w: 55, cls: "num" },
            { t: "盘盈筒数", k: "diffTubes", w: 55, cls: "num" }, { t: "盘前标重", k: "preStd", w: 75, cls: "num" }, { t: "实盘标重", k: "realStd", w: 75, cls: "num" },
            { t: "盘盈标重", k: "diffStd", w: 75, cls: "num" }, { t: "盘前毛重", k: "preGross", w: 75, cls: "num" }, { t: "实盘毛重", k: "realGross", w: 75, cls: "num" },
            { t: "盘前净重", k: "preNet", w: 75, cls: "num" }, { t: "实盘净重", k: "realNet", w: 75, cls: "num" }, { t: "盘盈净重", k: "diffNet", w: 75, cls: "num" },
            { t: "标定回潮", k: "stdMR", w: 60, cls: "num" }, { t: "实际回潮", k: "realMR", w: 60, cls: "num" },
            { t: "色号", k: "colorCode", w: 60, cls: "ctr" }, { t: "色名", k: "colorName", w: 60 },
            { t: "物料编号", k: "material", w: 100 }, { t: "纱支", k: "yarn", w: 65 }, { t: "原料明细", k: "comp", w: 120 }
          ], gen: "countDetail"
        },
        {
          title: "箱码明细", toolbar: ["添加", "移除"],
          columns: [
            { t: "编号", k: "code", w: 125 }, { t: "实盘标重", k: "realStd", w: 75, cls: "num" }, { t: "实盘净重", k: "realNet", w: 75, cls: "num" },
            { t: "实盘毛重", k: "realGross", w: 75, cls: "num" }, { t: "皮重", k: "tare", w: 65, cls: "num" }, { t: "筒管重", k: "tubeWeight", w: 65, cls: "num" }
          ], gen: "countBoxes"
        },
        { title: "备注信息", remarkOnly: true }
      ]
    }
  },

  stockMove: {
    title: "移库单", dateField: "单据日期",
    toolbar: ["新增", "编辑", "查看", "删除", "查询选项", "查询", "高级查询", "导入", "导出", "打印"],
    columns: [
      { t: "行号", k: "seq", w: 36, cls: "ctr" }, { t: "移库单号", k: "no", w: 125, cls: "bold c-blue" },
      { t: "单据日期", k: "date", w: 80 }, { t: "状态", k: "status", w: 55, cls: "ctr", cmap: { "生效": "c-green" } },
      { t: "ERP单号", k: "erpNo", w: 100 }, { t: "移库类型", k: "moveType", w: 70, cls: "ctr" },
      { t: "批号", k: "batch", w: 125 }, { t: "箱号", k: "box", w: 120 },
      { t: "调入仓库", k: "toWh", w: 80 }, { t: "调入仓位", k: "toLoc", w: 80 },
      { t: "调出仓库", k: "fromWh", w: 80 }, { t: "调出仓位", k: "fromLoc", w: 80 },
      { t: "创建者", k: "creator", w: 60 }, { t: "创建时间", k: "cTime", w: 88 },
      { t: "修改人", k: "editor", w: 60 }, { t: "修改时间", k: "eTime", w: 88 }, { t: "备注", k: "remark", w: 70 }
    ],
    gen: D.stockMove,
    edit: {
      toolbar: ["新建", "保存", "删除", "生效", "打印", "刷新", "联动更新", "关闭"],
      header: [
        [F("移库单号", "no", "text", { ro: true }), SEL("移库类型", "moveType", D.MOVE_TYPES), F("移库日期", "date", "date")]
      ],
      tabs: [
        {
          title: "明细信息", toolbar: ["添加", "设置库位", "删除"],
          columns: [
            { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "编号", k: "code", w: 130 }, { t: "批号", k: "batch", w: 125 },
            { t: "缸号", k: "vat", w: 105 }, { t: "目标仓库", k: "toWh", w: 80 }, { t: "目标库位", k: "toLoc", w: 80 }
          ], gen: "moveDetail"
        },
        { title: "备注信息", remarkOnly: true }
      ]
    }
  },

  /* ---------- 基础数据 ---------- */
  bizBatch: {
    title: "业务批次", dateField: "批次日期",
    toolbar: ["新建", "复制", "编辑", "查看", "删除", "批次初始化", "ERP库存盘存", "清空库存", "查询选项", "查询", "高级查询"],
    columns: [
      { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "批次编号", k: "no", w: 140, cls: "bold c-blue" },
      { t: "物料编码", k: "material", w: 110 }, { t: "物料名称", k: "materialName", w: 150 },
      { t: "纱支", k: "yarn", w: 70 }, { t: "批次日期", k: "date", w: 130 }
    ],
    gen: D.bizBatch,
    edit: {
      toolbar: EDIT_TOOLBAR_SIMPLE,
      header: [
        [F("批次编号", "no", "text", { ro: true, req: true }), F("物料编码", "material", "text", { req: true }), F("物料名称", "materialName")],
        [F("纱支", "yarn"), F("批次日期", "date", "date", { req: true }), TA("原料成分", "comp")]
      ]
    }
  },

  invBatch: {
    title: "库存批次", dateField: "批次日期",
    toolbar: ["新建", "复制", "编辑", "查看", "删除", "条码对账", "查询选项", "查询", "高级查询"],
    columns: [
      { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "库存批号", k: "no", w: 135, cls: "bold c-blue" },
      { t: "业务批号", k: "bizBatch", w: 125 }, { t: "缸号", k: "vat", w: 105 }, { t: "原料成分", k: "comp", w: 125 },
      { t: "总箱数", k: "totalBoxes", w: 55, cls: "num" }, { t: "总件数", k: "totalPieces", w: 55, cls: "num" },
      { t: "总库存量", k: "totalQty", w: 80, cls: "num" }, { t: "存货仓库", k: "wh", w: 80 }, { t: "存货仓位", k: "loc", w: 80 },
      { t: "仓位件数", k: "locPieces", w: 60, cls: "num" }, { t: "仓位库存", k: "locQty", w: 80, cls: "num" },
      { t: "箱装件数", k: "boxPieces", w: 60, cls: "num" }, { t: "箱装库存", k: "boxQty", w: 80, cls: "num" },
      { t: "条码件数", k: "barPieces", w: 60, cls: "num" }, { t: "条码数量", k: "barQty", w: 80, cls: "num" }
    ],
    gen: D.invBatch,
    edit: {
      toolbar: EDIT_TOOLBAR_SIMPLE,
      header: [
        [F("库存批号", "no", "text", { ro: true, req: true }), F("业务批号", "bizBatch", "text", { req: true }), F("缸号", "vat")],
        [F("存货仓库", "wh"), F("存货仓位", "loc"), TA("原料成分", "comp")]
      ]
    }
  },

  boxInfo: {
    title: "箱码信息", dateField: "入库时间",
    toolbar: ["刷新", "查看", "编辑", "删除", "批量打印", "箱码处理", "回单联解绑", "打印", "查询选项", "查询", "高级查询"],
    columns: [
      { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "箱码编号", k: "code", w: 130, cls: "bold c-blue" },
      { t: "批号", k: "batch", w: 125 }, { t: "缸号", k: "vat", w: 105 },
      { t: "箱子状态", k: "status", w: 60, cls: "ctr", cmap: { "待入": "c-red", "在库": "c-green", "已出库": "c-gray" } },
      { t: "所在仓库", k: "wh", w: 80 }, { t: "所在库位", k: "loc", w: 80 }, { t: "所在托盘号", k: "pallet", w: 90 },
      { t: "入库时间", k: "inTime", w: 88 }, { t: "出库时间", k: "outTime", w: 88 },
      { t: "筒数", k: "tubes", w: 40, cls: "num" }, { t: "标重", k: "stdWeight", w: 70, cls: "num" },
      { t: "单筒重量", k: "tubeWeight", w: 65, cls: "num" }, { t: "毛重", k: "grossWeight", w: 70, cls: "num" },
      { t: "净重", k: "netWeight", w: 70, cls: "num" }, { t: "待入重量", k: "waitWeight", w: 70, cls: "num" },
      { t: "箱子总重", k: "boxTotal", w: 65, cls: "num" }, { t: "单件管理量", k: "unitQty", w: 65, cls: "num" },
      { t: "实际回潮", k: "realMR", w: 60, cls: "num" }, { t: "公定回潮", k: "stdMR", w: 60, cls: "num" },
      { t: "质检组", k: "qcGroup", w: 65 }, { t: "检测时间", k: "qcTime", w: 88 }
    ],
    gen: D.boxInfo,
    edit: {
      toolbar: ["新建", "保存", "删除", "打印", "联动更新", "关闭"],
      header: [
        [F("箱码编号", "code", "text", { ro: true, req: true }), F("批号", "batch", "text", { req: true }), F("缸号", "vat")],
        [SEL("箱子状态", "status", ["待入", "在库", "已出库"]), F("所在仓库", "wh"), F("所在库位", "loc"), F("所在托盘号", "pallet")],
        [F("筒数", "tubes"), F("标重", "stdWeight"), F("毛重", "grossWeight"), F("净重", "netWeight")],
        [F("实际回潮", "realMR"), F("公定回潮", "stdMR"), F("质检组", "qcGroup"), F("检测时间", "qcTime")]
      ]
    }
  },

  palletInfo: {
    title: "托盘信息", dateField: "创建时间",
    toolbar: ["新建", "复制", "编辑", "查看", "删除", "快速打印", "批量新增", "近30天MES同步", "打印", "查询选项", "查询", "高级查询"],
    columns: [
      { t: "编号", k: "seq", w: 36, cls: "ctr" }, { t: "托盘号", k: "no", w: 100, cls: "bold c-blue" },
      { t: "空托", k: "empty", w: 40, cls: "ctr" }, { t: "当前库位", k: "loc", w: 85 },
      { t: "托盘状态", k: "status", w: 60, cls: "ctr", cmap: { "闲": "c-green", "货": "c-blue" } },
      { t: "立库状态", k: "asrsStatus", w: 60, cls: "ctr" }, { t: "空托数量", k: "emptyQty", w: 60, cls: "num" },
      { t: "箱个数", k: "boxCount", w: 55, cls: "num" }, { t: "批次号", k: "batch", w: 125 },
      { t: "总箱数", k: "totalBoxes", w: 55, cls: "num" }, { t: "总重量", k: "totalWeight", w: 75, cls: "num" },
      { t: "创建者", k: "creator", w: 60 }, { t: "创建时间", k: "cTime", w: 88 },
      { t: "最后编辑人", k: "editor", w: 70 }, { t: "最后编辑时间", k: "eTime", w: 88 }
    ],
    gen: D.palletInfo,
    edit: {
      toolbar: EDIT_TOOLBAR_SIMPLE,
      header: [
        [F("托盘号", "no", "text", { ro: true, req: true }), CHK("空托", "emptyChk"), SEL("托盘状态", "status", ["闲", "货", "锁定"])],
        [F("当前库位", "loc"), F("立库状态", "asrsStatus"), F("箱个数", "boxCount"), F("批次号", "batch")],
        [F("总箱数", "totalBoxes"), F("总重量", "totalWeight"), TA("备注", "remark")]
      ]
    }
  },

  locationInfo: {
    title: "库位信息", dateField: "创建时间",
    toolbar: ["新建", "复制", "编辑", "查看", "删除", "查询选项", "查询", "高级查询", "显示设置", "打印", "导出"],
    columns: [
      { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "ERP仓位号", k: "erpLoc", w: 85 },
      { t: "仓位编号", k: "locCode", w: 90, cls: "bold c-blue" }, { t: "仓位名称", k: "locName", w: 130 },
      { t: "所属仓库", k: "wh", w: 90 }, { t: "ERP编号", k: "erpNo", w: 70 }, { t: "ERP名称", k: "erpName", w: 90 },
      { t: "ERP最后同步时间", k: "lastSync", w: 110 }, { t: "启用", k: "enabled", w: 40, cls: "ctr", type: "check" },
      { t: "创建者", k: "creator", w: 60 }, { t: "创建时间", k: "cTime", w: 88 },
      { t: "最后编辑人", k: "editor", w: 70 }, { t: "最后编辑时间", k: "eTime", w: 88 }
    ],
    gen: D.locationInfo,
    edit: {
      toolbar: ["新建", "复制", "删除", "保存", "自动更新", "关闭"],
      header: [
        [F("仓位编号", "locCode", "text", { req: true }), F("仓位名称", "locName", "text", { req: true }), F("ERP仓位号", "erpLoc")],
        [SEL("所属仓库", "wh", D.WAREHOUSES.map(function (w) { return w.name; })), F("ERP编号", "erpNo"), F("ERP名称", "erpName")],
        [CHK("启用", "enabled"), F("ERP最后同步时间", "lastSync", "text", { ro: true })]
      ]
    }
  },

  warehouseInfo: {
    title: "仓库信息", dateField: "创建时间",
    toolbar: ["新增", "复制", "编辑", "查看", "删除", "同步ERP仓库", "查询条件", "查询", "高级查询", "显示设置", "打印", "导出"],
    columns: [
      { t: "编号", k: "seq", w: 36, cls: "ctr" }, { t: "名称", k: "name", w: 110, cls: "bold c-blue" },
      { t: "ERP编号", k: "erpNo", w: 70 }, { t: "ERP名称", k: "erpName", w: 100 },
      { t: "ERP最后同步时间", k: "lastSync", w: 115 }, { t: "仓库类型", k: "whType", w: 85, cls: "ctr" },
      { t: "启用", k: "enabled", w: 40, cls: "ctr", type: "check" },
      { t: "创建人", k: "creator", w: 60 }, { t: "创建时间", k: "cTime", w: 100 },
      { t: "修改人", k: "editor", w: 60 }, { t: "修改时间", k: "eTime", w: 100 }
    ],
    gen: D.warehouseInfo,
    edit: {
      toolbar: ["新建", "复制", "删除", "保存", "自动更新", "关闭"],
      width: 780, height: 560,
      header: [
        [F("仓库编号", "erpNo", "text", { req: true }), F("仓库名称", "name", "text", { req: true }), SEL("仓库类型", "whType", ["普通仓库", "立库(自营)"])],
        [CHK("启用状态", "enabled"), F("ERP编号", "erpNo"), F("ERP名称", "erpName"), F("最近同步时间", "lastSync", "text", { ro: true })]
      ],
      tabs: [
        {
          title: "仓位明细", tall: true, toolbar: ["添加", "删除"],
          columns: [
            { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "仓位名", k: "name", w: 70 }, { t: "ERP仓位号", k: "erpLoc", w: 80 },
            { t: "ERP仓位名", k: "erpName", w: 80 }, { t: "启用", k: "enabled", w: 40, cls: "ctr", type: "check" }, { t: "外升位", k: "outer", w: 60 }
          ], gen: "whLocations"
        },
        { title: "备注信息", remarkOnly: true }
      ]
    }
  },

  /* ---------- 系统管理 ---------- */
  bizDict: {
    title: "业务字典", dateField: "创建时间",
    toolbar: ["新建", "复制", "编辑", "查看", "删除", "查询选项", "高级查询", "刷新"],
    columns: [
      { t: "#", k: "seq", w: 36, cls: "ctr" }, { t: "字典编码", k: "code", w: 180, cls: "bold c-blue" },
      { t: "项目名称", k: "name", w: 220 },
      { t: "创建人", k: "creator", w: 60 }, { t: "创建时间", k: "cTime", w: 100 },
      { t: "编辑人", k: "editor", w: 60 }, { t: "编辑时间", k: "eTime", w: 100 }
    ],
    gen: D.bizDict,
    edit: {
      toolbar: ["新建", "复制", "删除", "保存", "联动更新", "关闭"],
      width: 640, height: 520,
      header: [[F("字典标识:", "code", "text", { req: true }), F("字典名称:", "name", "text", { req: true })]],
      tabs: [{
        title: "字典项目", toolbar: ["添加", "移除"],
        columns: [
          { t: "序号", k: "seq", w: 40, cls: "ctr" }, { t: "显示", k: "disp", w: 120 }, { t: "值", k: "val", w: 80 },
          { t: "更多1", k: "more1", w: 70 }, { t: "更多2", k: "more2", w: 70 }, { t: "启用", k: "on", w: 40, cls: "ctr", type: "check" }
        ], gen: "dictItems"
      }]
    }
  },

  sysSetting: {
    title: "系统设置", dateField: "创建时间",
    toolbar: ["新建", "复制", "编辑", "查看", "删除", "查询选项", "高级查询", "刷新", "设置"],
    columns: [
      { t: "#", k: "seq", w: 36, cls: "ctr" }, { t: "设置项编号", k: "code", w: 180, cls: "bold c-blue" },
      { t: "项目名称", k: "name", w: 220 },
      { t: "创建人", k: "creator", w: 60 }, { t: "创建时间", k: "cTime", w: 100 },
      { t: "编辑人", k: "editor", w: 60 }, { t: "编辑时间", k: "eTime", w: 100 }
    ],
    gen: D.sysSetting,
    edit: {
      toolbar: ["新建", "复制", "删除", "保存", "联动更新", "关闭"],
      width: 640, height: 520,
      header: [[F("设置项编号:", "code", "text", { req: true }), F("项目名称:", "name", "text", { req: true })]],
      tabs: [{
        title: "设置项", toolbar: ["添加", "移除"],
        columns: [
          { t: "序号", k: "seq", w: 40, cls: "ctr" }, { t: "显示", k: "disp", w: 120 }, { t: "值", k: "val", w: 80 },
          { t: "更多1", k: "more1", w: 70 }, { t: "更多2", k: "more2", w: 70 }, { t: "启用", k: "on", w: 40, cls: "ctr", type: "check" }
        ], gen: "dictItems"
      }]
    }
  },

  moduleGroup: {
    title: "模块组别", dateField: "创建时间",
    toolbar: ["新建", "复制", "编辑", "查看", "删除", "查询选项", "查询", "高级查询", "设置", "刷新"],
    columns: [
      { t: "组织代号", k: "code", w: 130, cls: "bold c-blue" }, { t: "组名", k: "name", w: 120 },
      { t: "图标", k: "icon", w: 160 }, { t: "排序", k: "sort", w: 50, cls: "num" },
      { t: "创建人", k: "creator", w: 60 }, { t: "创建时间", k: "cTime", w: 110 },
      { t: "编辑人", k: "editor", w: 60 }, { t: "编辑时间", k: "eTime", w: 110 }
    ],
    gen: D.moduleGroup,
    edit: {
      toolbar: EDIT_TOOLBAR_SIMPLE,
      header: [[F("组织代号", "code", "text", { req: true }), F("组名", "name", "text", { req: true }), F("图标", "icon"), F("排序", "sort")]]
    }
  },

  funcModule: {
    title: "功能模块", dateField: "创建时间",
    toolbar: ["新建", "复制", "编辑", "查看", "删除", "查询选项", "查询", "高级查询"],
    columns: [
      { t: "类名称", k: "cls", w: 170 }, { t: "组织ID", k: "orgId", w: 55, cls: "ctr" }, { t: "Id", k: "id", w: 50, cls: "ctr" },
      { t: "从属功能组", k: "group", w: 80, cls: "ctr" }, { t: "模块编号", k: "code", w: 130, cls: "bold c-blue" },
      { t: "模块名称", k: "name", w: 90 }, { t: "类型", k: "type", w: 45, cls: "ctr" },
      { t: "数据主表", k: "mainTable", w: 120 }, { t: "状态字段", k: "statusField", w: 80 },
      { t: "启用状态", k: "enabled", w: 60, cls: "ctr", type: "check" }, { t: "启用流程", k: "flow", w: 60, cls: "ctr", type: "check" },
      { t: "创建日志", k: "log", w: 55, cls: "ctr", type: "check" }, { t: "功能路径", k: "path", w: 150 },
      { t: "模块图标", k: "icon", w: 110 }, { t: "排序", k: "sort", w: 45, cls: "num" },
      { t: "创建人", k: "creator", w: 60 }, { t: "编辑人", k: "editor", w: 60 }, { t: "编辑时间", k: "eTime", w: 90 }
    ],
    gen: D.funcModule,
    edit: {
      toolbar: EDIT_TOOLBAR_SIMPLE,
      header: [
        [F("类名称", "cls", "text", { req: true }), F("组织ID", "orgId"), F("Id", "id", "text", { ro: true })],
        [F("从属功能组", "group"), F("模块编号", "code", "text", { req: true }), F("模块名称", "name", "text", { req: true })],
        [SEL("类型", "type", ["列表", "卡片", "报表"]), F("数据主表", "mainTable"), F("状态字段", "statusField")],
        [CHK("启用状态", "enabled"), CHK("启用流程", "flow"), CHK("创建日志", "log")],
        [F("功能路径", "path"), F("模块图标", "icon"), F("排序", "sort")]
      ]
    }
  },

  userInfo: {
    title: "用户信息", dateField: "创建时间",
    toolbar: ["新建", "复制", "编辑", "查看", "删除", "查询选项", "查询", "高级查询"],
    columns: [
      { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "用户编号", k: "account", w: 75, cls: "bold c-blue" },
      { t: "用户名", k: "userName", w: 75 }, { t: "启用", k: "enabled", w: 40, cls: "ctr", type: "check" },
      { t: "工号", k: "empId", w: 60 }, { t: "姓名", k: "name", w: 70 }, { t: "部门", k: "dept", w: 90 },
      { t: "公司", k: "company", w: 170 }, { t: "备注", k: "remark", w: 80 },
      { t: "创建人", k: "creator", w: 60 }, { t: "创建时间", k: "cTime", w: 90 },
      { t: "编辑人", k: "editor", w: 60 }, { t: "编辑时间", k: "eTime", w: 90 }
    ],
    gen: D.userInfo,
    edit: {
      toolbar: ["新建", "复制", "删除", "保存", "联动更新", "关闭"],
      width: 720, height: 520,
      header: [
        [F("用户账号", "account", "text", { req: true }), F("用户名称", "userName", "text", { req: true }), CHK("是否启用", "enabled")],
        [F("人事信息", "empId", "text", { req: true }), F("登录密码", "_pwd", "password"), F("ERP用户名称", "_erpUser")],
        [TA("备注", "remark")]
      ],
      tabs: [{
        title: "用户角色", toolbar: ["添加", "移除"],
        columns: [{ t: "角色", k: "name", w: 160 }],
        rows: [{ name: "成品库仓管员" }, { name: "仓库管理员" }]
      }]
    }
  },

  roleSetting: {
    title: "角色设置", dateField: "创建日期",
    toolbar: ["新建", "复制", "编辑", "查看", "删除", "查询选项", "查询", "高级查询", "过滤", "刷新"],
    columns: [
      { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "角色名称", k: "name", w: 140, cls: "bold c-blue" },
      { t: "备注", k: "remark", w: 140 }, { t: "创建日期", k: "cDate", w: 100 }, { t: "编辑日期", k: "eDate", w: 100 }
    ],
    gen: D.roleSetting,
    edit: {
      toolbar: ["新建", "复制", "删除", "保存", "联动更新", "关闭"],
      width: 680, height: 540,
      header: [[F("角色名", "name", "text", { req: true }), F("备注", "remark")]],
      tabs: [
        {
          title: "用户设置", toolbar: ["添加", "移除"], tall: true,
          columns: [{ t: "用户名", k: "account", w: 80 }, { t: "昵称", k: "name", w: 90 }],
          gen: "roleUsers"
        },
        { title: "模块权限", permTree: true }
      ]
    }
  },

  printTemplate: {
    title: "打印模板", dateField: "创建时间",
    toolbar: ["新建", "复制", "编辑", "查看", "删除", "查询选项", "查询", "高级查询"],
    columns: [
      { t: "序号", k: "seq", w: 36, cls: "ctr" }, { t: "所属模块", k: "module", w: 90, cls: "ctr" },
      { t: "模板名称", k: "name", w: 200, cls: "bold c-blue" }, { t: "启用", k: "enabled", w: 40, cls: "ctr", type: "check" },
      { t: "编辑人", k: "editor", w: 60 }, { t: "编辑时间", k: "eTime", w: 90 },
      { t: "创建人", k: "creator", w: 60 }, { t: "创建时间", k: "cTime", w: 90 }
    ],
    gen: D.printTemplate,
    edit: {
      toolbar: ["新建", "复制", "删除", "保存", "联动更新", "关闭"],
      header: [
        [F("所属模块", "module", "text", { req: true }), F("模板名称", "name", "text", { req: true }), CHK("启用", "enabled")],
        [TA("模板内容(JSON)", "_content")]
      ]
    }
  },

  remindSetting: {
    title: "提醒设置", dateField: "创建时间",
    toolbar: ["新建", "复制", "编辑", "查看", "删除", "查询选项", "高级查询", "刷新"],
    columns: [
      { t: "排序", k: "sort", w: 40, cls: "ctr" }, { t: "模块", k: "module", w: 90, cls: "ctr" },
      { t: "提醒名称", k: "name", w: 170, cls: "bold c-blue" }, { t: "启用", k: "enabled", w: 40, cls: "ctr", type: "check" },
      { t: "主键", k: "pk", w: 50, cls: "ctr" }, { t: "公开", k: "isPublic", w: 45, cls: "ctr", type: "check" },
      { t: "允许操作用户", k: "allowUsers", w: 150 },
      { t: "创建人", k: "creator", w: 60 }, { t: "创建时间", k: "cTime", w: 130 },
      { t: "编辑人", k: "editor", w: 60 }, { t: "编辑时间", k: "eTime", w: 100 }
    ],
    gen: D.remindSetting,
    edit: {
      toolbar: EDIT_TOOLBAR_SIMPLE,
      header: [
        [F("排序", "sort"), F("模块", "module", "text", { req: true }), F("提醒名称", "name", "text", { req: true })],
        [CHK("启用", "enabled"), F("主键", "pk"), CHK("公开", "isPublic"), F("允许操作用户", "allowUsers")]
      ]
    }
  }
};

/* ============ 菜单结构(模块组别对应) ============ */
var MENU_GROUPS = [
  { code: "WMOperate", name: "仓储作业", items: ["outNotice", "receiving", "palletOrder", "codeCollect", "pdaWork"] },
  { code: "ASRSOperate", name: "立库作业", items: ["whTask", "opTask", "opLog", "scanTerminal"] },
  { code: "RMOperate", name: "库存管理", items: ["outboundOrder", "inboundOrder", "stockCount", "stockMove"] },
  { code: "MDB", name: "基础数据", items: ["bizBatch", "invBatch", "boxInfo", "palletInfo", "locationInfo", "warehouseInfo"] },
  { code: "SystemSetting", name: "系统管理", items: ["bizDict", "sysSetting", "moduleGroup", "funcModule", "userInfo", "roleSetting", "printTemplate", "remindSetting"] }
];
