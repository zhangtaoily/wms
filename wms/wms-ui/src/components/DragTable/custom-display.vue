<template>
    <div class="custom-display">
        <div class="content">
            <div class="item">
                <div class="head">
                    <el-checkbox :indeterminate="isIndeterminate" v-model="checkAllColumn" @change="checkAllColumnChange">显示全部表格列</el-checkbox>
                    <el-tooltip effect="dark" content="恢复列序、列宽，显示所有列" placement="left">
                        <el-button size="small" icon="el-icon-refresh" circle @click="restore"></el-button>
                    </el-tooltip>
                </div>
                <div class="box">
                    <el-checkbox-group v-model="checkedColumn" @change="checkedColumnChange">
                        <draggable v-model="columnList" chosenClass="chosen" animation="300" scroll>
                            <transition-group class="item-wrapper">
                                <el-checkbox v-for="item in columnList" :label="item.prop" :key="item.prop">{{ item.label }}</el-checkbox>
                            </transition-group>
                        </draggable>
                    </el-checkbox-group>
                </div>
            </div>
        </div>
        <div class="footer">
            <div class="tip">
                <i class="el-icon-info"></i>
            </div>
            <div class="btns">
                <el-button type="primary" size="small" @click="submitData">确定</el-button>
                <el-button size="small" @click="cancel">取消</el-button>
            </div>
        </div>
    </div>
</template>

<script>
import draggable from 'vuedraggable'

export default {
  components: { draggable },
  props: {
    data: Array,
    keyName: String
  },
  data () {
    return {
      columnList: [],
      checkedColumn: [],
      isIndeterminate: false,
      checkAllColumn: false
    }
  },
  created () {
    this.initData()
  },
  methods: {
    initData () {
      const customDisplay = JSON.parse(localStorage.getItem('custom_display') || '{}')
      if (!customDisplay[this.keyName]) {
        customDisplay[this.keyName] = {}
      }
      const columnLocal = customDisplay[this.keyName].column || []
      const newColumnList = []
      const columnList = this.data
      // 对比columnLocal与columnList中的表格列，取columnList中的字段列表，取columnLocal中字段顺序、width和hide
      columnList.forEach((v, i) => {
        const objLocal = columnLocal.find(v1 => v1.prop === v.prop) || {}
        if (!objLocal.prop) {
          columnLocal.splice(i, 0, {
            prop: v.prop
          })
        }
      })
      columnLocal.forEach(v => {
        const obj = columnList.find(v1 => v1.prop === v.prop) || {}
        if (!obj.prop) return
        // width和hide都以localStorage为准，这样才能体现“用户习惯的记忆”
        const setObj = {
          label: obj.label,
          prop: obj.prop,
          width: v.width,
          hide: v.hide
        }
        newColumnList.push(setObj)
      })
      // 设置复选框组的isIndeterminate和checkAll属性
      // 表格列
      this.columnList = newColumnList
      const list = newColumnList.filter(v => !v.hide).map(v => v.prop)
      this.checkedColumn = list
      this.isIndeterminate = list.length > 0 && list.length < newColumnList.length
      this.checkAllColumn = list.length === newColumnList.length
    },
    // 选择全部表格列
    checkAllColumnChange (isCheckAll) {
      this.checkedColumn = isCheckAll ? this.columnList.map(v => v.prop) : []
      this.isIndeterminate = false
    },
    checkedColumnChange (v) {
      const count = v.length
      this.checkAllColumn = count === this.columnList.length
      this.isIndeterminate = count > 0 && count < this.columnList.length
    },
    restore () {
      this.$emit('change', [])
      this.$emit('close')
      this.$message.success('复原成功')
    },
    submitData () {
      const customDisplay = JSON.parse(localStorage.getItem('custom_display') || '{}')
      const column = (customDisplay[this.keyName] && customDisplay[this.keyName].column) || []
      // 自定义显示修改列序会影响this.columnList
      let columnList = JSON.parse(JSON.stringify(this.columnList))
      columnList.forEach(v => {
        delete v.label // localStorage不用存储label
        delete v.hide
        if (!this.checkedColumn.includes(v.prop) && !v.fixed) {
          v.hide = true
        }
      })
      const leftField = []
      const showField = []
      const hideField = []
      const rightField = []
      // 取columnLocal里的左、右冻结列
      column.forEach(v => {
        if (v.fixed === 'left') {
          leftField.push(v)
        } else if (v.fixed === 'right') {
          rightField.push(v)
        }
      })
      // 取columnList里的显示、隐藏列
      columnList.forEach(v => {
        if (!v.hide) {
          showField.push(v)
        } else if (v.hide) {
          hideField.push(v)
        }
      })
      // 按照【左冻结-显示-隐藏-右冻结】的顺序排列，保存进localStorage
      columnList = [...leftField, ...showField, ...hideField, ...rightField]
      customDisplay[this.keyName].column = columnList
      localStorage.setItem('custom_display', JSON.stringify(customDisplay))
      this.$emit('change', columnList)
      this.$emit('close')
    },
    cancel () {
      this.$emit('close')
    }
  }
}
</script>

<style lang="scss">
.custom-display {
    .content {
        display: flex;
        align-items: stretch;
        justify-content: space-between;
        .item {
            .head {
                display: flex;
                align-items: center;
                justify-content: space-between;
                min-height: 32px;
            }
            .box {
                width: 300px;
                height: calc(100% - 30px);
                min-height: 300px;
                max-height: 430px;
                overflow-y: scroll;
                margin-top: 10px;
                border: 1px solid #ccc;
                padding: 10px;
                .el-checkbox-group {
                    .item-wrapper {
                        display: flex;
                        flex-direction: column;
                        align-items: flex-start;
                        .el-checkbox {
                            margin: 0;
                            padding: 5px;
                        }
                    }
                }
            }
        }
    }
    .footer {
        margin-top: 30px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        .tip {
            visibility: hidden;
            user-select: none;
            color: #333;
        }
    }
}
</style>
