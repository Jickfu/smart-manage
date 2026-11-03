import { init } from 'echarts/core';
import { expect, it } from 'vitest';
import './echartsRegistry';

it('集中注册支持调度首页的堆叠柱状系列', () => {
  const chart = init(null, undefined, { renderer: 'svg', ssr: true, width: 600, height: 280 });
  try {
    chart.setOption({
      legend: {},
      xAxis: { type: 'category', data: ['11-01', '11-02'] },
      yAxis: { type: 'value' },
      series: [
        { name: '成功', type: 'bar', stack: 'execution', data: [10, 20] },
        { name: '失败', type: 'bar', stack: 'execution', data: [1, 2] },
        { name: '互斥跳过', type: 'bar', stack: 'execution', data: [2, 3] },
      ],
    });
    const option = chart.getOption() as { series: Array<{ type: string; data: number[] }> };
    expect(option.series.map((series) => series.type)).toEqual(['bar', 'bar', 'bar']);
    expect(option.series.map((series) => series.data)).toEqual([
      [10, 20],
      [1, 2],
      [2, 3],
    ]);
    expect(chart.renderToSVGString()).toContain('互斥跳过');
  } finally {
    chart.dispose();
  }
});
