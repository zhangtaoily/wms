// 校验是否小于等于0
export function greaterZero(label) {
  return (rule, value, callback) => {
    // console.log(rule, value, callback);
    if (parseFloat(value) <= 0) {
      callback(new Error(`${label}要大于0`));
    } else {
      callback();
    }
  };
}

// 表头生成必填校验红星样式
export function headerAddRedStar(h, { column }) {
  return [
    h("span", " " + column.label),
    h("span", { style: "color: red" }, " *"),
  ];
  // return h(
  //   "div",
  //   {
  //     style: {},
  //   },
  //   [
  //     h("span", { style: { color: "#F56C6C" } }, column.label),
  //     h("span", { style: { color: "#F56C6C" } }, "*"),
  //   ]
  // );
}
// 校验日期不小于当前日期
export function validateDate(rule, value, callback) {
  if(!value){
    callback();
  }
  const currentDate = new Date();
  const selectedDate = new Date(value);
  // 将时间部分设置为0，以便只比较日期部分
  currentDate.setHours(0, 0, 0, 0);
  selectedDate.setHours(0, 0, 0, 0);
  if (selectedDate < currentDate) {
    callback(new Error("日期不能小于当前日期"));
  } else {
    callback();
  }
}

// 校验日期不小于当前日期且不大于当前月份的最后一天
export function validateDateAndLessNextMonth(rule, value, callback) {
  const currentDate = new Date();
  const selectedDate = new Date(value);

  // 将时间部分设置为0，以便只比较日期部分
  currentDate.setHours(0, 0, 0, 0);
  selectedDate.setHours(0, 0, 0, 0);

  // 获取当前月份的最后一天
  const currentYear = currentDate.getFullYear();
  const currentMonth = currentDate.getMonth();
  const lastDayOfMonth = new Date(currentYear, currentMonth + 1, 0).getDate();

  // 设置当前日期为当前月份的最后一天
  const lastDateOfMonth = new Date(currentYear, currentMonth, lastDayOfMonth);
  lastDateOfMonth.setHours(0, 0, 0, 0);

  if (selectedDate < currentDate) {
    callback(new Error("日期不能小于当前日期"));
  } else if (selectedDate > lastDateOfMonth) {
    callback(new Error("日期不能大于当前月份的最后一天"));
  } else {
    callback();
  }
}

