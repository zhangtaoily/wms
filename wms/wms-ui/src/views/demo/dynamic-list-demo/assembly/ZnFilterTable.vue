<template>
  <div class="zn-filter-table" ref="tableDiv" id="tableBox">
    <!-- 表格  max-height="660px"-->
    <el-table class="table-box" ref="table" :data="tableData" style="width: 100%;" :max-height="tableHeight" border
      stripe @selection-change="handleSelectionChange" :header-cell-style="{ 'text-align': 'center', }">
      <el-table-column type="selection" width="55" align="center" v-if="multiple"></el-table-column>
      <el-table-column v-for="(item, index) in finallyColumns" :key="item.id" :label="item.label"
        :align="item.type === 'number_range' ? 'right' : (item.type === 'date_range' || item.type === 'datetime_range' || item.type === 'select' || item.type === 'radio') ? 'center' : 'left'"
        :prop="item.name" min-width="130" show-overflow-tooltip>
        <!-- <template slot="header">
            <type-popover :columnIndex="index" :column="item" :filterOptions="item.param" @tableUpdate="tableUpdate"></type-popover>
          </template> -->
        <template #default="{ row }">
          <!-- 每一列涉及value值判断显示label ,以及状态颜色 -->
          <!-- <span v-if="item.extra.columnStyle == 'labelTags'" :class="item.extra.labelTags[row.type.value]">
              {{ row.type.label }}
            </span> -->
          <span v-if="item.type === 'select' || item.type === 'radio'">
            <el-tag>{{ typeFormat(row[item.name], item.param) }}</el-tag>
          </span>
          <span v-else-if="item.type === 'street'">
            {{ streetFormat(row[item.name], item.param) }}
          </span>
          <span v-else>{{ row[item.name] }}</span>
        </template>
      </el-table-column>
      <!-- 如有操作列 ↓-->
      <el-table-column label="操作" align="center" width="150" class="table-fixed-right" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handleSelect(scope.row)">详情</el-button>
          <!-- <el-button size="mini" type="text" @click="handleDelete(scope.row)" class="mpc_del">删除</el-button> -->
          <el-dropdown size="small" type="primary" trigger="hover" style="margin-left: 10px;">
            <span style="color: #1890FF;font-size: 12px;">更多</span>
            <el-dropdown-menu slot="dropdown">
              <div v-for="item in buttons" :key="item.value">
                <el-dropdown-item v-if="item.value === 'del'" @click.native="handleDelete(scope.row)" class="mpc_del"
                  v-hasPermi="item.hasPermi">{{ item.label }}</el-dropdown-item>
                <el-dropdown-item v-else @click.native="handleUpdate(scope.row, item.value)"
                  style="font-size: 12px;color: #1890FF;" v-hasPermi="item.hasPermi">{{ item.label }}</el-dropdown-item>
              </div>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
<script>
import TypePopover from './TypePopover'
import ZnIcon from './ZnIcon'

export default {
  name: 'ZnFilterTable',
  components: { TypePopover, ZnIcon },
  props: {
    tableData: {
      type: Array, // table数据
      default: () => [],
    },
    finallyColumns: {
      type: Array, // table数据
      default: () => [],
    },
    multiple: {
      type: Boolean, // table数据
      default: () => false,
    },
    deatilsPath: {
      type: String, // table数据
      default: () => '',
    },
    buttons: {
      type: Array, // table数据
      default: () => [],
    },
  },
  data() {
    return {
      conditionList: [],
      multipleSelection: [], //table多选数据
      tableHeight: 0,
      hasHorizontalScroll: false
    }
  },
  computed: {},
  created() {
    this.tableHeight = window.innerHeight - 346
  },
  mounted() {
    //动态更改table高度
    this.handleTableHeight();
  },
  methods: {
    handleTableHeight: function () {
      var _this = this;
      window.onresize = () => {
        _this.tableHeight = window.innerHeight - 346
      };

    },
    handleSelectionChange(val) {
      this.multipleSelection = val
      this.$emit('selectList', this.multipleSelection)
    },
    handleSelect(item) {
      this.$emit('fetch-edit', item)
    },
    handleUpdate(item, value) {
      this.$emit('fetch-update', item, value)
    },
    handleDelete(item) {
      this.$emit('fetch-del', item)
    },
    // 自定义检索(popover组件(data为对象)和tag组件(data为数组))发射出来的事件
    tableUpdate(data) {
      console.log(data)
      let flag = true
      // 筛选条件如果已经存在，就更新,注意判别传递过来的数据类型
      // arr.constructor === Array
      if (this.conditionList.length === 0) {
        this.conditionList.push(data) // 如果没有就添加
        flag = false
      } else if (this.conditionList.length > 0) {
        this.conditionList.forEach((item, index) => {
          if (item.fieldName == data.fieldName) {
            item.value = data.value
            flag = false
          }
        })
      }
      if (flag) {
        this.conditionList.push(data.fieldName) // 如果没有就添加
      }
      this.$parent.fetchData(this.conditionList)
    },
    toDeatils(row) {
      if (this.deatilsPath) {
        this.$router.push({ path: this.deatilsPath, query: row.id })
      } else {
        this.$baseMessage(
          '请配置所需要跳转的路径',
          'warning',
          'zn-hey-message-warning'
        )
      }
    },
    // 拨打电话
    makeACall() {

    },
    //类型翻译
    typeFormat(item, params) {
      return this.selectType(params, item);
    },
    // 回显
    selectType(datas, value) {
      var actions = [];
      Object.keys(datas).some((key) => {
        if (datas[key].value == ('' + value)) {
          actions.push(datas[key].label);
          return true;
        }
      })
      return actions.join('');
    },
    //树形回显
    streetFormat(item, params) {
      return this.streetType(params, item);
    },
    // 回显
    streetType(datas, value) {
      var actions = [];
      actions.push(this.getTreeName(datas, value))
      return actions.join('');
    },
    //递归
    getTreeName(list, id) {
      for (let i = 0; i < list.length; i++) {
        if (list[i].id === id) {
          return list[i].name
        } else if (list[i].children && list[i].children.length > 0) {
          let res = this.getTreeName(list[i].children, id)
          if (res) {
            return res
          }
        }
      }
    }
  },
}
</script>
<style>
.zn-filter-table {
  /* border: 1px solid blue; */
}


.zn-filter-table .el-table-column--selection .cell {
  padding-right: 10px !important;
}

.zn-filter-table .el-table__fixed-body-wrapper .el-table__body {
  /* 这个得自己调试看多少合适 */
  padding-bottom: 20px;
}

/* 固定列高度与表格高度不一致 */

.zn-filter-table .el-table__fixed,
.zn-filter-table .el-table__fixed-right {
  height: calc(100% - 25px) !important;
  /* height: 100% !important; */
  right: 0 !important;
  /* 这个得自己调试看多少合适 */
  bottom: 12px !important;
}

.zn-filter-table .el-table__fixed::before,
.zn-filter-table .el-table__fixed-right::before {
  background: transparent !important;
  /* 去掉固定列下方的横线， 样式优化 */
  display: none;
}

/* 让滚动条区域撑开 */

.zn-filter-table .el-table__body-wrapper::-webkit-scrollbar {
  /* 水平滚动条宽度 */
  height: 20px;
  width: 0px !important;
}

.zn-filter-table .el-table__body-wrapper::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.3);
  /* 滚动条颜色 */
}

.zn-filter-table .el-table__body-wrapper::-webkit-scrollbar-track {
  background-color: #f0f0f0;
  /* 滚动条轨道颜色 */
}

.table-box {
  overflow-x: auto;

  /* 防止内部内容换行 */
  white-space: nowrap;
}

.table-fixed-right {}
</style>
<style scoped>
.mpc_del {
  color: red !important;
}

::v-deep .el-table__column-filter-trigger {
  /* // 占位，解决点击自己写的自定义筛选 会冒泡到排序 */
  display: none !important;
}

::v-deep.filter-table {

  /* // table状态标签颜色定义 */
  [class*='table-status'] {
    display: inline-block;
    border-radius: 2px;
    padding: 0px 12px;
  }

  /* // 蓝色 */
  [class*='table-status-blue'] {
    background: #e6effb;
    color: #005bd9;
  }

  /* // 棕色 */
  [class*='table-status-brown'] {
    background: #fff6ec;
    color: #ffa336;
  }

  /* // 绿色 */
  [class*='table-status-green'] {
    background: #e8fff0;
    color: #00b47e;
  }

  /* // 黄色 */
  [class*='table-status-yellow'] {
    background: #fffae8;
    color: #f9c200;
  }

  /* // 粉色 */
  [class*='table-status-pink'] {
    background: #ffece8;
    color: #f53f3f;
  }

  /* // 白色 */
  [class*='table-status-white'] {
    background: #f2f3f5;
    color: #1d2129;
  }
}
</style>