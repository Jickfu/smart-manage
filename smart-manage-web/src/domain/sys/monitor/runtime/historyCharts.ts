import type { EChartsCoreOption } from 'echarts/core';
import type { ColumnsType } from 'antd/es/table';
import { DATE_TIME_FORMAT, parseBeijingTime } from '@/domain/common/component/beijingTime';
import { chartCategoryColors } from '@/domain/common/chart/chartColors';
import { monitorHistoryValues } from './formatters';
import type { HistoryPoint } from './types';

function createHistoryColumns(
  definitions: Array<[name: string, key: keyof HistoryPoint, unit: string, multiplier?: number]>,
): ColumnsType<HistoryPoint> {
  return [
    {
      title: '采样时间（接口原值）',
      dataIndex: 'sampleTime',
      width: 320,
      // 核对历史采样点需要完整时间，不省略或重新截断。
      ellipsis: false,
      className: 'sm-runtime-history-data-value',
    },
    ...definitions.map(([name, key, unit]) => ({
      title: `${name.endsWith(` ${unit}`) ? name.slice(0, -unit.length - 1) : name.replace(' %', '')}（${unit}）`,
      dataIndex: key,
      width: 220,
      // 保留原始数值与精度，横向滚动承载宽表。
      ellipsis: false,
      className: 'sm-runtime-history-data-value',
      render: (value: unknown) => (value == null ? '—' : String(value)),
    })),
  ];
}

export function createHistoryCharts(
  scope: 'HOST' | 'INSTANCE',
  points: HistoryPoint[],
  range: string,
): Array<{ title: string; option: EChartsCoreOption; columns: ColumnsType<HistoryPoint> }> {
  const bucketMinutes: Record<string, number> = { '1h': 1, '6h': 5, '24h': 10, '7d': 30 };
  const step = bucketMinutes[range]! * 60_000;
  // 仅绘图补齐中间缺桶；空指标形成断点，原始查询结果仍供数据表使用。
  const plotPoints: HistoryPoint[] = [];
  for (const point of points) {
    const previous = plotPoints.at(-1);
    if (previous) {
      const end = parseBeijingTime(point.sampleTime);
      for (
        let bucket = parseBeijingTime(previous.sampleTime).add(step, 'millisecond');
        bucket.isBefore(end);
        bucket = bucket.add(step, 'millisecond')
      ) {
        plotPoints.push({ sampleTime: bucket.format(DATE_TIME_FORMAT) });
      }
    }
    plotPoints.push(point);
  }
  const groups: Array<{
    title: string;
    definitions: Array<[name: string, key: keyof HistoryPoint, unit: string, multiplier?: number]>;
  }> =
    scope === 'HOST'
      ? [
          {
            title: '主机利用率',
            definitions: [
              ['CPU %', 'cpuUsage', '原始比例，1=100%', 100],
              ['内存 %', 'memoryUsage', '原始比例，1=100%', 100],
              ['最高文件系统 %', 'filesystemUsage', '原始比例，1=100%', 100],
            ],
          },
          {
            title: '磁盘 IO',
            definitions: [
              ['读 B/s', 'diskReadBytesPerSecond', 'B/s'],
              ['写 B/s', 'diskWriteBytesPerSecond', 'B/s'],
            ],
          },
          {
            title: '网络 IO',
            definitions: [
              ['接收 B/s', 'networkReceiveBytesPerSecond', 'B/s'],
              ['发送 B/s', 'networkTransmitBytesPerSecond', 'B/s'],
            ],
          },
        ]
      : [
          {
            title: 'JVM 利用率',
            definitions: [
              ['进程 CPU %', 'processCpuUsage', '原始比例，1=100%', 100],
              ['Heap %', 'heapUsage', '原始比例，1=100%', 100],
              ['连接池 %', 'dbPoolUsage', '原始比例，1=100%', 100],
            ],
          },
          {
            title: 'HTTP 流量',
            definitions: [
              ['QPS', 'requestRate', 'req/s'],
              ['5xx req/s', 'serverErrorRate', 'req/s'],
            ],
          },
          {
            title: 'HTTP 延迟',
            definitions: [
              ['P95 ms', 'p95Ms', 'ms'],
              ['P99 ms', 'p99Ms', 'ms'],
            ],
          },
          {
            title: '线程与连接池等待',
            definitions: [
              ['线程数', 'threadCount', '个'],
              ['阻塞线程', 'blockedThreadCount', '个'],
              ['连接池等待', 'dbWaiting', '个'],
            ],
          },
        ];
  return groups.map(({ title, definitions }) => ({
    title,
    columns: createHistoryColumns(definitions),
    option: {
      color: [...chartCategoryColors],
      tooltip: { trigger: 'axis' },
      legend: { data: definitions.map((item) => item[0]), top: 0 },
      grid: { left: 56, right: 24, top: 48, bottom: 32 },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: plotPoints.map((item) => item.sampleTime.slice(0, 16)),
      },
      yAxis: { type: 'value' },
      series: definitions.map(([name, key, , multiplier]) => ({
        name,
        type: 'line',
        smooth: false,
        connectNulls: false,
        showSymbol: false,
        data: monitorHistoryValues(plotPoints, key, multiplier),
      })),
    },
  }));
}
