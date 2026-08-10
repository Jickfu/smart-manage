import { describe, expect, it } from 'vitest';
import { formatCpuUsage } from './formatters';

describe('formatCpuUsage', () => {
  it('CPU 使用率不可计算时显示占位符', () => {
    expect(formatCpuUsage(null)).toBe('-');
    expect(formatCpuUsage(undefined)).toBe('-');
  });

  it('CPU 使用率按两位小数显示', () => {
    expect(formatCpuUsage(12.345)).toBe('12.35%');
  });
});
