<template>
  <el-dropdown
    split-button
    v-if="disabled"
    :hide-timeout="500"
    @command="commandHandler"
  >
    <span @click="handleExport(1)">导 出</span>
    <el-dropdown-menu slot="dropdown">
      <el-dropdown-item command="export">导 出</el-dropdown-item>
      <el-dropdown-item command="exportAll">全量导出</el-dropdown-item>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>

import { exportExcel } from '@/api/system/common'

export default {
  name: 'ExportBtn',
  props: {
    disabled: {
      type: Boolean,
      default: () => false
    },
    getMultiDataList: {
      type: Function
    },
    getConditions: {
      type: Function
    }
  },
  created() {
    this.functionName = this.$route.query.functionName
  },
  data() {
    return {
      functionName: '',
    }
  },
  methods: {
    /** 导出按钮操作 */
    async handleExport(state) {
      let ids = this.getMultiDataList().map((item) => item.id);
      console.log(ids);
      if(state === 0){
        if (!ids || ids.length === 0) {
          return this.$message.error("请选择导出的数据");
        }
      }
      const data = {
        ids,
        isAll:state,
        conditions: this.getConditions()
      }
      await exportExcel(this.functionName, data);
    },
    // 点击下列项事件
    commandHandler(state) {
      switch (state) {
        case 'export':
          this.handleExport(0)
          break
        case 'exportAll':
          this.handleExport(1)
          break
        default:
          break
      }
    }
  },
  computed: {}
}
</script>

<style></style>
