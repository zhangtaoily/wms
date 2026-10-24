export default {
  bind(el, binding) {
    const mask = document.createElement("div");
    mask.className = "read-only-mask";

    const defaultOptions = {
      top: "0",
      left: "0",
      width: "100%",
      height: "100%",
    };

    const options = { ...defaultOptions, ...(binding.value || {}) };

    mask.style.position = "absolute";
    mask.style.top = options.top;
    mask.style.left = options.left;
    mask.style.width = options.width;
    mask.style.height = options.height;
    mask.style.background = "rgba(255, 255, 255, 0.5)";
    mask.style.zIndex = "10";
    mask.style.cursor = "not-allowed";

    el.style.position = "relative";
    el.appendChild(mask);
    mask.style.display = options.visible ? "block" : "none";
    el._readonlyMask = mask;
  },
  update(el, binding) {
    if (el._readonlyMask) {
      const options = { ...el._readonlyMask.dataset, ...(binding.value || {}) };
      el._readonlyMask.style.display = options.visible ? "block" : "none";
      el._readonlyMask.style.top = options.top;
      el._readonlyMask.style.left = options.left;
      el._readonlyMask.style.width = options.width;
      el._readonlyMask.style.height = options.height;
    }
  },
  unbind(el) {
    if (el._readonlyMask) {
      el._readonlyMask.remove();
      delete el._readonlyMask;
    }
  },
};

