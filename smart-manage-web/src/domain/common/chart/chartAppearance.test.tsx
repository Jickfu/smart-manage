import { renderToString } from 'react-dom/server';
import { init } from 'echarts/core';
import type { EChartsCoreOption } from 'echarts/core';
import { beforeEach, expect, it, vi } from 'vitest';
import './echartsRegistry';
import Home from '@/pages/home/Home';
import MessageHome from '@/domain/sys/message/home/MessageHome';
import SchedulerHome from '@/domain/sys/scheduler/home/SchedulerHome';

const captured = vi.hoisted(() => ({ options: [] as EChartsCoreOption[] }));
vi.mock('./SmChart', () => ({
  default: ({ option }: { option: EChartsCoreOption }) => {
    captured.options.push(option);
    return null;
  },
}));
vi.mock('@/domain/common/home/QuickLaunchCard', () => ({ default: () => null }));
vi.mock('@tanstack/react-query', () => ({
  useQuery: () => ({
    isLoading: false,
    isError: false,
    data: {
      totalJobs: 3,
      enabledJobs: 3,
      pausedJobs: 0,
      todayExecutions: 11,
      todayFailedExecutions: 2,
      runningExecutions: 0,
      trends: [{ date: '2026-11-03', success: 8, failed: 2, skipped: 1 }],
    },
  }),
}));

beforeEach(() => {
  captured.options = [];
});

it('实际首页和消息趋势使用项目分类色，折线保留非平滑与缺失断点', () => {
  renderToString(<Home />);
  renderToString(<MessageHome />);
  expect(captured.options).toHaveLength(4);
  for (const option of captured.options) {
    const chart = init(null, undefined, { renderer: 'svg', ssr: true, width: 600, height: 280 });
    try {
      chart.setOption(option);
      const rendered = chart.getOption() as {
        color: string[];
        series: Array<{ type: string; smooth?: boolean; connectNulls?: boolean }>;
      };
      expect(rendered.color).toEqual([
        '#0E5FD8',
        '#0F766E',
        '#8B5CF6',
        '#B45309',
        '#BE185D',
        '#475569',
      ]);
      for (const series of rendered.series.filter((series) => series.type === 'line')) {
        expect(series.smooth).toBe(false);
        expect(series.connectNulls).toBe(false);
      }
      expect(chart.renderToSVGString().toLowerCase()).toContain('#0e5fd8');
    } finally {
      chart.dispose();
    }
  }
});

it('实际调度系列换序和图例切换后仍按业务状态着色且不改变数据', () => {
  renderToString(<SchedulerHome />);
  const option = captured.options[0]!;
  const series = option.series as Array<{ name: string; data: number[] }>;
  const chart = init(null, undefined, { renderer: 'svg', ssr: true, width: 600, height: 280 });
  try {
    chart.setOption({ ...option, series: [...series].reverse() });
    chart.dispatchAction({ type: 'legendUnSelect', name: '成功' });
    chart.dispatchAction({ type: 'legendSelect', name: '成功' });
    const rendered = chart.getOption() as {
      series: Array<{ name: string; itemStyle: { color: string }; data: number[] }>;
    };
    expect(
      rendered.series.map(({ name, itemStyle, data }) => [name, itemStyle.color, data]),
    ).toEqual([
      ['互斥跳过', '#FF991C', [1]],
      ['失败', '#FB2323', [2]],
      ['成功', '#1BA854', [8]],
    ]);
    const svg = chart.renderToSVGString().toLowerCase();
    for (const color of ['#1ba854', '#fb2323', '#ff991c']) expect(svg).toContain(color);
  } finally {
    chart.dispose();
  }
});
