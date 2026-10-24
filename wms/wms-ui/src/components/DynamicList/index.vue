<template>
  <div class="app-container">
    <!-- 按钮组 -->
    <el-row :gutter="5" class="button-group">
      <template v-if="openMode != 'dialog'">
        <el-col :span="1.5">
          <el-button
            v-if="buttonIsShow('add') && functionEditable"
            :disabled="buttonIsDisabled('add')"
            icon="el-icon-plus"
            size="mini"
            type="primary"
            @click="handleAdd"
            >新增
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            v-if="buttonIsShow('copy') && functionEditable"
            :disabled="buttonIsDisabled('copy')"
            icon="el-icon-document-copy"
            size="mini"
            @click="handleCopy"
            >复制
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            v-if="buttonIsShow('copy') && functionEditable"
            :disabled="buttonIsDisabled('copy')"
            icon="el-icon-document-copy"
            size="mini"
            @click="handleCopyAll"
            >关联复制
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            v-if="functionEditable"
            :disabled="single"
            icon="el-icon-edit"
            size="mini"
            @click="handleUpdate()"
            >编辑
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            v-if="buttonIsShow('deleted') && functionEditable"
            :disabled="buttonIsDisabled('deleted') || multiple"
            icon="el-icon-delete"
            size="mini"
            @click="handleDelete"
            >删除
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <ImportBtn v-if="functionEditable" />
        </el-col>
        <el-col :span="1.5">
          <ExportBtn
            :disabled="buttonIsShow('export')"
            :getMultiDataList="getMultiDataList"
            :getConditions="getConditions"
          />
        </el-col>
        <template v-if="isBusinessDoc">
          <el-col
            :span="1.5"
            v-if="buttonIsShow('push-down')"
            class="push-down"
          >
            <el-button
              v-if="buttonIsShow('push-down') && showPushDownBtn"
              :disabled="
                buttonIsDisabled('push-down') || disablePushDownOptions
              "
              icon="el-icon-bottom"
              size="mini"
              @click="handlePushDown()"
              >下推
            </el-button>
          </el-col>
          <el-col v-if="buttonIsShow('check')" :span="1.5">
            <CheckBtn
              :disabled="buttonIsDisabled('check')"
              :getMultiDataList="getMultiDataList"
            />
          </el-col>
          <!-- <el-col :span="1.5">
          <el-button
            v-if="buttonIsShow('updated')"
            :disabled="buttonIsDisabled('updated') || single"
            icon="el-icon-edit"
            size="mini"
            @click="handleUpdate"
            >编辑
          </el-button>
        </el-col> -->
          <el-col :span="1.5" v-if="buttonIsShow('relation-query')">
            <el-button
              icon="el-icon-help"
              size="mini"
              @click="handleRelationQuery"
              :disabled="buttonIsDisabled('relation-query') || ids.length == 0"
              >联查
            </el-button>
          </el-col>
          <el-col v-if="buttonIsShow('rollback')" :span="1.5">
            <el-button
              size="mini"
              icon="el-icon-refresh-right"
              :loading="buttonLoading"
              @click="rollbackForm"
              :disabled="buttonIsDisabled('rollback') || single"
              >撤 回</el-button
            >
          </el-col>
          <el-col v-if="buttonIsShow('business-operation')" :span="1.5">
            <BusinessOperationBtn
              :disabled="
                buttonIsDisabled('business-operation') || !isSameBillAndPass
              "
              :functionName="functionName"
              :multiDataList="multiDataList"
              :showTableHeaderBtn="true"
              :hiddenButtonList="hiddenButtonList"
            />
          </el-col>
          <el-col v-if="buttonIsShow('print-template')" :span="1.5">
            <!-- 动态生成按钮 -->
            <el-select
              v-if="printButtonList.length > 0"
              placeholder="打印模版"
              size="mini"
              @change="handlePrintSelect"
              value="打印模版"
              class="print-select"
            >
              <el-option
                v-for="item in printButtonList"
                :key="item.id"
                :label="item.buttonName"
                :value="item"
              ></el-option>
            </el-select>
          </el-col>
        </template>
      </template>

      <el-col v-if="openMode != 'dialog'" :span="1.5">
        <!-- 字段管理 -->
        <FieldManagement
          v-if="buttonIsShow('custom-column')"
          :functionName="functionName"
          :key="fieldKey"
          :options="options"
          :style="
            requestParams.length > 5
              ? 'padding: 0px;margin-right: 5px;margin-left: 16px;margin-top:10px;'
              : 'padding: 0px;margin-right: 5px;'
          "
          @setColumns="getColumns"
          @getList="getList"
        />
      </el-col>
      <el-col :span="1.5">
        <slot name="buttons"></slot>
      </el-col>
      <el-col :span="1.5" v-if="isSecondaryDialog" style="margin: 10px 0">
        <el-button
          icon="el-icon-plus"
          size="mini"
          type="primary"
          @click="handleSecondaryAdd"
          >新增联系人
        </el-button>
      </el-col>
    </el-row>


    <!-- 筛选组件 -->
    <el-row :gutter="5">
      <el-col :span="1.5">
        <SiftButton
          ref="siftButton"
          :handleQuery="handleQuery"
          :options="queryOptions"
          :openAdvancedQuery="isFilterScheme"
          :resetQuery="resetQuery"
          :onlyAcceptConditions="onlyAcceptConditions"
          :defaultScheme="defaultScheme"
        />
      </el-col>
      <el-col v-if="showCustomColumnAfterSiftButton" :span="1.5">
        <!-- 字段管理 -->
        <FieldManagement
          :functionName="dialogName || functionName"
          :key="fieldKey"
          :options="options"
          :style="
            requestParams.length > 5
              ? 'padding: 0px;margin-right: 5px;margin-left: 16px;margin-top:10px;'
              : 'padding: 0px;margin-right: 5px;'
          "
          @setColumns="getColumns"
          @getList="getList"
        />
      </el-col>
      <el-col v-if="openMode == 'dialog'" :span="1.5">
        <columnManagement
          :functionName="dialogName || functionName"
          @getFieldConfig="getFieldConfig"
          :dictDataList="dictDataList"
          @get-list="getList"
          v-has-role="['admin']"
        ></columnManagement>
      </el-col>
      <el-col :span="1.5">
        <columnManagement
          :functionName="dialogName || functionName"
          @getFieldConfig="getFieldConfig"
          :dictDataList="dictDataList"
          @get-list="getList"
          v-has-role="['admin']"
        ></columnManagement>
      </el-col>
      <el-col :span="1.5">
        <slot name="second-buttons"></slot>
      </el-col>
    </el-row>


    <!-- 过滤方案 -->
    <FilterScheme
      v-if="isFilterScheme"
      @setDefaultScheme="setDefaultScheme"
      :options="queryOptions"
      :functionName="dialogName || functionName"
    />
    <!-- 列表 -->
    <el-row style="margin: 0px" :class="[isTree ? 'contnet' : '']">
      <!-- 左侧树形筛选 -->
      <el-col
        v-if="isTree"
        :span="4"
        :xs="24"
        :id="[isTree ? 'left-tree' : '']"
        :class="['tree-box', { 'left-tree': isTree }]"
      >
        <div v-if="openMode == 'list'" class="head-container">
          <!-- <el-input
            v-model="treeFilterInput"
            placeholder="请输入分组名称"
            clearable
            size="mini"
            prefix-icon="el-icon-search"
            style="margin-bottom: 20px"
          /> -->
          <el-button
            size="mini"
            style="margin-left: 0px"
            type="text"
            @click="addTreeHandler"
            >新增分组
          </el-button>
          <el-button
            size="mini"
            style="margin-left: 0px"
            type="text"
            @click="editTreeHandler"
            >编辑分组
          </el-button>
          <el-button
            size="mini"
            style="margin-left: 0px"
            type="text"
            @click="deleteTreeHandler"
            >删除分组
          </el-button>
        </div>

        <!-- 树形组件 -->
        <!-- :data="treeOptions" -->
        <div
          :class="[openMode == 'list' ? 'tree-container' : 'tree-container2']"
        >
          <el-tree
            ref="tree"
            :default-expanded-keys="['0']"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            :load="loadNode"
            :props="treeProps"
            highlight-current
            lazy
            node-key="id"
            @node-click="handleNodeClick"
            :key="treeKey"
            class="tree-group"
          >
            <span slot-scope="{ data }">
              {{ data.id == '0' ? data.cn_name : (data.code ? data.cn_name + ' ' + data.code : data.cn_name) }}
            </span>
          </el-tree>
        </div>

        <div id="resize" class="resize" title="收缩侧边栏"></div>
      </el-col>
      <!-- 右侧列表展示 -->
      <el-col
        :id="[isTree ? 'right-content' : '']"
        :class="[isTree ? 'right-content' : '']"
        :span="isTree ? 20 : 24"
        :xs="24"
      >
        <!-- 单选列表 -->
        <template v-if="openRadioSelection">
          <!-- 小数据量：原生表格 -->
          <el-table
            v-if="!useVirtualScroll"
            :key="datekey"
            ref="singleTable"
            v-loading="loading"
            :data="dataList"
            :header-cell-style="{
              height: 'auto',
              padding: '2px 0',
            }"
            :row-class-name="getRowClassName"
            :show-summary="showSummary"
            :summary-method="getSummaries"
            :row-key="getRowKey"
            border
            class="table-container"
            :height="listTableHeight"
            @current-change="handleCurrentChange"
            @row-click="handleRowClick"
            @row-dblclick="handleRowDblClick"
          >
            <el-table-column
              align="center"
              label="序号"
              type="index"
              width="55"
            />
            <el-table-column
              v-for="item in finallyColumnsList"
              :key="item.id"
              :align="isTableColumnAlign(item)"
              :fixed="isTableColumnFixed(item)"
              :label="item.headerText"
              :prop="item.columnName"
              :sortable="isTableColumnSortable(item)"
              :width="processWidth(item.columnWidth)"
              class="textLeft"
              min-width="50"
              show-overflow-tooltip
            >
              <template slot="header" slot-scope="scoped">
                <span>{{ scoped.column.label }}</span>
                <TableHeaderFilter
                  :column="scoped.column"
                  :options="headerFilterOptions[scoped.column.property] || []"
                  @visible-change="onFilterPopoverVisibleChange(scoped.column)"
                  @confirm="handleHeaderFilter"
                />
                <template v-if="showHeaderFilter">
                  <br />
                  <el-input
                    class="header-filter-input"
                    :key="item.columnName"
                    type="text"
                    @change="handleEnter(item.columnName)"
                    v-model="filterInputList[item.columnName]"
                    @clear="handleEnter(item.columnName)"
                    clearable
                  ></el-input>
                </template>
              </template>
              <template slot-scope="scoped">
                <el-popover
                  v-if="item.popover == 1"
                  placement="bottom"
                  width="200"
                  trigger="click"
                  :content="scoped.row[item.columnName] + ''"
                  style="max-height: 500px; overflow: scroll"
                >
                  <div
                    slot="reference"
                    :class="{ 'is-deficit': scoped.row[item.columnName] < 0 }"
                  >
                    {{ scoped.row[item.columnName] }}
                  </div>
                </el-popover>
                <template v-else>
                  <div
                    :class="{ 'is-deficit': scoped.row[item.columnName] < 0 }"
                  >
                    {{ scoped.row[item.columnName] }}
                  </div>
                </template>
              </template>
            </el-table-column>
          </el-table>
          <!-- 大数据量：虚拟滚动 -->
          <!-- INFO:
            key-prop 如果有一些地方没有 id 我们可能要考虑将 key-prop 这个参数作为可配置项
            否则会出现问题。
          -->
          <virtual-scroll
            v-else
            :key="datekey + 'virtual'"
            :data="dataList"
            :item-size="23"
            :key-prop="virtualKeyProp"
            :buffer="100"
            :dynamic="true"
            :throttleTime="60"
            @change="virtualList = $event"
          >
            <el-table
              ref="singleTable"
              v-loading="loading"
              :data="virtualList"
              :header-cell-style="{
                height: 'auto',
                padding: '2px 0',
              }"
              :row-class-name="getRowClassName"
              :show-summary="showSummary"
              :summary-method="getSummaries"
              :row-key="getRowKey"
              border
              class="table-container"
              :height="listTableHeight"
              @current-change="handleCurrentChange"
              @row-click="handleRowClick"
              @row-dblclick="handleRowDblClick"
            >
              <virtual-column
                align="center"
                label="序号"
                type="index"
                width="55"
              />
              <virtual-column
                v-for="item in finallyColumnsList"
                :key="item.id"
                :align="isTableColumnAlign(item)"
                :fixed="isTableColumnFixed(item)"
                :label="item.headerText"
                :prop="item.columnName"
                :sortable="isTableColumnSortable(item)"
                :width="processWidth(item.columnWidth)"
                class="textLeft"
                min-width="50"
                show-overflow-tooltip
              >
                <template slot="header" slot-scope="scoped">
                  <span>{{ scoped.column.label }}</span>
                  <TableHeaderFilter
                    :column="scoped.column"
                    :options="headerFilterOptions[scoped.column.property] || []"
                    @visible-change="
                      onFilterPopoverVisibleChange(scoped.column)
                    "
                    @confirm="handleHeaderFilter"
                  />
                  <template v-if="showHeaderFilter">
                    <br />
                    <el-input
                      class="header-filter-input"
                      :key="item.columnName"
                      type="text"
                      @change="handleEnter(item.columnName)"
                      v-model="filterInputList[item.columnName]"
                      @clear="handleEnter(item.columnName)"
                      clearable
                    ></el-input>
                  </template>
                </template>
                <template slot-scope="scoped">
                  <el-popover
                    v-if="item.popover == 1"
                    placement="bottom"
                    width="200"
                    trigger="click"
                    :content="scoped.row[item.columnName] + ''"
                    style="max-height: 500px; overflow: scroll"
                  >
                    <div
                      slot="reference"
                      :class="{ 'is-deficit': scoped.row[item.columnName] < 0 }"
                    >
                      {{ scoped.row[item.columnName] }}
                    </div>
                  </el-popover>
                  <template v-else>
                    <div
                      :class="{ 'is-deficit': scoped.row[item.columnName] < 0 }"
                    >
                      {{ scoped.row[item.columnName] }}
                    </div>
                  </template>
                </template>
              </virtual-column>
            </el-table>
          </virtual-scroll>
        </template>
        <!-- 多选列表 -->
        <template v-else>
          <!-- 小数据量：原生表格 -->
          <el-table
            v-if="!useVirtualScroll"
            :key="datekey2"
            ref="table"
            v-loading="loading"
            :data="dataList"
            :show-summary="showSummary"
            :summary-method="getSummaries"
            :header-cell-style="{ height: 'auto', padding: '2px 0' }"
            :row-class-name="getRowClassName"
            :row-key="getRowKey"
            border
            class="table-container"
            :height="listTableHeight"
            @selection-change="handleSelectionChange"
            @select="onSelect"
            @select-all="onSelectAll"
            @row-click="handleRowClick"
            @row-dblclick="handleRowDblClick"
          >
            <el-table-column
              align="center"
              type="selection"
              width="55"
              :reserve-selection="reserveSelection"
            />
            <el-table-column
              v-for="item in finallyColumnsList"
              :key="item.id"
              :align="isTableColumnAlign(item)"
              :fixed="isTableColumnFixed(item)"
              :label="item.headerText"
              :prop="item.columnName"
              :sortable="isTableColumnSortable(item)"
              :width="item.columnWidth"
              min-width="50"
              show-overflow-tooltip
              :column-key="item.columnName"
              class="columnIcon"
            >
              <!-- :filters="setFilters(item)"
            :filter-method="filterHandler" -->
              <template slot="header" slot-scope="scoped">
                <span>{{ scoped.column.label }} </span>
                <TableHeaderFilter
                  :column="scoped.column"
                  :options="headerFilterOptions[scoped.column.property] || []"
                  @visible-change="onFilterPopoverVisibleChange(scoped.column)"
                  @confirm="handleHeaderFilter"
                />

                <template v-if="showHeaderFilter">
                  <br />
                  <el-input
                    :key="item.columnName"
                    type="text"
                    @change="handleEnter(item.columnName)"
                    v-model="filterInputList[item.columnName]"
                    @clear="handleEnter(item.columnName)"
                    clearable
                  ></el-input>
                </template>
              </template>
            </el-table-column>
          </el-table>

          <!-- 大数据量：虚拟滚动 -->
          <virtual-scroll
            v-else
            :key="datekey2 + 'virtual'"
            :data="dataList"
            :item-size="23"
            :key-prop="virtualKeyProp"
            :buffer="100"
            :dynamic="true"
            @change="virtualList = $event"
            :throttleTime="120"
          >
            <el-table
              ref="table"
              v-loading="loading"
              :data="virtualList"
              :show-summary="showSummary"
              :summary-method="getSummaries"
              :header-cell-style="{ height: 'auto', padding: '2px 0' }"
              :row-class-name="getRowClassName"
              :row-key="getRowKey"
              border
              class="table-container"
              :height="listTableHeight"
              @selection-change="handleSelectionChange"
              @select="onSelect"
              @select-all="onSelectAll"
              @row-click="handleRowClick"
              @row-dblclick="handleRowDblClick"
            >
              <virtual-column
                align="center"
                type="selection"
                width="55"
                :reserve-selection="reserveSelection"
              />
              <virtual-column
                v-for="item in finallyColumnsList"
                :key="item.id"
                :align="isTableColumnAlign(item)"
                :fixed="isTableColumnFixed(item)"
                :label="item.headerText"
                :prop="item.columnName"
                :sortable="isTableColumnSortable(item)"
                :width="item.columnWidth"
                min-width="50"
                show-overflow-tooltip
                :column-key="item.columnName"
                class="columnIcon"
              >
                <template slot="header" slot-scope="scoped">
                  <span>{{ scoped.column.label }} </span>
                  <TableHeaderFilter
                    :column="scoped.column"
                    :options="headerFilterOptions[scoped.column.property] || []"
                    @visible-change="
                      onFilterPopoverVisibleChange(scoped.column)
                    "
                    @confirm="handleHeaderFilter"
                  />

                  <template v-if="showHeaderFilter">
                    <br />
                    <el-input
                      :key="item.columnName"
                      type="text"
                      @change="handleEnter(item.columnName)"
                      v-model="filterInputList[item.columnName]"
                      @clear="handleEnter(item.columnName)"
                      clearable
                    ></el-input>
                  </template>
                </template>
              </virtual-column>
            </el-table>
          </virtual-scroll>
        </template>
      </el-col>
    </el-row>


    <slot name="bottom"></slot>
    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :limit.sync="queryParams.pageSize"
      :page.sync="queryParams.pageNum"
      :total="total"
      @pagination="getList"
    >
      <template #checkNum v-if="!openRadioSelection && openMode == 'list'"
        ><span style="position: relative; top: 6px"
          ><el-link :underline="false">
            当前选中了<span>&emsp;{{ multiDataList.length }}&emsp;</span>行数据
          </el-link>
        </span></template
      >
    </pagination>
    <!-- 二级弹窗 -->
    <!-- backwardFun 子组件传过来新增的值 -->
    <SecondaryDialog
      v-if="isSecondaryDialog"
      ref="secondaryDialog"
      :parentForm="form"
      @backwardFun="backwardFun"
    >
    </SecondaryDialog>

    <!-- 联查弹窗 -->
    <RelationQuery
      v-if="showRelationQuery"
      :functionName="functionName"
      :showRelationQuery="showRelationQuery"
      @closeDialog="showRelationQuery = false"
      :relationQueryId="relationQueryId"
      :selectedRow="selectedRow"
      :selectDetailIds="selectDetailIds"
    ></RelationQuery>
  </div>
</template>

<script>
import { getDictDataList } from "@/api/system/dict/data";
import { radioList, pushDownList } from "@/utils/variables.js";
import columnManagement from "@/components/columnManagement/index.vue";
import FieldManagement from "@/components/QueryForm/FieldManagement";
import DynamicQuery from "@/components/DynamicQuery/index.vue";
import SiftButton from "@/components/SiftButton/index.vue";
import { getPrintButton, rollBackData } from "@/api/system/common";
import qs from "qs";
import _ from "lodash";
import { checkPushDown } from "@/api/system/common";
import FilterScheme from "@/components/FilterScheme/index.vue";
import BusinessOperationBtn from "./BusinessOperationBtn.vue";
import TableHeaderFilter from "./TableHeaderFilter.vue";
import { checkPermi, checkRole } from "@/utils/permission"; // 权限判断函数
import {
  getFieldConfigList,
  getTableList,
  deleteById,
  getCopyData,
  getColumnConfig,
  saveColumnConfig,
} from "@/api/system/common";
import CheckBtn from "@/components/Btn/CheckBtn/index.vue";
import ExportBtn from "@/components/Btn/ExportBtn/index.vue";
import ImportBtn from "@/components/Btn/ImportBtn/index.vue";
import { VirtualScroll, VirtualColumn } from "el-table-virtual-scroll";
import virtualTableConfig from "@/config/virtual-table";

const isTrue = {
  NO: 0,
  YES: 1,
};

// 只读功能（台账类，数据由作业生成，不提供手工新增/编辑/删除/导入）
const READONLY_FUNCTIONS = ["INV_BATCH", "BOX_INFO", "PALLET_INFO", "WMS_RECEIVE"];
export default {
  name: "DynamicList",
  components: {
    FieldManagement,
    DynamicQuery,
    SiftButton,
    columnManagement,
    FilterScheme,
    BusinessOperationBtn,
    CheckBtn,
    ExportBtn,
    ImportBtn,
    VirtualScroll,
    VirtualColumn,
    TableHeaderFilter: TableHeaderFilter,
    RelationQuery: () => import("@/components/RelationQuery/index.vue"),
  },
  props: {
    // 展示筛选组件后面的显示隐藏列按钮
    showCustomColumnAfterSiftButton: {
      type: Boolean,
      default: () => false,
    },
    isFilterScheme: {
      type: Boolean,
      default: () => true,
    },
    isSecondaryDialog: {
      type: Boolean,
      default: false,
    },
    form: {
      type: Object,
      default: () => {},
    },
    // 显示按钮list
    // ['add','copy','push-down','deleted','updated','export','change','change-history','relation-query','business-operation','cancel','closed','print-template','custom-column']
    hiddenButtonList: {
      type: Array,
      default: () => [],
    },
    // 禁用按钮
    // ['add','copy','push-down','deleted','updated','export']
    disabledButtonList: {
      type: Array,
      default: () => [],
    },
    // 自定义添加按钮事件
    handleCustomAdd: {
      type: Function,
      default: null,
    },
    // 自定义修改按钮事件
    handleCustomUpdate: {
      type: Function,
      default: null,
    },
    // 自定义删除按钮事件
    handleCustomDelete: {
      type: Function,
      default: null,
    },
    // 是否开启单选功能
    openRadioSelection: {
      type: Boolean,
      default: () => true,
    },
    buttons: {
      type: Array, // button数据
      default: () => [],
    },
    // 模块名称
    functionName: {
      type: String,
      required: true,
    },
    // 弹窗名称
    dialogName: {
      type: String,
      default: (val) => null,
    },
    // 打开组件的模式 list dialog
    openMode: {
      type: String,
      default: "list",
    },
    // 自定义搜索
    // 例子：[{
    //     "logicalOperator": "OR",
    //     "fieldType": "TEXT",
    //     "field": "purpose_id",
    //     "operate": "LIKE",
    //     "value": "11"
    // }]
    // 自定义搜索条件
    customizeConditions: {
      type: Array,
      default: () => [],
    },
    isTree: {
      type: Boolean,
      default: () => false,
    },
    // 树形默认Props
    treeProps: {
      type: Object,
      default: () => ({
        children: "children",
        label: "cn_name",
        isLeaf: "leaf",
      }),
    },
    // 树形结构的模块名
    treeFunctionName: {
      type: String,
      default: () => "",
    },
    // 树形结构主键字段
    treePrimaryKey: { type: String, default: () => "id" },
    // 外键字段（列表里面，关联树的id）
    foreignKey: { type: String, default: () => "parent_id" },
    // 树形结构外键字段（树的上级id）
    treeForeignKey: { type: String, default: () => "parent_id" },
    // 树形的打开链接
    treePath: {
      type: String,
    },
    // 左侧树的默认查询参数 只有在本身页面为分组（如物料分组）时不做筛选
    treeDefaultParams: {
      type: Array,
      default: () => [
        {
          logicalOperator: "AND",
          fieldType: "TEXT",
          field: "pact_state",
          operate: "EQ",
          value: "通过",
          filterGroup: "ADVANCE",
        },
        {
          logicalOperator: "AND",
          fieldType: "TEXT",
          field: "enabled",
          operate: "EQ",
          value: "是",
          filterGroup: "ADVANCE",
        },
      ],
    },
    // 树形模糊查询的字段
    // [{'key':'code','value':'code'}]
    treeLikeFieldArray: {
      type: Array,
      default: () => null,
    },
    // 在弹窗模式下是否单击行返回数据
    singleClickReturnData: {
      type: Boolean,
      default: () => false,
    },
    // table的高度
    tableHeight: {
      type: String,
      default: () => `calc(100vh - 220px)`,
    },
    // 打开数据列表时 已选择的数据设置为勾选状态
    reserveSelection: {
      type: Boolean,
      default: () => false,
    },
    // 回显的数据
    echoRowDataList: {
      type: [Array, String],
      default: () => [],
    },
    // 变更单的functionName
    changeOrderFN: {
      type: String,
      default: () => null,
    },
    // 变更单的路径
    changeOrderPath: {
      type: String,
      default: () => null,
    },
    // 只用传入的筛选条件
    onlyAcceptConditions: {
      type: Boolean,
      default: () => false,
    },
    // 联查的路径
    relationQueryPath: {
      type: String,
      default: () => "",
    },
    // 显示表头过滤输入框
    showHeaderFilter: {
      type: Boolean,
      default: () => false,
    },
    // 上选 多选时,需要数据中的key对应的值一致才能选择
    needCompareKeys: {
      type: Array,
      default: () => [],
    },
    // 双击列表打开新页面
    doubleClickOpenNewPage: {
      type: Boolean,
      default: () => false,
    },
    // 是否显示合计
    showSummary: {
      type: Boolean,
      default: () => false,
    },
    // 单选弹框保存前的校验
    validateRadioSelection: {
      type: Function,
      default: null,
    },
    // 获取列表后
    afterGetList: {
      type: Function,
      default: () => {},
    },
    // 传入的自定义默认查询条件
    extraDefaultSchemeCallBack: {
      type: Function,
      default: () => [],
    },
    // 弹窗模式下是否显示页面大小选择器（用于调整弹窗宽高）
    showPageSizeSelect: {
      type: Boolean,
      default: false,
    },
  },
  mounted() {
    // 初始化收缩侧边栏
    this.initDragResize();
    // 弹窗模式下，根据弹窗当前尺寸同步页面大小选择器状态（弹窗内联样式在重新打开后会保留）
    if (this.showPageSizeSelect && this.openMode == "dialog") {
      this.syncDialogSizeState();
    }
  },
  data() {
    return {
      // 记录每列选中的值
      activeHeaderFilters: {},
      // 存放表头搜索项，key为property，value是过滤选项数组
      headerFilterOptions: {},
      // 存放搜索条件
      filters: [],
      // 存放过滤输入框中的值
      filterInputList: [],
      dicts: [
        "t_date_range",
        "field_type",
        "field_formatter_type",
        "field_align_type",
      ],
      radioList: radioList,
      // 按钮loading
      buttonLoading: false,
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 表格数据
      // 业务数据 - 用于查找、导出、过滤等业务操作
      // 无过滤时 = _dataList，有过滤时 = 从 _dataList 筛选得出
      dataList: [],
      // 数据源备份 - API 返回的原始完整数据（使用 Object.freeze 冻结，防止修改）
      _dataList: [],
      // 虚拟滚动渲染的数据
      virtualList: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 200,
      },
      requestParams: [],
      selectedItem: null,
      datekey: 0,
      datekey2: 0,
      paginationkey: 0,
      fieldKey: Date.now(),
      // 列表最终显示的字段
      // finallyColumnsList: [],
      // 接受后端的字段配置，字段显示配置
      options: [],
      // 接受后端的字段配置，查询配置
      queryOptions: [],
      // 默认查询方案
      defaultScheme: [],
      transfers: [],
      queryList: [],
      // 存放多选数据
      multiDataList: [],
      // 打印按钮列表
      printButtonList: [],
      // 树形过滤input
      treeFilterInput: "",
      // 树形数据
      treeOptions: [],
      // 存放单选数据
      radioData: null,
      // 存放树形节点，选中的数据
      selectedTreeItem: null,
      dictDataList: {},
      treeKey: Date.now(),
      // 在回显已选择的多选框时，toggleRowSelection会触发handleSelectionChange，导致报错，用变量来阻断此时的事件
      echoFlag: true,
      // 变更历史弹窗是否显示
      changeHistoryVisible: false,
      // 展示联查弹窗
      showRelationQuery: false,
      relationQueryId: "",
      // 当前选中行
      selectedRow: [],
      // 下推列表
      pushDownList: pushDownList,
      summaryFieldMap: this.variables.summaryFieldMap,
      // 显示下推按钮的函数
      shwoPushDownFns: [],
      // 下推对象
      pushDownObj: "",
      selectDetailIds: [],
      // 页面权限字符串
      perms: null,
      // 所有按钮权限字符串
      allBtnPermsStrList: this.variables.allBtnPermsStrList,
      commonSummaryFieldList: ["amount", "qty"], // 需要计算的字段
      // ===== 弹窗页面大小选择 =====
      // 页面大小类型：default(默认60%) / medium(75%) / large(90%) / full(全屏) / custom(自定义)
      dialogSizeType: "default",
      // 自定义宽度（单位固定为%，只填数值，如 90）
      customDialogWidth: "",
      // 自定义高度（单位固定为vh，只填数值，如 80；为空则高度自适应）
      customDialogHeight: "",
      // 弹窗设置固定高度后，表格高度随之调整（为空则使用传入的 tableHeight）
      dialogTableHeight: "",
    };
  },
  watch: {
    // 根据名称筛选部门树
    treeFilterInput(val) {
      this.$refs.tree.filter(val);
    },
    // 监听页面权限字符串，生成按钮权限
    perms(newVal, oldVal) {
      if (newVal == null || this.openMode == "dialog") {
        return;
      }
      this.allBtnPermsStrList.forEach((item) => {
        const perms = `${newVal}:${item}`;
        if (!this.checkPermi([perms])) {
          this.hiddenButtonList.push(item);
        }
      });
    },
    // 监听多选的数据，判断是否禁用按钮
    multiDataList(newVal, oldVal) {
      // 过滤出isSkipRow为true的数据
      const filterList = newVal.filter((item) => !item?.isSkipRow);
      console.log("filterList", filterList);

      // 是否存在比较多选的值
      if (this.needCompareKeys.length) {
        const obj = {};
        filterList.forEach((el) => {
          this.needCompareKeys.forEach((key) => {
            if (!obj[key]) obj[key] = [];
            if (!obj[key].includes(el[key])) obj[key].push(el[key]);
          });
        });

        const upLookDisabled = Object.values(obj).some((el) => el.length !== 1);
        this.$emit("setBtnDisable", upLookDisabled);
      }
    },
    "queryParams.pageSize"(newVal, oldVal) {
      const storageKey = this.getPageSizeLocalStoreKey();
      // 保存到 localStorage
      localStorage.setItem(storageKey, newVal.toString());
    },
  },
  created() {
    console.log("执行了DynamicList的created函数");
    // 获取数据字典数据
    this.getDictData();
    // 获取传入的模块名称
    this.queryParams.functionName = this.functionName;
    console.log("【DynamicList中传入的functionName】:",this.functionName);
    // 获取字段配置数据
    this.getFieldConfig();
    this.rightcheckchange();
    // 获取打印按钮数据
    this.getPrintButtonList();
    // 初始化监听localStorage
    this.addStorageEventListener();
    // 判断是否开启按钮权限
    if (this.$route.meta?.isOpenBtnPerms == 1) {
      // 获取页面权限字符串
      this.perms = this.$route.meta.perms;
    }
    // 保存回显数据
    if (this.reserveSelection) {
      this.multiDataList.push(...this.echoRowDataList);
    }
    // 从 localStorage 恢复 pageSize 设置
    this.restorePageSizeFromStorage();
  },

  beforeDestroy() {
    this.removeStorageEventListener();
  },
  computed: {
    // 当前功能是否允许手工维护（台账类只读）
    functionEditable() {
      const fn = this.$route.query.functionName || this.functionName;
      return !READONLY_FUNCTIONS.includes(fn);
    },
    // 列表最终显示的字段
    finallyColumnsList: function () {
      console.log(this.options, "this.options");

      return this.options.filter(
        (item) => item.columnWidth > 0 && item.isShow == 1
      );
    },
    // 表格高度：弹窗设置固定高度时随弹窗高度变化，否则使用传入的 tableHeight
    listTableHeight: function () {
      return this.dialogTableHeight || this.tableHeight;
    },
    rollBackBtnIsShow: function () {
      if (!this.single) {
        return true;
      }
      return (
        this.dataList.find((item) => item.id == this.ids[0])?.status == "PASS"
      );
    },
    // 虚拟滚动阈值（可配置）
    virtualThreshold() {
      const config = virtualTableConfig.moduleConfig[this.functionName];
      return config?.threshold || virtualTableConfig.defaultThreshold;
    },
    // 是否使用虚拟滚动
    useVirtualScroll() {
      return this.dataList.length >= this.virtualThreshold;
    },
    // 虚拟滚动 key-prop：固定使用 "uid"，确保 key 稳定
    virtualKeyProp() {
      return "uid";
    },
    // 下推禁用条件
    disablePushDownOptions() {
      // 单据状态为通过
      // 已关闭/已取消的单据 不允许下推
      // 是否取消 0正常 1取消
      // 是否关闭 1、2为自动手动关闭 0开启
      let selectData = [];
      let singleDataFns = []; // 销售出库单为单选,选择的为对象,使用数组方法报错,所以构造为数组
      if (singleDataFns.includes(this.functionName)) {
        selectData = this.radioData ? [this.radioData] : [];
      } else {
        selectData = this.multiDataList;
      }

      let isPassStatus = selectData.every((el) => {
        if (
          this.hiddenButtonList.includes("cancel") &&
          this.hiddenButtonList.includes("close")
        ) {
          return el.pact_state == "通过";
        } else {
          return (
            el.pact_state == "通过" &&
            el.is_cancel == 0 &&
            el.is_cancel_detail == 0 &&
            (el.is_closed == 0 || el.is_closed == 2) &&
            (el.is_closed_detail == 0 || el.is_closed_detail == 2)
          );
        }
      });

      // 找到要求相同的key
      let currentObj = this.pushDownList.find(
        (el) => el.currentFn == this.functionName
      );
      // 没有则直接禁用
      if (!currentObj) return true;
      let requireKeys = currentObj.requireSameKeys;
      let isSingle = currentObj.isSingle;
      // 根据key构建对应的key值数组
      let requireSameObj = {};
      selectData.forEach((el) => {
        requireKeys.forEach((key) => {
          if (!requireSameObj[key]) requireSameObj[key] = [];
          requireSameObj[key].push(el[key]);
        });
      });
      // key值数组长度都为1 代表值都相同
      let isSameVals = Object.values(requireSameObj).every(
        (el) => new Set(el).size === 1
      );


      console.log("isSingle", isSingle, this.isSameBillAndPass, selectData);
      if (isSingle) {
        if (isPassStatus && this.isSameBillAndPass) {
          return false;
        } else {
          return true;
        }
      } else if (isPassStatus && selectData.length && isSameVals) {
        return false;
      } else {
        return true;
      }
    },
    //  显示下推按钮
    showPushDownBtn() {
      const { functionName } = this.$route.query;
      return this.shwoPushDownFns.includes(functionName);
    },
    // 返回过滤条件
    filterInputCondition() {
      return this.filters.map((item) => ({
        logicalOperator: "AND",
        fieldType: "TEXT",
        field: item.field,
        operate: "LIKE",
        value: item.value,
        filterGroup: "ADVANCE",
      }));
    },
    // 是否是业务单据
    isBusinessDoc() {
      return this.$route.meta.isBusinessDoc == 1;
    },
    // 判断是否同一单据
    isSameBill() {
      const list = this.multiDataList;
      if (list.length <= 0) return false;
      // 通过main_id进行判断是否选择的是 不同单据
      let firstMainId = list[0].main_id;
      return list.find((item) => item.main_id != firstMainId) ? false : true;
    },
    // 判断是否同一单据
    isSameBillAndPass() {
      return this.isSameBill && this.multiDataList[0].pact_state == "通过";
    },
    summaryFieldList() {
      let list = this.summaryFieldMap[this.functionName] || [];
      return [...list, ...this.commonSummaryFieldList];
    },
  },
  methods: {
    checkPermi,
    checkRole,
    // ===== 弹窗页面大小调整 =====
    // 获取当前组件所在的 el-dialog 元素
    getDialogEl() {
      return this.$el ? this.$el.closest(".el-dialog") : null;
    },
    // 将页面大小状态同步给父组件（选择器 UI 渲染在 DynamicDialogBtn 弹窗页脚）
    emitSizeState() {
      this.$emit("size-state-change", {
        dialogSizeType: this.dialogSizeType,
        customDialogWidth: this.customDialogWidth,
        customDialogHeight: this.customDialogHeight,
      });
    },
    // 页面大小下拉框选择事件
    handleDialogSizeChange(val) {
      this.dialogSizeType = val;
      const widthMap = {
        default: "60%",
        medium: "75%",
        large: "90%",
        full: "98%",
      };
      if (val == "custom") {
        this.handleCustomSizeChange();
        return;
      }
      this.applyDialogSize(widthMap[val], "");
      this.emitSizeState();
    },
    // 自定义宽高输入事件（宽固定单位%、高固定单位vh，只填数值）
    handleCustomSizeChange() {
      const widthNum = parseFloat(
        (this.customDialogWidth || "").toString().trim()
      );
      const heightNum = parseFloat(
        (this.customDialogHeight || "").toString().trim()
      );
      if (
        this.customDialogWidth &&
        (isNaN(widthNum) || widthNum < 10 || widthNum > 100)
      ) {
        this.$message.warning("宽度需为 10-100 之间的数值（单位 %）");
        return;
      }
      if (
        this.customDialogHeight &&
        (isNaN(heightNum) || heightNum < 20 || heightNum > 100)
      ) {
        this.$message.warning("高度需为 20-100 之间的数值（单位 vh）");
        return;
      }
      const width = this.customDialogWidth ? `${widthNum}%` : "";
      const height = this.customDialogHeight ? `${heightNum}vh` : "";
      this.applyDialogSize(width, height);
      this.emitSizeState();
    },
    // 将宽高应用到弹窗上（宽度支持 %/px/vw，高度支持 vh/px/%）
    applyDialogSize(width, height) {
      const dialogEl = this.getDialogEl();
      if (!dialogEl) return;
      const bodyEl = dialogEl.querySelector(".el-dialog__body");
      if (width) {
        dialogEl.style.width = width;
        // 宽度变化后重新水平居中（与 v-dialogDrag 的居中逻辑保持一致）
        let widthPx = parseFloat(width);
        if (width.includes("%")) {
          widthPx = (document.body.clientWidth * parseFloat(width)) / 100;
        } else if (width.includes("vw")) {
          widthPx =
            (document.documentElement.clientWidth * parseFloat(width)) / 100;
        }
        dialogEl.style.left = `${(document.body.clientWidth - widthPx) / 2}px`;
      }
      if (height) {
        // 固定高度：弹窗改为纵向 flex，body 弹性填充并可滚动，表格高度随之调整
        dialogEl.style.height = height;
        dialogEl.style.display = "flex";
        dialogEl.style.flexDirection = "column";
        if (bodyEl) {
          bodyEl.style.flex = "1";
          bodyEl.style.overflow = "auto";
        }
        this.dialogTableHeight = `calc(${height} - 220px)`;
      } else {
        dialogEl.style.height = "";
        dialogEl.style.display = "";
        dialogEl.style.flexDirection = "";
        if (bodyEl) {
          bodyEl.style.flex = "";
          bodyEl.style.overflow = "";
        }
        this.dialogTableHeight = "";
      }
    },
    // 弹窗重新打开时（组件重建），根据弹窗当前内联样式同步选择器状态
    syncDialogSizeState() {
      const dialogEl = this.getDialogEl();
      if (!dialogEl) return;
      const width = dialogEl.style.width || "";
      const height = dialogEl.style.height || "";
      if (!height && (width == "60%" || width == "")) {
        this.dialogSizeType = "default";
        this.emitSizeState();
        return;
      }
      // 宽度统一换算成百分比后匹配预设
      let widthPct;
      if (width.includes("%")) {
        widthPct = parseFloat(width);
      } else if (width.includes("px")) {
        widthPct = (parseFloat(width) / document.body.clientWidth) * 100;
      } else {
        widthPct = 60;
      }
      widthPct = Math.round(widthPct);
      const presetMap = { 75: "medium", 90: "large", 98: "full" };
      if (!height && presetMap[widthPct]) {
        this.dialogSizeType = presetMap[widthPct];
        return;
      }
      this.dialogSizeType = "custom";
      this.customDialogWidth = widthPct;
      // 高度换算成 vh 数值回显（% 数值近似等于 vh，px 需要换算）
      if (height) {
        if (height.includes("px")) {
          this.customDialogHeight = Math.round(
            (parseFloat(height) / document.documentElement.clientHeight) * 100
          );
        } else {
          this.customDialogHeight = Math.round(parseFloat(height));
        }
      } else {
        this.customDialogHeight = "";
      }
      if (height) {
        // 恢复固定高度相关的样式与表格高度
        dialogEl.style.display = "flex";
        dialogEl.style.flexDirection = "column";
        const bodyEl = dialogEl.querySelector(".el-dialog__body");
        if (bodyEl) {
          bodyEl.style.flex = "1";
          bodyEl.style.overflow = "auto";
        }
        this.dialogTableHeight = `calc(${height} - 220px)`;
      }
      this.emitSizeState();
    },
    initDragResize() {
      if (!this.isTree) {
        return;
      }
      const left = document.querySelector(".left-tree");
      const resize = document.querySelector("#resize");
      const right = document.querySelector(".right-content");
      let isDragging = false;

      // 鼠标按下事件
      resize.addEventListener("mousedown", (e) => {
        e.preventDefault();
        isDragging = true;
        const startX = e.clientX;
        const startWidth = left.offsetWidth;

        // 鼠标移动事件
        document.addEventListener("mousemove", (e) => {
          if (!isDragging) return;
          const deltaX = e.clientX - startX;
          const newWidth = startWidth + deltaX;
          const minWidth = 200;
          const maxWidth = window.innerWidth * 0.6;

          // 限制宽度范围
          const clampedWidth = Math.max(minWidth, Math.min(newWidth, maxWidth));
          left.style.width = `${clampedWidth}px`;
          right.style.width = `calc(100% - ${clampedWidth + 8}px)`;
        });

        // 鼠标释放事件
        document.addEventListener("mouseup", () => {
          isDragging = false;
        });
      });

      // 监听树形容器尺寸变化
      const resizeObserver = new ResizeObserver((entries) => {
        entries.forEach((entry) => {
          const { width } = entry.contentRect;
          right.style.width = `calc(100% - ${width + 8}px)`;
        });
      });
      resizeObserver.observe(left);
    },
    // 列过滤的数据
    setFilters(val) {
      let arr = [];
      arr = this.dataList.map((v, i) => {
        return v[val.columnName];
      });
      arr = [...new Set(arr)].map((value) => {
        return {
          text: value,
          value: value,
        };
      });
      return arr || [];
    },
    //  列过滤
    filterHandler(value, row, column) {
      const property = column["property"];
      return row[property] === value;
    },
    // 计算合计
    getSummaries(param) {
      const { columns, data } = param;
      const sums = [];
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = "合计";
          return;
        }
        // 计算元素是否参与合计
        let isSummary = false;
        for (let i = 0; i < this.summaryFieldList.length; i++) {
          if (column.property == this.summaryFieldList[i]) {
            isSummary = true;
            break;
          }
        }
        if (isSummary) {
          const values = data.map((item) => Number(item[column.property]));
          if (!values.every((value) => isNaN(value))) {
            sums[index] = values.reduce((prev, curr) => {
              const value = Number(curr);
              if (!isNaN(value)) {
                // 处理合计字段精度问题
                return parseFloat((prev + value).toFixed(2));
              } else {
                return prev;
              }
            }, 0);
            sums[index];
          } else {
            sums[index] = "N/A";
          }
        } else {
          sums[index] = "";
        }
      });

      return sums;
    },
    loadNode(node, resolve) {
      if (!this.treeFunctionName) {
        this.$message.error("请输入树形结构的模块名称");
        return resolve([]);
      }
      // 初始化顶级节点
      if (node.level === 0) {
        const topLevel = [
          {
            cn_name: "全部",
            code: "all",
            id: "0",
          },
        ];
        resolve(topLevel);
      }
      // 获取其他节点
      if (node.level > 0) {
        this.getTree(node.data.id).then((res) => {
          resolve(res);
        });
      }
    },
    /** 查询树结构 */
    getTree(value) {
      let initTreeParams = {
        pageNum: 1,
        pageSize: 999,
        functionName: this.treeFunctionName,
        conditions: [
          {
            logicalOperator: "AND",
            fieldType: "TEXT",
            field: this.treeForeignKey,
            operate: "EQ",
            value: value,
            filterGroup: "ADVANCE",
          },
          ...this.treeDefaultParams,
        ],
      };
      return getTableList(initTreeParams).then((response) => {
        console.log("当前树形结构返回的结构response",response);
        return response.rows;
      });
    },
    // 节点单击事件
    handleNodeClick(data) {
      this.selectedTreeItem = data;
      // let conditions = [];
      // if (data?.id != "0") {
      //   conditions = [
      //     {
      //       logicalOperator: "AND",
      //       fieldType: "TEXT",
      //       field: this.foreignKey,
      //       operate: "EQ",
      //       value: data[this.treePrimaryKey],
      //     },
      //   ];
      // }
      this.getList();
    },
    // 获取树节点的过滤条件
    getTreeConditions() {
      if (this.selectedTreeItem) {
        const data = this.selectedTreeItem;
        let conditions = [];
        if (data?.id != "0") {
          conditions = [];
          // 是否需要模糊查询
          if (this.treeLikeFieldArray) {
            this.treeLikeFieldArray.forEach((item) => {
              let value = data[item.value] + ".";
              if (value.startsWith("6.")) {
                value = data[item.value];
              }
              conditions.push({
                logicalOperator: "AND",
                fieldType: "TEXT",
                field: item.key,
                operate: "L_LIKE",
                value: value,
                filterGroup: "ADVANCE",
              });
            });
          } else {
            conditions.push({
              logicalOperator: "AND",
              fieldType: "TEXT",
              field: this.foreignKey,
              operate: "EQ",
              value: data[this.treePrimaryKey],
              filterGroup: "ADVANCE",
            });
          }
        }
        return conditions;
      } else {
        return [];
      }
    },
    // 筛选节点
    filterNode(value, data) {
      if (!value) return true;
      return data.cn_name.indexOf(value) !== -1;
    },
    addStorageEventListener() {
      window.addEventListener("storage", this.handleStorageChange);
    },
    removeStorageEventListener() {
      window.removeEventListener("storage", this.handleStorageChange);
    },
    handleStorageChange(event) {
      console.log(1212);
      if (this.openMode == "list") {
        if (event.key === "needUpdatedFunctionName") {
          if (event.newValue != "" && event.newValue == this.dialogName) {
            this.getList();
          }
          if (event.newValue != "" && event.newValue == this.functionName) {
            this.getList();
          }
          if (event.newValue != "" && event.newValue == this.treeFunctionName) {
            // this.resetChecked();
            this.treeKey = Date.now();
          }
        }
      }
    },
    // 获取选中的第一个item
    getSelectedItem() {
      return this.dataList.find((item) => item.id == this.ids[0]);
    },
    // 获取选中的所有item（id 类型归一比较，防 number/string 漂移导致选择丢失）
    getSelectedList() {
      const idStrs = (this.ids || []).map((i) => String(i));
      return this.dataList.filter((item) => idStrs.includes(String(item.id)));
    },
    // 获取选中的item，id列表
    getSelectedDetailsIdList() {
      if (this.dataList[0]?.main_id) {
        return this.multiDataList.map((item) => item.id);
      } else {
        return [];
      }
    },
    /** 反审核按钮 */
    rollbackForm() {
      if (!this.single) {
        this.$modal
          .confirmHTML(
            '是否<span style="color: #28cd6b;">撤销</span>数据',
            "确定"
          )
          .then(() => {
            const selectRow = this.getSelectedItem();
            const id = selectRow?.main_id || selectRow?.id;
            if (!selectRow || !id) return this.$message.warning("请选择数据");
            console.log("selectRow", selectRow);
            if (selectRow?.pact_state != "通过")
              return this.$message.warning("请选择通过的单据");
            this.buttonLoading = true;
            rollBackData(this.functionName, id)
              .then((res) => {
                this.getList();
                this.$modal.msgSuccess("反审核成功");
              })
              .finally(() => {
                this.buttonLoading = false;
              });
          })
          .catch((err) => {
            console.log("err :>> ", err); // this.$modal.msgSuccess("取消提交", err);
          });
      } else {
        this.$message.warning("请选择单条数据");
      }
    },
    // 根据传入的name，判断按钮是否显示
    buttonIsShow(name) {
      return !this.hiddenButtonList.includes(name);
    },
    // 根据传入的name，判断按钮是否显示
    buttonIsDisabled(name) {
      return this.disabledButtonList.includes(name);
    },
    // 获取打印按钮list
    getPrintButtonList() {
      getPrintButton(this.functionName).then((res) => {
        // this.form = res.data;
        this.printButtonList = res.data;
      });
    },
    setCurrent(row) {
      this.$refs.singleTable.setCurrentRow(row);
    },
    handleCurrentChange(val) {},
    getRowClassName({ row, rowIndex }) {
      // if (row?.is_cancel_detail == 1) {
      //   if (this.ids.includes(row.id)) {
      //     return "cancel-row-color selected-row-color";
      //   } else {
      //     return "cancel-row-color";
      //   }
      // }
      // if (row?.is_closed_detail == 1 || row?.is_closed_detail == 2) {
      //   if (this.ids.includes(row.id)) {
      //     return "closed-row-color selected-row-color";
      //   } else {
      //     return "closed-row-color";
      //   }
      // }
      // if (this.ids.includes(row.id)) {
      //   return "selected-row-color";
      // }
      // if (row?.is_cancel_detail == 1) {
      //   if (this.ids.includes(row.id)) {
      //     return "cancel-row-color selected-row-color";
      //   } else {
      //     return "cancel-row-color";
      //   }
      // }
      // if (row?.is_closed_detail == 1 || row?.is_closed_detail == 2) {
      //   if (this.ids.includes(row.id)) {
      //     return "closed-row-color selected-row-color";
      //   } else {
      //     return "closed-row-color";
      //   }
      // }
      // if (this.ids.includes(row.id)) {
      //   return "selected-row-color";
      // }
      if (this.reserveSelection) {
        const ids = this.multiDataList.map((item) => item.id) || [];
        if (ids.includes(row.id)) {
          return "selected-row-color";
        }
      } else {
        if (this.ids.includes(row.id)) {
          return "selected-row-color";
        }
      }
      return "";
    },
    // 行单击方法
    handleRowClick(row, column, event) {
      if (this.openRadioSelection) {
        // this.radioData = row;
        this.$set(this, "radioData", row);

        // 实现双击功能
        if (this.ids.includes(row.id) || this.singleClickReturnData) {
          // 如果是弹出框模式，并且是单选，需要触发方法
          if (this.openMode == "dialog" && this.openRadioSelection) {
            this.$emit("handle-dialog-submit", row);
          }
          this.setCurrent(null);
          this.ids = [];
          this.single = this.ids.length !== 1;
          this.multiple = !this.ids.length;
        } else {
          this.ids = [row.id];
          this.single = this.ids.length !== 1;
          this.multiple = !this.ids.length;
        }
      } else {
        // 获取选择的数据列表
        const selectedRows = this.$refs.table.selection;
        // 是否选中当前行
        const isRowSelected = selectedRows.some(
          (selectedRow) => selectedRow.id === row.id
        );
        console.log("selectedRows", selectedRows, row, isRowSelected);
        this.$refs.table.toggleRowSelection(row, !isRowSelected);
        this.onSelect(selectedRows, row);
      }
    },
    getRowKey(row) {
      // 优先使用 uid（与虚拟滚动 key-prop 一致）
      if (row?.uid !== undefined) {
        return row.uid;
      }
      // 降级到 id/main_id
      if (row?.id !== undefined && row?.id !== null && row?.id !== "") {
        return row.id;
      }
      if (
        row?.main_id !== undefined &&
        row?.main_id !== null &&
        row?.main_id !== ""
      ) {
        return row.main_id;
      }
      return row?.id;
    },
    // 行双击方法
    handleRowDblClick(row, column, event) {
      this.$emit("row-dblclick", row, column, event);
      if (this.openMode == "list" || this.doubleClickOpenNewPage) {
        let viweMode =
          row.pact_state == "通过" || row.pact_state == "审核中"
            ? "show"
            : "edit";
        this.openPage(viweMode, row.main_id || row.id);
      }
      // 实现双击功能 -- 如果是弹出框模式，并且选中了，需要触发方法
      if (this.openMode == "dialog" && !this.openRadioSelection) {
        if (!this.needCompareKeys.length) {
          this.$emit("handle-dialog-submit", [row]);
          return;
        }

        let obj = {};
        let allSelectRows = [...this.multiDataList, row];
        allSelectRows.forEach((el) => {
          this.needCompareKeys.forEach((key) => {
            if (!obj[key]) obj[key] = [];
            if (!obj[key].includes(el[key])) obj[key].push(el[key]);
          });
        });
        // 选择数据的key对应的值是否不一致
        // 不一致就不让双击
        let uplookDisabled = Object.values(obj).some((el) => el.length != 1);
        if (uplookDisabled) {
          return;
        } else {
          this.$emit("handle-dialog-submit", allSelectRows);
        }
      }
    },
    refreshTableKeys({
      refreshSingle = false,
      refreshMultiple = false,
      refreshPagination = false,
    } = {}) {
      if (refreshSingle) {
        this.datekey += 1;
      }
      if (refreshMultiple) {
        this.datekey2 += 1;
      }
      if (refreshPagination) {
        this.paginationkey += 1;
      }
    },
    finallyColumns2() {
      let list = this.columns.filter((item) =>
        this.checkList.includes(item.headerText)
      );
      if (!this.reserveSelection) {
        this.refreshTableKeys({
          refreshSingle: true,
          refreshMultiple: false,
          refreshPagination: true,
        });
      }
      // this.finallyColumnsList = list.filter((item) => item.columnWidth > 0);
      return list;
    },
    // 子组件筛选条件返回
    getColumns(columns, checkList) {
      this.columns = columns;
      this.checkList = checkList;
      this.finallyColumns2();
    },
    //搜索
    upSearch(val) {
      //调用子组件查询列表方法
      this.queryParams = val;
      this.queryParams.pageNum = this.queryForm.pageNum;
      this.queryParams.pageSize = this.queryForm.pageSize;
      this.$emit("templete-list", this.queryParams);
    },
    rightcheckchange() {
      this.requestParams = [];
      for (let i = 0; i < this.transfers.length; i++) {
        for (let j = 0; j < this.options.length; j++) {
          if (this.transfers[i] === this.options[j].name) {
            this.requestParams.push(this.options[j]);
          }
        }
      }
    },
    cellStyleForm() {
      if (this.requestParams.length + 1 > 6) {
        return "mpc-section-zkaw";
      } else {
        return "mpc-section";
      }
    },
    // 点击了打印按钮
    handlePrintSelect(item) {
      // this.getSelectedItem()
      console.log(this.getSelectedItem());
      let pactId;
      if (
        item.buttonName == "总厂箱单打印" ||
        item.buttonName == "总厂码单打印" ||
        item.buttonName == "总厂箱单打印(三重量)"
      ) {
        const idSet = new Set();
        this.multiDataList.forEach((item) => {
          idSet.add(item.erp_code);
        });
        pactId = Array.from(idSet).join(",");
        if (!pactId) {
          this.$message.error("请选择数据");
          return;
        }
        // }
        window.open(
          item.linkUrl + "&fbillno=" + pactId + "&__pi__=false",
          "_blank"
        );
      } else if (item.buttonName == "厚源箱单打印") {
        const idSet = new Set();
        this.multiDataList.forEach((item) => {
          idSet.add(item.erp_id);
        });
        pactId = Array.from(idSet).join(",");
        if (!pactId) {
          this.$message.error("请选择数据");
          return;
        }
        // }
        window.open(item.linkUrl + "&fid=" + pactId, "_blank");
      } else if (item.buttonName == "厚源码单打印") {
        const idSet = new Set();
        this.multiDataList.forEach((item) => {
          idSet.add(item.erp_id);
        });
        pactId = Array.from(idSet).join(",");
        // }
        window.open(
          item.linkUrl + "&fid=" + pactId + "&flx=XSCKD&__pi__=false",
          "_blank"
        );
      } else {
        if (this.openRadioSelection) {
          const selectItem = this.getSelectedItem();
          pactId = selectItem.main_id || selectItem.id;
        } else {
          // 使用 Set 来去重
          const idSet = new Set();
          this.multiDataList.forEach((item) => {
            idSet.add(item.main_id || item.id);
          });
          pactId = Array.from(idSet).join(",");
        }
        if (!pactId) {
          this.$message.error("请选择数据");
          return;
        }
        window.open(item.linkUrl + "&pact_id=" + pactId, "_blank");
      }
    },
    getConditions(addConditions) {
      // 创建一个新的条件数组副本
      let conditions = [...(this.queryParams.conditions || [])]; // 如果this.queryParams.conditions是undefined或null，则默认为空数组

      // 自定义查询条件
      if (
        Array.isArray(this.customizeConditions) &&
        this.customizeConditions.length > 0
      ) {
        conditions.push(...this.customizeConditions);
      }
      // 表头查询条件
      if (
        Array.isArray(this.filterInputCondition) &&
        this.filterInputCondition.length > 0
      ) {
        conditions.push(...this.filterInputCondition);
      }

      // 添加额外的查询条件
      // if (Array.isArray(addConditions) && addConditions.length > 0) {
      //   conditions.push(...addConditions);
      // }
      // console.log("判断是否选择了树形节点",this.selectedTreeItem);

      // 判断是否选择了树形节点
      if (this.selectedTreeItem) {
        const treeConditions = this.getTreeConditions();
        if (Array.isArray(treeConditions) && treeConditions.length > 0) {
          if (this.functionName == "MATERIAL") {
            // 物料传进来的 CODE L_LIKE  5. 这里结合后弹窗只能出现5.开头的数据，需要把这个条件去除
            conditions = conditions.filter((item) => item.value != "5.");
          }
          conditions.push(...treeConditions);
        }
      }
      return conditions;
    },

    getList: _.debounce(function (addConditions) {
      console.log("DynamicList中的getList函数被触发了");
      this.loading = true;
      const conditions = this.getConditions(addConditions);
      // 发送请求
      getTableList({
        functionName: this.functionName,
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize,
        dialogName: this.dialogName,
        conditions,
      })
        .then((response) => {
          // 处理响应
          console.log("DynamicList中的getList函数内部的getTableList函数被触发了获得的数据respones:",response);
          this.total = response.total;
          this.options = response.headers;
          if (!this.reserveSelection) {
            this.refreshTableKeys({
              refreshSingle: true,
              refreshMultiple: true,
              refreshPagination: true,
            });
          }
          this.fieldKey = Date.now();
          this.fieldKey2 = Date.now();
          // this.dataList = this.isTree
          //   ? this.handleTree(response.rows, "id", "parent_id")
          //   : response.rows;

          // 为所有数据添加 uid 字段，确保虚拟滚动 key-prop 稳定
          const rowsWithUid = response.rows.map((row, index) => {
            // 如果已有 uid 则保留（避免覆盖）
            if (row.uid !== undefined) {
              return row;
            }
            // 优先使用现有 id/main_id 作为 uid
            if (row?.id !== undefined && row?.id !== null && row?.id !== "") {
              return { ...row, uid: row.id };
            }
            if (
              row?.main_id !== undefined &&
              row?.main_id !== null &&
              row?.main_id !== ""
            ) {
              return { ...row, uid: String(row.main_id) };
            }
            // 都没有则生成新的 uid（时间戳 + 索引确保唯一性）
            return { ...row, uid: `uid_${Date.now()}_${index}` };
          });

          this.dataList = Object.freeze(rowsWithUid);
          this._dataList = Object.freeze(rowsWithUid);
          this.tableEcho();
          // 接收到新数据，需要把过滤数据清空，重新计算
          this.headerFilterOptions = {};
          this.activeHeaderFilters = {};
        })
        .finally(() => {
          this.loading = false;
          this.$refs.siftButton.isLoading = false;
          this.afterGetList();
        });
    }, 300),


    getFieldConfig() {
      getFieldConfigList(
        this?.dialogName || this.queryParams.functionName
      ).then((res) => {
        this.queryOptions = res.data.fieldConfigList;
        this.setDefaultScheme(res.data.defaultScheme);
        this.fieldKey = Date.now();
        console.log("DynamicList的getFieldConfig函数获取的res",res);
        console.log("DynamicList的getFieldConfig函数获取的queryOptions：",res.data.fieldConfigList);
        console.log("DynamicList的getFieldConfig函数获取的defaultScheme：",res.data.defaultScheme);


      });
    },
    /** 搜索按钮操作 */
    handleQuery(conditions) {
      this.queryParams.pageNum = 1;
      this.queryParams.conditions = [];
      if (conditions) {
        this.queryParams.conditions = conditions;
      }
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      if (this.reserveSelection) return;
      if (!this.echoFlag) return;
      console.log("selection", this.multiDataList, selection);
      this.multiDataList = selection;

      this.ids = this.multiDataList.map((item) => item.id);
      this.single = this.multiDataList.length !== 1;
      this.multiple = !this.multiDataList.length;
    },
    /**
     * 表格复选框勾选时事件
     * @param selection 当前勾选的数据
     * @param row 当前被勾选的行数据
     */
    onSelect(selection, row) {
      if (!this.reserveSelection) return;
      const { id } = row;
      const index = this.multiDataList.findIndex((i) => i.id === id);
      // 如果已经包含该项 说明本次是取消勾选
      if (index > -1) {
        this.multiDataList.splice(index, 1);
      } else {
        // 否则，将已勾选的push进去
        this.multiDataList.push(row);
      }
      console.log("multiDataList", this.multiDataList);
    },
    selectAll(isSelect) {
      this.$refs.table.toggleAllSelection(isSelect);
    },
    onSelectAll(selection) {
      if (this.reserveSelection) {
        /**
         * this.multiDataList.length > selection.length
         * 表示全不选操作
         * 那么就要把存在于this.multiDataList
         * 且不存在于selection中的元素从this.multiDataList中删除
         * */
        if (this.multiDataList.length > selection.length) {
          const sysIds = selection.map((i) => i.id);
          this.multiDataList = this.multiDataList.filter((item) =>
            sysIds.includes(item.id)
          );
        } else if (this.multiDataList.length < selection.length) {
          /**
           * this.multiDataList.length < selection.length
           * 表示全选操作
           * 把存在于selection且不存在于this.multiDataList的元素
           * 添加到this.multiDataList中
           */
          const sysIds = this.multiDataList.map((i) => i.id);
          const unExist = selection.filter((i) => !sysIds.includes(i.id));
          this.multiDataList.push(...unExist);
        }
      } else {
        this.multiDataList = selection;
      }
    },
    /** 新增按钮操作 */
    handleAdd() {
      if (this.handleCustomAdd) {
        this.handleCustomAdd();
      } else {
        this.openPage("add");
      }
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      if (this.handleCustomUpdate) {
        this.handleCustomUpdate(row.id || this.ids);
      } else {
        const item = this.getSelectedItem();
        this.openPage("edit", item?.main_id || item.id);
      }
    },
    openPage(mode, id, detailId) {
      let modeMap = {
        add: "新增",
        edit: "编辑",
        show: "查看",
        copy: "复制",
        copyAll: "关联复制",
      };
      let str = modeMap[mode];
      // 打开页签
      let { path, query, meta } = this.$route;
      console.log("当前的DynamicList中的openPage的path:",path);
      console.log("当前的DynamicList中的openPage的query:",query);
      console.log("当前的DynamicList中的openPage的meta:",meta);
      // 判断是否变更
      let changeOrderFN = null;
      if (mode == "change") {
        changeOrderFN = this.changeOrderFN;
      }
      let params = {
        functionName: changeOrderFN || query.functionName || this.functionName,
        mode,
        id: this.ids && this.ids[0],
      };
      if (this.dialogName) {
        params.dialogName = this.dialogName;
      }
      if (id) {
        params.id = id;
        if (mode == "copy" || mode == "copyAll") {
          params.detailId = detailId;
        }
      }

      if (mode == "add") {
        delete params.id;
      }
      let detailPath = null;
      // 变更路径/详情路径
      if (mode == "change") {
        path = this.changeOrderPath;
        params.detailId = detailId;
      }
      // 联查路径
      if (this.relationQueryPath) {
        path = this.relationQueryPath;
        params.functionName = this.functionName;
      }
      // 下推
      if (mode == "pushDown") {
        path = this.pushDownObj.path;
        params.originalFn = this.functionName;
        params.functionName = this.pushDownObj.targetFn;
        params.pushDownParams = this.pushDownObj.pushDownParams;
      }
      // 格式化url
      let path2 =
        path +
        "/form?" +
        decodeURIComponent(qs.stringify(params), {
          arrayFormat: "indices",
        }).replace(/\[([0-9]+)\]=/g, "=");

      window.open(path2, "_blank");
      // this.$tab.openPage(`${meta.title}-${str}`, path + "/form", params);
    },
    /** 删除按钮操作 */
    handleDelete() {
      let ids = this.ids;
      if (this.openRadioSelection) {
        const item = this.getSelectedItem();
        ids = item?.main_id || item.id;
      } else {
        const item = this.getSelectedList().map((i) => i.main_id || i.id);
        ids = Array.from(new Set(item)).join(",");
      }
      if (!ids || ids === "" || (Array.isArray(ids) && !ids.length)) {
        return this.$message.warning("请选择要删除的数据");
      }
      if (this.handleCustomDelete) {
        this.handleCustomDelete(ids);
      } else {
        this.$modal
          .confirmHTML('是否确认<span style="color: red;">删除</span>该条数据')
          .then(() => {
            this.loading = true;
            return deleteById(this.functionName, ids);
          })
          .then(() => {
            this.loading = false;
            this.getList();
            this.$modal.msgSuccess("删除成功");
          })
          .catch(() => {})
          .finally(() => {
            this.loading = false;
          });
      }
    },
    handleCopy() {
      const item = this.getSelectedItem();
      if (this.openRadioSelection) {
        // 单选逻辑
        if (item.main_id) {
          this.openPage("copy", item.main_id, item.id);
        } else {
          this.openPage("copy", item.id);
        }
      } else {
        // 多选逻辑
        if (item.main_id) {
          // 校验是否同一表单
          const mainId = this.checkIfSameForm();
          if (mainId) {
            this.openPage("copy", mainId, this.ids);
          } else {
            this.$message.error("禁止跨表单复制");
          }
        } else {
          if (this.ids.length > 1) {
            this.$message.error("禁止跨表单复制");
          } else {
            this.openPage("copy", item.id);
          }
        }
      }
    },
    handleCopyAll() {
      const item = this.getSelectedItem();
      if (this.openRadioSelection) {
        // 单选逻辑
        if (item.main_id) {
          this.openPage("copyAll", item.main_id, item.id);
        } else {
          this.openPage("copyAll", item.id);
        }
      } else {
        // 多选逻辑
        if (item.main_id) {
          // 校验是否同一表单
          const mainId = this.checkIfSameForm();
          if (mainId) {
            this.openPage("copyAll", mainId, this.ids);
          } else {
            this.$message.error("禁止跨表单复制");
          }
        } else {
          if (this.ids.length > 1) {
            this.$message.error("禁止跨表单复制");
          } else {
            this.openPage("copyAll", item.id);
          }
        }
      }
    },
    // 处理多选数据返回
    handleMultiDataSubmit() {
      this.$emit("handle-dialog-submit", this.multiDataList);
    },
    // 处理单选返回
    handleRadioDataSubmit() {
      // 保存前的校验
      if (this.validateRadioSelection) {
        if (!this.validateRadioSelection(this.radioData)) {
          return;
        }
      }
      this.$emit("handle-dialog-submit", this.radioData);
    },
    // 检查是否同一表单
    checkIfSameForm() {
      const idsSet = new Set(this.ids);
      const mainIds = this.dataList
        .filter((item) => idsSet.has(item.id))
        .map((item) => item.main_id);

      if (new Set(mainIds).size === 1) {
        return mainIds[0];
      } else {
        return false;
      }
    },
    processWidth(num) {
      // num'%'结尾
      if (typeof num === "string" && num.endsWith("%")) {
        // 如果是，直接返回
        return num;
      } else {
        // 如果不是，将其转换为字符串（如果它还不是），并拼接'px'
        return `${num}px`;
      }
    },
    // 获取ids
    getIds() {
      let idLit = [];
      this.ids.forEach((id) => {
        const findItem = this.dataList.find((item) => item.id == id);
        idLit.push(findItem?.main_id || findItem?.id || null);
      });
      return idLit;
    },
    // 清空树形筛选条件
    resetChecked() {
      this.handleNodeClick(null);
      this.$refs.tree.setCurrentKey(null);
      // this.$refs.tree.setCheckedKeys(null);
    },
    // 返回字段是否排序
    isTableColumnSortable(item) {
      if (isTrue.YES == item.columnSort) {
        return true;
      } else {
        return false;
      }
    },
    // 返回字段是否是固定列
    isTableColumnFixed(item) {
      if (isTrue.YES == item.fixedColumn) {
        return true;
      } else {
        return false;
      }
    },
    // 返回字段对齐方式
    isTableColumnAlign(item) {
      const textAlign = {
        LEFT: 0,
        CENTER: 1,
        RIGHT: 2,
      };
      if (item.align == textAlign.LEFT) {
        return "left";
      } else if (item.align == textAlign.CENTER) {
        return "center";
      } else if (item.align == textAlign.RIGHT) {
        return "right";
      } else {
        return "left";
      }
    },
    // 树形组件-添加按钮
    addTreeHandler() {
      this.treeOpenUrlHandler("add");
    },
    // 树形组件-编辑按钮
    editTreeHandler() {
      this.treeOpenUrlHandler("edit");
    },
    // 树形组件-删除按钮
    deleteTreeHandler() {
      if (this.selectedTreeItem) {
        const id = this.selectedTreeItem.id;
        this.$modal
          .confirmHTML('是否确认<span style="color: red;">删除</span>该条数据')
          .then(() => {
            // return deleteById("RAW_MATERIAL_GROUP", id);
            return deleteById(this.treeFunctionName, id);
          })
          .then(() => {
            this.$refs.tree.remove(this.selectedTreeItem);
            this.$modal.msgSuccess("删除成功");
          })
          .catch(() => {})
          .finally(() => {});
      } else {
        this.$message.warning("请选择树形节点");
      }
    },
    // 根据模式处理树形组件按钮
    treeOpenUrlHandler(mode) {
      if (this.selectedTreeItem || mode == "add") {
        const params = {
          functionName: this.treeFunctionName,
          mode,
          id: this.selectedTreeItem?.id || "",
        };
        let path = this.treePath + "?" + qs.stringify(params);
        window.open(path, "_blank");
      } else {
        this.$message.warning("请选择树形节点");
      }
    },
    // 获取全部数据字典数据
    getDictData() {
      this.dictList = getDictDataList(this.dicts)
        .then((res) => {
          this.dictDataList = res.data;
          console.log("当前获取全部字典的是什么初始res",res);
          console.log("当前获取全部字典的是什么初始res.data",res.data);
          console.log("当前获取全部字典的所有数据字段是什么",this.dictList);

        })
        .catch((err) => {
          // 被后续相同请求取代而取消的，不视为错误（数据由后发出的请求返回）
          if (err && err.canceled) return;
          this.$message.error("获取数据字段数据失败");
        });
    },
    handleSecondaryAdd() {
      this.$refs.secondaryDialog.handleAdd();
    },
    backwardFun(data) {
      this.$emit("handle-dialog-submit", [data]);
    },
    // 联查
    handleRelationQuery() {

      this.showRelationQuery = true;

      // const item = this.getSelectedItem();
      // this.selectedRow = item;
      // this.relationQueryId = item?.main_id || item.id;
      let selectRows = this.dataList.filter((el) => this.ids.includes(el.id));
      this.selectedRow = selectRows;
      this.relationQueryId = selectRows.map((el) =>
        el.main_id ? el.main_id : el.id
      );
      this.selectDetailIds = selectRows.map((el) =>
        el.id ? el.id : el.main_id
      );
    },
    // 下推
    handlePushDown(targetFn) {
      let selectData = [];
      let singleDataFns = [];
      if (singleDataFns.includes(this.functionName)) {
        selectData = this.radioData ? [this.radioData] : [];
      } else {
        selectData = this.multiDataList;
      }

      let params = [];
      selectData.forEach((el) => {
        let findSameMainIdIndex = params.findIndex(
          (item) => item.id == el.main_id
        );
        if (findSameMainIdIndex == -1) {
          params.push({
            id: el.main_id,
            detailsId: [el.id],
          });
        } else {
          params[findSameMainIdIndex].detailsId.push(el.id);
        }
      });

      this.pushDownObj = this.pushDownList.find((el) => {
        if (targetFn) {
          return el.currentFn == this.functionName && el.targetFn == targetFn;
        } else {
          return el.currentFn == this.functionName;
        }
      });
      this.pushDownObj.pushDownParams = params;
      const ids = params?.map((item) => item.id);

      // 同步构建下推目标URL，在用户手势同步调用栈中 window.open，避免 iOS Safari 拦截
      let pushUrlParams = {
        functionName: this.pushDownObj.targetFn,
        mode: "pushDown",
        id: this.ids && this.ids[0],
        originalFn: this.functionName,
        pushDownParams: this.pushDownObj.pushDownParams,
      };
      if (this.dialogName) {
        pushUrlParams.dialogName = this.dialogName;
      }
      let pushUrl =
        this.pushDownObj.path +
        "/form?" +
        decodeURIComponent(qs.stringify(pushUrlParams), {
          arrayFormat: "indices",
        }).replace(/\[([0-9]+)\]=/g, "=");

      // 同步打开窗口（在用户点击的同步调用栈中）
      let openedWindow = window.open(pushUrl, "_blank");

      // 手机端 WebView 可能拦截 window.open，fallback 到 router.push
      if (!openedWindow || openedWindow.closed) {
        console.warn("window.open 被拦截，fallback 到 router.push");
        this.$router.push(pushUrl);
        return;
      }

      // 检查是否存在正在审核中的变更单
      checkPushDown(ids, this.functionName)
        .then((res) => {
          // 校验通过，窗口已打开为正确URL
        })
        .catch((err) => {
          // 校验失败时关闭已打开的窗口
          if (openedWindow) {
            openedWindow.close();
          }
        });
    },
    handleClickPushDown(targetFn) {
      this.handlePushDown(targetFn);
    },
    // 第一行input输入框的搜索事件
    handleEnter: _.debounce(function (field) {
      const value = this.filterInputList[field];
      // 如果为空，删除查询条件
      if (!value) {
        this.filters = this.filters.filter((item) => item.field != field);
      } else {
        // 保存查询条件
        this.filters = [...this.filters, { field, value }];
      }
      // 重新构建查询条件
      this.getList();

      // 过滤当前页
      // this.dataList = this._dataList.filter((item) => {
      //   return this.filters.every((filter) => {
      //     return (
      //       item[filter.field] &&
      //       String(item[filter.field]).includes(String(filter.value))
      //     );
      //   });
      // });
    }),
    // 列表回显
    tableEcho() {
      // 将已选择的数据状态变为已勾选
      if (this.reserveSelection && this.echoRowDataList.length > 0) {
        this.$nextTick(() => {
          this.echoFlag = false;
          const tableRef = this.$refs.table;
          const ids = this.multiDataList.map((i) => i.id);
          this.dataList.forEach((i) => {
            if (ids.includes(i.id)) {
              tableRef.toggleRowSelection(i, true);
            }
          });
          console.log("multiDataList", this.multiDataList);
          // this.echoRowDataList.forEach((row) => {
          //   let selectRow = this.dataList.find((item) => row.id == item.id);
          //   if (selectRow) {
          //     this.$refs.table.toggleRowSelection(selectRow, true);
          //     this.multiDataList.push(selectRow);
          //   } else {
          //     this.multiDataList.push(row);
          //   }
          // });
          this.ids = this.multiDataList.map((item) => item.id);
          this.single = this.multiDataList.length !== 1;
          this.multiple = !this.multiDataList.length;

          this.echoFlag = true;
        });
      }
    },
    // 修改默认方案
    setDefaultScheme(defaultScheme) {
      // 获取额外的默认方案
      const extraDefaultScheme = this.extraDefaultSchemeCallBack();
      if (extraDefaultScheme) {
        defaultScheme.push(...extraDefaultScheme);
      }
      // if (this.openMode == "list") {
      this.defaultScheme = defaultScheme;
      // }
    },
    // 获取多选的数据
    getMultiDataList() {
      return this.multiDataList;
    },
    /**
     * 表头过滤组件数据发生改变
     * @param column 列信息
     * @param selectedList 选中的项
     */
    handleHeaderFilter({ property }, selectedList) {
      // console.log("选中的项：", property,selectedList);
      // 保存选中的值
      this.$set(this.activeHeaderFilters, property, selectedList);

      // 重新计算 dataList
      this.applyFilters();
    },
    /**
     * 列过滤组件显隐状态改变
     * @param column
     */
    onFilterPopoverVisibleChange(column) {
      const key = column.property;

      // 已经有缓存的 options 不再处理
      if (this.headerFilterOptions[key]) return;

      // 从 dataList 中提取当前列的唯一值
      const optionsSet = new Set();
      this.dataList.forEach((row) => {
        const val = row[column.property];
        if (val !== undefined && val !== null) {
          optionsSet.add(val);
        }
      });

      // 排序 + 去重 + 转数组
      const options = Array.from(optionsSet).sort();

      // 缓存到 filterOptions 中
      this.$set(this.headerFilterOptions, key, options);
    },
    /**
     * 列过滤组件数据发生改变
     */
    applyFilters() {
      const filterKeys = Object.keys(this.activeHeaderFilters).filter(
        (key) => this.activeHeaderFilters[key]?.length > 0
      );

      // 如果没有过滤条件，直接使用原始数据
      if (filterKeys.length === 0) {
        this.dataList = this._dataList;
        return;
      }

      // 缓存过滤集合，避免重复创建Set对象
      const filterSets = {};
      for (let i = 0; i < filterKeys.length; i++) {
        const key = filterKeys[i];
        filterSets[key] = new Set(this.activeHeaderFilters[key]);
      }

      // 使用for循环替代every方法，提升大数据量时的性能
      this.dataList = this._dataList.filter((row) => {
        for (let i = 0; i < filterKeys.length; i++) {
          const key = filterKeys[i];
          if (!filterSets[key].has(row[key])) {
            return false;
          }
        }
        return true;
      });
    },
    /**
     * 获取分页大小本地存储的key
     * @returns {string}
     */
    getPageSizeLocalStoreKey() {
      return `${this.openMode}_${this.functionName}_${
        this.dialogName || "default"
      }_pageSize`;
    },
    /**
     * 从 localStorage 恢复分页大小
     */
    restorePageSizeFromStorage() {
      // 从 localStorage 获取之前保存的 pageSize
      const storageKey = this.getPageSizeLocalStoreKey();
      const savedPageSize = localStorage.getItem(storageKey);

      if (savedPageSize) {
        this.queryParams.pageSize = parseInt(savedPageSize);
      }
    },
  },
};
</script>

<style lang="scss" scoped>
.head-container {
  display: flex;
  height: 30px;
  justify-content: center;
  align-content: center;
}

.button-group {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.table-container {
  height: calc(100vh - 220px);
  // overflow: scroll;
}

.pagination-container {
  margin: 0px !important;
  padding: 0px !important;
}

::v-deep .el-input__inner,
.el-textarea__inner,
.el-input {
  border: 1px solid #dcdfe6 !important;
  background-color: white !important;
  border-color: #dcdfe6 !important;
}

::v-deep .el-table th.el-table__cell > .cell {
  white-space: nowrap;
  text-align: center;
}
::v-deep .el-table .el-table__cell:hover .filter-icon {
  display: block !important;
}
.tree-box {
  // border: 1px solid #ccc;
  height: calc(100vh - 220px);
  overflow-y: auto !important;
  overflow-x: hidden !important;
}
.tree-group {
  width: 100%; /* 确保宽度自适应容器 */
  min-width: fit-content; /* 防止内容被截断 */
}
.fixed-field {
  ::v-deep .el-table th.el-table__cell.is-leaf,
  .el-table td.el-table__cell {
    padding: 10px 0px;
  }
}

::v-deep .el-radio {
  margin-right: 10px !important;
}

.required {
  color: red;
  margin-left: 3px;
}
::v-deep .el-dialog__body {
  padding: 0 20px !important;
}
::v-deep .el-dialog__body .el-table .cell {
  text-align: center;
}

.table-container::v-deep .el-table__body-wrapper {
  padding-bottom: 10px;
}

.table-container::v-deep .el-table__body-wrapper::-webkit-scrollbar {
  width: 20px !important; /*滚动条宽度*/
  height: 20px !important; /*滚动条高度*/
}

// .is-deficit {
//   color: red;
// }
::v-deep .header-filter-input {
  .el-input__inner {
    padding: 0px;
  }
  .el-input__validateIcon {
    display: none;
  }
}

::v-deep .print-select {
  .el-input__inner {
    width: 100px;
  }
}
::v-deep .el-table__footer-wrapper tbody td.el-table__cell {
  height: 20px;
  background: white;
}
.contnet {
  display: flex;
}
.left-tree {
  position: relative;
  width: 200px;
  min-width: 200px; /* 添加最小宽度 */
  background: #fff;
  box-shadow: 0 0 2px 2px rgba(0, 0, 0, 0.050980392156862744);
  border-radius: 4px;
  vertical-align: top;
  display: inline-block;
  box-sizing: border-box;
  flex-shrink: 0;
  padding: 10px 0 0 10px;
  margin-right: 8px;
  overflow: hidden; /* 防止内容溢出 */
}
.resize {
  cursor: col-resize;
  position: absolute;
  bottom: 0;
  top: 50%;
  right: -6px; /* 调整为相对于右侧边界 */
  transform: translateY(-50%); /* 垂直居中 */
  background-color: #d6d6d6;
  border-radius: 5px;
  width: 10px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
  z-index: 10; /* 确保在树内容之上 */
  user-select: none; /* 防止拖拽时选中文本 */
}
.resize:hover {
  background: #409eff;
  color: white;
}
.right-content {
  display: inline-block;
  width: calc(100% - 210px);
  height: 100%;
  background: #fff;
  -webkit-box-shadow: 0 0 2px 2px rgba(0, 0, 0, 0.050980392156862744);
  box-shadow: 0 0 2px 2px rgba(0, 0, 0, 0.050980392156862744);
  border-radius: 4px;
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
  vertical-align: top;
  overflow: auto;
  padding-left: 10px;
  transition: width 0.2s; /* 添加平滑过渡 */
}
.tree-container2 {
  height: calc(100% - 1px);
  overflow-y: auto;
  overflow-x: hidden;
}
.tree-container {
  height: calc(100% - 30px);
  overflow-y: auto;
  overflow-x: hidden;
}
.iconfont {
  display: inline-block;
  width: 0.4em;
  height: 0.4em;
  border: 2px solid #409eff;
  border-top: none; /* 去掉会变成原本的宽高12 */
  border-right-color: transparent;
  border-top-color: transparent;
  transform: rotate(-45deg);
}
</style>
