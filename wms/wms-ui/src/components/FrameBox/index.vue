<template>
  <div :class="['frame-box', 'frame-box-col-' + col]">
    <div v-if="title">{{ title }}</div>
    <slot name="default"></slot>
    <div class="container">
      <div>
        <slot name="col-1"></slot>
      </div>
      <div>
        <slot name="col-2"></slot>
      </div>
      <div>
        <slot name="col-3"></slot>
      </div>
      <div>
        <slot name="col-4"></slot>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "FrameBox",
  props: {
    title: {
      type: String,
      default: () => "标题",
    },
    col: {
      type: Number,
      default: () => 1,
    },
  },
  data() {
    return {};
  },
};
</script>

<style lang="scss" scoped>
.frame-box {
  border: 1px solid rgb(106.8, 108.9, 113.1); /* 设置1px灰色边框 */
  border-radius: 8px; /* 设置边框的弧度 */
  padding: 10px; /* 设置内边距 */
  position: relative; /* 设置相对定位，以便对齐标题 */
  padding-top: 15px;
  margin-right: 10px;
  margin-top: 10px;
  padding-top: 15px;
}

.frame-box > div:first-child {
  position: absolute; /* 设置绝对定位 */
  top: -10px; /* 根据需要调整，使标题位于边框上方 */
  left: 10%; /* 使标题水平居中 */
  transform: translateX(-50%); /* 使标题水平居中 */
  background-color: white; /* 设置标题背景色为白色，使其不被边框遮挡 */
  padding: 0 5px; /* 设置标题的内边距 */
}
.container {
  display: flex;
  justify-content: space-between; /* 控制子元素之间的间距 */
  align-items: flex-start; /* 垂直居中对齐 */
}
</style>
