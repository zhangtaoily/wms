<template>
  <!-- 每个table表头的popover -->
  <!-- 注意：逻辑部分尽量不好写到这个组件内，因为这个组件是根据外面table循环创建的，在这里写逻辑会非常影响性能 -->
  <div class="customHeader" style="display: inline-block">
    <el-popover placement="bottom" trigger="click" :ref="`popover-${columnIndex}`">
      <!-- table表头文字显示-->
      <span slot="reference" class="label">
        <zn-icon :icon="column.extra.icon" />
        {{ column.label }} &nbsp;
        <i class="el-icon-arrow-down"></i>
      </span>
      <!-- text 文本 -->
      <div v-if="column.type == 'text'">
        <el-input clearable v-model.trim="filterForm.value" placeholder="请输入查询内容"
          @keyup.native.enter="confirm()"></el-input>
      </div>
      <!-- number 数字框 -->
      <div v-else-if="column.type == 'number'">
        <el-input clearable oninput="value=value.replace(/[^0-9.]/g,'')" v-model.trim="filterForm.value"
          placeholder="请输入数字" @keyup.native.enter="confirm()"></el-input>
      </div>
      <!-- number_range 数字范围-->
      <div v-else-if="column.type == 'number_range'">
        <el-input style="width: 120px" clearable oninput="value=value.replace(/[^0-9.]/g,'')"
          v-model.trim="filterForm.value" placeholder="请输入数字"></el-input>
        -
        <el-input style="width: 120px" clearable oninput="value=value.replace(/[^0-9.]/g,'')" v-model.trim="spareValue"
          placeholder="请输入数字"></el-input>
      </div>
      <!-- date 单个日期-->
      <div v-else-if="column.type == 'date'">
        <el-date-picker v-model="filterForm.value" type="date" clearable placeholder="选择日期" value-format="yyyy-MM-dd" />
      </div>
      <!-- datetime 日期时间-->
      <div v-else-if="column.type == 'datetime'">
        <el-date-picker v-model="filterForm.value" type="datetime" placeholder="选择日期时间"
          value-format="yyyy-MM-dd HH:mm:ss"></el-date-picker>
      </div>
      <!-- date_range 日期范围-->
      <div v-else-if="column.type == 'date_range'">
        <el-date-picker v-model="filterForm.value" type="daterange" range-separator="至" start-placeholder="开始日期"
          end-placeholder="结束日期" value-format="yyyy-MM-dd"></el-date-picker>
      </div>
      <!-- datetime_range 日期时分秒范围-->
      <div v-else-if="column.type == 'datetime_range'">
        <el-date-picker v-model="filterForm.value" clearable type="datetimerange" range-separator="至"
          start-placeholder="开始日期" end-placeholder="结束日期" value-format="yyyy-MM-dd HH:mm:ss"></el-date-picker>
      </div>
      <!-- select 下拉选择-->
      <div v-else-if="column.type == 'select'">
        <el-select v-model="filterForm.value" placeholder="请选择" style="width: 100%" clearable>
          <el-option v-for="(item, index) in filterOptions" :key="index" :label="item.label"
            :value="item.value"></el-option>
        </el-select>
      </div>
      <!-- radio 单选-->
      <div v-else-if="column.type == 'radio'">
        <el-radio-group v-model="filterForm.value">
          <el-radio v-for="(item, index) in filterOptions" :key="index" :label="item.value" :value="item.value">
            {{ item.label }}
          </el-radio>
        </el-radio-group>
      </div>
      <!-- checkBox 多选-->
      <div v-else-if="column.type == 'checkBox'">
        <el-checkbox-group v-model="checkboxList">
          <el-checkbox v-for="(item, index) in filterOptions" :key="index" :label="item.value" :value="item.value">
            {{ item.label }}
          </el-checkbox>
        </el-checkbox-group>
      </div>

      <!-- confirm 确定框-->
      <div style="text-align: right">
        <el-button @click="confirm()" type="primary" size="mini" class="confirm">
          确定
        </el-button>
      </div>
    </el-popover>
  </div>
</template>
<script>
import ZnIcon from './ZnIcon'
export default {
  name: 'typePopover',
  components: {
    ZnIcon
  },
  // column 当前列数据，filterOptions 多选/单选/下拉/数据
  props: ['column', 'filterOptions', 'columnIndex'],
  data() {
    return {
      filterForm: {
        tagLabel: this.column.label, //筛选tag label(tag用)
        tagValue: '', //筛选tag value(tag用)
        value: '', //所筛选的数据(后端接收用)
        fieldName: this.column.name, //当前表头字段(后端接收用)
      },
      spareValue: '', //备用Value popover里如是两个值的话需要用此来拼接
      checkboxList: [],
    }
  },
  created() { },
  methods: {
    confirm() {
      let minValue = this.filterForm.value //数值双向绑定  做个闭环赋值
      let type = this.column.type
      // 跟后端商定 , 多个值存在时进行判断 , 以filterForm.value一个值为字符串的形式传递
      // 根据需求做了处理
      // checkBox和radio和select由于value值的原因需要处理
      if (type == 'checkBox' || type == 'radio' || type == 'select') {
        if (type == 'checkBox') {
          this.filterForm.value = this.checkboxList.join(',')
        }
        if (this.column.param && this.column.param.length > 0) {
          let str = ''
          this.column.param.forEach((i, t) => {
            if (type == 'checkBox' && i.value == Number(this.checkboxList[t])) {
              str = str + i.label
            }
            if (type == 'radio' && i.value == Number(this.filterForm.value)) {
              str = str + i.label
            }
            if (type == 'select' && i.value == Number(this.filterForm.value)) {
              str = str + i.label
            }
          })
          this.filterForm.tagValue = str
        }
      }
      // 数字范围
      else if (type == 'number_range') {
        this.filterForm.tagValue =
          this.filterForm.value + ' - ' + this.spareValue
        this.filterForm.value = this.filterForm.value + ',' + this.spareValue
      } else if (this.filterForm.value == '' && !this.spareValue) {
        return this.$message.warning('请输入或选择筛选条件')
      } else {
        this.filterForm.tagValue = this.filterForm.value //其他类型的赋值给tag用
      }
      this.$emit('tableUpdate', this.filterForm) //传递的是对象
      this.filterForm.value = minValue //数值双向绑定  做个闭环赋值 ,俗称瞒天过海
      this.$refs['popover-' + this.columnIndex].doClose() // 关闭popover
    },
  },
}
</script>
<style scoped>
.confirm {
  margin-top: 10px;
}

/* 禁止双击选中文字 */
.label {
  -moz-user-select: none;
  /*火狐*/
  -webkit-user-select: none !important;
  /*webkit浏览器*/
  -ms-user-select: none;
  /*IE10*/
  -khtml-user-select: none;
  /*早期浏览器*/
  user-select: none;
}

.labelColor {
  color: #409eff;
}

.el-icon-arrow-down {
  cursor: pointer;
}

.el-checkbox-group {
  display: flex;
  justify-content: flex-start;
  flex-direction: column;
  max-height: 150px;
  overflow-y: auto;
}
</style>