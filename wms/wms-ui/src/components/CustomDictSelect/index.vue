<template>
  <el-select
    :value="selectValue"
    @input="handleInput"
    @change="handleChange"
    placeholder="请选择"
    filterable
    clearable
    default-first-option
    :disabled="disabled"
    :filter-method="filterOptions"
  >
    <el-option
      v-for="dict in filteredOptions"
      :key="dict.dictValue"
      :label="dict.dictLabel"
      :value="dictValueCalc(dict.dictValue)"
    >
      <span style="float: left">{{ dict.dictLabel }}</span>
      <span style="float: right; color: #8492a6; font-size: 12px">
        {{ dict.dictValue }}
      </span>
    </el-option>
  </el-select>
</template>

<script>
export default {
  name: "CustomDictSelect",
  props: {
    disabled: {
      type: Boolean,
      default: false,
    },
    options: {
      type: Array,
      default: () => [], // 设置默认值为空数组
    },
    value: {
      type: [String, Number],
      default: null,
    },
    dictValueType:{
      type: String,
      default: 'String'
    }
  },
  data() {
    return {
      selectValue: this.value,
      currentQuery: "", // 新增一个数据属性来存储当前查询条件
    };
  },
  computed: {
    filteredOptions() {
      if (!this.currentQuery) {
        return this.options;
      }
      return this.options.filter(
        (option) =>
          option?.dictLabel?.includes(this.currentQuery) ||
          String(option?.dictValue).includes(this.currentQuery)
      );
    },
  },
  watch: {
    value(newValue) {
      this.selectValue = newValue;
    },
  },
  methods: {
    handleInput(value) {
      this.selectValue = value;
      // 清空查询条件
      this.currentQuery = null;
      this.$emit("input", value);
    },
    filterOptions(query) {
      // 更新查询条件，触发filteredOptions重新计算
      this.currentQuery = query;
    },
    // 触发change事件
    handleChange(value) {
      this.$emit("change", value);
    },
    dictValueCalc(dictValue){
      if(this.dictValueType == 'Number'){
        return Number(dictValue);
      }else{
        return dictValue;
      }
    }
  },
};
</script>
