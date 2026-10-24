import Vue from 'vue'

import Cookies from 'js-cookie'

import Element from 'element-ui'
import './assets/styles/element-variables.scss'

import '@/assets/styles/index.scss' // global css
import '@/assets/styles/ruoyi.scss' // ruoyi css
import App from './App'
import store from './store'
import router from './router'
import directive from './directive' // directive
import plugins from './plugins' // plugins
import { download } from '@/utils/request'

import './assets/icons' // icon
import './permission' // permission control
import { getDicts } from "@/api/system/dict/data";
import { getConfigKey, updateConfigByKey } from "@/api/system/config";
import { parseTime, resetForm, addDateRange, selectDictLabel, selectDictLabels, handleTree } from "@/utils/ruoyi";
// 分页组件
import Pagination from "@/components/Pagination";
// 自定义表格工具组件
import RightToolbar from "@/components/RightToolbar"
// 富文本组件
import Editor from "@/components/Editor"
// 文件上传组件
import FileUpload from "@/components/FileUpload"
// 图片上传组件
import ImageUpload from "@/components/ImageUpload"
// 图片预览组件
import ImagePreview from "@/components/ImagePreview"
// 字典标签组件
import DictTag from '@/components/DictTag'
// 头部标签组件
import VueMeta from 'vue-meta'
// 字典数据组件
import DictData from '@/components/DictData'
// 动态弹窗组件/关联单选
import DynamicDialog from "@/components/DynamicDialog/index.vue";
// 动态弹窗树形组件（脏代码比较多）
import DynamicTree from "@/components/DynamicTree/index.vue";
// 二级弹窗
import SecondaryDialog from "@/components/SecondaryDialog/index.vue";
// 详情页按钮组件
import FormButtons from "@/components/FormButtons/index.vue";

// 子表组件
import SubTable from "@/components/SubTable/index.vue";
// 可拖拽列子表组件
import DragSubTable from "@/components/SubTable/DragSubTable.vue";

// 数字输入框组件
import InputNumber from "@/components/InputNumber/index.vue";
// 接口日志列表
import InterfaceLogList from '@/components/InterfaceLogList/index.vue'
// 全局自定义变量
import { globalVariable } from "@/utils/variables.js";
import FrameBox from "@/components/FrameBox/index.vue";
// 是/否 单选组件
import YNSelect from '@/components/YNSelect/index.vue'
// 数组字典下拉框
import CustomDictSelect from '@/components/CustomDictSelect/index.vue'
// 千分位输入框可以为空
import CommaInput from "@/components/commaInput/index.vue";
import SmartCommaInput from "@/components/SmartCommaInput/index.vue";
// ERP单号组件
// 上传文件组件
import UploadFiles from "@/components/UploadFiles/index.vue";
// 全局方法挂载
Vue.prototype.getDicts = getDicts
Vue.prototype.getConfigKey = getConfigKey
Vue.prototype.updateConfigByKey = updateConfigByKey
Vue.prototype.parseTime = parseTime
Vue.prototype.resetForm = resetForm
Vue.prototype.addDateRange = addDateRange
Vue.prototype.selectDictLabel = selectDictLabel
Vue.prototype.selectDictLabels = selectDictLabels
Vue.prototype.download = download
Vue.prototype.handleTree = handleTree
Vue.prototype.variables = globalVariable;

// 全局组件挂载
Vue.component('DictTag', DictTag)
Vue.component('Pagination', Pagination)
Vue.component('RightToolbar', RightToolbar)
Vue.component('Editor', Editor)
Vue.component('FileUpload', FileUpload)
Vue.component('ImageUpload', ImageUpload)
Vue.component('ImagePreview', ImagePreview)
Vue.component("DynamicDialog", DynamicDialog);
Vue.component("DynamicTree", DynamicTree);
Vue.component("FormButtons", FormButtons);
Vue.component("SubTable", SubTable);
Vue.component("DragSubTable", DragSubTable);
Vue.component("InputNumber", InputNumber);
Vue.component("SecondaryDialog", SecondaryDialog);
Vue.component("InterfaceLogList", InterfaceLogList);
Vue.component("FrameBox", FrameBox);
Vue.component("YNSelect", YNSelect);
Vue.component("CustomDictSelect", CustomDictSelect);
Vue.component("CustomDictSelect", CustomDictSelect);
Vue.component("CommaInput", CommaInput);
Vue.component("SmartCommaInput", SmartCommaInput);
Vue.component("UploadFiles", UploadFiles);
Vue.use(directive)
Vue.use(plugins)
Vue.use(VueMeta)
DictData.install()

/**
 * If you don't want to use mock-server
 * you want to use MockJs for mock api
 * you can execute: mockXHR()
 *
 * Currently MockJs will be used in the production environment,
 * please remove it before going online! ! !
 */

// 修改 el-dialog 默认点击遮照为不关闭
Element.Dialog.props.closeOnClickModal.default = false

Vue.use(Element, {
  size: Cookies.get('size') || 'mini' // set element-ui default size
})

Vue.config.productionTip = false
Vue.config.devtools = true
new Vue({
  el: '#app',
  router,
  store,
  render: h => h(App)
})
