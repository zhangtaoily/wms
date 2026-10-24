<template>
  <el-col :span="1.5">
    <el-dropdown
      v-if="$route.query.functionName == 'SAL_CONT'"
      :disabled="disabled || isDisableBtn"
      trigger="click"
    >
      <el-button icon="el-icon-arrow-down" size="mini">下推 </el-button>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item
          @click.native="handleClickPushDown('SAL_CONT')"
          :disabled="isSonContDisabled"
        >
          销售子合同
        </el-dropdown-item>
        <el-dropdown-item @click.native="handleClickPushDown('SAL_ORDER')">
          销售订单
        </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>

    <el-dropdown
      v-else-if="$route.query.functionName == 'SAL_ORDER'"
      :disabled="disabled"
      trigger="click"
    >
      <el-button icon="el-icon-arrow-down" size="mini">下推</el-button>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item @click.native="handleClickPushDown('SAL_PRDNOTICE')">
          生产通知单
        </el-dropdown-item>
        <el-dropdown-item @click.native="handleClickPushDown('SAL_NOTICE')">
          发货通知单
        </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
    <el-dropdown
      v-else-if="$route.query.functionName == 'SAL_INT_CONT'"
      :disabled="disabled"
      trigger="click"
    >
      <el-button icon="el-icon-arrow-down" size="mini">下推 </el-button>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item @click.native="handleClickPushDown('SAL_CONT')"
          >销售合同</el-dropdown-item
        >
        <!-- <el-dropdown-item @click.native="handleClickPushDown('SAL_PRDNOTICE')"
          >生产通知单</el-dropdown-item
        > -->
      </el-dropdown-menu>
    </el-dropdown>

    <el-button
      v-else
      :icon="icon"
      :loading="buttonLoading"
      @click="handleClickDropDown()"
      :disabled="disabled"
    >
      下 推
    </el-button>
  </el-col>
</template>

<script>
import qs from "qs";
import { pushDownList } from "@/utils/variables.js";
import { checkPushDown } from "@/api/system/common";

export default {
  props: {
    buttonLoading: {
      type: Boolean,
      default: () => true,
    },
    disabled: {
      type: Boolean,
      default: () => true,
    },
    functionName: {
      type: String,
      default: () => "",
    },
    detailData: {
      type: Object,
      default: () => {},
    },
  },
  data() {
    return {
      icon: "el-icon-arrow-down",
      // 下推列表
      pushDownList: pushDownList,
      // 下推对象
      pushDownObj: "",
      // 显示下推按钮的函数
      shwoPushDownFns: [
        "SAL_INT_CONT",
        "SAL_CONT",
        "SAL_ORDER",
        "SAL_OUTSTOCK",
      ],
      // 子合同不允许下推子合同
      isSonContDisabled: false,
      // 同步打开的窗口引用（手机端弹窗拦截兼容）
      openedWindow: null,
    };
  },
  computed: {
    isDisableBtn() {
      let sonSheet = this.pushDownList.find(
        (el) => el.currentFn == this.functionName
      ).sonSheet;
      if (!sonSheet) return true;

      // 查找是否有符合条件的单据
      let isHavePassDetail = this.detailData[sonSheet].some(
        (el) => el.isCancel == 0 && (el.isClosed == 0 || el.isClosed == 2)
      );
      return !isHavePassDetail;
    },
  },
  watch: {
    "detailData.contId": {
      handler(newVal) {
        this.isSonContDisabled = newVal ? true : false;
      },
    },
  },
  methods: {
    handleClickPushDown(targetFn) {
      this.handleClickDropDown(targetFn);
    },
    handleClickDropDown(targetFn) {
      this.pushDownObj = this.pushDownList.find((el) => {
        if (targetFn) {
          return el.currentFn == this.functionName && el.targetFn == targetFn;
        } else {
          return el.currentFn == this.functionName;
        }
      });

      let params = [
        {
          id: this.detailData.id,
          detailsId: this.detailData[this.pushDownObj.sonSheet]
            .filter(
              (el) => el.isCancel == 0 && (el.isClosed == 0 || el.isClosed == 2)
            )
            .map((el) => el.id),
        },
      ];

      this.pushDownObj.pushDownParams = params;
      const ids = params?.map((item) => item.id);

      // 同步构建下推目标URL，在用户手势同步调用栈中 window.open，避免 iOS Safari 拦截
      let pushUrlParams = {
        functionName: this.pushDownObj.targetFn,
        mode: "pushDown",
        id: this.ids && this.ids[0],
        originalFn: this.functionName,
        pushDownParams: this.pushDownObj.pushDownParams,
      };
      let pushUrl =
        this.pushDownObj.path +
        "/form?" +
        decodeURIComponent(qs.stringify(pushUrlParams), {
          arrayFormat: "indices",
        }).replace(/\[([0-9]+)\]=/g, "=");

      // 同步打开窗口（在用户点击的同步调用栈中）
      this.openedWindow = window.open(pushUrl, "_blank");

      // 手机端 WebView 可能拦截 window.open，fallback 到 router.push
      if (!this.openedWindow || this.openedWindow.closed) {
        console.warn("window.open 被拦截，fallback 到 router.push");
        this.$router.push(pushUrl);
        return;
      }

      // 检查是否存在正在审核中的变更单
      checkPushDown(ids, this.functionName)
        .then((res) => {
          // 校验通过，窗口已打开为正确URL
          this.openedWindow = null;
        })
        .catch((err) => {
          // 校验失败时关闭已打开的窗口
          if (this.openedWindow) {
            this.openedWindow.close();
            this.openedWindow = null;
          }
        });
    },
    openPage(mode, id, detailId) {
      // 打开页签
      let { path, query, meta } = this.$route;
      const params = {
        functionName: query.functionName,
        mode,
        id: this.ids && this.ids[0],
      };

      // 下推
      if (mode == "pushDown") {
        path = this.pushDownObj.path;
        params.originalFn = this.functionName;
        params.functionName = this.pushDownObj.targetFn;
        params.pushDownParams = this.pushDownObj.pushDownParams;
      }
      console.log("params :>> ", params);
      // console.log("query", this.$route, params, this.changeOrderPath);
      // 格式化url
      let path2 =
        path +
        "/form?" +
        decodeURIComponent(qs.stringify(params), {
          arrayFormat: "indices",
        }).replace(/\[([0-9]+)\]=/g, "=");

      console.log("path :>> ", path);
      console.log("path2 :>> ", path2);
      window.open(path2, "_blank");
    },
  },
};
</script>

<style scoped lang="scss">
</style>
