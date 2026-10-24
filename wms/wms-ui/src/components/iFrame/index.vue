<template>
  <div v-loading="loading" :style="'height:' + height">
    <iframe
      :src="src"
      frameborder="no"
      style="width: 100%; height: 100%"
      scrolling="auto"
    />
  </div>
</template>
<script>
export default {
  props: {
    src: {
      type: String,
      required: true
    },
  },
  data() {
    return {
      height: document.documentElement.clientHeight - 94.5 + "px;",
      loading: true,
      url: this.src
    };
  },
  mounted: function () {
    // 直接在实例上存储非响应式属性（不需要响应式追踪）
    this._resizeTimer = setTimeout(() => {
      this.loading = false;
    }, 300);

    // 使用箭头函数保存引用
    this._resizeHandler = () => {
      this.height = document.documentElement.clientHeight - 94.5 + "px;";
    };

    // 使用 addEventListener 而非直接赋值
    window.addEventListener('resize', this._resizeHandler);
  },
  beforeDestroy() {
    // 清理定时器
    if (this._resizeTimer) {
      clearTimeout(this._resizeTimer);
      this._resizeTimer = null;
    }
    // 移除事件监听器
    if (this._resizeHandler) {
      window.removeEventListener('resize', this._resizeHandler);
      this._resizeHandler = null;
    }
  }
};
</script>
