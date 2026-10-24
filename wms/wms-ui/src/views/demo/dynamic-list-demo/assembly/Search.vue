<template>
  <div class="search-class">
    <el-form :model="queryParams" ref="queryParams" :inline="true">
      <el-form-item v-for="item in options" :key="item.id" :prop="item.name" style="margin-right: 10px !important">
        <el-select v-if="item.type === 'select'" v-model="queryParams[item.name]" :placeholder="'请选择' + item.label"
          @change="handleQuery" clearable filterable>
          <el-option v-for="dict in item.param" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
        <div v-else-if="item.type == 'street'" class="div-street">
          <treeselect v-model="queryParams[item.name]" :placeholder="'请选择' + item.label" :options="item.param"
            :normalizer="normalizer" style="width: 200px;" />
        </div>
        <!-- date 单个日期-->
        <el-date-picker v-else-if="item.type == 'date'" v-model="queryParams[item.name]" type="date" clearable
          :placeholder="'选择' + item.label" value-format="yyyy-MM-dd" @change="handleQuery" />
        <!-- datetime 日期时间-->
        <el-date-picker v-else-if="item.type == 'datetime'" v-model="queryParams[item.name]" type="datetime"
          :placeholder="'选择' + item.label" value-format="yyyy-MM-dd HH:mm:ss" @change="handleQuery"></el-date-picker>
        <!-- date 单个时间-->
        <el-time-picker v-else-if="item.type == 'time'" v-model="queryParams[item.name]" :value-format="'HH:mm'"
          format="HH:mm" :placeholder="'选择' + item.label" @change="handleQuery">
        </el-time-picker>
        <!-- date_range 日期范围-->
        <el-date-picker v-else-if="item.type == 'date_range'" v-model="queryParams[item.name]" type="daterange"
          range-separator="至" :start-placeholder="'开始' + item.label" :end-placeholder="'结束' + item.label"
          value-format="yyyy-MM-dd" @change="handleQuery"></el-date-picker>
        <!-- datetime_range 日期时分秒范围-->
        <el-date-picker v-else-if="item.type == 'datetime_range'" v-model="queryParams[item.name]" clearable
          type="datetimerange" range-separator="至" :start-placeholder="'开始' + item.label"
          :end-placeholder="'结束' + item.label" value-format="yyyy-MM-dd HH:mm:ss" @change="handleQuery"></el-date-picker>
        <!-- time_range时分秒范围-->
        <el-time-picker is-range v-else-if="item.type == 'time_range'" v-model="queryParams[item.name]" clearable
          type="datetimerange" range-separator="至" :start-placeholder="'开始' + item.label"
          :end-placeholder="'结束' + item.label" value-format="HH:mm" format="HH:mm" @change="handleQuery"></el-time-picker>
        <el-input v-else v-model="queryParams[item.name]" clearable :placeholder="'请输入' + item.label"
          @clear="handleQuery"></el-input>

      </el-form-item>
      <el-form-item class="option-button" :style="options.length > 0 ? 'right: 20px;' : 'right: 20px;top: 0px;'">
        <el-button type="primary" size="mini" @click="handleQuery">
          查询
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
export default {
  name: 'Search',
  components: {
    Treeselect,
  },
  props: {
    options: {
      type: Array, // table数据
      default: () => [],
    },
  },
  data() {
    return {
      isActive: false,
      searchText: '',
      queryParams: {},
    }
  },
  computed: {},
  // watch: {
  //   options: {
  //     deep: true,
  //     handler(val) {
  //       this.getOptions(val)
  //     }
  //   },
  // },
  created() {
    this.queryParams = {}
  },
  mounted() { },
  methods: {
    /** 转换树数据结构 */
    normalizer(node) {
      if (node.children && !node.children.length) {
        delete node.children
      }
      return {
        id: node.id,
        label: node.name,
        isDisabled: false,
        children: node.children
      }
    },
    getOptions(item) {
      for (let i = 0; i < item.length; i++) {
        let res = item[i]
        this.queryParams[res.name] = res.value
      }
    },
    handleQuery() {
      this.$emit('up-Search', this.queryParams) //改变搜索字段的value
    },
  },
}
</script>

<style>
.div-street .vue-treeselect__control {
  padding-left: 5px;
  padding-right: 5px;
  display: table;
  table-layout: fixed;
  width: 100%;
  height: 28px;
  border: 1px solid #ddd;
  border-radius: 0px;
}

.div-street .vue-treeselect--single .vue-treeselect__input {
  align-items: center;
}

.search-class .el-form-item__content {
  line-height: 28px;
  height: 28px !important;
  margin-left: 16px;
}
</style>
<style lang="scss" scoped>
.public-search {
  display: inline-block;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid white;
}

::v-deep.isActive {
  border: 1px solid red;
  transition: all 0.3s ease-in-out;
}

.option-button {
  position: absolute;
  right: 10px;
}
</style>