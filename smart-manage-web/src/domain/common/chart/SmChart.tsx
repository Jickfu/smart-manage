import { useEffect, useRef } from 'react';
import { init } from 'echarts/core';
import type { EChartsCoreOption } from 'echarts/core';
import { theme } from 'antd';
import './echartsRegistry';
import './chart.css';

interface SmChartProps {
  option: EChartsCoreOption;
  ariaLabel: string;
}

/** 统一管理 ECharts 生命周期、主题色与容器尺寸变化。 */
export default function SmChart({ option, ariaLabel }: SmChartProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const { token } = theme.useToken();

  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;
    const chart = init(container, undefined, { renderer: 'svg' });
    chart.setOption({
      textStyle: { color: token.colorText },
      ...option,
    });
    const observer = new ResizeObserver(() => chart.resize());
    observer.observe(container);
    return () => {
      observer.disconnect();
      chart.dispose();
    };
  }, [option, token.colorText]);

  return <div ref={containerRef} className="sm-chart" role="img" aria-label={ariaLabel} />;
}
