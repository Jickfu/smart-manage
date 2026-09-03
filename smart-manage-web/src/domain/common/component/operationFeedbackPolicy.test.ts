import { describe, expect, it } from 'vitest';
import { getOperationFeedbackClassName } from './operationFeedbackPolicy';

describe('operationFeedbackPolicy', () => {
  it('生成稳定的语义样式类名', () => {
    expect(getOperationFeedbackClassName('success')).toBe(
      'sm-operation-feedback sm-operation-feedback--success',
    );
  });
});
