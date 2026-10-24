<template>
  <div class="table-wrapper">
    <el-table
      ref="dragTable"
      class="drag-table sub-table"
      :class="{
        'drag-table_moving': dragState.dragging,
        'is-disabled-table': disabled,
      }"
      v-bind="$attrs"
      v-on="$listeners"
      :data="data"
      :cell-class-name="cellClassName"
      :header-cell-class-name="headerCellClassName"
      @header-dragend="headerDragend"
      style="width: 100%; margin-top: 5px"
      :header-row-style="
        () => {
          return 'line-height:30px';
        }
      "
    >
      <dd-table-column
        v-for="(col, idx) in visibleColumn"
        :key="idx"
        v-bind="col"
        :column-key="idx.toString()"
        :mousedown="handleMouseDown"
        :mousemove="handleMouseMove"
        :is-last-column="idx === visibleColumn.length - 1"
        @customDisplay="handleDisplay"
      ></dd-table-column>
    </el-table>
    <el-dialog
      :disabled="false"
      title="自定义显示"
      :visible.sync="showCustomDisplay"
      :close-on-click-modal="false"
      append-to-body
      width="340px"
    >
      <el-form :disabled="false">
        <custom-display
          v-if="showCustomDisplay"
          :data="displayData"
          :key-name="keyName"
          @change="displayChange"
          @close="showCustomDisplay = false"
        ></custom-display>
      </el-form>
    </el-dialog>
  </div>
</template>

<script>
import _ from "lodash";
import DdTableColumn from "./dd-table-column.vue";
import CustomDisplay from "./custom-display.vue";

export default {
  name: "DragTable",
  components: { DdTableColumn, CustomDisplay },
  props: {
    data: {
      type: Array,
      default: () => [],
    },
    externalHeaderCellClassName: {
      type: Function,
      default: null,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    address: String,
  },
  data() {
    return {
      allColumn: [], // 所有列
      visibleColumn: [], // 可见列
      dragState: {
        start: -1, // 起始元素的index
        end: -1, // 移动鼠标时所覆盖的元素index
        dragging: false, // 是否正在拖动
        direction: "", // 拖动方向
      },
      scrollLeft: 0, // 表体左偏移量
      flag: 0, // 用于规定：由selectedColumn变化而导致的visibleColumn变化，不执行saveData方法
      displayData: [], // 自定义显示数据
      showCustomDisplay: false, // 是否打开自定义显示弹框
      selectedColumn: [], // 选择显示的列
      keyName: "", // 当前表格在本地存储的键名
    };
  },
  watch: {
    allColumn() {
      this.initData();
    },
    selectedColumn(val) {
      if (val.length) {
        this.flag = 1;
        this.initData();
      } else {
        this.restore();
      }
    },
    visibleColumn(val) {
      if (this.flag === 0) {
        this.saveData(val);
      } else {
        this.flag = 0;
      }
    },
  },
  mounted() {
    this.dom = this.$refs.dragTable.bodyWrapper;
    // 如果表格滑动方向是水平方向（而非竖直），在滑动结束后等待50毫秒，重置虚拟容器（div.virtual）的位置
    this.dom.addEventListener(
      "scroll",
      _.debounce(() => {
        const { scrollLeft } = this.dom;
        const leftDiff = scrollLeft - this.scrollLeft;
        this.scrollLeft = scrollLeft;
        if (leftDiff !== 0) {
          this.setVirtual(true);
        }
      }, 50)
    );
    this.allColumn = this.getAllColumn();
    this.$nextTick(() => {
      this.$refs.dragTable.doLayout();
    });
  },
  methods: {
    getTable() {
      return this.$refs.dragTable;
    },
    // 获取所有表格列（template里显式书写的el-table-column列表）
    getAllColumn() {
      const slotDefault = this.$slots.default.filter((v) => v.tag).slice(0);
      const arr = [];
      for (let i = 0; i < slotDefault.length; i++) {
        const vnode = slotDefault[i];
        const propsData = vnode.componentOptions.propsData;
        propsData.cellType = "slots";
        // 手动为'expand', 'selection', 'index'三类特殊列设置prop，值就是其type
        if (propsData.type) {
          propsData.prop = propsData.type;
        }
        const scopedSlots = vnode.data.scopedSlots;
        const renderCell =
          scopedSlots && scopedSlots.default
            ? scopedSlots.default
            : (scope) => <span>{scope.row[propsData.prop]}</span>;
        arr.push({ ...propsData, renderCell });
      }
      return arr;
    },

    // 初始化数据
    initData() {
      const routeName = this.$route.name;
      const address = this.address;
      const keyName = address ? `${routeName}_${address}` : `${routeName}`;
      this.keyName = keyName;
      const customDisplay = JSON.parse(
        localStorage.getItem("custom_display") || "{}"
      );
      const columnLocal =
        (customDisplay[keyName] && customDisplay[keyName].column) || [];
      if (!columnLocal.length) {
        // 该页还没有记忆数据

        this.visibleColumn = this.allColumn;
      } else {
        // 该页已有记忆数据
        // 沿用最新表格列的数据（字段）
        // 可能的场景：未来的某个迭代需要新增表格列，可是用户的本地没有这些新列，就需要新增这些数据（未来的迭代删除列则不用处理）。
        this.allColumn.forEach((v, i) => {
          const obj = columnLocal.find((t) => t.prop === v.prop) || {};
          if (!obj.prop) {
            // 指定在原数组位置插入新列
            columnLocal.splice(i, 0, {
              prop: v.prop,
              width: v.width,
            });
            // 头部插入新列
            // columnLocal.unshift({
            //   prop: v.prop,
            //   width: v.width,
            // });
          }
        });
        // 沿用columnLocal的列序
        const visibleColumn = [];
        columnLocal.forEach((v) => {
          if (v.hide) return; // 忽略已隐藏的列
          const obj = this.allColumn.find((v1) => v1.prop === v.prop) || {};
          if (!obj.prop) return; // 忽略已废弃的列
          const setObj = {
            ...obj,
            width: v.width, // 沿用columnLocal的列宽
          };
          visibleColumn.push({ ...setObj });
        });
        this.visibleColumn = visibleColumn;
      }
    },

    // 复原列序列宽和显示隐藏
    restore() {
      this.visibleColumn = this.getAllColumn();
    },

    // 保存到localStorage
    saveData(val) {
      let columnList = [];
      // 沿用val（visibleColumn）的列序（拖动易位）
      val.forEach((v) => {
        const obj = this.allColumn.find((v1) => v1.prop === v.prop) || {};
        const setObj = {
          prop: obj.prop,
          width: v.width,
        };
        if (obj.fixed) {
          setObj.fixed = obj.fixed;
        }
        columnList.push(setObj);
      });
      // val（visibleColumn）没有的字段都标为hide（取消勾选字段后刷新）
      this.allColumn.forEach((v) => {
        const obj = val.find((v1) => v1.prop === v.prop) || {};
        if (!obj.prop) {
          const setObj = {
            prop: v.prop,
            width: v.width,
            hide: true,
          };
          if (v.fixed) {
            setObj.fixed = v.fixed;
          }
          columnList.unshift(setObj);
        }
      });
      const leftField = [];
      const showField = [];
      const hideField = [];
      const rightField = [];
      columnList.forEach((v) => {
        if (v.fixed === "left") {
          leftField.push(v);
        } else if (v.fixed === "right") {
          rightField.push(v);
        } else if (!v.hide) {
          showField.push(v);
        } else if (v.hide) {
          hideField.push(v);
        }
      });
      // 按照【左冻结-显示-隐藏-右冻结】的顺序排列，保存进localStorage
      columnList = [...leftField, ...showField, ...hideField, ...rightField];
      const customDisplay = JSON.parse(
        localStorage.getItem("custom_display") || "{}"
      );
      if (!customDisplay[this.keyName]) {
        customDisplay[this.keyName] = {};
      }
      customDisplay[this.keyName].column = columnList;
      localStorage.setItem("custom_display", JSON.stringify(customDisplay));
    },

    // 设置虚拟容器的宽高和位置
    setVirtual(hide = false) {
      this.$nextTick(() => {
        const table = this.$el;
        const virtual = table.getElementsByClassName("virtual");
        const scrollLeft = table.querySelector(
          ".el-table__body-wrapper"
        ).scrollLeft;
        for (const item of virtual) {
          item.style.height = `${table.clientHeight - 1}px`;
          item.style.width = hide
            ? 0
            : `${item.parentElement.parentElement.clientWidth}px`;
          item.style.top = `${table.getBoundingClientRect().top + 1}px`;
          // 这里减10，因为css里面.el-table th .virtual的marginLeft是-10px
          item.style.marginLeft = `${-scrollLeft - 10}px`;
        }
      });
    },

    // 按下鼠标
    handleMouseDown(e, column, tIdx) {
      if (!column.fixed) {
        this.dragState.dragging = true;
        this.dragState.start = tIdx;
        this.setVirtual();
        // 绑定松开鼠标事件
        document.addEventListener("mouseup", this.handleMouseUp);
      }
    },

    // 拖动中
    handleMouseMove(e, column, tIdx) {
      if (!this.dragState.dragging) return;
      // 获取左右冻结列数量
      const countLeft = this.visibleColumn.filter(
        (v) => v.fixed === "left"
      ).length;
      const countRight = this.visibleColumn.filter(
        (v) => v.fixed === "right"
      ).length;
      let index = tIdx;
      if (index !== this.dragState.start) {
        this.dragState.direction =
          index - this.dragState.start < 0 ? "left" : "right";
        // 禁止向左侧冻结区域拖拽
        if (index <= countLeft - 1) {
          index = countLeft;
        }
        // 禁止向右侧冻结区域拖拽
        if (index >= this.visibleColumn.length - countRight) {
          index = this.visibleColumn.length - countRight - 1;
        }
        this.dragState.end = index;
      } else {
        this.dragState.direction = "";
      }
    },

    // 松开鼠标
    handleMouseUp() {
      if (this.dragState.direction !== "") {
        this.dragColumn(this.dragState);
      }
      // 恢复拖动状态
      this.dragState = {
        start: -1,
        end: -1,
        dragging: false,
        direction: "",
      };
      this.setVirtual(true);
      document.removeEventListener("mouseup", this.handleMouseUp);
    },

    // 拖动易位
    dragColumn({ start, end, direction }) {
      const tempData = [];
      const isLeft = direction === "left";
      const min = isLeft ? end : start - 1;
      const max = isLeft ? start + 1 : end;

      for (let i = 0; i < this.visibleColumn.length; i++) {
        if (i === end) {
          tempData.push(this.visibleColumn[start]);
        } else if (i > min && i < max) {
          tempData.push(this.visibleColumn[isLeft ? i - 1 : i + 1]);
        } else {
          tempData.push(this.visibleColumn[i]);
        }
      }
      this.visibleColumn = tempData;
    },

    // 设置目标列的样式（左或右虚线边框）
    headerCellClassName({ column, columnIndex }) {
      // 调用外部传入的类名函数
      let externalClass = "";
      if (typeof this.externalHeaderCellClassName === "function") {
        externalClass =
          this.externalHeaderCellClassName({ column, columnIndex }) || "";
      }

      // 内部类名逻辑
      const { start, end, direction } = this.dragState;
      const activeClass = columnIndex === end ? `darg_active_${direction}` : "";
      const startClass = columnIndex === start ? "darg_start" : "";
      const internalClass = `${activeClass} ${startClass}`.trim();

      // 合并外部和内部类名
      return [externalClass, internalClass].filter(Boolean).join(" ");
    },

    // 设置当前列的样式（浅灰色）
    cellClassName({ column, columnIndex }) {
      return columnIndex === this.dragState.start ? "darg_start" : "";
    },

    // 调整列宽
    headerDragend(newWidth, oldWidth, column) {
      const arr = this.visibleColumn;
      const idx = arr.findIndex((v) => v.prop === column.property);
      const value = { ...arr[idx], width: newWidth.toString() };
      this.$set(this.visibleColumn, idx, value);
    },

    // 自定义显示
    handleDisplay() {
      this.displayData = this.getDisplayData();
      this.showCustomDisplay = true;
    },

    // 获取显示数据
    getDisplayData() {
      const slotDefault = this.$slots.default.filter((v) => v.tag).slice(0);
      const arr = [];
      for (let i = 0; i < slotDefault.length; i++) {
        const vnode = slotDefault[i];
        const propsData = vnode.componentOptions.propsData;
        arr.push({ ...propsData });
      }
      const columnList = arr
        .filter((v) => !v.necessary && !v.fixed)
        .map((v) => {
          const obj = {
            label: v.label,
            prop: v.prop,
          };
          if (v.hide) {
            obj.hide = v.hide;
          }
          return obj;
        });
      return columnList;
    },

    // 自定义显示变化
    displayChange(v) {
      this.selectedColumn = v || [];
      this.$nextTick(() => {
        this.$refs.dragTable.doLayout();
      });
    },
  },
};
</script>

<style lang="scss">
:root {
  --color-primary: #7794f4;
}

/* 基础工具类 */
.mr-5 {
  margin-right: 5px;
}

/* 表格行高亮 */
::v-deep .el-table__body tr.current-row > td.el-table__cell {
  background: var(--color-primary) !important;
}

/* 子表格全局样式 */
::v-deep .sub-table {
  /* 输入框样式 */
  .el-input__inner {
    border: 0;
    border-radius: 0;
    padding: 0 5px;
    height: 40px;

    /* 错误状态 */
    .is-error & {
      border-color: #ffefef;
      background-color: #ffefef;
    }

    /* 禁用状态 */
    .is-disabled & {
      pointer-events: none;
    }
  }

  /* 表单项调整 */
  .el-form-item__content {
    margin-left: 0 !important;

    .is-error & {
      margin-bottom: 12px !important;
    }
  }

  /* 错误提示 */
  .is-error .el-form-item__error {
    background: white;
  }

  /* 选择器 */
  .el-select {
    width: 100%;

    .is-disabled {
      pointer-events: none;
    }
  }
}

/* 单元格基础样式 */
::v-deep .el-table {
  .cell {
    padding: 0 !important;
    line-height: 1.5 !important; /* 修复行高问题 */
  }

  .el-table__cell {
    text-align: center;

    /* 表头单元格 */
    &.is-leaf {
      text-align: right;
      padding: 0 5px;
    }

    /* 居中对齐 */
    &.is-center,
    &.is-center .cell {
      text-align: center;
    }
  }

  /* 高亮表头 */
  .highlight-header {
    background-color: var(--color-primary) !important;
    font-weight: bold;
  }
}

/* 禁用行样式 */
.disable-row {
  pointer-events: none;
  opacity: 0.5;
  cursor: default;
}

/* 表头样式 */
::v-deep .el-table th.el-table__cell > .cell {
  white-space: nowrap;
  text-align: center;
}

/* 页脚调整 */
::v-deep .el-table__footer-wrapper tbody td.el-table__cell {
  height: 20px;
  background: white;
}

/* 滚动条优化 */
::v-deep .sub-table .el-table__body-wrapper {
  min-height: 120px;

  &::-webkit-scrollbar {
    width: 13px !important;
    height: 20px !important;
    background: transparent;
  }
}

/* 按钮容器 */
.button-box {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  flex-wrap: wrap;
}

/* 可调整表格容器 */
.resizable-table-container {
  position: relative;
  margin-top: 5px;

  .drag-handle {
    position: relative;
    height: 7px;
    cursor: row-resize;
    background-color: transparent;
    margin-top: 5px;

    &:hover {
      background-color: #409eff;
    }
  }
}

/* 拖拽状态 */
body.resizing-active {
  cursor: row-resize;
  user-select: none;
}

/* 表格拖拽交互 */
.table-wrapper {
  flex: auto;

  .drag-table {
    min-height: 100%;

    /* 拖拽中状态 */
    &.drag-table_moving {
      th .thead-cell.fixed-cell,
      .el-table__fixed,
      .el-table__fixed-right {
        cursor: not-allowed;
      }
    }

    /* 拖拽起始 */
    .darg_start {
      background-color: #f3f3f3;
    }

    /* 表头单元格 */
    th {
      /* 拖拽指示器 */
      .virtual {
        position: fixed;
        display: block;
        width: 0;
        height: 0;
        margin-left: -10px;
        z-index: 1;
      }

      /* 左侧拖拽指示 */
      &.darg_active_left .virtual {
        border-left: 2px dashed var(--color-primary);
        z-index: 99;

        &::before,
        &::after {
          content: "";
          position: absolute;
          left: -6px;
          width: 0;
          height: 0;
          border-left: 5px solid transparent;
          border-right: 5px solid transparent;
        }

        &::before {
          top: 0;
          border-top: 10px solid var(--color-primary);
        }

        &::after {
          bottom: 0;
          border-bottom: 10px solid var(--color-primary);
        }
      }

      /* 右侧拖拽指示 */
      &.darg_active_right .virtual {
        border-right: 2px dashed var(--color-primary);
        z-index: 99;

        &::before,
        &::after {
          content: "";
          position: absolute;
          right: -6px;
          width: 0;
          height: 0;
          border-left: 5px solid transparent;
          border-right: 5px solid transparent;
        }

        &::before {
          top: 0;
          border-top: 10px solid var(--color-primary);
        }

        &::after {
          bottom: 0;
          border-bottom: 10px solid var(--color-primary);
        }
      }
    }

    /* 表头单元格内容 */
    .thead-cell {
      padding: 0;
      display: inline-flex;
      flex-direction: column;
      align-items: left;
      cursor: ew-resize;
      overflow: initial;

      /* 特殊状态 */
      &.fixed-cell {
        cursor: unset;
      }

      &.last-column {
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: space-between;
      }

      /* 操作图标 */
      .el-icon-edit {
        margin-left: 10px;
        padding: 10px;
        cursor: pointer;
        color: var(--color-primary);
        z-index: 2;
      }

      a {
        display: flex;
        align-items: center;
        line-height: 34px;
        z-index: 2;

        &:hover,
        &:active {
          color: var(--color-primary);
        }
      }
    }

    /* 固定列补丁 */
    .el-table__fixed-right-patch {
      z-index: 3;
    }
  }
}
</style>
