import { describe, expect, it } from 'vitest';
import { getErrorFeedbackType, getOperationFeedbackClassName } from './operationFeedbackPolicy';

describe('operationFeedbackPolicy', () => {
  it.each([100400, 100404, 100409, 100410, 100411, 100413, 100422, 100429, 101600, 101601, 200001])(
    '将可修正业务错误码 %i 映射为警告',
    (errorCode) => {
      expect(getErrorFeedbackType(errorCode)).toBe('warning');
    },
  );

  it.each([-1, 100401, 100403, 100419, 100500, 100501, 100502, 100503, 100504, 999999])(
    '将安全、系统或未知错误码 %i 映射为错误',
    (errorCode) => {
      expect(getErrorFeedbackType(errorCode)).toBe('error');
    },
  );

  it('生成稳定的语义样式类名', () => {
    expect(getOperationFeedbackClassName('success')).toBe(
      'sm-operation-feedback sm-operation-feedback--success',
    );
  });
});
