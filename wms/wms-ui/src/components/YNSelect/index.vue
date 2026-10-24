<template>
  <el-select
    :disabled="disabled"
    @change="selectChange"
    v-model="selectedValue"
    placeholder="请选择"
  >
    <el-option
      v-for="item in options"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    >
    </el-option>
  </el-select>
</template>

<script>
// 是/否 组件
export default {
  name: "YNSelect",
  props: {
    value: {
      type: Number,
      default: () => null,
    },
    disabled: {
      type: Boolean,
      default: () => true,
    },
  },
  data() {
    return {
      options: [
        {
          value: 1,
          label: "是",
        },
        {
          value: 0,
          label: "否",
        },
      ],
      selectedValue: this.value,
    };
  },
  watch: {
    value(newValue) {
      this.selectedValue = newValue;
    },
    selectedValue(newValue) {
      this.$emit("input", newValue);
      this.$emit("change", newValue);
    },
  },
  methods: {
    selectChange(value) {
      this.selectedValue = value;
    },
  },
};
</script>

<style>
</style>