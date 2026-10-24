<template>
  <el-input
    v-bind="$attrs"
    :value="displayValue"
    :disabled="disabled"
    @input="handleInput"
    @blur="handleBlur"
    @focus="handleFocus"
  />
</template>
  
  <script>
export default {
  name: "SmartCommaInput",
  props: {
    value: [Number, String],
    disabled: Boolean,
    precision: {
      type: Number,
      default: 2,
    },
  },
  data() {
    return {
      displayValue: "",
      rawValue: "",
    };
  },
  watch: {
    value: {
      immediate: true,
      handler(newVal) {
        if (newVal === null || newVal === "" || isNaN(newVal)) {
          this.rawValue = "";
          this.displayValue = "";
          return;
        }
        if (newVal !== null && newVal !== undefined) {
          this.rawValue = Number(newVal).toFixed(this.precision);
          this.displayValue = this.addComma(this.rawValue);
        } else {
          this.rawValue = "";
          this.displayValue = "";
        }
      },
    },
  },
  methods: {
    handleInput(value) {
      if (value === "") {
        this.rawValue = "";
        this.displayValue = "";
        this.$emit("input", null); // 立即触发空值
        return;
      }
      // 过滤非法字符并处理小数点
      let filtered = value.replace(/[^0-9.]/g, "");
      const parts = filtered.split(".");

      // 处理多个小数点
      if (parts.length > 2) {
        filtered = parts[0] + "." + parts.slice(1).join("");
      }

      // 更新显示值（无逗号）
      this.displayValue = filtered;
      this.rawValue = filtered;
    },

    handleBlur(e) {
      if (this.rawValue.trim() === "") {
        this.displayValue = "";
        this.$emit("input", null);
        this.$emit("blur", e);
        return;
      }
      // 格式化处理
      const num = this.parseNumber(this.rawValue);
      if (!isNaN(num)) {
        const formatted = this.addComma(num.toFixed(this.precision));
        this.displayValue = formatted;
        this.$emit("input", num);
      }
      this.$emit("blur", e);
    },

    handleFocus() {
      // 聚焦时显示原始数值
      this.displayValue = this.rawValue.replace(/,/g, "");
    },

    addComma(numStr) {
      if (!numStr) return "";
      const [integer, decimal] = numStr.split(".");
      return (
        integer.replace(/\B(?=(\d{3})+(?!\d))/g, ",") +
        (decimal ? `.${decimal}` : "")
      );
    },

    parseNumber(value) {
      const num = parseFloat(value.replace(/,/g, ""));
      return isNaN(num) ? null : num;
    },
  },
};
</script>

<style lang="scss" scoped>
::v-deep .el-input__inner {
  text-align: right;
  padding-right: 5px !important;
  font-family: monospace; /* 确保所有字符等宽 */
  letter-spacing: 0.5px; /* 防止逗号残留视觉问题 */
}
::v-deep .el-input-number {
  width: 100%;
}

.resource-association-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  .read-only {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: calc(100%);
    // background: rgba(255, 255, 255, 0.5);
    z-index: 10;
  }
}
</style>
