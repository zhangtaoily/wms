<template>
  <div>
    <el-upload
      ref="upload"
      class="upload-demo"
      action="https://your-server-url/upload"
      :multiple="true"
      :auto-upload="false"
      :on-change="handleChange"
      :file-list="fileList"
      :before-upload="handleBeforeUpload"
      :on-remove="handleDelete"
      :on-error="handleUploadError"
      :on-preview="handlePictureCardPreview"
      :disabled="uploadDisabled"
    >
      <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
      <div slot="tip" class="el-upload__tip">请选择要上传的文件</div>
    </el-upload>
  </div>
</template>

<script>
import { getToken } from "@/utils/auth";
import { getUploadFilesList, deleteFiles } from "@/api/system/ossConfig.js";
import axios from "axios";
export default {
  props: {
    isFormDisabled: {
      type: Boolean,
    },
    functionName: {
      type: String,
      required: true,
    },
    id: {
      type: String || Number,
      default: null,
    },
    // 大小限制(MB)
    fileSize: {
      type: Number,
      default: 5,
    },
    form: {
      type: Object,
      default: () => ({}),
    },
  },
  data() {
    return {
      // 文件列表
      fileList: [],
      imgUrl: process.env.VUE_APP_BASE_IMG,
    };
  },
  computed: {
    uploadDisabled() {
      return false;
    },
  },
  watch: {},
  created() {
    this.getList();
  },
  methods: {
    getList() {
      console.log("当前附件中的functionName和id",this.functionName,'  ',this.id);
      getUploadFilesList(this.functionName, this.id).then((res) => {
        this.setFileList(res.data);
      });
    },
    setFileList(list) {
      this.fileList = list.map((item) => ({
        name: item.name,
        path: item.path,
        uid: item.id,
      }));
    },
    convertToRelativePath(absolutePath) {
      let relativePath = absolutePath.replace(/^D:\/wms\//, "");
      if (relativePath.startsWith("/")) {
        relativePath = relativePath.substr(1); // 去掉开头的斜杠
      }
      return relativePath;
    },
    // 预览
    handlePictureCardPreview(file) {
      window.open(
        this.imgUrl + this.convertToRelativePath(file.path),
        "_blank"
      );
    },
    // 删除
    handleDelete(file) {
      deleteFiles([file.uid])
        .then((res) => {
          this.$message({
            message: "删除成功",
            type: "success",
          });
          const findex = this.fileList.map((f) => f.uid).indexOf(file.uid);
          if (findex > -1) {
            this.fileList.splice(findex, 1);
          }
        })
        .catch((err) => {
          console.log(err);
        });
    },
    // 上传
    handleChange(file, fileList) {
      this.fileList = fileList;
      const formData = new FormData();
      formData.append("files", file.raw);
      axios
        .post(
          process.env.VUE_APP_BASE_API +
            `/system/oss/upload/files/${this.functionName}/${this.id}`,
          formData,
          {
            headers: {
              "Content-Type": "multipart/form-data",
              authorization: "Bearer " + getToken(),
            },
          }
        )
        .then(({ data }) => {
          this.$message({
            message: "上传成功",
            type: "success",
          });
          this.setFileList(data.data.savedFiles);
        });
    },
    // 上传前校验
    handleBeforeUpload(file) {
      let isImg = false;
      if (this.fileType.length) {
        let fileExtension = "";
        if (file.name.lastIndexOf(".") > -1) {
          fileExtension = file.name.slice(file.name.lastIndexOf(".") + 1);
        }
        isImg = this.fileType.some((type) => {
          if (file.type.indexOf(type) > -1) return true;
          if (fileExtension && fileExtension.indexOf(type) > -1) return true;
          return false;
        });
      } else {
        isImg = file.type.indexOf("image") > -1;
      }

      if (!isImg) {
        this.$modal.msgError(
          `文件格式不正确, 请上传${this.fileType.join("/")}图片格式文件!`
        );
        return false;
      }
      if (this.fileSize) {
        const isLt = file.size / 1024 / 1024 < this.fileSize;
        if (!isLt) {
          this.$modal.msgError(`上传头像图片大小不能超过 ${this.fileSize} MB!`);
          return false;
        }
      }
      this.$modal.loading("正在上传图片，请稍候...");
      this.number++;
    },
    handleUploadError(res) {
      this.$modal.msgError("上传图片失败，请重试");
      this.$modal.closeLoading();
    },
  },
};
</script>

<style scoped lang="scss">
// .el-upload--picture-card 控制加号部分
::v-deep.hide .el-upload--picture-card {
  display: none;
}
// 去掉动画效果
::v-deep .el-list-enter-active,
::v-deep .el-list-leave-active {
  transition: all 0s;
}

::v-deep .el-list-enter,
.el-list-leave-active {
  opacity: 0;
  transform: translateY(0);
}
</style>
