const state = {
  moduleName: "",
  // 单据状态
  currentStatus: null,
  // 数据字段
  dictDataList: null,
  // 按钮禁用列表
  hiddenButtonList: [],
  // 子表选中行
  multipleSelection: [],
  // 表单数据
  form: null,
};

const mutations = {
  CHANGE_MODULENAME: (state, moduleName) => {
    console.log("CHANGE_MODULENAME", moduleName);
    state.moduleName = moduleName;
    console.log(state.moduleName);
  },
  CHANGE_CURRENTSTATUS: (state, currentStatus) => {
    state.currentStatus = currentStatus;
  },
  DICT_DATA_LIST: (state, data) => {
    state.dictDataList = data;
  },
  HIDDEN_BUTTON_LIST: (state, data) => {
    state.hiddenButtonList = data;
  },
  CHANGE_SUBTABLESELECTION: (state, data) => {
    state.multipleSelection = data;
  },
  CHANGE_FORM: (state, data) => {
    state.form = data;
  },
};

const actions = {
  changeModuleName({ commit }, moduleName) {
    commit("CHANGE_MODULENAME", moduleName);
  },
  changeCurrentStatus({ commit }, currentStatus) {
    commit("CHANGE_CURRENTSTATUS", currentStatus);
  },
  dictDataList({ commit }, data) {
    commit("DICT_DATA_LIST", data);
  },
  hiddenButtonList({ commit }, data) {
    commit("HIDDEN_BUTTON_LIST", data);
  },
  changeForm({ commit }, data) {
    commit("CHANGE_FORM", data);
  },
};

export default {
  namespaced: true,
  state,
  mutations,
  actions,
};
