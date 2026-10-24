/**
 * 添加一年的时间
 * @param {String} dateStr
 * @returns
 */
export function addOneYear(dateStr) {
  var date = new Date(dateStr);
  date.setFullYear(date.getFullYear() + 1);
  return date.toISOString().slice(0, 10);
}

/**
 * 根据传来数值来添加月份
 * @param {String} dateStr 日期
 * @param {Number} monthNum 需要加几个月
 * @returns
 */
export function addMonths(dateStr, monthNum) {
  var date = new Date(dateStr);
  date.setMonth(date.getMonth() + monthNum);
  return date.toISOString().slice(0, 10);
}

export function addWeeks(dateStr, weekNum) {
  var date = new Date(dateStr);
  date.setDate(date.getDate() + weekNum * 7);
  return date.toISOString().slice(0, 10);
}

export function  addDays(dateString, daysToAdd) {
    // 手动解析日期字符串
    const [year, month, day] = dateString.split('-').map(Number);

    // 将日期转换为时间戳（基于本地时间）
    const date = new Date(year, month - 1, day);
    const timestamp = date.getTime(); // 获取时间戳

    // 增加天数（1 天 = 24 * 60 * 60 * 1000 毫秒）
    const newTimestamp = timestamp + daysToAdd * 24 * 60 * 60 * 1000;

    // 将时间戳转换回日期
    const newDate = new Date(newTimestamp);

    // 格式化结果为 YYYY-MM-DD
    const resultYear = newDate.getFullYear();
    const resultMonth = String(newDate.getMonth() + 1).padStart(2, '0'); // 月份补零
    const resultDay = String(newDate.getDate()).padStart(2, '0'); // 日期补零

    return `${resultYear}-${resultMonth}-${resultDay}`;
}