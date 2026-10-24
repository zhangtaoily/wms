import request from "@/utils/request";

// ===== M7 同步对账 =====
// 对账报表：同步完整性 + 台账差异（含差异单据定位）
export function getReconcile() {
  return request({ url: "/common/sync/reconcile", method: "get" });
}
