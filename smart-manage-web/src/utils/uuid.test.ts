import { afterEach, describe, expect, it, vi } from 'vitest';
import { createQueryFeedbackRuntime } from '@/api/queryErrorFeedback';
import { createAddNewTabKey } from '@/domain/common/page/tab/tabKeys';
import { createDataScopeRuleDraft } from '@/domain/sys/base/role/dataScopeRuleEditor';
import { generateUUID } from './index';

afterEach(() => vi.unstubAllGlobals());

describe('UUID HTTP 兼容', () => {
  it('优先调用原生 randomUUID 并保留接收对象', () => {
    const nativeUUID = '01234567-89ab-4cde-8fab-0123456789ab';
    const cryptoStub = {
      randomUUID() {
        expect(this).toBe(cryptoStub);
        return nativeUUID;
      },
      getRandomValues: vi.fn(),
    };
    vi.stubGlobal('crypto', cryptoStub);
    expect(generateUUID()).toBe(nativeUUID);
    expect(cryptoStub.getRandomValues).not.toHaveBeenCalled();
  });

  it.each([
    [0, '00000000-0000-4000-8000-000000000000'],
    [255, 'ffffffff-ffff-4fff-bfff-ffffffffffff'],
  ])('回退正确设置版本和变体位并保留随机位：%i', (fill, expected) => {
    vi.stubGlobal('crypto', {
      getRandomValues: (bytes: Uint8Array) => bytes.fill(fill),
    });
    expect(generateUUID()).toBe(expected);
  });

  it('缺失 randomUUID 时启动反馈运行时、创建页签及规则行均正常', () => {
    const getRandomValues = crypto.getRandomValues.bind(crypto);
    vi.stubGlobal('crypto', { getRandomValues });
    const runtime = createQueryFeedbackRuntime();
    try {
      const identifiers = Array.from({ length: 100 }, () => generateUUID());
      expect(new Set(identifiers).size).toBe(identifiers.length);
      for (const identifier of identifiers) {
        expect(identifier).toMatch(
          /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/,
        );
      }
      expect(createAddNewTabKey('demo')).not.toBe(createAddNewTabKey('demo'));
      const rule = { resourceType: 'purchase', scopeType: 'ORG' as const, orgIds: [] };
      expect(createDataScopeRuleDraft(rule).localKey).not.toBe(
        createDataScopeRuleDraft(rule).localKey,
      );
    } finally {
      runtime.queryClient.clear();
    }
  });
});
