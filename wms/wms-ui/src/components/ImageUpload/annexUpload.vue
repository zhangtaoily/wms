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
      :disabled="isFormDisabled"
    >
      <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
      <div slot="tip" class="el-upload__tip">请选择要上传的文件</div>
    </el-upload>
  </div>
</template>

<script>
import { getToken } from "@/utils/auth";
import axios from "axios";
export default {
  props: {
    isFormDisabled: {
      type: Boolean,
    },
    value:{
      type: Array,
      default: [],
    },
    // 大小限制(MB)
    fileSize: {
      type: Number,
      default: 5,
    },
  },
  data() {
    return {
      fileList: [],
      imgUrl: process.env.VUE_APP_BASE_IMG,
    };
  },
  watch: {
    value: {
      async handler(val) {
        if (val) {
          console.log(val, "fileList");

          this.fileList = val.map((item) => {
            // 提取文件名称
            let name;
            if (item.path.match) {
              name = item.path.match(/[^\/]+$/)[0];
            } else {
              item.name;
            }
            // 是否保存过
            let isSave = true;
            return { ...item, name, isSave };
          });
        }
      },
      deep: true,
      immediate: true,
    },
  },
  methods: {
    convertToRelativePath(absolutePath) {
      let relativePath = absolutePath.replace(/^D:\/wms\//, "");
      if (relativePath.startsWith("/")) {
        relativePath = relativePath.substr(1); // 去掉开头的斜杠
      }
      return relativePath;
    },
    handlePictureCardPreview(file) {
      window.open(
        this.imgUrl + this.convertToRelativePath(file.path),
        "_blank"
      );
    },
    // 删除图片
    handleDelete(file) {
      const findex = this.fileList.map((f) => f.uid).indexOf(file.uid);
      if (findex > -1) {
        this.fileList.splice(findex, 1);
        this.$emit("input", this.fileList);
      }
    },
    handleChange(file, fileList) {
      this.fileList = fileList;
      const formData = new FormData();
      formData.append("files", file.raw);
      formData.append("custId", this.$route.query.id);
      axios
        .post(
          process.env.VUE_APP_BASE_API + "/system/oss/upload/cust/files",
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
          this.fileList[this.fileList.length - 1].path =
            data.data.savedFiles[0];
          this.fileList[this.fileList.length - 1].custId = this.$route.query.id;
          this.$emit("input", this.fileList);
        });
    },
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
