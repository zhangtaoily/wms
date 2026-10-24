<template>
  <div class="app-container">
    <FormButtons
      ref="formButtons"
      :isOpenDocStatus="false"
      :hiddenButtonList="hiddenButtonList"
      :disabledButtonList="['check']"
      @setReaderOnly="setReaderOnly"
      @setForm="setForm"
      :form="form"
    >
      <template slot="frontButtonSlot">
        <!-- 盘盈亏主管确认（M5 Q2）：盘存单已生效且存在待确认的盈亏调整记录 -->
        <el-button
          v-if="showCountConfirmBtn"
          type="success"
          icon="el-icon-finished"
          @click="handleCountConfirm(true)"
        >盈亏确认</el-button>
        <el-button
          v-if="showCountConfirmBtn"
          type="danger"
          plain
          icon="el-icon-close"
          @click="handleCountConfirm(false)"
        >盈亏驳回</el-button>
        <!-- 红冲：仅已生效且非红冲单的入库/出库/移库单可发起 -->
        <el-button
          v-if="showReverseBtn"
          type="warning"
          icon="el-icon-refresh-left"
          @click="handleReverse"
        >红冲</el-button>
        <!-- 重发同步：同步失败（未同步）的入库/出库单可重复触发，后端幂等 -->
        <el-button
          v-if="showResendBtn"
          type="primary"
          icon="el-icon-position"
          @click="handleResend"
        >重发同步</el-button>
      </template>
      <template slot="form">
        <el-form
          ref="form"
          :model="form"
          :rules="rules"
          label-width="130px"
          class="form-box"
          :disabled="isReadOnly"
        >
          <el-form-item
            v-for="field in formFields"
            :key="field.name"
            :label="field.label"
            :prop="field.name"
          >
            <!-- 仓库下拉（入库单关联仓库） -->
            <el-select
              v-if="field.type === 'warehouse'"
              v-model="form[field.name]"
              placeholder="请选择仓库"
              filterable
              clearable
              style="width: 100%"
              @change="onWarehouseChange"
            >
              <el-option
                v-for="wh in warehouseList"
                :key="wh.id"
                :label="wh.warehouse_name"
                :value="wh.id"
              />
            </el-select>
            <!-- 下拉（静态选项） -->
            <el-select
              v-else-if="field.type === 'select'"
              v-model="form[field.name]"
              placeholder="请选择"
              filterable
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="opt in selectOptions[functionName + '.' + field.name] || selectOptions[field.name] || []"
                :key="opt"
                :label="opt"
                :value="opt"
              />
            </el-select>
            <!-- 是/否 单选 -->
            <el-radio-group v-else-if="field.type === 'radio'">
              <el-radio :label="1">是</el-radio>
              <el-radio :label="0">否</el-radio>
            </el-radio-group>
            <!-- 数字（readonly 为前端聚合字段，仅展示） -->
            <el-input-number
              v-else-if="field.type === 'number'"
              v-model="form[field.name]"
              :min="field.readonly ? undefined : 0"
              :disabled="field.readonly"
              controls-position="right"
              style="width: 100%"
            />
            <!-- 日期 -->
            <el-date-picker
              v-else-if="field.type === 'date'"
              v-model="form[field.name]"
              type="date"
              value-format="yyyy-MM-dd"
              placeholder="请选择日期"
              style="width: 100%"
            />
            <!-- 长文本 -->
            <el-input
              v-else-if="field.type === 'textarea'"
              v-model="form[field.name]"
              type="textarea"
              :rows="4"
              placeholder="请输入"
            />
            <!-- 文本 -->
            <el-input v-else v-model="form[field.name]" placeholder="请输入" />
          </el-form-item>
        </el-form>

        <!-- 单据类功能子表（行内编辑；输入组件在 el-form 外需显式禁用） -->
        <div v-for="table in subTables" :key="table.key" class="sub-table-box">
          <div class="sub-table-header">
            <span class="sub-table-title">{{ table.title }}</span>
            <el-button
              v-if="!isReadOnly"
              type="primary"
              plain
              size="mini"
              icon="el-icon-plus"
              @click="addSubRow(table)"
            >添加一行</el-button>
          </div>
          <el-table :data="form[table.key] || []" border size="small">
            <el-table-column
              v-if="!isReadOnly"
              label="操作"
              width="60"
              align="center"
              fixed="left"
            >
              <template slot-scope="scope">
                <el-button
                  type="text"
                  size="mini"
                  @click="removeSubRow(table, scope.row)"
                >删除</el-button>
              </template>
            </el-table-column>
            <el-table-column
              v-for="col in table.columns"
              :key="col.prop"
              :label="col.label"
              :width="col.width"
              :min-width="col.minWidth"
              align="center"
            >
              <template slot-scope="scope">
                <!-- 托号下拉（组托箱子归属托盘，选项来自托盘信息子表） -->
                <el-select
                  v-if="col.type === 'palletSelect'"
                  v-model="scope.row[col.prop]"
                  :disabled="isReadOnly"
                  filterable
                  clearable
                  size="small"
                  style="width: 100%"
                >
                  <el-option
                    v-for="p in palletOptions"
                    :key="p"
                    :label="p"
                    :value="p"
                  />
                </el-select>
                <span v-else-if="col.type === 'seq'">{{ scope.row[col.prop] }}</span>
                <!-- 计算列（盘存盈亏=实盘-盘前，后端生效时复核） -->
                <span v-else-if="col.type === 'computed'">{{ scope.row[col.prop] }}</span>
                <el-input-number
                  v-else-if="col.type === 'number'"
                  v-model="scope.row[col.prop]"
                  :min="0"
                  :disabled="isReadOnly"
                  controls-position="right"
                  size="small"
                  style="width: 100%"
                />
                <el-input
                  v-else
                  v-model="scope.row[col.prop]"
                  :disabled="isReadOnly"
                  size="small"
                  placeholder="请输入"
                />
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>
    </FormButtons>
  </div>
</template>

<script>
import {
  getDetails,
  getColumnConfig,
  getTableList,
  save,
  reSendData,
  confirmCountAdjust,
} from "@/api/system/common";

/**
 * M3 单据表单静态字段定义。
 * M3 实体存在 @TableField 别名（code↔code_no/inbound_no、pactState↔status），
 * 列配置 columnName 驼峰化后 ≠ 实体属性名，故不走列配置推导，直接静态声明（键=实体属性驼峰）。
 */
const M3_FORM_FIELDS = {
  WMS_RECEIVE: [
    { name: "receiveNo", label: "收货单号", type: "text" },
    { name: "sourceType", label: "来源类型", type: "text" },
    { name: "outDate", label: "发货日期", type: "date" },
    { name: "supplierCode", label: "供应商编码", type: "text" },
    { name: "supplierName", label: "供应商名称", type: "text" },
    { name: "outNo", label: "ERP出库单号", type: "text" },
    { name: "tranStatus", label: "在途状态", type: "text" },
  ],
  WMS_CODE_COLLECT: [
    { name: "bizType", label: "业务类型", type: "select" },
    { name: "collectDate", label: "单据日期", type: "date" },
    { name: "erpNo", label: "ERP订单号", type: "text" },
    { name: "batchNo", label: "批号", type: "text" },
    { name: "qty", label: "数量(件)", type: "number", readonly: true },
    { name: "remark", label: "备注", type: "textarea" },
  ],
  WMS_PALLET_ORDER: [
    { name: "palletType", label: "组托类型", type: "select" },
    { name: "orderDate", label: "组托日期", type: "date" },
    { name: "batchNo", label: "库存批号", type: "text" },
    { name: "vatNo", label: "缸号", type: "text" },
    { name: "remark", label: "备注", type: "textarea" },
  ],
  WMS_INBOUND: [
    { name: "inboundType", label: "入库类型", type: "select" },
    { name: "inboundDate", label: "入库日期", type: "date" },
    { name: "warehouseId", label: "仓库", type: "warehouse" },
    { name: "batchNo", label: "库存批次", type: "text" },
    { name: "vatNo", label: "缸号", type: "text" },
    { name: "weight", label: "标重(KG)", type: "number", readonly: true },
    { name: "boxes", label: "箱数", type: "number", readonly: true },
    { name: "workMode", label: "作业方式", type: "select" },
    { name: "erpDocNo", label: "ERP单据号", type: "text" },
  ],
  WMS_OUT_NOTICE: [
    { name: "noticeType", label: "通知来源", type: "select" },
    { name: "noticeDate", label: "单据日期", type: "date" },
    { name: "erpNo", label: "ERP单号", type: "text" },
    { name: "erpDate", label: "ERP单据日期", type: "date" },
    { name: "sales", label: "销售员", type: "text" },
    { name: "customerName", label: "客户名称", type: "text" },
    { name: "supplierName", label: "供应商名称", type: "text" },
    { name: "qty", label: "数量(KG)", type: "number" },
  ],
  WMS_OUTBOUND: [
    { name: "outboundType", label: "出库类型", type: "select" },
    { name: "outboundDate", label: "出库日期", type: "date" },
    { name: "warehouseId", label: "仓库", type: "warehouse" },
    { name: "workMode", label: "作业方式", type: "select" },
    { name: "batchNo", label: "库存批次", type: "text" },
    { name: "vatNo", label: "缸号", type: "text" },
    { name: "weight", label: "标重(KG)", type: "number", readonly: true },
    { name: "boxes", label: "箱数", type: "number", readonly: true },
    { name: "erpOrderNo", label: "ERP订单号", type: "text" },
    { name: "customerName", label: "客户名称", type: "text" },
  ],
  WMS_STOCK_MOVE: [
    { name: "moveType", label: "移库类型", type: "select" },
    { name: "moveDate", label: "移库日期", type: "date" },
    { name: "fromWarehouseId", label: "源仓库", type: "warehouse" },
    { name: "fromLocCode", label: "源仓位", type: "text" },
    { name: "toWarehouseId", label: "目标仓库", type: "warehouse" },
    { name: "toLocCode", label: "目标仓位", type: "text" },
    { name: "batchNo", label: "批号", type: "text" },
  ],
  WMS_STOCK_COUNT: [
    { name: "countDate", label: "盘存日期", type: "date" },
    { name: "workMode", label: "作业方式", type: "select" },
    { name: "warehouseId", label: "仓库", type: "warehouse" },
    { name: "locCode", label: "盘存仓位", type: "text" },
    { name: "batchNo", label: "批号", type: "text" },
    { name: "vatNo", label: "缸号", type: "text" },
    { name: "realPieces", label: "实盘件数", type: "number", readonly: true },
    { name: "realWeight", label: "实盘重量(KG)", type: "number", readonly: true },
  ],
  ASRS_WH_TASK: [
    { name: "taskDate", label: "任务日期", type: "date" },
    { name: "taskType", label: "任务类型", type: "select" },
    { name: "execStatus", label: "执行状态", type: "select" },
    { name: "batchNo", label: "批号", type: "text" },
    { name: "materialCode", label: "物料编码", type: "text" },
    { name: "taskBatchNo", label: "任务批号", type: "text" },
    { name: "outboundType", label: "出库类型", type: "text" },
    { name: "erpDocNo", label: "ERP单号", type: "text" },
    { name: "boxes", label: "箱数", type: "number" },
  ],
  ASRS_OP_TASK: [
    { name: "wcsTaskId", label: "WCS任务ID", type: "text" },
    { name: "palletNo", label: "托盘号", type: "text" },
    { name: "fromLoc", label: "源仓位", type: "text" },
    { name: "toLoc", label: "目标仓位", type: "text" },
    { name: "opType", label: "作业类型", type: "select" },
    { name: "port", label: "出入口", type: "select" },
    { name: "taskTime", label: "任务下发时间", type: "date" },
    { name: "execStatus", label: "执行状态", type: "select" },
    { name: "remark", label: "备注", type: "textarea" },
  ],
  ASRS_OP_LOG: [
    { name: "docDate", label: "单据日期", type: "date" },
    { name: "opType", label: "作业类型", type: "select" },
    { name: "result", label: "作业结果", type: "select" },
    { name: "palletNo", label: "托盘号", type: "text" },
    { name: "fromLoc", label: "源仓位", type: "text" },
    { name: "fromName", label: "源位置名称", type: "text" },
    { name: "toLoc", label: "目标仓位", type: "text" },
    { name: "toName", label: "目标位置名称", type: "text" },
    { name: "analysis", label: "分析状态", type: "select" },
    { name: "wcsTime", label: "WCS上报时间", type: "date" },
  ],
};

// 单据类功能（走状态机：制作中→审核→生效，允许提交生效）
const M3_DOC_FUNCTIONS = [
  "WMS_CODE_COLLECT",
  "WMS_PALLET_ORDER",
  "WMS_INBOUND",
  "WMS_OUT_NOTICE",
  "WMS_OUTBOUND",
  "WMS_STOCK_MOVE",
  "WMS_STOCK_COUNT",
  "ASRS_WH_TASK",
  "ASRS_OP_TASK",
];

/**
 * M3 子表定义。key=子表表名（引擎按表名回填数组，行主键/外键自动填充），
 * 行列 prop=实体属性驼峰。
 */
const SUB_TABLES = {
  WMS_CODE_COLLECT: [
    {
      key: "doc_code_collect_detail",
      title: "条码数据明细",
      columns: [
        { prop: "seq", label: "序号", type: "seq", width: 55 },
        { prop: "boxBarcode", label: "条码号", type: "text", minWidth: 160 },
        { prop: "batchNo", label: "业务批号", type: "text", minWidth: 110 },
        { prop: "invBatchNo", label: "库存批号", type: "text", minWidth: 110 },
        { prop: "tubes", label: "管数", type: "number", width: 110 },
        { prop: "stdWeight", label: "标重", type: "number", width: 110 },
        { prop: "grossWeight", label: "毛重", type: "number", width: 110 },
        { prop: "netWeight", label: "净重", type: "number", width: 110 },
        { prop: "tare", label: "皮重", type: "number", width: 110 },
        { prop: "stdBack", label: "标回潮", type: "number", width: 110 },
        { prop: "realBack", label: "实回潮", type: "number", width: 110 },
      ],
    },
  ],
  WMS_PALLET_ORDER: [
    {
      key: "doc_pallet_order_pallet",
      title: "托盘信息",
      columns: [
        { prop: "seq", label: "序号", type: "seq", width: 55 },
        { prop: "palletNo", label: "托号", type: "text", minWidth: 150 },
        { prop: "tubes", label: "管数", type: "number", width: 110 },
        { prop: "stdWeight", label: "标重", type: "number", width: 110 },
        { prop: "netWeight", label: "净重", type: "number", width: 110 },
        { prop: "grossWeight", label: "毛重", type: "number", width: 110 },
        { prop: "tubeWeight", label: "筒重管", type: "number", width: 110 },
        { prop: "realTube", label: "实际筒重", type: "number", width: 110 },
        { prop: "stdTube", label: "标定筒重", type: "number", width: 110 },
      ],
    },
    {
      key: "doc_pallet_order_detail",
      title: "箱子明细",
      columns: [
        { prop: "seq", label: "序号", type: "seq", width: 55 },
        { prop: "palletNo", label: "所属托号", type: "palletSelect", minWidth: 150 },
        { prop: "boxCode", label: "箱号", type: "text", minWidth: 150 },
        { prop: "markOut", label: "标出", type: "text", width: 80 },
        { prop: "boxStatus", label: "箱状态", type: "text", width: 90 },
        { prop: "warehouseName", label: "仓库", type: "text", minWidth: 120 },
        { prop: "locCode", label: "库位编号", type: "text", minWidth: 100 },
        { prop: "locName", label: "库位名称", type: "text", minWidth: 100 },
        { prop: "boxCount", label: "箱数", type: "number", width: 90 },
        { prop: "stdWeight", label: "标重", type: "number", width: 100 },
        { prop: "batchNo", label: "批号", type: "text", minWidth: 100 },
        { prop: "vatNo", label: "缸号", type: "text", minWidth: 90 },
      ],
    },
  ],
  WMS_INBOUND: [
    {
      key: "doc_inbound_detail",
      title: "入库明细",
      columns: [
        { prop: "seq", label: "序号", type: "seq", width: 55 },
        { prop: "boxCode", label: "箱号", type: "text", minWidth: 150 },
        { prop: "batchNo", label: "批号", type: "text", minWidth: 100 },
        { prop: "vatNo", label: "缸号", type: "text", minWidth: 90 },
        { prop: "locCode", label: "库位", type: "text", minWidth: 100 },
        { prop: "colorCode", label: "色号", type: "text", minWidth: 90 },
        { prop: "colorName", label: "颜色", type: "text", minWidth: 90 },
        { prop: "comp", label: "成分", type: "text", minWidth: 90 },
        { prop: "materialCode", label: "原料编号", type: "text", minWidth: 110 },
        { prop: "materialName", label: "原料名称", type: "text", minWidth: 110 },
        { prop: "yarn", label: "纱支", type: "text", minWidth: 90 },
        { prop: "tubes", label: "管数", type: "number", width: 100 },
        { prop: "stdWeight", label: "标重", type: "number", width: 110 },
        { prop: "grossWeight", label: "毛重", type: "number", width: 110 },
        { prop: "netWeight", label: "净重", type: "number", width: 110 },
      ],
    },
  ],
  WMS_OUT_NOTICE: [
    {
      key: "doc_out_notice_detail",
      title: "通知明细",
      columns: [
        { prop: "seq", label: "序号", type: "seq", width: 55 },
        { prop: "deliveType", label: "送货方式", type: "text", width: 100 },
        { prop: "batchNo", label: "批号", type: "text", minWidth: 100 },
        { prop: "vatNo", label: "缸号", type: "text", minWidth: 90 },
        { prop: "materialCode", label: "物料编码", type: "text", minWidth: 110 },
        { prop: "materialName", label: "物料名称", type: "text", minWidth: 110 },
        { prop: "spec", label: "规格(纱支)", type: "text", minWidth: 90 },
        { prop: "comp", label: "成分", type: "text", minWidth: 90 },
        { prop: "colorTop", label: "条染色号", type: "text", minWidth: 90 },
        { prop: "colorPiece", label: "绞染色号", type: "text", minWidth: 90 },
        { prop: "colorComp", label: "公司色号", type: "text", minWidth: 90 },
        { prop: "colorCust", label: "客户色号", type: "text", minWidth: 90 },
        { prop: "colorName", label: "色名", type: "text", minWidth: 90 },
        { prop: "qty", label: "数量", type: "number", width: 100 },
        { prop: "unit", label: "单位", type: "text", width: 70 },
        { prop: "warehouseName", label: "仓库", type: "text", minWidth: 110 },
        { prop: "locCode", label: "仓位", type: "text", minWidth: 90 },
      ],
    },
  ],
  WMS_OUTBOUND: [
    {
      key: "doc_outbound_detail",
      title: "出库明细",
      columns: [
        { prop: "seq", label: "序号", type: "seq", width: 55 },
        { prop: "erpDocNo", label: "ERP单号", type: "text", minWidth: 130 },
        { prop: "erpDate", label: "ERP日期", type: "text", width: 110 },
        { prop: "batchNo", label: "批号", type: "text", minWidth: 100 },
        { prop: "batchInfo", label: "批次信息", type: "text", minWidth: 110 },
        { prop: "vatNo", label: "缸号", type: "text", minWidth: 90 },
        { prop: "boxCode", label: "箱码", type: "text", minWidth: 150 },
        { prop: "boxes", label: "箱数", type: "number", width: 90 },
        { prop: "tubes", label: "筒数", type: "number", width: 90 },
        { prop: "pieces", label: "件数", type: "number", width: 90 },
        { prop: "stdWeight", label: "标重", type: "number", width: 100 },
        { prop: "grossWeight", label: "毛重", type: "number", width: 100 },
        { prop: "netWeight", label: "净重", type: "number", width: 100 },
        { prop: "materialCode", label: "物料编码", type: "text", minWidth: 110 },
        { prop: "materialName", label: "物料名称", type: "text", minWidth: 110 },
        { prop: "yarn", label: "纱支", type: "text", minWidth: 90 },
        { prop: "comp", label: "成分", type: "text", minWidth: 90 },
        { prop: "colorCode", label: "色号", type: "text", minWidth: 90 },
      ],
    },
  ],
  WMS_STOCK_MOVE: [
    {
      key: "doc_stock_move_detail",
      title: "明细信息",
      columns: [
        { prop: "seq", label: "序号", type: "seq", width: 55 },
        { prop: "boxCode", label: "箱码", type: "text", minWidth: 150 },
        { prop: "palletNo", label: "托盘号", type: "text", minWidth: 140 },
        { prop: "batchNo", label: "批号", type: "text", minWidth: 110 },
        { prop: "vatNo", label: "缸号", type: "text", minWidth: 90 },
        { prop: "toWarehouseName", label: "目标仓库", type: "text", minWidth: 110 },
        { prop: "toLocCode", label: "目标仓位", type: "text", minWidth: 100 },
      ],
    },
  ],
  WMS_STOCK_COUNT: [
    {
      key: "doc_stock_count_detail",
      title: "条码盘存作业",
      columns: [
        { prop: "seq", label: "序号", type: "seq", width: 55 },
        { prop: "locCode", label: "仓位", type: "text", minWidth: 100 },
        { prop: "invBatchNo", label: "库存批号", type: "text", minWidth: 120 },
        { prop: "vatNo", label: "缸号", type: "text", minWidth: 90 },
        { prop: "preTubes", label: "盘前筒数", type: "number", width: 100 },
        { prop: "realTubes", label: "实盘筒数", type: "number", width: 100 },
        { prop: "diffTubes", label: "盈亏筒数", type: "computed", width: 100 },
        { prop: "preStd", label: "盘前标重", type: "number", width: 100 },
        { prop: "realStd", label: "实盘标重", type: "number", width: 100 },
        { prop: "diffStd", label: "盈亏标重", type: "computed", width: 100 },
        { prop: "preGross", label: "盘前毛重", type: "number", width: 100 },
        { prop: "realGross", label: "实盘毛重", type: "number", width: 100 },
        { prop: "diffGross", label: "盈亏毛重", type: "computed", width: 100 },
        { prop: "preNet", label: "盘前净重", type: "number", width: 100 },
        { prop: "realNet", label: "实盘净重", type: "number", width: 100 },
        { prop: "diffNet", label: "盈亏净重", type: "computed", width: 100 },
        { prop: "colorCode", label: "色号", type: "text", minWidth: 90 },
        { prop: "colorName", label: "色名", type: "text", minWidth: 90 },
        { prop: "materialCode", label: "物料编码", type: "text", minWidth: 110 },
        { prop: "yarn", label: "纱支", type: "text", minWidth: 90 },
        { prop: "comp", label: "成分", type: "text", minWidth: 90 },
      ],
    },
  ],
};

// 各功能表单必填字段（键=实体属性驼峰，与保存引擎 convertToEntity 一致）
const REQUIRED_FIELDS = {
  WAREHOUSE: ["warehouseNo", "warehouseName"],
  LOCATION: ["locCode", "warehouseId"],
  MATERIAL: ["materialCode", "materialName"],
  BIZ_BATCH: ["bizBatchNo"],
  PRINT_TEMPLATE: ["moduleName", "tplName"],
  REMIND_SETTING: ["moduleName", "remindName"],
  WMS_CODE_COLLECT: ["bizType", "collectDate"],
  WMS_PALLET_ORDER: ["palletType", "orderDate"],
  WMS_INBOUND: ["inboundType", "inboundDate", "warehouseId"],
  WMS_OUT_NOTICE: ["noticeType", "noticeDate"],
  WMS_OUTBOUND: ["outboundType", "outboundDate", "warehouseId"],
  WMS_STOCK_MOVE: [
    "moveType",
    "moveDate",
    "fromWarehouseId",
    "toWarehouseId",
    "fromLocCode",
    "toLocCode",
  ],
  WMS_STOCK_COUNT: ["countDate", "workMode", "warehouseId"],
  ASRS_WH_TASK: ["taskDate"],
};

// 列表配置为 CASE 展示列（是/否）时，表单静态补充可编辑字段
const FORM_EXTRAS = {
  WAREHOUSE: [{ name: "enabled", label: "是否启用", type: "radio" }],
  LOCATION: [{ name: "enabled", label: "是否启用", type: "radio" }],
  PRINT_TEMPLATE: [{ name: "enabled", label: "是否启用", type: "radio" }],
  REMIND_SETTING: [
    { name: "enabled", label: "是否启用", type: "radio" },
    { name: "isPublic", label: "公开", type: "radio" },
  ],
};

// 不进表单的列（审计/同步字段）
const EXCLUDED_COLUMNS = [
  "id",
  "creator",
  "updater",
  "create_by",
  "update_by",
  "create_time",
  "update_time",
  "last_sync",
];

const TEXTAREA_COLUMNS = ["tplContent", "allowUsers", "comp"];

// 静态下拉选项（与原型编辑弹窗一致）
const SELECT_OPTIONS = {
  whType: ["普通仓库", "立库(自营)"],
  bizType: ["调拨入库", "销售入库", "采购入库", "产件入库", "退料入库"],
  palletType: ["件入条码组托", "传入条码组托", "手动组托"],
  inboundType: [
    "采购入库",
    "产件入库",
    "调拨入库",
    "样品入库",
    "退料入库",
    "产存入库",
    "销售退货入库",
  ],
  workMode: ["条码作业", "批次作业"],
  moveType: ["托盘挪库", "编码移库", "批次移库", "空托移库"],
  noticeType: ["发货通知单", "领料申请单", "采购退料单", "手工新建"],
  outboundType: [
    "销售出库",
    "调拨出库",
    "领料出库",
    "直接拣货出库",
    "复磅检出库",
    "采购退料出库",
    "样品出库",
  ],
  taskType: ["条码入库", "条码出库", "空托出库"],
  opType: ["入库", "出库", "移库"],
  port: ["出口自动", "月台1号回路", "月台2号回路", "5#自动线"],
  result: ["正常", "异常"],
  analysis: ["待分析", "已分析"],
  "ASRS_WH_TASK.execStatus": ["待执行", "执行中", "已完成"],
  "ASRS_OP_TASK.execStatus": ["执行中", "成功", "失败"],
};

export default {
  name: "DynamicFormDemo",
  data() {
    return {
      isReadOnly: false,
      form: {},
      formFields: [],
      rules: {},
      warehouseList: [],
      selectOptions: SELECT_OPTIONS,
      functionName: "",
      id: null,
      mode: "show",
    };
  },
  computed: {
    hiddenButtonList() {
      const hidden = [
        "add",
        "copy",
        "draft",
        "rollback",
        "print-template",
        "relation-query",
        "status",
        "business-operation",
        "push-down",
      ];
      // 仅单据类功能开放提交生效
      if (!M3_DOC_FUNCTIONS.includes(this.functionName)) {
        hidden.push("submit");
      }
      // 收货清单由 ERP 在途推送生成，不提供删除
      if (this.functionName === "WMS_RECEIVE") {
        hidden.push("deleted");
      }
      // 作业日志由 WCS 上报生成，只读不可维护
      if (this.functionName === "ASRS_OP_LOG") {
        hidden.push("save", "deleted");
      }
      // 同步监控记录由系统同步轴写入，只读（人工动作仅"重发同步"）
      if (this.functionName === "SYNC_RECORD") {
        hidden.push("save", "deleted");
      }
      if (this.mode === "show") {
        hidden.push("save", "deleted");
      }
      return hidden;
    },
    subTables() {
      return SUB_TABLES[this.functionName] || [];
    },
    // 组托箱子明细的所属托号选项（取托盘信息子表已填托号）
    palletOptions() {
      const rows = this.form.doc_pallet_order_pallet || [];
      return rows.map((r) => r.palletNo).filter((p) => p);
    },
    // 红冲：仅已生效且非红冲单的入库/出库/移库单可发起
    showReverseBtn() {
      return (
        ["WMS_INBOUND", "WMS_OUTBOUND", "WMS_STOCK_MOVE"].includes(this.functionName) &&
        !!this.form.id &&
        this.form.pactStateDesc === "通过" &&
        this.form.reverseFlag !== 1
      );
    },
    // 重发同步：单据已生效且同步失败（未同步）可触发；同步监控记录未成功即可人工重推（M7 Q1 超限转人工）
    showResendBtn() {
      if (this.functionName === "SYNC_RECORD") {
        return !!this.form.id && ["未同步", "失败"].includes(this.form.status);
      }
      return (
        ["WMS_INBOUND", "WMS_OUTBOUND", "WMS_STOCK_MOVE", "WMS_STOCK_COUNT"].includes(this.functionName) &&
        !!this.form.id &&
        this.form.pactStateDesc === "通过" &&
        this.form.syncStatus === 2
      );
    },
    // 盘盈亏主管确认（M5 Q2）：盘存单已生效且存在待确认的盈亏调整记录
    showCountConfirmBtn() {
      return (
        this.functionName === "WMS_STOCK_COUNT" &&
        !!this.form.id &&
        this.form.pactStateDesc === "通过" &&
        this.form.adjustState === 1
      );
    },
  },
  watch: {
    // 明细行变动联动主表聚合（数量/箱数/标重）
    "form.doc_code_collect_detail": { deep: true, handler: "calcAggregates" },
    "form.doc_inbound_detail": { deep: true, handler: "calcAggregates" },
    "form.doc_out_notice_detail": { deep: true, handler: "calcAggregates" },
    "form.doc_outbound_detail": { deep: true, handler: "calcAggregates" },
    // 盘存明细行变动联动盈亏计算（盈亏=实盘-盘前）与主表实盘聚合
    "form.doc_stock_count_detail": { deep: true, handler: "calcCountDiff" },
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      this.form = {};
      this.id = null;
      let { functionName, id, mode } = this.$route.query;
      this.functionName = functionName;
      this.id = id;
      this.mode = mode || "show";
      this.isReadOnly = this.mode === "show";
      if (M3_FORM_FIELDS[functionName]) {
        this.formFields = M3_FORM_FIELDS[functionName];
        this.buildRules();
      } else {
        this.loadFields();
      }
      if (["LOCATION", "WMS_INBOUND", "WMS_OUTBOUND", "WMS_STOCK_MOVE", "WMS_STOCK_COUNT"].includes(functionName)) {
        this.loadWarehouses();
      }
      if (this.mode !== "add" && this.id) {
        this.getData();
      }
    },
    /** 从字段配置推导表单字段（M2 功能；排除 join 列/CASE 展示列/审计列，键驼峰化） */
    loadFields() {
      getColumnConfig(this.functionName).then((res) => {
        const list = res.data || [];
        const extras = FORM_EXTRAS[this.functionName] || [];
        const hasEditableEnabled = list.some(
          (it) => it.columnName === "enabled" && !/case\s+when/i.test(it.sqlColumn || "")
        );
        const fields = list
          .filter((it) => {
            const col = it.columnName;
            if (EXCLUDED_COLUMNS.includes(col)) return false;
            const sql = it.sqlColumn || "";
            if (sql.includes(".")) return false; // join 展示列
            if (/case\s+when/i.test(sql)) return false; // CASE 展示列
            return true;
          })
          .map((it) => ({
            name: this.camelize(it.columnName),
            label: it.headerText,
            type: this.fieldTypeOf(it),
          }));
        // CASE 展示的 enabled 在表单中补为可编辑单选
        if (extras.length && !hasEditableEnabled) {
          fields.push(...extras);
        }
        this.formFields = fields;
        this.buildRules();
      });
    },
    fieldTypeOf(it) {
      const col = this.camelize(it.columnName);
      if (col === "warehouseId") return "warehouse";
      if (SELECT_OPTIONS[col]) return "select";
      if (col === "enabled" || col === "isPublic") return "radio";
      if (TEXTAREA_COLUMNS.includes(col)) return "textarea";
      if ((it.fieldType || "").startsWith("NUMBER")) return "number";
      if ((it.fieldType || "").startsWith("DATE")) return "date";
      return "text";
    },
    camelize(str) {
      return String(str || "").replace(/[_-]([a-z0-9])/gi, (m, c) => c.toUpperCase());
    },
    buildRules() {
      const required = REQUIRED_FIELDS[this.functionName] || [];
      const rules = {};
      required.forEach((name) => {
        const field = this.formFields.find((f) => f.name === name);
        rules[name] = [
          {
            required: true,
            message: `${field ? field.label : name}为必填项`,
            trigger: ["blur", "change"],
          },
        ];
      });
      this.rules = rules;
    },
    /** 仓库下拉数据（与列表页同参数结构，行键为下划线列名） */
    loadWarehouses() {
      getTableList({
        functionName: "WAREHOUSE",
        pageNum: 1,
        pageSize: 9999,
        conditions: [],
      }).then((res) => {
        this.warehouseList = res.rows || [];
      });
    },
    getData() {
      getDetails(this.functionName, this.id).then((res) => {
        const data = res.data || {};
        // 子表键缺省时补空数组，保证行内编辑/聚合联动可用
        this.subTables.forEach((t) => {
          if (!Array.isArray(data[t.key])) this.$set(data, t.key, []);
        });
        this.setForm(data);
      });
    },
    setReaderOnly(boolean) {
      this.isReadOnly = boolean;
    },
    setForm(form, isInit = false) {
      this.form = form || {};
      this.id = this.form?.id;
      if (isInit) {
        if (M3_FORM_FIELDS[this.functionName]) {
          // 新增单据默认值：单据日期取今天、子表置空
          this.formFields.forEach((f) => {
            if (
              ["collectDate", "orderDate", "inboundDate", "noticeDate", "outboundDate", "taskDate", "docDate", "moveDate", "countDate"].includes(f.name) &&
              !this.form[f.name]
            ) {
              this.$set(this.form, f.name, this.fmtToday());
            }
          });
          this.subTables.forEach((t) => {
            this.$set(this.form, t.key, []);
          });
        } else {
          this.$set(this.form, "enabled", 1);
        }
      }
      this.$nextTick(() => {
        this.$refs.formButtons.setOldForm(this.form);
      });
    },
    /** 仓库选择联动仓库名称 */
    onWarehouseChange(id) {
      const wh = this.warehouseList.find((w) => w.id === id);
      this.$set(this.form, "warehouseName", wh ? wh.warehouse_name : "");
    },
    /** 子表添加一行（预定义列键保证新增属性响应式） */
    addSubRow(table) {
      const row = {};
      table.columns.forEach((c) => {
        row[c.prop] = c.type === "number" ? undefined : "";
      });
      const rows = this.form[table.key] || [];
      rows.push(row);
      this.$set(this.form, table.key, rows);
      this.renumber(table.key);
      this.calcAggregates();
    },
    removeSubRow(table, row) {
      const rows = this.form[table.key] || [];
      const idx = rows.indexOf(row);
      if (idx > -1) rows.splice(idx, 1);
      this.renumber(table.key);
      this.calcAggregates();
    },
    renumber(key) {
      (this.form[key] || []).forEach((r, i) => {
        this.$set(r, "seq", i + 1);
      });
    },
    /** 主表聚合：码单数量=明细行数；入库箱数=明细行数、标重=明细标重合计（红冲单不回算） */
    calcAggregates() {
      if (!M3_FORM_FIELDS[this.functionName] || this.form.reverseFlag === 1) return;
      if (this.functionName === "WMS_CODE_COLLECT") {
        const rows = this.form.doc_code_collect_detail || [];
        this.$set(this.form, "qty", rows.length);
      } else if (this.functionName === "WMS_INBOUND") {
        const rows = this.form.doc_inbound_detail || [];
        this.$set(this.form, "boxes", rows.length);
        const weight = rows.reduce((sum, r) => sum + (Number(r.stdWeight) || 0), 0);
        this.$set(this.form, "weight", Math.round(weight * 100) / 100);
      } else if (this.functionName === "WMS_OUTBOUND") {
        const rows = this.form.doc_outbound_detail || [];
        this.$set(this.form, "boxes", rows.length);
        const weight = rows.reduce((sum, r) => sum + (Number(r.stdWeight) || 0), 0);
        this.$set(this.form, "weight", Math.round(weight * 100) / 100);
      }
    },
    /** 盘存盈亏联动：行盈亏=实盘-盘前（展示用，后端生效时复核落库）；主表实盘件数/重量聚合 */
    calcCountDiff() {
      if (this.functionName !== "WMS_STOCK_COUNT") return;
      const rows = this.form.doc_stock_count_detail || [];
      rows.forEach((r) => {
        this.$set(r, "diffTubes", (Number(r.realTubes) || 0) - (Number(r.preTubes) || 0));
        this.$set(r, "diffStd", this.round2((Number(r.realStd) || 0) - (Number(r.preStd) || 0)));
        this.$set(r, "diffGross", this.round2((Number(r.realGross) || 0) - (Number(r.preGross) || 0)));
        this.$set(r, "diffNet", this.round2((Number(r.realNet) || 0) - (Number(r.preNet) || 0)));
      });
      this.$set(this.form, "realPieces", rows.reduce((sum, r) => sum + (Number(r.realTubes) || 0), 0));
      this.$set(
        this.form,
        "realWeight",
        this.round2(rows.reduce((sum, r) => sum + (Number(r.realStd) || 0), 0))
      );
    },
    round2(n) {
      return Math.round(n * 100) / 100;
    },
    /** 盘盈亏主管确认（Q2）：放行后端调整台账并回写ERP；驳回需填原因，台账不变 */
    handleCountConfirm(approved) {
      if (approved) {
        this.$confirm(
          `确认盘存单 ${this.form.code} 的盘盈亏调整？确认后将校平库存台账并同步ERP`,
          "盘盈亏确认",
          { type: "warning" }
        )
          .then(() => confirmCountAdjust(this.form.id, true))
          .then(() => {
            this.$message.success("盘盈亏已确认，库存台账已校平并同步ERP");
            this.getData();
          })
          .catch(() => {});
      } else {
        this.$prompt("请输入驳回原因", "盘盈亏驳回", {
          type: "warning",
          inputPattern: /\S/,
          inputErrorMessage: "驳回原因不能为空",
        })
          .then(({ value }) => confirmCountAdjust(this.form.id, false, value))
          .then(() => {
            this.$message.success("盘盈亏调整已驳回，台账未调整");
            this.getData();
          })
          .catch(() => {});
      }
    },
    /** 红冲：按当前已生效单据生成红冲单（制作中），引用原单号，原单不删不改 */
    handleReverse() {
      const src = JSON.parse(JSON.stringify(this.form));
      const labels = { WMS_INBOUND: "入库单", WMS_OUTBOUND: "出库单", WMS_STOCK_MOVE: "移库单" };
      const label = labels[this.functionName] || "单据";
      const tip =
        this.functionName === "WMS_STOCK_MOVE"
          ? `将按移库单 ${src.code} 生成红冲单（源/目标仓位对调反向移库，引用原单号），是否继续？`
          : `将按${label} ${src.code} 生成红冲单（重量与箱数取负，引用原单号），是否继续？`;
      this.$confirm(tip, "红冲确认", { type: "warning" })
        .then(() => {
          const builders = {
            WMS_INBOUND: this.buildInboundReverse,
            WMS_OUTBOUND: this.buildOutboundReverse,
            WMS_STOCK_MOVE: this.buildMoveReverse,
          };
          const data = builders[this.functionName].call(this, src);
          save(this.functionName, { data }).then((res) => {
            const code = (res.data && res.data.code) || "";
            this.$message.success(`红冲单${code}已生成（制作中），请检查后提交生效`);
            this.$router.push({
              path: this.$route.path,
              query: { ...this.$route.query, mode: "edit", id: res.data.id },
            });
            this.init();
          });
        })
        .catch(() => {});
    },
    /** 移库红冲单（Q4）：源/目标仓位对调的反向移库，明细正值复制；台账反向转移由后端 afterPass 按 reverse 分支处理 */
    buildMoveReverse(src) {
      const fields = ["seq", "boxCode", "palletNo", "batchNo", "vatNo"];
      const details = (src.doc_stock_move_detail || []).map((row, i) => {
        const item = {};
        fields.forEach((f) => {
          if (row[f] !== undefined && row[f] !== null) item[f] = row[f];
        });
        item.seq = i + 1;
        item.toLocCode = src.fromLocCode;
        return item;
      });
      return {
        moveType: src.moveType,
        moveDate: this.fmtToday(),
        fromWarehouseId: src.toWarehouseId,
        fromLocCode: src.toLocCode,
        toWarehouseId: src.fromWarehouseId,
        toLocCode: src.fromLocCode,
        batchNo: src.batchNo,
        reverseFlag: 1,
        sourceMoveNo: src.code,
        sourceMoveId: src.id,
        syncStatus: 2,
        doc_stock_move_detail: details,
      };
    },
    /** 入库红冲单：明细正值复制（去 id/外键/行同步状态，序号重排）；台账负向由后端 afterPass 按 sign=-1 写入 */
    buildInboundReverse(src) {
      const fields = [
        "seq",
        "boxCode",
        "batchNo",
        "vatNo",
        "locCode",
        "colorCode",
        "colorName",
        "comp",
        "materialCode",
        "materialName",
        "yarn",
        "tubes",
        "stdWeight",
        "grossWeight",
        "netWeight",
      ];
      const details = (src.doc_inbound_detail || []).map((row, i) => {
        const item = {};
        fields.forEach((f) => {
          if (row[f] !== undefined && row[f] !== null) item[f] = row[f];
        });
        item.seq = i + 1;
        return item;
      });
      return {
        inboundType: src.inboundType,
        inboundDate: this.fmtToday(),
        warehouseId: src.warehouseId,
        warehouseName: src.warehouseName,
        batchNo: src.batchNo,
        vatNo: src.vatNo,
        workMode: src.workMode,
        erpDocNo: src.erpDocNo,
        sourceType: "WMS",
        reverseFlag: 1,
        sourceInboundNo: src.code,
        sourceInboundId: src.id,
        weight: src.weight != null ? -Math.abs(Number(src.weight) || 0) : null,
        boxes: src.boxes != null ? -Math.abs(Number(src.boxes) || 0) : null,
        syncStatus: 2,
        doc_inbound_detail: details,
      };
    },
    /** 出库红冲单：明细正值复制，库存回加与红冲校验由后端 afterPass/beforePass 按 reverse 分支处理 */
    buildOutboundReverse(src) {
      const fields = [
        "seq",
        "erpDocNo",
        "batchNo",
        "batchInfo",
        "vatNo",
        "boxCode",
        "boxes",
        "tubes",
        "pieces",
        "stdWeight",
        "grossWeight",
        "netWeight",
        "materialCode",
        "materialName",
        "yarn",
        "comp",
        "colorCode",
      ];
      const details = (src.doc_outbound_detail || []).map((row, i) => {
        const item = {};
        fields.forEach((f) => {
          if (row[f] !== undefined && row[f] !== null) item[f] = row[f];
        });
        item.seq = i + 1;
        return item;
      });
      return {
        outboundType: src.outboundType,
        outboundDate: this.fmtToday(),
        warehouseId: src.warehouseId,
        warehouseName: src.warehouseName,
        workMode: src.workMode,
        erpOrderNo: src.erpOrderNo,
        customerName: src.customerName,
        batchNo: src.batchNo,
        vatNo: src.vatNo,
        reverseFlag: 1,
        sourceOutboundNo: src.code,
        sourceOutboundId: src.id,
        weight: src.weight != null ? -Math.abs(Number(src.weight) || 0) : null,
        boxes: src.boxes != null ? -Math.abs(Number(src.boxes) || 0) : null,
        syncStatus: 2,
        doc_outbound_detail: details,
      };
    },
    /** 同步失败重发（后端以 ERP 单号判重，幂等） */
    handleResend() {
      reSendData(this.functionName, this.form.id).then(() => {
        this.$message.success("重发成功，已重新同步ERP");
        this.getData();
      });
    },
    fmtToday() {
      const d = new Date();
      const p = (n) => String(n).padStart(2, "0");
      return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
    },
  },
};
</script>

<style scoped>
.sub-table-box {
  margin: 8px 12px;
}
.sub-table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 12px 0 8px;
}
.sub-table-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
</style>
