import request from '@/utils/request'

/**
 * 数据同步进度
 * @param {Object} data 表单数据
 * @returns 
 */
export function cancelDataSync(userId) {
  return request({
    url: `/system-maintain/data-import/cancel/${userId}`,
    method: "post",
  });
}