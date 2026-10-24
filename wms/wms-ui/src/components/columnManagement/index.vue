<template>
  <div>
    <el-col :span="1.5">
      <el-button
        size="mini"
        style="position: absolute; right: 30px; top: 0"
        type="primary"
        @click="handleField"
        >字段配置
      </el-button>
    </el-col>

    <el-dialog
      :visible.sync="dialogTableVisible"
      :modal="false"
      title="列配置"
      width="80%"
      style="z-index: 3000"
    >
      <div>
        <div style="margin: 10px 0">
          <el-button icon="el-icon-plus" size="mini" type="primary" @click="add"
            >添 加
          </el-button>
          <el-button
            :disabled="deleteIsShow"
            icon="el-icon-delete"
            size="mini"
            type="danger"
            @click="deleteItem"
            >移 除
          </el-button>
          <el-button
            icon="el-icon-arrow-up"
            @click="moveUp"
            :disabled="deleteIsShow"
            >上移</el-button
          >
          <el-button
            icon="el-icon-arrow-down"
            @click="moveDown"
            :disabled="deleteIsShow"
            >下移</el-button
          >
        </div>
        <el-table
          ref="singleTable"
          :data="gridData"
          border
          height="500"
          @row-click="rowClick"
          @selection-change="handleSelection"
        >
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column label="列排序" property="sortIndex" width="70">
            <template #header>
              <span>列排序</span>
              <span class="required">*</span>
            </template>
          </el-table-column>
          <el-table-column label="列名" property="columnName" width="140">
            <template #header>
              <span>列名</span>
              <span class="required">*</span>
            </template>
            <template slot-scope="scope">
              <el-input v-model="scope.row.columnName"></el-input>
            </template>
          </el-table-column>
          <el-table-column label="列展示名称" property="headerText" width="140">
            <template #header>
              <span>列展示名称</span>
              <span class="required">*</span>
            </template>
            <template slot-scope="scope">
              <el-input v-model="scope.row.headerText"></el-input>
            </template>
          </el-table-column>
          <el-table-column label="字段类型" property="fieldType" width="160">
            <template #header>
              <span>字段类型</span>
              <span class="required">*</span>
            </template>
            <template slot-scope="scope">
              <el-select
                v-model="scope.row.fieldType"
                clearable
                placeholder="请选择"
              >
                <el-option
                  v-for="dict in dictDataList.field_type"
                  :key="dict.dictValue"
                  :label="dict.dictLabel"
                  :value="dict.dictValue"
                ></el-option>
              </el-select>
            </template>
          </el-table-column>

          <el-table-column label="SQL表达式" property="sqlColumn" width="200">
            <template #header>
              <span>SQL表达式</span>
              <span class="required">*</span>
            </template>
            <template slot-scope="scope">
              <el-input
                v-model="scope.row.sqlColumn"
                type="textarea"
                :rows="2"
                placeholder="可留空：默认按列名在主表解析（支持驼峰/下划线）"
              ></el-input>
            </template>
          </el-table-column>

          <el-table-column
            label="可查询"
            align="‘center’"
            property="search"
            width="80"
          >
            <template slot-scope="scope">
              <el-checkbox
                :true-label="1"
                :false-label="0"
                v-model="scope.row.search"
              ></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column label="默认查询" property="checked" width="80">
            <template slot-scope="scope">
              <el-checkbox
                :true-label="1"
                :false-label="0"
                v-model="scope.row.checked"
              ></el-checkbox>
            </template>
          </el-table-column>

          <el-table-column label="数据排序" property="orderByIndex" width="80">
            <template slot-scope="scope">
              <InputNumber
                v-model="scope.row.orderByIndex"
                :precision="0"
                placeholder="请输入"
              />
            </template>
          </el-table-column>
          <el-table-column label="排序模式" property="orderByIndex" width="80">
            <template slot-scope="scope">
              <el-input v-model="scope.row.sort" placeholder="请输入" />
            </template>
          </el-table-column>
          <el-table-column label="列宽" property="columnWidth" width="80">
            <template #header>
              <span>列宽</span>
              <span class="required">*</span>
            </template>
            <template slot-scope="scope">
              <el-input v-model="scope.row.columnWidth" placeholder="请输入" />
            </template>
          </el-table-column>
          <el-table-column label="格式化" property="format" width="200">
            <template slot-scope="scope">
              <el-select
                v-model="scope.row.format"
                clearable
                filterable
                placeholder="请选择"
              >
                <el-option
                  v-for="dict in dictDataList.field_formatter_type"
                  :key="dict.dictValue"
                  :label="dict.dictLabel"
                  :value="dict.dictValue"
                ></el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column
            label="日期默认值"
            property="defaultValue"
            width="120"
          >
            <template slot-scope="scope">
              <el-select
                v-model="scope.row.defaultValue"
                clearable
                filterable
                placeholder="请选择"
              >
                <el-option
                  v-for="dict in dictDataList.t_date_range"
                  :key="dict.dictValue"
                  :label="dict.dictLabel"
                  :value="dict.dictValue"
                ></el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="对齐方式" property="align" width="120">
            <template slot-scope="scope">
              <el-select
                v-model="scope.row.align"
                clearable
                filterable
                placeholder="请选择"
              >
                <el-option
                  v-for="dict in dictDataList.field_align_type"
                  :key="dict.dictValue"
                  :label="dict.dictLabel"
                  :value="Number(dict.dictValue)"
                ></el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="固定列" property="fixedColumn" width="80">
            <template slot-scope="scope">
              <el-checkbox
                :true-label="1"
                :false-label="0"
                v-model="scope.row.fixedColumn"
              ></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column
            label="开启列表排序"
            property="columnSort"
            width="100"
          >
            <template slot-scope="scope">
              <el-checkbox
                :true-label="1"
                :false-label="0"
                v-model="scope.row.columnSort"
              ></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column label="开启悬浮层" property="columnSort" width="100">
            <template slot-scope="scope">
              <el-checkbox
                :true-label="1"
                :false-label="0"
                v-model="scope.row.popover"
              ></el-checkbox>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogTableVisible = false">关 闭</el-button>
        <el-button type="primary" @click="determine">保 存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getDictDataList } from "@/api/system/dict/data";
import { radioList } from "@/utils/variables.js";

import FieldManagement from "@/components/QueryForm/FieldManagement";
import DynamicQuery from "@/components/DynamicQuery/index.vue";
import SiftButton from "@/components/SiftButton/index.vue";
import { getPrintButton, rollBackData } from "@/api/system/common";
import qs from "qs";
import {
  getFieldConfigList,
  getTableList,
  deleteById,
  getCopyData,
  getColumnConfig,
  saveColumnConfig,
} from "@/api/system/common";

export default {
  name: "DynamicList",

  props: {
    functionName: {
      type: String,
      required: true,
    },
    dictDataList: {
      type: Object,
      default: () => [],
    },
  },

  data() {
    return {
      dicts: [
        "t_date_range",
        "field_type",
        "field_formatter_type",
        "field_align_type",
      ],
      loading: true,
      dialogTableVisible: false,
      gridData: [],
      selectedData: [],
      deleteIsShow: true,
    };
  },
  watch: {
    selectedData: {
      handler(newVal, oldVal) {
        console.log("new", newVal);
        // 当 items 发生变化时，检查数组长度并更新 hasItems 的值
        if (newVal) {
          if (newVal[0]) {
            this.deleteIsShow = !(newVal.length > 0);
            console.log("deleteIsShow", this.deleteIsShow);
          } else {
            this.deleteIsShow = true;
          }
        } else {
          this.deleteIsShow = true;
        }
      },
      deep: true, // 深度监听，这样才能捕获数组内部的变化
    },
  },
  created() {
    // this.getDictData()
  },

  methods: {
    //上移
    moveUp() {
      if (this.selectedData.length === 0) return;
      // 获取选中行的索引
      const selectedIndexes = this.selectedData.map((row) =>
        this.gridData.indexOf(row)
      );
      selectedIndexes.sort((a, b) => a - b); // 从小到大排序

      // 确定目标位置（最小索引的位置的上面）
      const targetIndex = selectedIndexes[0] - 1;

      // 移除选中的行
      const rowsToMove = [];
      for (let i = selectedIndexes.length - 1; i >= 0; i--) {
        const index = selectedIndexes[i];
        rowsToMove.unshift(this.gridData.splice(index, 1)[0]); // 从后到前移除
      }

      // 插入行到目标位置
      const insertIndex = Math.max(targetIndex, 0);
      this.gridData.splice(insertIndex, 0, ...rowsToMove);
      this.updateSortIndex();
    },
    // 下移
    moveDown() {
      console.log(this.selectedData);
      if (this.selectedData.length === 0) return;
      // 获取选中行的索引
      const selectedIndexes = this.selectedData.map((row) =>
        this.gridData.indexOf(row)
      );
      console.log(selectedIndexes);
      selectedIndexes.sort((a, b) => a - b); // 从小到大排序

      let targetIndex;
      if (selectedIndexes.length == 1) {
        targetIndex = selectedIndexes[0] + 1;
      } else {
        targetIndex = Math.min(
          selectedIndexes[selectedIndexes.length - 1],
          this.gridData.length
        );
      }
      // 确定目标位置（最小索引的位置的上面）

      // const targetIndex = selectedIndexes[0] - 1;

      // 移除选中的行
      const rowsToMove = [];
      for (let i = selectedIndexes.length - 1; i >= 0; i--) {
        const index = selectedIndexes[i];
        rowsToMove.unshift(this.gridData.splice(index, 1)[0]); // 从后到前移除
      }
      // 插入行到目标位置
      const insertIndex = Math.max(targetIndex, 0);
      this.gridData.splice(insertIndex, 0, ...rowsToMove);
      this.updateSortIndex();
    },
    // 更新idnex
    updateSortIndex() {
      this.gridData.forEach((row, index) => {
        row.sortIndex = index; // 更新 sortIndex 以反映新的顺序
      });
    },
    //删除
    deleteItem() {
      console.log(this.selectedData);
      this.$modal
        .confirmHTML('是否确认<span style="color: red;">删除</span>该条数据')
        .then(() => {
          this.loading = true;
          return deleteById("FIELD_CONFIG", this.selectedData[0].id);
        })
        .then(() => {
          this.loading = false;
          this.handleField();
          this.$emit("get-list");
          this.$modal.msgSuccess("删除成功");
        })
        .catch(() => {})
        .finally(() => {
          this.loading = false;
        });
    },
    // 选中行
    rowClick(row, column) {
      this.$refs.singleTable.toggleRowSelection(row);
    },
    handleSelection(selectedRows) {
      if (selectedRows.length > 1) {
        this.$refs.singleTable.clearSelection();
        this.$refs.singleTable.toggleRowSelection(selectedRows.pop());
      } else {
        this.selectedData = [];
        this.selectedData.push(selectedRows.pop());
      }
    },

    // 数据校验
    checkRequiredFields(arr, requiredFields) {
      // 创建一个对象来保存结果
      const result = {
        isValid: true,
        invalidIndices: [],
        missingOrInvalidFields: [],
      };

      // 遍历数组中的每个对象
      arr.forEach((item, index) => {
        // 创建一个临时数组来保存当前对象中缺失或无效的字段
        const missingFields = requiredFields.filter(
          (field) =>
            item[field] === undefined ||
            item[field] === null ||
            item[field] === ""
        );

        // 如果有缺失或无效的字段，则更新结果
        if (missingFields.length > 0) {
          result.isValid = false;
          result.invalidIndices.push(index);
          result.missingOrInvalidFields.push({
            index: index,
            fields: missingFields,
          });
        }
      });

      // 返回结果对象
      return result;
    },
    add() {
      // 实施友好默认值：只需填 列名+列标题（SQL表达式可留空，引擎按列名在主表解析）
      this.gridData.push({
        name: this.functionName,
        sortIndex: this.gridData.length,
        fieldType: "text",
        columnWidth: "100",
        align: 0,
        isShow: 1,
        search: 0,
        checked: 0,
        fixedColumn: 0,
        columnSort: 0,
        popover: 0,
      });
    },
    determine() {
      console.log(this.gridData);

      // sqlColumn 可留空：留空时引擎按列名在主表自动解析（逻辑列），故不列入必填
      const requiredFields = [
        "name",
        "fieldType",
        "columnName",
        "headerText",
        "sortIndex",
        "columnWidth",
      ];

      let isValid = this.checkRequiredFields(this.gridData, requiredFields);
      console.log(isValid);

      if (!isValid.isValid) {
        return this.$message.error(
          `请输入列排序${isValid.invalidIndices[0]}行的必填项`
        );
      }

      // 数据排序字段除0外 不得有数字重复
      let repeatNums = this.checkOrderRepeat(this.gridData);
      if (repeatNums.length) {
        return this.$message.error(
          `数据排序字段有重复值 ${repeatNums.join("，")}`
        );
      }

      let data = {
        datas: [...this.gridData],
      };

      saveColumnConfig("FIELD_CONFIG", data).then((res) => {
        console.log(res);
        this.dialogTableVisible = false;
        // this.getList()
        this.$emit("get-list");
        this.$emit("getFieldConfig");
      });
    },
    handleField() {
      console.log(this.functionName);

      getColumnConfig(this.functionName).then((res) => {
        console.log(res);
        this.gridData = res.data;
        this.dialogTableVisible = true;
      });
    },
    // 获取全部数据字典数据
    getDictData() {
      this.dictList = getDictDataList(this.dicts)
        .then((res) => {
          console.log("dictList", res);
          this.dictDataList = res.data;
        })
        .catch((err) => {
          this.$message.error("获取数据字段数据失败");
        });
    },
    // 数据排序字段除0外 不得有数字重复
    checkOrderRepeat(data) {
      let orderIndexArr = data
        .map((el) => el.orderByIndex)
        .filter((el) => el > 0);
      let orderArr = orderIndexArr.map((el) => ({ value: el, repeatTimes: 0 }));

      orderIndexArr.forEach((el) => {
        orderArr.forEach((item) => {
          if (item.value == el) {
            item.repeatTimes++;
          }
        });
      });

      let repeatNums = orderArr
        .filter((el) => el.repeatTimes > 1)
        .map((el) => el.value);

      let uniqueRepeatNums = [...new Set(repeatNums)];
      return uniqueRepeatNums;
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
  margin-bottom: 5px;
}

.table-container {
  height: calc(100vh - 220px);
  overflow: scroll;
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

.tree-box {
  border: 1px solid #ccc;
  height: calc(100vh - 220px);
  overflow: scroll;
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
::v-deep .el-dialog_wrapper {
  z-index: 3000 !important;
}
::v-deep .el-dialog {
  z-index: 3000 !important;
}
::v-deep .el-dialog__body {
  padding: 0 20px !important;
}
::v-deep .el-dialog__body .el-table .cell {
  text-align: center;
}
</style>
