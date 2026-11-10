import { expect, it } from 'vitest';
import { DATE_TIME_FORMAT, parseBeijingTime } from '@/domain/common/component/beijingTime';
import { createHistoryCharts } from './historyCharts';

it.each([
  ['1h', 1],
  ['6h', 5],
  ['24h', 10],
  ['7d', 30],
] as const)('%s 整桶缺失断线且不修改原始数据', (range, minutes) => {
  const start = parseBeijingTime('2026-12-31 23:30:00');
  const points = [
    { sampleTime: start.format(DATE_TIME_FORMAT), processCpuUsage: 0 },
    { sampleTime: start.add(3 * minutes, 'minute').format(DATE_TIME_FORMAT), processCpuUsage: 0.5 },
  ];
  const original = structuredClone(points);
  const chart = createHistoryCharts('INSTANCE', points, range)[0]!;
  const series = chart.option.series as Array<{ data: unknown[]; connectNulls: boolean }>;
  expect(series[0]!.data).toEqual([0, null, null, 50]);
  expect(series[0]!.connectNulls).toBe(false);
  expect(points).toEqual(original);
});

it('相邻桶和已有空值保持原意，空结果不虚构记录', () => {
  const points = [
    { sampleTime: '2027-01-01 00:00:00', processCpuUsage: undefined },
    { sampleTime: '2027-01-01 00:01:00', processCpuUsage: 0 },
  ];
  const chart = createHistoryCharts('INSTANCE', points, '1h')[0]!;
  const series = chart.option.series as Array<{ data: unknown[] }>;
  expect(series[0]!.data).toEqual([null, 0]);
  expect((chart.option.xAxis as { data: string[] }).data).toEqual([
    '2027-01-01 00:00',
    '2027-01-01 00:01',
  ]);
  const empty = createHistoryCharts('HOST', [], '7d')[0]!;
  expect((empty.option.xAxis as { data: string[] }).data).toEqual([]);
});
