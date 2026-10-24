/**
 * 处理溢短率输入
 * @param {any} value - 输入的值
 * @param {Object} messageHandler - 消息处理对象，包含 $message 方法
 * @returns {string} 处理后的值
 */
export function handleSpillShortRateInput(value,messageHandler) {
  // 处理空值
  if (!value && value !== 0) {
    return '';
  }

  const original = value.toString();

  // 1. 移除非数字和小数点的所有字符
  let cleaned = original.replace(/[^\d.]/g, '');

  // 2. 处理多个小数点：只保留第一个，其余的删除
  const dots = cleaned.split('').filter(char => char === '.');
  if (dots.length > 1) {
    const firstDotPos = cleaned.indexOf('.');
    const beforeDot = cleaned.substring(0, firstDotPos + 1);
    const afterDot = cleaned.substring(firstDotPos + 1).replace(/\./g, '');
    cleaned = beforeDot + afterDot;

    // 弹窗提示
    if (messageHandler && messageHandler.$message) {
      messageHandler.$message.error('溢短率格式不正确，不能包含多个小数点');
    }
    return cleaned;
  }

  // 3. 防止以多个0开头（如 00123）
  if (cleaned !== '' && cleaned !== '.' && !cleaned.startsWith('0.')) {
    const parts = cleaned.split('.');
    if (parts[0] && parts[0].length > 1 && parts[0].startsWith('0')) {
      parts[0] = parts[0].replace(/^0+/, '');
      // 如果全部是0，保留一个0
      if (parts[0] === '') parts[0] = '0';
      cleaned = parts.join('.');
    }
  }

  // 4. 检查是否包含非法字符（如果清理后和原值不同，说明有非法字符）
  if (cleaned !== original) {
    // 区分是多小数点还是其他非法字符
    const hasMultipleDots = (original.match(/\./g) || []).length > 1;
    if (hasMultipleDots) {
      if (messageHandler && messageHandler.$message) {
        messageHandler.$message.error('溢短率格式不正确，不能包含多个小数点');
      }
    } else {
      if (messageHandler && messageHandler.$message) {
        messageHandler.$message.error('溢短率必须为数值，不能包含字母、符号或其他字符');
      }
    }
    return cleaned;
  }

  return cleaned;
}

/**
 * 默认导出（可选）
 */
export default handleSpillShortRateInput;
