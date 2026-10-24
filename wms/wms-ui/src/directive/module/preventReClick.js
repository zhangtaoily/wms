// 防止多次点击，重复请求
import Vue from "vue";

const preventReClick = Vue.directive("preventReClick", {
  inserted: function (el, binding) {
    el._clickHandler = () => {
      if (!el.disabled) {
        el.disabled = true;
        el._timer = setTimeout(() => {
          el.disabled = false;
        }, binding.value || 3000);
      }
    };
    el.addEventListener("click", el._clickHandler);
  },
  unbind: function (el) {
    // 移除事件监听器
    if (el._clickHandler) {
      el.removeEventListener("click", el._clickHandler);
      delete el._clickHandler;
    }
    // 清除定时器
    if (el._timer) {
      clearTimeout(el._timer);
      delete el._timer;
    }
    // 重置状态
    el.disabled = false;
  }
});

export default { preventReClick };
