import request from "@/utils/request";

// ===== 立库扫码终端（M6 FR-07）=====
// 月台回路列表（11 个）
export function getBays() {
  return request({ url: "/common/terminal/bays", method: "get" });
}
// 任务检索（扫描/输入批次任务号）：业务批次任务 + 托盘批次任务双面板
export function searchTasks(keyword) {
  return request({ url: "/common/terminal/tasks", method: "get", params: { keyword } });
}
// 开始作业：仓储任务下发 WCS 选托下架 {whTaskId, bay}
export function startOutbound(data) {
  return request({ url: "/common/terminal/start", method: "post", data });
}
// 空托出库 {bay}
export function startEmptyOutbound(data) {
  return request({ url: "/common/terminal/start/empty", method: "post", data });
}
// 条码出库测试（终端联调）{bay}
export function startTestOutbound(data) {
  return request({ url: "/common/terminal/start/test", method: "post", data });
}
// 作业结果回流（WCS 完成上报）{opTaskId, palletNo}
export function completeOutbound(data) {
  return request({ url: "/common/terminal/complete", method: "post", data });
}
// 执行中/近期作业任务
export function getOpTasks(execStatus) {
  return request({ url: "/common/terminal/op-tasks", method: "get", params: { execStatus } });
}

// ===== PDA 工作台（M6）=====
// 扫托查询：托盘→箱码→批次→仓位 追溯
export function palletTrace(palletNo) {
  return request({ url: "/common/terminal/pda/trace", method: "get", params: { palletNo } });
}
// 扫箱回填：箱码台账信息（码单收录/组托用）
export function boxTrace(boxCode) {
  return request({ url: "/common/terminal/pda/box", method: "get", params: { boxCode } });
}
// PDA 入库待办
export function pdaInbounds() {
  return request({ url: "/common/terminal/pda/inbounds", method: "get" });
}
// PDA 出库待办
export function pdaOutbounds() {
  return request({ url: "/common/terminal/pda/outbounds", method: "get" });
}
// PDA 同步状态查询
export function pdaSyncList() {
  return request({ url: "/common/terminal/pda/sync", method: "get" });
}
// PDA 引擎字段配置（mobile_show/mobile_seq/scan_input）
export function mobileFieldConfig(name) {
  return request({ url: `/system/field/config/mobile/${name}`, method: "get" });
}
