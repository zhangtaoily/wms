export const radioList = [
  {
    value: 1,
    label: "是",
  },
  {
    value: 0,
    label: "否",
  },
];
export const custList = [
  {
    value: 1,
    label: "集团",
  },
  {
    value: 0,
    label: "单公司",
  },
];
export const sexList = [
  {
    label: "男",
    value: 1,
  },
  {
    label: "女",
    value: 0,
  },
];
// 存放数据字典对应数据
export const dictEnum = {
  // 计重方式-绞数
  twistNumber: "04",
  // 染色方式-筒染
  tubeDyeing: "5",
  // 包装形态-绞纱交货
  skein: "02",
};

export const changeOrderMap = {
  // 意向合同
  SAL_INT_CONT: {
    changeOrderFN: "SAL_INT_CONT_C",
    changeOrderPath: "/change-order/sal-int-cont",
  },
  // 销售合同
  SAL_CONT: {
    changeOrderFN: "SAL_CONT_C",
    changeOrderPath: "/change-order/sal-cont",
  },
  // 销售订单
  SAL_ORDER: {
    changeOrderFN: "SAL_ORDER_C",
    changeOrderPath: "/change-order/sal-order-cont",
  },
  // 生产通知单
  SAL_PRDNOTICE: {
    changeOrderFN: "SAL_PRDNOTICE_C",
    changeOrderPath: "/change-order/prod-notice",
  },
};

// 联查
export const relationQueryList = [
  {
    currentPact: {
      label: "生产通知变更单",
      function: "SAL_PRDNOTICE_C",
      sonSheet: "sal_prdnotice_goods_c",
    },
    parentPact: [
      {
        label: "生产通知单",
        function: "SAL_PRDNOTICE",
        path: "/business-management/prod-notice",
        // field: "order_id",
        // field: "orderGoodsId",
        field: "id",
      },
    ],
    sonPact: [],
  },
  {
    currentPact: {
      label: "销售订单变更单",
      function: "SAL_ORDER_C",
      sonSheet: "sal_order_goods_c",
    },
    parentPact: [
      {
        label: "销售订单",
        function: "SAL_ORDER",
        path: "/business-management/sal-order-cont",
        // field: "cont_id",
        field: "sourceGoodsId",
      },
    ],
    sonPact: [],
  },
  {
    currentPact: {
      label: "销售合同变更单",
      function: "SAL_CONT_C",
      sonSheet: "sal_cont_goods_c",
    },
    parentPact: [
      {
        label: "销售合同",
        function: "SAL_CONT",
        field: "sourceGoodsId",
        path: "/business-management/sal-cont",
      },
    ],
    sonPact: [],
  },
  {
    currentPact: {
      label: "意向合同变更单",
      function: "SAL_INT_CONT_C",
      sonSheet: "sal_int_cont_goods_c",
    },
    parentPact: [
      {
        label: "意向合同",
        function: "SAL_INT_CONT",
        field: "sourceGoodsId",
        path: "/business-management/sal-int-cont",
      },
    ],
    sonPact: [],
  },
  {
    currentPact: {
      label: "意向合同",
      function: "SAL_INT_CONT",
      sonSheet: "sal_int_cont_goods",
    },
    parentPact: [
      {
        label: "根级意向合同",
        function: "SAL_INT_CONT",
        path: "/business-management/sal-int-cont",
        field: "erpRootIntContCode",
      },
      {
        label: "父级意向合同",
        function: "SAL_INT_CONT",
        path: "/business-management/sal-int-cont",
        field: "erpUpIntContCode",
      },
    ],
    sonPact: [
      {
        label: "销售合同",
        function: "SAL_CONT",
        path: "/business-management/sal-cont",
        // field: "int_cont_id",
        field: "int_cont_goods_id",
      },
      {
        label: "意向合同变更单",
        function: "SAL_INT_CONT_C",
        path: "/change-order/sal-int-cont",
        // field: "int_cont_id",
        field: "source_goods_id",
      },
    ],
  },
  {
    currentPact: {
      label: "销售合同",
      function: "SAL_CONT",
      sonSheet: "sal_cont_goods",
    },
    parentPact: [
      {
        label: "意向合同",
        function: "SAL_INT_CONT",
        field: "intContGoodsId",
        path: "/business-management/sal-int-cont",
      },
      {
        label: "父级合同",
        function: "SAL_CONT",
        // field: "cont_id",
        field: "contGoodsId",
        path: "/business-management/sal-cont",
      },
    ],
    sonPact: [
      {
        label: "销售子合同",
        function: "SAL_CONT",
        path: "/business-management/sal-cont",
        field: "cont_id",
      },
      {
        label: "销售订单",
        function: "SAL_ORDER",
        path: "/business-management/sal-order-cont",
        // field: "cont_id",
        field: "cont_goods_id",
      },
      {
        label: "销售合同变更单",
        function: "SAL_CONT_C",
        path: "/change-order/sal-cont",
        field: "source_goods_id",
      },
    ],
  },
  {
    currentPact: {
      label: "销售订单",
      function: "SAL_ORDER",
      sonSheet: "sal_order_goods",
    },
    parentPact: [
      {
        label: "销售合同",
        function: "SAL_CONT",
        field: "contGoodsId",
        path: "/business-management/sal-cont",
      },
      {
        label: "采购申请单",
        function: "PUR_APPLY",
        field: "purApplyDetailId",
        path: "/business-management/pur-form",
      },
    ],
    sonPact: [
      {
        label: "发货通知单",
        function: "SAL_NOTICE",
        path: "/business-management/sal-notice",
        // field: "order_id",
        field: "order_goods_id",
      },
      {
        label: "生产通知单",
        function: "SAL_PRDNOTICE",
        path: "/business-management/prod-notice",
        // field: "order_id",
        field: "order_goods_id",
      },
      {
        label: "销售订单变更单",
        function: "SAL_ORDER_C",
        path: "/change-order/sal-order-cont",
        field: "source_goods_id",
      },
    ],
  },
  {
    currentPact: {
      label: "生产通知单",
      function: "SAL_PRDNOTICE",
      sonSheet: "sal_prdnotice_goods",
    },
    parentPact: [
      {
        label: "销售订单",
        function: "SAL_ORDER",
        field: "orderGoodsId",
        path: "/business-management/sal-order-cont",
      },
    ],
    sonPact: [
      {
        label: "生产通知变更单",
        function: "SAL_PRDNOTICE_C",
        path: "/change-order/prod-notice",
        field: "source_id",
      },
    ],
  },
  {
    currentPact: {
      label: "发货通知单",
      function: "SAL_NOTICE",
      sonSheet: "sal_notice_goods",
    },
    parentPact: [
      {
        label: "销售订单",
        function: "SAL_ORDER",
        // 当前单据详情中，与该上游单据对应的字段
        field: "orderGoodsId",
        path: "/business-management/sal-order-cont",
      },
    ],
    sonPact: [
      {
        label: "销售出库单",
        function: "SAL_OUTSTOCK",
        path: "/business-management/sal-out",
        // field: "crm_notice_id",
        field: "notice_goods_id",
      },
      {
        label: "出库源单",
        function: "SAL_ORG_OUTSTOCK",
        path: "/business-management/sal-org-outstock",
        // field: "crm_notice_id",
        field: "notice_goods_id",
      },
       {
        label: "物流申请单",
        function: "SAL_LOGISTICS_APPLY",
        path: "/business-management/sal-logistics-apply",
        // field: "crm_notice_id",
        field: "noticeId",
      },
    ],
  },
  {
    currentPact: {
      label: "采购申请单",
      function: "PUR_APPLY",
      sonSheet: "pur_apply_detail",
    },
    parentPact: [],
    sonPact: [
      {
        label: "销售订单",
        function: "SAL_ORDER",
        // 当前单据详情中，与该上游单据对应的字段
        field: "orderGoodsId",
        path: "/business-management/sal-order-cont",
      },
    ],
  },
  {
    currentPact: {
      label: "送货申请单",
      function: "SAL_DELIVERY_REQUEST",
      sonSheet: "sal_delivery_request_detail",
    },
    parentPact: [],
    sonPact: [
      {
        label: "运输单",
        function: "SAL_TRANSPORT",
        // 当前单据详情中，与该上游单据对应的字段
        field: "crmDeliveryRequestId",
        path: "/business-management/sal-transport",
      },
    ],
  },
  {
    currentPact: {
      label: "运输单",
      function: "SAL_TRANSPORT",
      sonSheet: "sal_transport_detail",
    },
    parentPact: [
      {
        label: "送货申请单",
        function: "SAL_DELIVERY_REQUEST",
        // 当前单据详情中，与该上游单据对应的字段
        field: "crmDeliveryRequestId",
        path: "/business-management/sal-delivery-request",
      },
    ],
    sonPact: [],
  },
    {
    currentPact: {
      label: "物流申请单",
      function: "SAL_LOGISTICS_APPLY",
      sonSheet: "sal_logistics_apply_detail",
    },
    parentPact: [
      {
        label: "发货通知单",
        function: "SAL_NOTICE",
        // 当前单据详情中，与该上游单据对应的字段
        field: "noticeId",
        path: "/business-management/sal-notice",
      },
    ],
    sonPact: [],
  },
  {
    currentPact: {
      label: "销售出库单",
      function: "SAL_OUTSTOCK",
      sonSheet: "sal_outstock_goods",
    },
    parentPact: [
      {
        label: "发货通知单",
        function: "SAL_NOTICE",
        // 当前单据详情中，与该上游单据对应的字段
        path: "/business-management/sal-notice",
        field: "noticeGoodsId",
      },
      {
        label: "出库源单",
        function: "SAL_ORG_OUTSTOCK",
        path: "/business-management/sal-org-outstock",
        field: "crmOrgOutStockGoodsId",
      },
    ],
    sonPact: [
      {
        label: "应收单",
        function: "FI_AR",
        path: "/finance-manage/finance-manage/receive-bill",
        field: "out_stock_goods_id",
      },
      {
        label: "退货通知单",
        function: "SAL_RENOTICE",
        path: "/business-management/sal-return",
        field: "outstock_goods_id",
      },
    ],
  },
  {
    currentPact: {
      label: "出库源单",
      function: "SAL_ORG_OUTSTOCK",
      sonSheet: "sal_org_outstock_goods",
    },
    parentPact: [
      {
        label: "发货通知单",
        function: "SAL_NOTICE",
        // 当前单据详情中，与该上游单据对应的字段
        path: "/business-management/sal-notice",
        field: "noticeGoodsId",
      },
    ],
    sonPact: [
      {
        label: "销售出库单",
        function: "SAL_OUTSTOCK",
        path: "/business-management/sal-out",
        // field: "crm_notice_id",
        field: "crm_org_out_stock_goods_id",
      },
    ],
  },
  {
    currentPact: {
      label: "打样通知单",
      function: "PP_NOTICE",
      sonSheet: "pp_notice_detail",
    },
    parentPact: [],
    sonPact: [
      {
        label: "打样记录列表",
        function: "PP_RECORD",
        path: "/business-management/pp-record",
        // field: "notice_id",
        field: "notice_detail_id",
      },
    ],
  },
  {
    currentPact: {
      label: "打样记录列表",
      function: "PP_RECORD",
      sonSheet: "pp_record_detail",
    },
    parentPact: [
      {
        label: "打样通知单",
        function: "PP_NOTICE",
        path: "/business-management/labdips-notice",
        field: "ppRecordId",
      },
    ],
    sonPact: [],
  },
  {
    currentPact: {
      label: "锁库申请单",
      function: "WH_LOCKAPPLY",
      sonSheet: "wh_lock_apply_detail",
    },
    parentPact: [],
    sonPact: [
      {
        label: "锁库单",
        function: "WH_LOCKSTOCK",
        path: "/inventory/locks-stock",
        field: "lock_apply_detail_id",
      },
    ],
  },
  {
    currentPact: {
      label: "锁库单",
      function: "WH_LOCKSTOCK",
      sonSheet: "wh_lock_stock_detail",
    },
    parentPact: [
      {
        label: "锁库申请单",
        function: "WH_LOCKAPPLY",
        path: "/inventory/locks-apply",
        field: "lockApplyDetailId",
      },
    ],
    sonPact: [],
  },
  {
    currentPact: {
      label: "退货通知单",
      function: "SAL_RENOTICE",
      sonSheet: "sal_renotice_goods",
    },
    // 父级 直接在getTableList中 查id id通过当前详情取出关联的id
    // field 为详情中关联的key
    parentPact: [
      {
        label: "销售出库单",
        function: "SAL_OUTSTOCK",
        path: "/business-management/sal-out",
        field: "outstockGoodsId",
      },
    ],
    // 子级 需要在子级列表页面 添加对应查询条件 条件为关联的父级id
    sonPact: [
      {
        label: "销售退货单",
        function: "SAL_RESTOCK",
        path: "/business-management/mer-return",
        field: "renotice_goods_id",
      },
      {
        label: "退库源单",
        function: "SAL_ORG_RESTOCK",
        path: "/business-management/sal-org-restock",
        field: "crm_sal_renotice_goods_id",
      },
    ],
  },
  {
    currentPact: {
      label: "销售退货单",
      function: "SAL_RESTOCK",
      sonSheet: "sal_restock_goods",
    },
    // 父级 直接在getTableList中 查id id通过当前详情取出关联的id
    // field 为详情中关联的key
    parentPact: [
      {
        label: "退货通知单",
        function: "SAL_RENOTICE",
        path: "/business-management/sal-return",
        field: "renoticeGoodsId",
      },
    ],
    // 子级 需要在子级列表页面 添加对应查询条件 条件为关联的父级id
    sonPact: [
      {
        label: "应收单",
        function: "FI_AR",
        path: "/finance-manage/finance-manage/receive-bill",
        field: "re_stock_goods_id",
      },
    ],
  },
  {
    currentPact: {
      label: "退库源单",
      function: "SAL_ORG_RESTOCK",
      sonSheet: "sal_org_restock_goods",
    },
    // 父级 直接在getTableList中 查id id通过当前详情取出关联的id
    // field 为详情中关联的key
    parentPact: [
      {
        label: "退货通知单",
        function: "SAL_RENOTICE",
        path: "/business-management/sal-return",
        field: "crmSalRenoticeGoodsId",
      },
    ],
    // 子级 需要在子级列表页面 添加对应查询条件 条件为关联的父级id
    sonPact: [
      {
        label: "销售退货单",
        function: "SAL_RESTOCK",
        path: "/business-management/mer-return",
        // field: "crm_org_restock_id",
        field: "crm_org_restock_goods_id",
      },
    ],
  },
  {
    currentPact: {
      label: "客诉单",
      function: "SAL_CC",
      sonSheet: "sal_cc_outstock",
    },
    // 父级 直接在getTableList中 查id id通过当前详情取出关联的id
    // field 为详情中关联的key
    parentPact: [
      {
        label: "销售出库单",
        function: "SAL_OUTSTOCK",
        path: "/business-management/sal-out",
        field: "outstockGoodsId",
      },
    ],
    // 子级 需要在子级列表页面 添加对应查询条件 条件为关联的父级id
    sonPact: [
      {
        label: "退货通知单",
        function: "SAL_RENOTICE",
        path: "/business-management/sal-return",
        field: "cc_id",
      },
    ],
  },
  {
    currentPact: {
      label: "应收单",
      function: "FI_AR",
      sonSheet: "fi_ar_goods",
    },
    // 父级 直接在getTableList中 查id id通过当前详情取出关联的id
    // field 为详情中关联的key
    parentPact: [
      {
        label: "销售出库单",
        function: "SAL_OUTSTOCK",
        path: "/business-management/sal-out",
        field: "outStockGoodsId",
      },
    ],
    // 子级 需要在子级列表页面 添加对应查询条件 条件为关联的父级id
    sonPact: [
      {
        label: "开票记录",
        function: "FI_INVRECORD",
        path: "/finance-manage/finance-manage/bill-record",
        field: "ar_goods_id",
      },
      {
        label: "核销单",
        function: "FI_RBILL",
        path: "/finance-manage/finance-manage/charge-off-record",
        field: "arId",
      },
    ],
  },
  {
    currentPact: {
      label: "客户回款单",
      function: "FI_CUSTRETURN",
      sonSheet: "fi_custreturn_detail",
    },
    // 父级 直接在getTableList中 查id id通过当前详情取出关联的id
    // field 为详情中关联的key
    parentPact: [],
    // 子级 需要在子级列表页面 添加对应查询条件 条件为关联的父级id
    sonPact: [
      {
        label: "收款单",
        function: "FI_RECORD",
        path: "/finance-manage/finance-manage/receipt",
        field: "custreturn_detail_id",
      },
    ],
  },
  {
    currentPact: {
      label: "收款单",
      function: "FI_RECORD",
      sonSheet: "",
    },
    // 父级 直接在getTableList中 查id id通过当前详情取出关联的id
    // field 为详情中关联的key
    parentPact: [
      {
        label: "回款单",
        function: "FI_CUSTRETURN",
        path: "/finance-manage/finance-manage/customer-pay",
        field: "custreturn_detail_id",
      },
    ],
    // 子级 需要在子级列表页面 添加对应查询条件 条件为关联的父级id
    sonPact: [
      {
        label: "核销单",
        function: "FI_RBILL",
        path: "/finance-manage/finance-manage/charge-off-record",
        field: "recordId",
      },
    ],
  },
  {
    currentPact: {
      label: "开票记录",
      function: "FI_INVRECORD",
      sonSheet: "fi_invrecord_detail",
    },
    // 父级 直接在getTableList中 查id id通过当前详情取出关联的id
    // field 为详情中关联的key
    parentPact: [
      {
        label: "应收单",
        function: "FI_AR",
        path: "/finance-manage/finance-manage/receive-bill",
        field: "arGoodsId",
      },
    ],
    sonPact: [],
  },
  {
    currentPact: {
      label: "核销单",
      function: "FI_RBILL",
      // sonSheet: ["fi_rbill_ar", "fi_rbill_record"],
      sonSheet: "",
    },
    // 父级 直接在getTableList中 查id id通过当前详情取出关联的id
    // field 为详情中关联的key
    parentPact: [
      {
        label: "应收单",
        function: "FI_AR",
        path: "/finance-manage/finance-manage/receive-bill",
        field: "arId",
        sonSheet: "fi_rbill_ar",
      },
      {
        label: "收款单",
        function: "FI_RECORD",
        path: "/finance-manage/finance-manage/receipt",
        field: "recordId",
        sonSheet: "fi_rbill_record",
      },
    ],
    sonPact: [],
  },
];

// 下推
export const pushDownList = [
  {
    // 销售意向合同 => 销售合同
    // 当前函数名
    currentFn: "SAL_INT_CONT",
    // 下推目标单据函数名
    targetFn: "SAL_CONT",
    // 下推目标单据路径
    path: "/business-management/sal-cont",
    // 要求相同的值的key
    requireSameKeys: [
      "int_cont_type",
      "sal_org_id",
      "brand_id",
      "cust_id",
      "saler_id",
      "dept_id",
      "trade_mode",
      "trade_type",
      "curr_id",
    ],
    isSingle: true,
    sonSheet: "sal_int_cont_goods",
  },
  {
    // 销售意向合同 => 生产通知单
    // 当前函数名
    currentFn: "SAL_INT_CONT",
    // 下推目标单据函数名
    targetFn: "SAL_PRDNOTICE",
    // 下推目标单据路径
    path: "/business-management/prod-notice",
    // 要求相同的值的key
    requireSameKeys: [
      "int_cont_type",
      "sal_org_id",
      "brand_id",
      "cust_id",
      "saler_id",
      "dept_id",
      "trade_mode",
      "trade_type",
      "curr_id",
    ],
    isSingle: true,
    sonSheet: "sal_int_cont_goods",
  },
  {
    // 销售合同 => 销售订单
    currentFn: "SAL_CONT",
    targetFn: "SAL_ORDER",
    path: "/business-management/sal-order-cont",
    requireSameKeys: [
      "int_cont_type",
      "cont_type",
      "brand_id",
      "cust_id",
      "saler_id",
      "dept_id",
      "sal_org_id",
      "curr_id",
      "trade_mode",
      "trade_type",
    ],
    isSingle: false,
    sonSheet: "sal_cont_goods",
  },
  {
    // 销售合同 => 销售子合同
    currentFn: "SAL_CONT",
    targetFn: "SAL_CONT",
    path: "/business-management/sal-cont",
    requireSameKeys: [],
    isSingle: true,
    sonSheet: "sal_cont_goods",
  },

  {
    // 销售订单 => 生产通知单
    currentFn: "SAL_ORDER",
    targetFn: "SAL_PRDNOTICE",
    path: "/business-management/prod-notice",
    requireSameKeys: [
      // "cont_type",
      // "brand_id",
      "cust_id",
      "saler_id",
      "dept_id",
      "prd_org_id",
    ],
    isSingle: false,
    sonSheet: "sal_order_goods",
  },
  {
    // 销售订单 => 发货通知单
    currentFn: "SAL_ORDER",
    targetFn: "SAL_NOTICE",
    path: "/business-management/sal-notice",
    requireSameKeys: [
      "cust_id",
      "saler_id",
      "dept_id",
      "sal_org_id",
      "prd_org_id",
      "curr_id",
    ],
    isSingle: false,
    sonSheet: "sal_order_goods",
  },
  {
    // 销售出库单 => 退货通知单
    currentFn: "SAL_OUTSTOCK",
    targetFn: "SAL_RENOTICE",
    path: "/business-management/sal-return",
    requireSameKeys: [],
    isSingle: true,
    sonSheet: "sal_outstock_goods",
  },
  {
    // 销售退货单 => 客诉单
    currentFn: "SAL_RESTOCK",
    targetFn: "SAL_CC",
    path: "/business-management/customer-complaint",
    requireSameKeys: [],
    isSingle: true,
    sonSheet: "sal_restock_goods",
  },
    {
    // 发货通知单 => 物流申请单
    currentFn: "SAL_NOTICE",
    targetFn: "SAL_LOGISTICS_APPLY",
    path: "/business-management/sal-logistics-apply",
    requireSameKeys: [
      "transMode",
      "sal_org_name",
    //   "cust_id",
    //   "saler_id",
    //   "dept_id",
    //   "sal_org_id",
    //   "prd_org_id",
    //   "curr_id",
    ],
    isSingle: false,
    sonSheet: "sal_notice_goods",
  },
];
// 显示取消按钮的页面
export const cancelBtnFunctionNameList = [
  "SAL_INT_CONT",
  "SAL_CONT",
  "SAL_ORDER",
  "SAL_PRDNOTICE",
  "SAL_NOTICE",
];
// 显示关闭按钮的页面
export const closedBtnFunctionNameList = [
  "SAL_INT_CONT",
  "SAL_CONT",
  "SAL_ORDER",
];
export const allBtnPermsStrList = [
  "add",
  "copy",
  "push-down",
  "deleted",
  "updated",
  "change",
  "relation-query",
  "business-operation",
  "cancel",
  "closed",
  "print-template",
  "custom-column",
  "cancel",
  "reCancel",
  "cancelTable",
  "reCancelTable",
  "close",
  "reClose",
  "closeTable",
  "reCloseTable",
  "draft",
  "save",
  "submit",
  "rollback",
  "check",
];
// 所有的操作符
export const allOperateList = [
  // 单参数比较
  { key: "=", val: "EQ" },
  { key: "!=", val: "NE" },
  { key: ">", val: "GT" },
  { key: ">=", val: "GE" },
  { key: "<", val: "LT" },
  { key: "<=", val: "LE" },
  // 双参数比较
  // { key: "大于小于", val: "GT_LT" },
  // { key: "大于小于等于", val: "GT_LE" },
  // { key: "大于等于小于", val: "GE_LT" },
  // { key: "大于等于小于等于", val: "GE_LE" },
  // { key: "大于等于小于等于", val: "GE_LE" },
  // 模糊匹配
  { key: "包含", val: "LIKE" },
  { key: "不包含", val: "NOT_LIKE" },
  { key: "左包含", val: "L_LIKE" },
  { key: "右包含", val: "R_LIKE" },
  // NULL运算符
  { key: "为空", val: "IS_NULL" },
  { key: "不为空", val: "IS_NOT_NULL" },
  // 日期类型
  { key: "最近一个周", val: "WEEK" },
  { key: "最近一个月", val: "MONTH" },
  { key: "最近三个月", val: "THREEMONTHS" },
  { key: "最近半年", val: "SIXMONTHS" },
  { key: "最近一年", val: "YEAR" },
  // 集合比较
  { key: "IN", val: "IN" },
  { key: "NOT_IN", val: "NOT_IN" },
  { key: "FIND_IN_SET", val: "FIND_IN_SET" },
  // 未知
  // { key: "UNKNOW", val: "UNKNOW" },
];
// 操作符Map
export const operateMap = {
  TEXT: [
    { key: "=", val: "EQ" },
    { key: "!=", val: "NE" },
    { key: "IN", val: "IN" },
    { key: "NOT_IN", val: "NOT_IN" },
    // 模糊匹配
    { key: "包含", val: "LIKE" },
    { key: "不包含", val: "NOT_LIKE" },
    { key: "左包含", val: "L_LIKE" },
    { key: "右包含", val: "R_LIKE" },
    // NULL运算符
    { key: "为空", val: "IS_NULL" },
    { key: "不为空", val: "IS_NOT_NULL" },
  ],
  NUMBER: [
    // 单参数比较
    { key: "=", val: "EQ" },
    { key: "!=", val: "NE" },
    { key: ">", val: "GT" },
    { key: ">=", val: "GE" },
    { key: "<", val: "LT" },
    { key: "<=", val: "LE" },
  ],
  DATE: [
    // 单参数比较
    { key: "=", val: "EQ" },
    { key: "!=", val: "NE" },
    { key: ">", val: "GT" },
    { key: ">=", val: "GE" },
    { key: "<", val: "LT" },
    { key: "<=", val: "LE" },
    // 日期类型
    { key: "今天", val: "TODAY" },
    { key: "昨天", val: "YESTERDAY" },
    { key: "明天", val: "TOMORROW" },
    { key: "大于今天", val: "GTTODAY" },
    { key: "大于等于今天", val: "GETODAY" },
    { key: "前两天", val: "TWODAYSBEFORE" },
    { key: "前三天", val: "THREEDAYSBEFORE" },
    { key: "上周", val: "LASTWEEK" },
    { key: "上个月", val: "LASTMONTH" },
    { key: "本月", val: "THISMONTH" },
    { key: "最近一个周", val: "WEEK" },
    { key: "最近一个月", val: "MONTH" },
    { key: "最近三个月", val: "THREEMONTHS" },
    { key: "最近半年", val: "SIXMONTHS" },
    { key: "最近一年", val: "YEAR" },
  ],
};
// 时间类型范围操作符
export const dateRangeOperateList = [
  "TODAY",
  "YESTERDAY",
  "TOMORROW",
  "GTTODAY",
  "GETODAY",
  "THREEDAYSBEFORE",
  "TWODAYSBEFORE",
  "LASTWEEK",
  "LASTMONTH",
  "THISMONTH",
  "WEEK",
  "MONTH",
  "THREEMONTHS",
  "SIXMONTHS",
  "YEAR",
];

// 默认可批量填充的变量
export const relationshipsList = {
  needCo:'needCo',
  custId:'custId',
  bulkTypeCode:'bulkTypeCode',
  compName:'compName',
  printName:'printName',
  rawCode:'rawCode',
  endDate:"endDate",
  hlCardId: "hlCardId",
  matId: "matId",
  rawMatId: "rawMatId",
  printColorId: "printColorId",
  compColorId: "compColorId",
  color: "color",
  qty: "qty",
  price: "price",
  bCurrRate: "bCurrRate",
  cFee: "cFee",
  otherFee: "otherFee",
  yarnUse: "yarnUse",
  remark: "remark",
  mlRate: "mlRate",
  yarnType: "yarnType",
  ctWeight: "ctWeight",
  packReq: "packReq",
  cfReq: "cfReq",
  dyeReq: "dyeReq",
  psReq: "psReq",
  taxRate: "taxRate",
  yarnSalType: "yarnSalType",
  hsInfo: "hsInfo",
  tpReq: "tpReq",
  devType: "devType",
  labelPrintReq: "labelPrintReq",
  isNewyarn: "isNewyarn",
  confirmColor: "confirmColor",
  isDdColor: "isDdColor",
  msQty: "msQty",
  isSingle: "isSingle",
  isWax: "isWax",
  isHsConfirm: "isHsConfirm",
  longSilkColor: "longSilkColor",
  hsQty: "hsQty",
  isFast: "isFast",
  isEt: "isEt",
  fastDeliveryDate: "fastDeliveryDate",
  isProof: "isProof",
  isPFee: "isPFee",
  pFeeRate: "pFeeRate",
  devDate: "devDate",
  requireDate: "requireDate",
  custPo: "custPo",
  custColorNo: "custColorNo",
  custLotNo: "custLotNo",
  custMatNo: "custMatNo",
  suType: "suType",
  suYarnType: "suYarnType",
  spinProcess: "spinProcess",
  custColorName: "custColorName",
  lotNo: "lotNo",
  dyeCode: "dyeCode",
  busFee: "busFee",
  seaFee: "seaFee",
  expFee: "expFee",
  airFee: "airFee",
  isFree: "isFree",
  isCb: "isCb",
  costPrice: "costPrice",
  eYarnLotNo: "eYarnLotNo",
  checkPrice: "checkPrice",
  tpFee: "tpFee",
  isDm: "isDm",
  dmPrice: "dmPrice",
  otherSpinNotes: "otherSpinNotes",
  rawMatReq: "rawMatReq",
  isFollowingCombination: "isFollowingCombination",
  dateStart: "dateStart",
  dateEnd: "dateEnd",
  isBleach: "isBleach",
  cDyeFee: "cDyeFee",
  addCostPrice: "addCostPrice",
  totalCostPrice: "totalCostPrice",
  packType: "packType",
  weightType: "weightType",
  rawMatCnPrint: "rawMatCnPrint",
  rawMatEnPrint: "rawMatEnPrint",
  isRd:"isRd"
};

// 显示检查按钮的列表
export const showCheckBtnList = ["SAL_INT_CONT", "SAL_CONT"];

export const citiesTwo = [
  {
    value: "执行国际羊毛局手洗标准(不包括起球);",
    key: 1,
  },
  {
    value: "执行国际羊毛局机洗标准(不包括起球);",
    key: 2,
  },
  {
    value:
      "按客户标准(另外提出):用于夹条产品，且对比度大的颜色，买方下单时应特别注明，价格将作适当调整",
    key: 3,
  },
];
export const cities = [
  {
    value: `执行国际羊毛局手洗标准(不包括起球);\n `,
    val: "According with lWS handwash specifications (not including pilling);\n ",
    key: 1,
  },
  {
    value: `执行国际羊毛局机洗标准(不包括起球);\n `,
    val: "According with lWS Machinewashable specifications(not including pilling);\n ",
    key: 2,
  },
  {
    value: `按客户标准(另外提出):用于夹条产品，且对比度大的颜色，买方下单时应特别注明，价格将作适当调整\n `,
    val: "According with the customer stands(addition): lf the yarns are used to make strip product and there isblg ditference in color eftects, the buyer should point out when placing the order and the seller will adjustthe price accordingly",
    key: 3,
  },
];
export const roleEnum = {
  SYS: { code: "sys", desc: "系统管理员" },
  SECTION_CHIEF: { code: "sectionChief", desc: "销售科长" },
  INTER_TWO: { code: "inter-two", desc: "跟单员(只能看到跟单数据)" },
  TECH_REVIEW: { code: "techReview", desc: "技术评审" },
  ADMIN: { code: "admin", desc: "超级管理员" },
  COMMON: { code: "common", desc: "业务超级角色" },
  SALER: { code: "saler", desc: "销售员" },
  DIRECTOR: { code: "director", desc: "销售总监" },
  CEO: { code: "ceo", desc: "总裁" },
  UPDATE_PAPER_CONT: { code: "updatePaperCont", desc: "修改纸质合同" },
  AUDITOR: { code: "auditor", desc: "计划评审" },
  SEARCH: { code: "search", desc: "查看角色" },
  SALES_ASSESSMENT: { code: "salesAssessment", desc: "销售考核" },
  CONT_TERMS: { code: "contTerms", desc: "合同条款" },
  INTER: { code: "inter", desc: "跟单员" },
};
const DyeMethodEnum = {
  STRIP_DYEING: { code: "1", name: "条染" },
  TWIST_DYEING: { code: "2", name: "绞染" },
  EMBRYO_YARN: { code: "3", name: "胚纱" },
  TWISTING: { code: "4", name: "合股" },
  CONE_DYEING: { code: "5", name: "筒染" },
  OTHER: { code: "9", name: "其他" },
};

// 配置合计列
export const summaryFieldMap = {
  SAL_NOTICE: ["out_qty", "outQty", "amount"],
};
export const globalVariable = {
  radioList,
  sexList,
  dictEnum,
  changeOrderMap,
  cancelBtnFunctionNameList,
  closedBtnFunctionNameList,
  // 总厂名称
  mainFactoryName: "浙江云梭纺织股份有限公司",
  // 系统默认过滤模块名称
  SYS_FILTER_SCHEME: "SYS_FILTER_SCHEME",
  allBtnPermsStrList,
  showCheckBtnList,
  citiesTwo,
  cities,
  roleEnum,
  DyeMethodEnum,
  summaryFieldMap,
};
