<template>
  <div>
    <el-button size="mini" @click="fieldClick">显示/隐藏列</el-button>
    <el-dialog width="60%" title="显示隐藏列" :visible.sync="dialogVisible">
      <el-row>
        <el-col :span="18">
          <el-table
            ref="tableNode"
            border
            :data="filteredTableList"
            class="table"
            row-key="sortIndex"
            :key="tableKey"
            highlight-current-row
            @row-click="rowClick"
          >
            <el-table-column
              type="index"
              label="序号"
              width="55"
              align="center"
            />
            <el-table-column prop="headerText" label="列名" width="200">
              <template slot="header" slot-scope="scoped">
                <span>{{ scoped.column.label }}</span>
                <br />
                <el-input
                  placeholder="过滤条件"
                  class="header-filter-input"
                  type="text"
                  v-model="filterInputValue"
                  clearable
                ></el-input>
              </template>
              <template slot-scope="scope">
                <div :class="'my-handle'">
                  {{ scope.row.headerText }}
                </div>
              </template>
            </el-table-column>
            <el-table-column label="显示" property="isShow" width="80">
              <template slot="header" slot-scope="scoped">
                <el-checkbox
                  :true-label="1"
                  :false-label="0"
                  v-model="allShow"
                  @change="allShowChangeHandler"
                />
                <span>{{ scoped.column.label }}</span>
              </template>
              <template slot-scope="scope">
                <el-checkbox
                  :true-label="1"
                  :false-label="0"
                  v-model="scope.row.isShow"
                ></el-checkbox>
              </template>
            </el-table-column>
            <el-table-column label="宽度" property="columnWidth" width="80">
              <template slot-scope="scope">
                <el-input
                  v-model="scope.row.columnWidth"
                  placeholder="请输入"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-col>
        <el-col :span="6">
          <div class="button-group">
            <el-button
              :disabled="selectedData.length == 0"
              icon="el-icon-arrow-up"
              @click="moveUp"
              >上 移
            </el-button>
            <el-button
              :disabled="selectedData.length == 0"
              icon="el-icon-arrow-down"
              @click="moveDown"
              >下 移
            </el-button>
            <el-button icon="el-icon-bottom" @click="updatedList"
              >更新列
            </el-button>
            <el-button icon="el-icon-setting" @click="resetDefaultHandler"
              >默认值
            </el-button>
          </div>
          <div class="footer">
            <el-button :loading="buttonLoading" @click="dialogVisible = false"
              >取 消</el-button
            >
            <el-button
              :loading="buttonLoading"
              type="primary"
              @click="submitHandler"
              >确 定</el-button
            >
          </div>
        </el-col>
      </el-row>
      <!-- <span slot="footer" class="dialog-footer">
        <el-button :loading="buttonLoading" @click="dialogVisible = false"
          >取 消</el-button
        >
        <el-button
          :loading="buttonLoading"
          type="primary"
          @click="submitHandler"
          >确 定</el-button
        >
      </span> -->
    </el-dialog>
  </div>
</template>

<script>
import Sortable from "sortablejs";
import {
  save,
  getTableList,
  resetFieldShowConfig,
  updatedFieldShowConfig,
} from "@/api/system/common";
export default {
  name: "FieldManagement",
  props: {
    options: {
      type: Array, // table数据
      default: () => [],
    },
    functionName: String,
  },
  data() {
    return {
      dialogVisible: false,
      tableList: [],
      tableKey: Date.now(),
      buttonLoading: false,
      // 查询参数
      queryParams: {
        functionName: "B_FIELD_SHOW",
        pageNum: 1,
        pageSize: 200,
      },
      // 列名过滤值
      filterInputValue: null,
      // 全选
      allShow: 0,
      // 选中的行
      selectedData: [],
    };
  },
  computed: {
    // 过滤后的数据
    filteredTableList() {
      if (this.filterInputValue) {
        // 根据名称进行模糊过滤
        const regex = new RegExp(this.filterInputValue, "i"); // 'i' 表示忽略大小写
        return this.tableList.filter((item) => regex.test(item.headerText));
      } else {
        return this.tableList;
      }
    },
  },
  watch: {
    tableList: {
      handler(newVal) {
        // 检查是否所有项的 isShow 都为 true
        const allChecked = newVal.every((item) => item.isShow);
        // 更新全选框的值
        this.allShow = allChecked ? 1 : 0;
      },
      deep: true, // 深度监听
    },
  },
  created() {},
  mounted() {},
  methods: {
    getList() {
      const userId = this.$store.state.user.id;
      const conditions = [
        {
          logicalOperator: "AND",
          fieldType: "TEXT",
          field: "moduleName",
          operate: "EQ",
          value: this.functionName,
          filterGroup: "ADVANCE",
        },
        {
          logicalOperator: "AND",
          fieldType: "TEXT",
          field: "userId",
          operate: "EQ",
          value: userId,
          filterGroup: "ADVANCE",
        },
      ];
      if (!!this.$route.query?.dialogName) {
        conditions[0] = {
          logicalOperator: "AND",
          fieldType: "TEXT",
          field: "moduleName",
          operate: "EQ",
          value: this.$route.query?.dialogName,
          filterGroup: "ADVANCE",
        };
      }

      this.$set(this.queryParams, "conditions", conditions);
      return getTableList(this.queryParams)
        .then((res) => {
          if (res.rows && res.rows.length != 0) {
            this.tableList = res.rows;
          } else {
            this.tableList = this.options
              // .filter((item) => item.columnWidth != 0)
              .map((item) => ({
                userId,
                moduleName: !!this.$route.query?.dialogName
                  ? this.$route.query?.dialogName
                  : this.functionName,
                isShow: item.columnWidth != 0 ? 1 : 0,
                columnWidth: item.columnWidth,
                sortIndex: item.sortIndex,
                fieldConfigId: item.id,
                headerText: item.headerText,
              }));
          }
        })
        .finally(() => {
          this.$nextTick(() => {
            this.pullSort(); //声明表格拖动排序方法
          });
        });
    },
    fieldClick() {
      this.getList();
      this.dialogVisible = true;
    }, //表格拖动排序方法
    pullSort() {
      // 通过ref获取Dom节点
      const el = this.$refs.tableNode.$el.querySelectorAll(
        ".el-table__body-wrapper > table > tbody"
      )[0];
      this.sortable = new Sortable.create(el, {
        handle: ".my-handle", //格式为简单css选择器的字符串，使列表单元中符合选择器的元素成为拖                 动的手柄，只有按住拖动手柄才能使列表单元进行拖动
        animation: 600, //拖拽动画(毫秒)
        setData: function (dataTransfer) {
          dataTransfer.setData("Text", "");
        },
        // 开始拖拽
        onStart: (evt) => {
          // 更改拖拽元素的背景色
          // evt.item.style.backgroundColor = "#cce8ff";
        },
        // 结束拖拽
        onEnd: (evt) => {
          const { oldIndex, newIndex } = evt;
          // 创建一个新数组，这是为了确保原数组不被直接修改
          const newDataArray = [...this.tableList];
          // 使用splice方法移动元素到新位置
          const movedItem = newDataArray.splice(oldIndex, 1)[0];
          newDataArray.splice(newIndex, 0, movedItem);
          // 更新组件的data
          this.tableList = newDataArray;
        },
      });
    },
    // 更新index
    updateSortIndex() {
      this.tableList.forEach((row, index) => {
        row.sortIndex = index; // 更新 sortIndex 以反映新的顺序
      });
    },
    // 通过这个方法，给每一个sonTable添加一个index
    tableRowClassName({ row, rowIndex }) {
      row.sortIndex = rowIndex + 1;
    },
    submitHandler() {
      this.buttonLoading = true;
      this.updateSortIndex();
      // 向父组件发送数据
      let list = [];
      this.tableList.forEach((item) => {
        if (item.isShow) {
          list.push(item.headerText);
        }
      });
      this.$emit("setColumns", this.options, list);
      save("B_FIELD_SHOW", { datas: this.tableList })
        .then((res) => {
          this.$emit("getList");
          this.dialogVisible = false;
          this.$message.success("保存成功");
        })
        .finally(() => {
          this.buttonLoading = false;
        });
    },
    // 全选按钮
    allShowChangeHandler(value) {
      this.tableList.forEach((item) => {
        item.isShow = value;
      });
    },
    rowClick(row) {
      // console.log(row);
      this.selectedData = [row];
    },
    //上移
    moveUp() {
      if (this.selectedData.length === 0) return;
      // 获取选中行的索引
      const selectedIndexes = this.selectedData.map((row) =>
        this.tableList.indexOf(row)
      );
      selectedIndexes.sort((a, b) => a - b); // 从小到大排序
      // 确定目标位置（最小索引的位置的上面）
      const targetIndex = selectedIndexes[0] - 1;

      // 移除选中的行
      const rowsToMove = [];
      for (let i = selectedIndexes.length - 1; i >= 0; i--) {
        const index = selectedIndexes[i];
        rowsToMove.unshift(this.tableList.splice(index, 1)[0]); // 从后到前移除
      }

      // 插入行到目标位置
      const insertIndex = Math.max(targetIndex, 0);
      this.tableList.splice(insertIndex, 0, ...rowsToMove);
      this.updateSortIndex();
    },
    // 下移
    moveDown() {
      if (this.selectedData.length === 0) return;
      // 获取选中行的索引
      const selectedIndexes = this.selectedData.map((row) =>
        this.tableList.indexOf(row)
      );
      selectedIndexes.sort((a, b) => a - b); // 从小到大排序

      let targetIndex;
      if (selectedIndexes.length == 1) {
        targetIndex = selectedIndexes[0] + 1;
      } else {
        targetIndex = Math.min(
          selectedIndexes[selectedIndexes.length - 1],
          this.tableList.length
        );
      }
      // 确定目标位置（最小索引的位置的上面）
      // const targetIndex = selectedIndexes[0] - 1;
      // 移除选中的行
      const rowsToMove = [];
      for (let i = selectedIndexes.length - 1; i >= 0; i--) {
        const index = selectedIndexes[i];
        rowsToMove.unshift(this.tableList.splice(index, 1)[0]); // 从后到前移除
      }
      // 插入行到目标位置
      const insertIndex = Math.max(targetIndex, 0);
      this.tableList.splice(insertIndex, 0, ...rowsToMove);
    },
    // 恢复默认值
    resetDefaultHandler() {
      resetFieldShowConfig(
        this.$route.query.dialogName || this.functionName
      ).then((res) => {
        this.$message.success("成功恢复");
        this.dialogVisible = false;
        this.$emit("getList");
      });
    },
    // 更新列
    updatedList() {
      updatedFieldShowConfig(
        this.$route.query.dialogName || this.functionName
      ).then((res) => {
        this.$message.success("更新成功");
        this.dialogVisible = false;
        this.$emit("getList");
      });
    },
  },
};
</script>

<style lang="scss" scoped>
::v-deep .table {
  .el-table__body-wrapper {
    overflow: scroll;
    height: 500px;
  }
}
.my-handle {
  cursor: move;
  cursor: -webkit-grabbing;
}
::v-deep .button-group {
  height: 500px;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  column-gap: 20;
  .el-button {
    margin: 0px;
    margin-top: 10px;
  }
}
.footer {
  display: flex;
  justify-content: space-between;
  padding: 0 30px;
}
</style>
