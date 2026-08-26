import { describe, expect, it } from 'vitest';
import {
  monitorBytes,
  monitorHealthPresentation,
  monitorHistoryValues,
  monitorPercent,
  monitorRatio,
} from './formatters';
describe('monitor formatters', () => {
  it('formats ratios as percentages instead of raw bytes', () => {
    expect(monitorPercent(0.9)).toBe(90);
    expect(monitorRatio(45, 50)).toBe(90);
  });
  it('formats byte units explicitly', () => {
    expect(monitorBytes(1024)).toBe('1.0 KB');
    expect(monitorBytes(0)).toBe('0 B');
  });
  it('keeps unknown distinct from a real zero', () => {
    expect(monitorPercent(null)).toBeNull();
    expect(monitorPercent(0)).toBe(0);
    expect(monitorRatio(undefined, 100)).toBeNull();
    expect(monitorBytes(null)).toBe('-');
    expect(monitorBytes(0)).toBe('0 B');
    expect(monitorHistoryValues([{ value: null }, { value: 0 }], 'value', 100)).toEqual([null, 0]);
  });
  it('distinguishes healthy, unhealthy and unavailable health states', () => {
    expect(monitorHealthPresentation('UP')).toEqual({ color: 'success', text: 'UP（健康）' });
    expect(monitorHealthPresentation('DOWN')).toEqual({ color: 'error', text: 'DOWN（异常）' });
    expect(monitorHealthPresentation('UNKNOWN')).toEqual({
      color: 'warning',
      text: 'UNKNOWN（状态暂不可用）',
    });
  });
});
