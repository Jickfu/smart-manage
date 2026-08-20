import { describe, expect, it } from 'vitest';
import { parseNumberRuleScopeKey } from './scopeTree';

describe('parseNumberRuleScopeKey', () => {
  it.each([
    ['domain:430000000000000001', { type: 'domain', id: '430000000000000001' }],
    ['app:430000000000000002', { type: 'app', id: '430000000000000002' }],
    ['feature:430000000000000003', { type: 'feature', id: '430000000000000003' }],
  ])('解析树节点 %s', (key, expected) => {
    expect(parseNumberRuleScopeKey(key)).toEqual(expected);
  });

  it.each([undefined, 'all', 'domain:', 'unknown:430000000000000001'])(
    '无有效范围的节点 %s 回退到全部',
    (key) => {
      expect(parseNumberRuleScopeKey(key)).toEqual({ type: 'all' });
    },
  );
});
