export type OperationFeedbackType = 'success' | 'warning' | 'error' | 'info';

export function getOperationFeedbackClassName(type: OperationFeedbackType) {
  return `sm-operation-feedback sm-operation-feedback--${type}`;
}
