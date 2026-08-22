export type OperationFeedbackType = 'success' | 'warning' | 'error' | 'info';

/** 用户修正输入或业务状态后即可重试的稳定错误码。 */
const WARNING_ERROR_CODES = new Set([
  100400, // 请求格式或方式不符合接口约束
  100404, // 业务对象已不存在
  100409, // 乐观锁冲突
  100410, // 唯一性冲突
  100411, // 资源仍被引用
  100413, // 文件超过大小限制
  100422, // 参数校验失败
  100429, // 请求频率过高
  101600, // 验证码错误
  101601, // 验证码过期
  200001, // 单据状态不允许当前操作
]);

/** 未知异常默认按错误处理，避免把系统故障弱化为普通业务提醒。 */
export function getErrorFeedbackType(errorCode: number): 'warning' | 'error' {
  return WARNING_ERROR_CODES.has(errorCode) ? 'warning' : 'error';
}

export function getOperationFeedbackClassName(type: OperationFeedbackType) {
  return `sm-operation-feedback sm-operation-feedback--${type}`;
}
