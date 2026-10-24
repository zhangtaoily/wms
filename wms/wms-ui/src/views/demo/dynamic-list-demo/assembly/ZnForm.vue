<template>
  <div class="app-container">
    <el-form ref="form" :model="form" :rules="rules" label-width="80px" size="medium" style="margin-bottom: 20px"
      :validate-on-rule-change="false">
      <el-row class="form-treeselect">
        <div v-for="item in options" :key="item.id">
          <div v-if="item.isAdd">
            <el-col :span="12" v-if="item.type === 'select'">
              <el-form-item :label="item.label" :prop="item.name">
                <el-select v-model="form[item.name]" :placeholder="'请选择' + item.label" clearable filterable
                  style="width: 100%" :disabled="disabled">
                  <el-option v-for="dict in item.param" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12" v-else-if="item.type === 'radio'">
              <el-form-item :label="item.label" :prop="item.name">
                <el-radio-group v-model="form[item.name]" :disabled="disabled">
                  <el-radio v-for="dict in item.param" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <!-- date 单个日期-->
            <el-col :span="12" v-else-if="item.type == 'date' || item.type == 'date_range'">
              <el-form-item :label="item.label" :prop="item.name">
                <el-date-picker v-model="form[item.name]" type="date" placeholder="选择日期" value-format="yyyy-MM-dd"
                  style="width: 100%" :disabled="disabled" />
              </el-form-item>
            </el-col>
            <!-- date 单个时间-->
            <el-col :span="12" v-else-if="item.type == 'time' || item.type == 'time_range'">
              <el-form-item :label="item.label" :prop="item.name">
                <el-time-picker v-model="form[item.name]" :disabled="disabled" :value-format="'HH:mm'" format="HH:mm"
                  style="width: 100%" placeholder="请选择时间">
                </el-time-picker>
              </el-form-item>
            </el-col>
            <!-- datetime 日期时间-->
            <el-col :span="12" v-else-if="item.type == 'datetime' || item.type == 'datetime_range'">
              <el-form-item :label="item.label" :prop="item.name">
                <el-date-picker v-model="form[item.name]" type="datetime" placeholder="选择日期时间"
                  value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%" :disabled="disabled"></el-date-picker>
              </el-form-item>
            </el-col>
            <el-col :span="12" v-else-if="item.type == 'number_range'">
              <el-form-item :label="item.label" :prop="item.name">
                <el-input v-model.number="form[item.name]" clearable :placeholder="'请输入' + item.label"
                  :disabled="disabled"></el-input>
              </el-form-item>
            </el-col>
            <div class="vue-treeselect" v-else-if="item.type === 'street'">
              <el-col :span="12">
                <el-form-item :label="item.label" :prop="item.name">
                  <treeselect v-model="form[item.name]" :options="item.param" :normalizer="normalizer"
                    :props="{ checkStrictly: true, emitPath: false }" :disabled="disabled"
                    :placeholder="'请选择' + item.label" />
                </el-form-item>
              </el-col>
            </div>
            <el-col :span="12" v-else>
              <el-form-item :label="item.label" :prop="item.name">
                <el-input v-model="form[item.name]" clearable :placeholder="'请输入' + item.label"
                  :disabled="disabled"></el-input>
              </el-form-item>
            </el-col>
          </div>
        </div>
      </el-row>
    </el-form>
  </div>
</template>

<script>
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
export default {
  name: 'ZnForm',
  components: {
    Treeselect,
  },
  props: {
    options: {
      type: Array, // table数据
      default: () => [],
    },
    disabled: {
      type: Boolean,
      default: () => false
    },
    resForm: {
      type: Object,
      default: () => { }
    }
  },
  watch: {
    options: {
      immediate: true,
      handler(val) {
        this.getOptions(val)
      }
    },
    resForm: {
      immediate: true,
      handler(val) {
        this.setValue(val)
      }
    },
    disabled: {
      immediate: true,
      handler(val) {
        this.setButtonName(val)
      }
    },
  },
  data() {
    return {
      buttonName: '取 消',
      rules: {},
      form: {},
      //主键暂存
      id: undefined,
      ruleParam: [
        { required: true, message: "不能为空", trigger: ["blur", "change", "input"] },
      ],
    }
  },
  created() {
  },
  mounted() {
  },
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
    //设置按钮名称
    setButtonName(item) {
      if (item) {
        this.buttonName = '关 闭'
      } else {
        this.buttonName = '取 消'
      }
    },
    //form赋值
    setValue(item) {
      this.form = item
      if (item.id !== undefined) {
        this.id = item.id
      }
    },
    //表单校验
    getOptions(item) {
      let ruleMap = new Map()
      for (var i in item) {
        if (item[i].isValidate === true) {
          this.ruleParam[0].message = item[i].label + "不能为空"
          ruleMap.set(item[i].name, this.ruleParam)
          this.ruleParam = [
            { required: true, message: "不能为空", trigger: ["blur", "change", "input"] },
          ]
        }
      }
      //map转对象
      this.rules = Object.fromEntries(ruleMap.entries())
    },
    // 取消按钮
    cancel() {
      this.$emit('dialog-cancel', this.form)
      this.form = {};
    },
    reset() {
      this.form = {};
    },
    /** 提交按钮 */
    submit: function () {
      this.form.id = this.id;
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.$emit('dialog-submit', this.form)
        }
      });
    },
  }
}
</script>

<style>
.form-treeselect .vue-treeselect .vue-treeselect__control {
  height: 28px !important;
  border-radius: 0;
}

.form-treeselect .vue-treeselect .el-form-item--medium .el-form-item__content {
  line-height: 28px !important;
}

.form-treeselect .vue-treeselect--disabled .vue-treeselect__control {
  background-color: #F5F7FA;
  border-color: #dfe4ed;
}

.form-treeselect .vue-treeselect--disabled .vue-treeselect__single-value {
  position: static;
  color: #C0C4CC !important;
}

.form-treeselect .el-form-item.is-error .vue-treeselect__control,
.form-treeselect .el-form-item.is-error .vue-treeselect__control:focus {
  border-color: #ff4949;
}

.form-treeselect .el-col-12 {
  height: 54px !important;
}
</style>
<style scoped>
.dialog-footer {
  border-top: 1px solid #D7D7D7;
  padding: 24px 24px 24px 24px !important;
  background-color: #FFFFFF;
  text-align: right;

}
</style>