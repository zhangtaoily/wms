import hasRole from "./permission/hasRole";
import hasPermi from "./permission/hasPermi";
import dialogDrag from "./dialog/drag";
import dialogDragWidth from "./dialog/dragWidth";
import dialogDragHeight from "./dialog/dragHeight";
import dialogResize from "./dialog/resize";
import clipboard from "./module/clipboard";
import { preventReClick } from "./module/preventReClick";
import readonly from "./readonly";
import redLabelClass from "./form/redLabelClass";
import tableRedLabelClass from "./table/tableRedLabelClass";
import diffRedLabelClass from "./form/diffRedLabelClass";
import rowItemRedLabelClass from "./table/rowItemRedLabelClass";

const install = function (Vue) {
  Vue.directive("hasRole", hasRole);
  Vue.directive("hasPermi", hasPermi);
  Vue.directive("clipboard", clipboard);
  Vue.directive("dialogDrag", dialogDrag);
  Vue.directive("dialogDragWidth", dialogDragWidth);
  Vue.directive("dialogDragHeight", dialogDragHeight);
  Vue.directive("dialogResize", dialogResize);
  Vue.directive("preventReClick", preventReClick);
  Vue.directive("readonly", readonly);  
  Vue.directive("redLabelClass", redLabelClass);
  Vue.directive("tableRedLabelClass", tableRedLabelClass);
  Vue.directive("diffRedLabelClass", diffRedLabelClass);
  Vue.directive("rowItemRedLabelClass", rowItemRedLabelClass);
};

if (window.Vue) {
  window["hasRole"] = hasRole;
  window["hasPermi"] = hasPermi;
  Vue.use(install); // eslint-disable-line
}

export default install;
