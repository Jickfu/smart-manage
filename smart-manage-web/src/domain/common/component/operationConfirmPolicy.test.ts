import { describe, expect, it } from 'vitest';
import { getOperationConfirmPolicy } from './operationConfirmPolicy';

describe('操作确认策略', () => {
  it.each(['delete', 'destructive'] as const)('%s 使用危险按钮', (type) => {
    expect(getOperationConfirmPolicy(type)).toEqual({ dangerous: true });
  });

  it.each(['warning', 'normal'] as const)('%s 使用普通主按钮', (type) => {
    expect(getOperationConfirmPolicy(type)).toEqual({ dangerous: false });
  });
});
