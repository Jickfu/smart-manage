import { use as registerEChartsComponents } from 'echarts/core';
import { BarChart, GaugeChart, LineChart, PieChart } from 'echarts/charts';
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components';
import { SVGRenderer } from 'echarts/renderers';

registerEChartsComponents([
  BarChart,
  GaugeChart,
  LineChart,
  PieChart,
  GridComponent,
  LegendComponent,
  TooltipComponent,
  SVGRenderer,
]);
