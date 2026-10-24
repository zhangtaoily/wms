<template>
  <div class="resource-association-item">
    <div v-if="readonly" class="read-only"></div>
    <el-input
      v-if="isCommaSeparated"
      :disabled="disabled"
      type="text"
      v-model="myNumStr"
      @change="inputChangeHandler"
      @focus="selectText"
    />
    <el-input-number
      v-show="!isCommaSeparated"
      ref="inputNumber"
      style="width: 100%; text-align: right"
      :controls="false"
      :value="myNum"
      @input="valueChange"
      :precision="precision"
      :placeholder="placeholder"
      :disabled="disabled"
      @change="onChange"
      :min="min"
      :max="max"
      @focus="selectText"
    >
    </el-input-number>
  </div>
</template>

<script>
export default {
  name: "InputNumber",
  props: {
    // 数值
    value: {
      type: [String, Number],
      default: () => 0,
    },
    // 小数点位数
    precision: {
      type: Number,
      default: () => 2,
    },
    // 占位符
    placeholder: {
      type: String,
      default: () => "请输入",
    },
    // 是否禁用
    disabled: {
      type: Boolean,
      default: () => false,
    },
    // 是否只读
    readonly: {
      type: Boolean,
      default: () => false,
    },
    // min
    min: {
      type: Number,
    },
    max: {
      type: Number,
    },
    // 是否开启千分位，用逗号分隔
    isCommaSeparated: {
      type: Boolean,
      default: () => true,
    },
    // 不允许负数
    allowNegative: {
      type: Boolean,
      default: false,
    },
    // 禁止增大
    disableIncrease: {
      type: Boolean,
      default: false,
    },
    // 禁止减小
    disableDecrease: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      myNum: this.value,
      myNumStr: "",
    };
  },
  methods: {
    // 更新数值方法
    valueChange(val) {
      // 检查是否允许改变
      if (this.isChangeAllowed(val)) {
        let newNum = val.toFixed(this.precision);
        this.$emit("input", newNum);
        this.isCommaSeparated && this.changeMyNumStr(newNum);
        return newNum;
      } else {
        // 如果不允许改变，恢复原来的值
        this.$nextTick(() => {
          this.myNum = this.value;
        });
        return this.value;
      }
    },
    onChange(val) {
      this.$emit("change", val);
    },
    // 当输入框获得焦点时调用
    selectText(event) {
      // if (this.myNum == 0) {
      // 使用event.target获取到当前的DOM元素，并调用select()方法
      event.target.select();
      // }
    },
    // 计算属性，将数值转换为带逗号的字符串
    formattedValue() {
      return this.value.toLocaleString("zh-CN"); // 使用 toLocaleString 方法，设置语言为中文以得到逗号分隔
    },
    // 添加逗号
    addComma(num) {
      if (num == "null" || num == null) return "";
      var str = num.toString();
      var reg = str.includes(".")
        ? /(\d)(?=(\d{3})+\.)/g
        : /(\d)(?=(?:\d{3})+$)/g;
      return str.replace(reg, "$1,");
    },
    //去除千分位中的‘，’
    removeComma(num) {
      if (num && num != "undefined" && num != "null") {
        let numS = num;
        numS = numS.toString();
        if (this.allowNegative) {
          numS = numS.replace(/[^\d.-]/g, ""); // 清除"数字"、"."和"-"以外的字符
        } else {
          numS = numS.replace(/[^\d.]/g, ""); // 清除"数字"和"."以外的字符
        }

        return parseFloat(numS).toFixed(this.precision);
      } else {
        return Number(num);
      }
    },
    // 当input输入框发生改变
    inputChangeHandler(val) {
      let num = this.removeComma(val);
      num = parseFloat(num);
      // 检查是否允许改变
      if (this.isChangeAllowed(num)) {
        this.valueChange(num);
        // 对inputNumber赋值
        this.$set(this, "myNum", num);
        // 触发inputNumber改变方法
        this.onChange(num);
        this.$forceUpdate();
      } else {
        // 如果不允许改变，恢复原来的值
        this.changeMyNumStr(this.value);
        this.$nextTick(() => {
          this.myNum = this.value;
        });
      }

    },
    // 改变myNumStr的值
    changeMyNumStr(val) {
      this.$set(this, "myNumStr", this.addComma(val));
      // console.log("改变myNumStr的值", this.myNumStr, val);
    },
    
    // 检查值变更是否允许
    isChangeAllowed(newVal) {
      const currentVal = parseFloat(this.value);
      newVal = parseFloat(newVal);
      if (this.disableIncrease && newVal > currentVal) return false;
      if (this.disableDecrease && newVal < currentVal) return false;
      if (this.disableIncrease && this.disableDecrease) return false;
      return true;
    },
  },
  watch: {
    value: function (newVal, oldVal) {
      let a1 = this.addComma(newVal);
      // console.log(a1);
      // console.log(this.removeComma(a1));
      this.myNum = newVal;
    },
  },
};
</script>

<style lang="scss" scoped>
::v-deep .el-input__inner {
  text-align: right;
  padding-right: 5px !important;
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
