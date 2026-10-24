/**
 * 虚拟滚动全局配置
 */
export default {
  // 默认虚拟滚动阈值
  defaultThreshold: 240,

  // 每个模块的自定义配置
  moduleConfig: {
    // KEY 使用的是 functionName 字段
    // 示例：物料数据模块
    // 'MAT_DATA': { threshold: 300, forceVirtual: false },
    // 示例：客户信息模块
    // 'CLIENT_INFO': { threshold: 200, forceVirtual: true }
  }
}
