<template>
  <el-form
    class="form-box"
    ref="form"
    :model="form"
    :rules="rules"
    label-width="80px"
  >
    <el-form-item label="创建人" prop="createName">
      <el-input v-model="form.createName" type="text" placeholder="" disabled />
    </el-form-item>
    <el-form-item label="创建时间" prop="createTime">
      <el-date-picker
        clearable
        v-model="form.createTime"
        type="datetime"
        value-format="yyyy-MM-dd HH:mm:ss"
        placeholder=""
        style="width: 100%"
        disabled
      >
      </el-date-picker>
    </el-form-item>
    <el-form-item label="修改人" prop="updateName">
      <el-input v-model="form.updateName" type="text" placeholder="" disabled />
    </el-form-item>
    <el-form-item label="修改时间" prop="updateTime">
      <el-date-picker
        clearable
        v-model="form.updateTime"
        type="datetime"
        value-format="yyyy-MM-dd HH:mm:ss"
        placeholder=""
        style="width: 100%"
        disabled
      >
      </el-date-picker>
    </el-form-item>
    <el-form-item label="审核人" prop="auditName">
      <el-input v-model="form.auditName" type="text" placeholder="" disabled />
    </el-form-item>
    <el-form-item label="审核时间" prop="auditTime">
      <el-date-picker
        clearable
        v-model="form.auditTime"
        type="datetime"
        value-format="yyyy-MM-dd HH:mm:ss"
        placeholder=""
        style="width: 100%"
        disabled
      >
      </el-date-picker>
    </el-form-item>
    <el-form-item v-if="form.syncTime" label="同步时间" prop="syncTime">
      <el-date-picker
        clearable
        v-model="form.syncTime"
        type="datetime"
        value-format="yyyy-MM-dd HH:mm:ss"
        placeholder=""
        style="width: 100%"
        disabled
      >
      </el-date-picker>
    </el-form-item>
    <template v-if="showCancelCloseItem">
      <el-form-item label="关闭状态" prop="isClosed">
        <el-select disabled v-model="form.isClosed" filterable clearable>
          <el-option
            v-for="dict in dictDataList.close_status"
            :key="dict.dictValue"
            :label="dict.dictLabel"
            :value="Number(dict.dictValue)"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="关闭人" prop="closedName">
        <el-input disabled v-model="form.closedName" placeholder="请输入" />
      </el-form-item>
      <el-form-item label="关闭日期" prop="closedTime">
        <el-date-picker
          disabled
          clearable
          editable
          v-model="form.closedTime"
          type="datetime"
          value-format="yyyy-MM-dd HH:mm:ss"
        >
        </el-date-picker>
      </el-form-item>
      <el-form-item label="取消状态" prop="isCancel">
        <el-select disabled v-model="form.isCancel" filterable clearable>
          <el-option
            v-for="dict in dictDataList.cancel_status"
            :key="dict.dictValue"
            :label="dict.dictLabel"
            :value="Number(dict.dictValue)"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="取消人" prop="cancelName">
        <el-input disabled v-model="form.cancelName" />
      </el-form-item>
      <el-form-item label="取消日期" prop="cancelTime">
        <el-date-picker
          disabled
          clearable
          editable
          v-model="form.cancelTime"
          type="datetime"
          value-format="yyyy-MM-dd HH:mm:ss"
        >
        </el-date-picker>
      </el-form-item>
    </template>
    <slot name="form-items"></slot>
    <el-form-item label="同步状态" prop="syncStatus">
      <el-select
        disabled
        v-model="form.syncStatus"
        placeholder="请选择"
        filterable
        clearable
        :class="form.syncStatus == '0' ? 'sync-error' : ''"
      >
        <el-option
          v-for="dict in dictDataList.sync_status"
          :key="dict.dictValue"
          :label="dict.dictLabel"
          :value="dict.dictValue"
        ></el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="回传单号" prop="responsePactCode">
      <el-input disabled v-model="form.responsePactCode" placeholder="请输入" />
    </el-form-item>
    <el-form-item v-if="form.erpCode" label="ERP单号" prop="erpCode">
      <el-input disabled v-model="form.erpCode" placeholder="请输入" />
    </el-form-item>
      <el-form-item label="审批意见" v-if="form.approvalOpinions" prop="approvalOpinions">
        <el-input disabled v-model="form.approvalOpinions" placeholder="请输入" />
      </el-form-item>
  </el-form>
</template>

<script>
export default {
  props: {
    form: {
      type: Object,
      default: () => {},
    },
    rules: {
      type: Object,
      default: () => {},
    },
    // 显示关闭取消item
    showCancelCloseItem: {
      type: Boolean,
      default: () => false,
    },
    // 字典
    dictDataList: [Object, Array],
  },
  data() {
    return {};
  },
  methods: {},
};
</script>

<style scoped lang="scss">
</style>
