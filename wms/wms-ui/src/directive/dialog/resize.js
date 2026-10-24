/**
* v-dialogResize 弹窗左右边缘拖拽缩放
* Copyright (c) 浙江云梭纺织股份有限公司
*/

// 最小宽度约束
const MIN_WIDTH = 400;
// 距离视口边缘的最小间隙，防止拖出屏幕
const VIEWPORT_GAP = 8;

export default {
    bind(el) {
        const dragDom = el.querySelector('.el-dialog');
        if (!dragDom) return;

        // 缩放手柄：dir 用于判断拖拽方向
        const handleConfigs = [
            // 左边缘
            { dir: 'left', css: 'position:absolute;top:12%;bottom:12%;left:-3px;width:6px;cursor:w-resize;z-index:99;' },
            // 右边缘
            { dir: 'right', css: 'position:absolute;top:12%;bottom:12%;right:-3px;width:6px;cursor:e-resize;z-index:99;' }
        ];

        function startResize(e, dir) {
            e.preventDefault();
            e.stopPropagation();
            const cursor = window.getComputedStyle(e.target).cursor;
            const startX = e.clientX;
            const startRect = dragDom.getBoundingClientRect();
            const viewW = document.documentElement.clientWidth;
            const hasL = dir === 'left';

            document.body.style.cursor = cursor;
            document.body.style.userSelect = 'none';

            const moveHandler = function(ev) {
                ev.preventDefault(); // 移动时禁用默认事件
                const dx = ev.clientX - startX;
                let width;
                let left = startRect.left;

                if (hasL) {
                    // 左边缘：右边界固定，向左拖增大宽度
                    width = Math.min(
                        Math.max(startRect.width - dx, MIN_WIDTH),
                        startRect.right - VIEWPORT_GAP
                    );
                    left = startRect.right - width;
                } else {
                    // 右边缘：左边界固定，向右拖增大宽度
                    width = Math.min(
                        Math.max(startRect.width + dx, MIN_WIDTH),
                        viewW - startRect.left - VIEWPORT_GAP
                    );
                }

                dragDom.style.width = `${width}px`;
                if (hasL) dragDom.style.left = `${left}px`;
            };

            const upHandler = function() {
                document.removeEventListener('mousemove', moveHandler);
                document.removeEventListener('mouseup', upHandler);
                document.body.style.cursor = '';
                document.body.style.userSelect = '';
            };

            document.addEventListener('mousemove', moveHandler);
            document.addEventListener('mouseup', upHandler);
        }

        handleConfigs.forEach(({ dir, css }) => {
            const handle = document.createElement('div');
            handle.className = 'dialog-resize-handle';
            handle.style.cssText = css;
            handle.addEventListener('mousedown', function(e) {
                startResize(e, dir);
            });
            dragDom.appendChild(handle);
        });
    }
};
