<template>
  <div class="component-upload-image">
    <div class="el-upload-list el-upload-list--picture-card" v-if="imageUrl">
      <div class="el-upload-list__item is-success">
        <img
          width="100%"
          height="100%"
          :src="imgUrl + convertToRelativePath(imageUrl)"
          class="avatar"
        />
        <label class="el-upload-list__item-status-label">
          <i class="el-icon-upload-success el-icon-check"></i>
        </label>
        <span class="el-upload-list__item-actions">
          <span @click="handlePictureCardPreview()">
            <i class="el-icon-zoom-in"></i>
          </span>
          <span class="el-upload-list__item-delete" v-if="disabledBoolean?false:true">
            <i class="el-icon-delete" @click.stop="handleRemove()"></i>
          </span>
        </span>
      </div>
    </div>

    <el-upload
      v-if="!imageUrl"
      multiple
      :action="uploadImgUrl"
      list-type="picture-card"
      :on-success="handleUploadSuccess"
      :before-upload="handleBeforeUpload"
      :on-error="handleUploadError"
      :on-exceed="handleExceed"
      ref="imageUpload"
      class="imageUpload"
      :on-remove="handleDelete"
      :show-file-list="false"
      :headers="headers"
      :file-list="fileList"
      :on-preview="handlePictureCardPreview"
      :class="{ hide: this.fileList.length >= this.limit }"
    >
      <i class="el-icon-plus avatar-uploader-icon"></i>
    </el-upload>
    <!-- 上传提示 -->
    <div class="el-upload__tip" slot="tip" v-if="showTip">
      请上传
      <template v-if="fileSize">
        大小不超过 <b style="color: #f56c6c">{{ fileSize }}MB</b>
      </template>
      <template v-if="fileType">
        格式为 <b style="color: #f56c6c">{{ fileType.join("/") }}</b>
      </template>
      的文件
    </div>

    <el-dialog
      :visible.sync="dialogVisible"
      title="预览"
      width="800"
      append-to-body
    >
      <img
        :src="imgUrl + convertToRelativePath(imageUrl)"
        style="display: block; max-width: 100%; margin: 0 auto"
      />
    </el-dialog>
  </div>
</template>

<script>
import { getToken } from "@/utils/auth";
import { listByIds, delOss } from "@/api/system/oss";

export default {
  props: {
    value: [String, Object, Array],
    // 图片数量限制
    limit: {
      type: Number,
      default: 5,
    },
    // 大小限制(MB)
    fileSize: {
      type: Number,
      default: 5,
    },
    // 文件类型, 例如['png', 'jpg', 'jpeg']
    fileType: {
      type: Array,
      default: () => ["png", "jpg", "jpeg"],
    },
    // 是否显示提示
    isShowTip: {
      type: Boolean,
      default: true,
    },
    disabledBoolean:{//父组件传过来是编辑，还是查看
      type: Boolean,
      default() {
        return false;
      }
    },
  },
  data() {
    return {
      number: 0,
      uploadList: [],
      dialogVisible: false,
      hideUpload: false,
      imgUrl: process.env.VUE_APP_BASE_IMG,
      baseUrl: process.env.VUE_APP_BASE_API,
      imageUrl: "",
      uploadImgUrl:
        process.env.VUE_APP_BASE_API + "/system/oss/upload/brand/icon", // 上传的图片服务器地址
      headers: {
        Authorization: "Bearer " + getToken(),
      },
      fileList: [],
    };
  },
  watch: {
    value: {
      async handler(val) {
        if (val) {
          this.imageUrl = val;
        }
      },
      deep: true,
      immediate: true,
    },
  },
  computed: {
    // 是否显示提示
    showTip() {
      return this.isShowTip && (this.fileType || this.fileSize);
    },
  },
  methods: {
    handleRemove() {  
      this.$emit("input", "");
      this.$set(this, 'imageUrl', "")
    },
    convertToRelativePath(absolutePath) {
      let relativePath = absolutePath.replace(/^D:\/wms\//, "");
      if (relativePath.startsWith("/")) {
        relativePath = relativePath.substr(1); // 去掉开头的斜杠
      }
      return relativePath;
    },
    // 上传前loading加载
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
    // 文件个数超出
    handleExceed() {
      this.$modal.msgError(`上传文件数量不能超过 ${this.limit} 个!`);
    },
    // 上传成功回调
    handleUploadSuccess(res, file) {
      if (res.code === 200) {
        console.log(res, file, "res");

        this.uploadList.push({
          name: res.data.fileName,
          url: res.data.url,
          ossId: res.data.ossId,
        });
        this.$emit("input", res.data.savedFiles);
        this.$modal.closeLoading();
        // this.uploadedSuccessfully();
      } else {
        this.number--;
        this.$modal.closeLoading();
        this.$modal.msgError(res.msg);
        this.$refs.imageUpload.handleRemove(file);
        this.$modal.closeLoading();
        // this.uploadedSuccessfully();
      }
    },
    // 删除图片
    handleDelete(file) {
      const findex = this.fileList.map((f) => f.name).indexOf(file.name);
      if (findex > -1) {
        let ossId = this.fileList[findex].ossId;
        delOss(ossId);
        this.fileList.splice(findex, 1);
        this.$emit("input", "");
        // this.$emit("input", this.listToString(this.fileList));
      }
    },
    // 上传失败
    handleUploadError(res) {
      this.$modal.msgError("上传图片失败，请重试");
      this.$modal.closeLoading();
    },
    // 上传结束处理
    uploadedSuccessfully() {
      if (this.number > 0 && this.uploadList.length === this.number) {
        this.fileList = this.fileList.concat(this.uploadList);
        this.uploadList = [];
        this.number = 0;
        console.log(this.fileList);

        this.$emit("input", this.listToString(this.fileList));
        this.$modal.closeLoading();
      }
    },
    // 预览
    handlePictureCardPreview(file) {
      this.dialogVisible = true;
    },
    // 对象转成指定字符串分隔
    listToString(list, separator) {
      let strs = "";
      separator = separator || ",";
      for (let i in list) {
        if (list[i].ossId) {
          strs += list[i].ossId + separator;
        }
      }
      return strs != "" ? strs.substr(0, strs.length - 1) : "";
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
.imageUpload{
  margin: 8px;
}
.el-upload-list--picture-card .el-upload-list__item{
  display: block !important;
  margin: 8px;
}
</style>
