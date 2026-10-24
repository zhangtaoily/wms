<template>
  <el-input
    v-bind="$attrs"
    :value="displayValue"
    :disabled="disabled"
    @input="handleInput"
    @blur="handleBlur"
    @focus="handleFocus"
    @change="handleChange"
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
    defaultValue: {
      type: [Number, String],
      default: 0,
    },
  },
  data() {
    return {
      displayValue: 0,
      rawValue: 0,
    };
  },
  watch: {
    value: {
      immediate: true,
      handler(newVal) {
        this.handleChange(newVal);
        if (newVal === null || newVal === "" || isNaN(newVal)) {
          this.rawValue = this.defaultValue;
          this.displayValue = this.addComma(this.rawValue);
          return;
        }
        if (newVal !== null && newVal !== undefined) {
          this.rawValue = Number(newVal).toFixed(this.precision);
          this.displayValue = this.addComma(this.rawValue);
        } else {
          this.rawValue = this.defaultValue;
          this.displayValue = this.addComma(this.rawValue);
        }
      },
    },
  },
  methods: {
    handleInput(value) {
      if (value === "") {
        this.rawValue = "";
        this.displayValue = "";
        this.$emit("input", null);
        return;
      }

      // 过滤非法字符并处理负号
      let filtered = value.replace(/[^0-9.-]/g, "");

      // 处理负号：只能出现在开头且唯一
      filtered = filtered.replace(/-/g, (m, index) => (index === 0 ? "-" : ""));

      // 处理多个小数点
      const parts = filtered.split(".");
      if (parts.length > 2) {
        filtered = parts[0] + "." + parts.slice(1).join("");
      }

      // 处理纯负号情况
      if (filtered === "-") {
        this.rawValue = "-";
        this.displayValue = "-";
        return;
      }

      this.rawValue = filtered;
      this.displayValue = filtered;
    },

    handleBlur(e) {
      if (this.rawValue.trim() === "" || this.rawValue === "-") {
        this.displayValue = this.addComma(this.defaultValue);
        this.$emit("input", this.defaultValue);
        this.$emit("blur", e);
        return;
      }

      // 格式化处理
      const num = this.parseNumber(this.rawValue);
      if (!isNaN(num)) {
        const formatted = this.addComma(num.toFixed(this.precision));
        this.displayValue = formatted;
        this.$emit("input", num);
      } else {
        this.displayValue = this.addComma(this.defaultValue);
        this.$emit("input", this.defaultValue);
      }
      this.$emit("blur", e);
      this.handleChange(num || this.defaultValue);
    },

    handleFocus() {
      this.displayValue = this.rawValue.replace(/,/g, "");
    },

    addComma(numStr) {
      if (!numStr && numStr !== 0) return this.addComma(this.defaultValue);
      let sign = "";
      if (numStr.toString().startsWith("-")) {
        sign = "-";
        numStr = numStr.toString().slice(1);
      }
      const [integer, decimal] = numStr.toString().split(".");
      const formattedInteger = integer.replace(/\B(?=(\d{3})+(?!\d))/g, ",");
      return sign + (formattedInteger || "0") + (decimal ? `.${decimal}` : "");
    },

    parseNumber(value) {
      const num = parseFloat(value.toString().replace(/,/g, ""));
      return isNaN(num) ? null : num;
    },
    handleChange(value) {
      this.$emit("change", value || this.defaultValue);
    },
  },
};
</script>

<style lang="scss" scoped>
/* 保持原有样式不变 */
::v-deep .el-input__inner {
  text-align: right;
  padding-right: 5px !important;
  font-family: monospace;
  letter-spacing: 0.5px;
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
    z-index: 10;
  }
}
</style>