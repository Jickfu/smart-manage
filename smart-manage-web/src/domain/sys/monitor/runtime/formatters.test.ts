import { describe, expect, it } from 'vitest';
import { monitorBytes, monitorPercent, monitorRatio } from './formatters';
describe('monitor formatters', () => {
  it('formats ratios as percentages instead of raw bytes', () => {
    expect(monitorPercent(0.9)).toBe(90);
    expect(monitorRatio(45, 50)).toBe(90);
  });
  it('formats byte units explicitly', () => {
    expect(monitorBytes(1024)).toBe('1.0 KB');
    expect(monitorBytes(0)).toBe('0 B');
  });
});
