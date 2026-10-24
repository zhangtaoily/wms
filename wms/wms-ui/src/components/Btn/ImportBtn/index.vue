<template>
  <div>
    <el-button
      icon="el-icon-upload2"
      size="mini"
      @click="handleOpen"
      >导入
    </el-button>
    <el-dialog
      title="Excel 导入"
      :visible.sync="dialogVisible"
      width="480px"
      append-to-body
      @closed="handleClosed"
    >
      <div class="import-tip">
        <p>1. 点击「下载模板」获取标准导入模板；</p>
        <p>2. 在模板中填写数据后选择文件上传，系统逐行校验并保存；</p>
        <p>3. 编码重复等不符合规则的数据将导入失败，并提示所在行号。</p>
      </div>
      <el-upload
        ref="upload"
        action="#"
        accept=".xlsx,.xls"
        :auto-upload="false"
        :limit="1"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        drag
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击选择文件</em></div>
        <div class="el-upload__tip" slot="tip">仅支持 xlsx / xls 文件</div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button
          size="mini"
          icon="el-icon-download"
          :loading="downloading"
          @click="handleDownloadTemplate"
          >下载模板</el-button
        >
        <el-button size="mini" @click="dialogVisible = false">取 消</el-button>
        <el-button
          size="mini"
          type="primary"
          :loading="uploading"
          :disabled="!file"
          @click="handleImport"
          >导 入</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { downloadImportTemplate, importExcelData } from "@/api/system/common";

export default {
  name: "ImportBtn",
  data() {
    return {
      functionName: "",
      dialogVisible: false,
      downloading: false,
      uploading: false,
      file: null,
    };
  },
  created() {
    this.functionName = this.$route.query.functionName;
  },
  methods: {
    /** 打开导入对话框 */
    handleOpen() {
      this.dialogVisible = true;
    },
    /** 选择文件 */
    handleFileChange(file) {
      this.file = file.raw;
    },
    /** 移除文件 */
    handleFileRemove() {
      this.file = null;
    },
    /** 下载导入模板 */
    async handleDownloadTemplate() {
      this.downloading = true;
      try {
        await downloadImportTemplate(this.functionName);
      } finally {
        this.downloading = false;
      }
    },
    /** 执行导入 */
    async handleImport() {
      if (!this.file) return;
      this.uploading = true;
      try {
        const formData = new FormData();
        formData.append("file", this.file);
        const res = await importExcelData(this.functionName, formData);
        if (res.code === 200) {
          this.$message.success(res.msg || "导入成功");
          this.dialogVisible = false;
          this.$emit("success");
        } else {
          this.$message.error(res.msg || "导入失败");
        }
      } catch (e) {
        this.$message.error("导入失败：" + e.message);
      } finally {
        this.uploading = false;
      }
    },
    /** 关闭后重置 */
    handleClosed() {
      this.file = null;
      if (this.$refs.upload) this.$refs.upload.clearFiles();
    },
  },
};
</script>

<style scoped>
.import-tip {
  margin-bottom: 12px;
  padding: 8px 12px;
  background: #f4f4f5;
  border-radius: 4px;
}
.import-tip p {
  margin: 4px 0;
  font-size: 12px;
  color: #606266;
  line-height: 1.6;
}
</style>
