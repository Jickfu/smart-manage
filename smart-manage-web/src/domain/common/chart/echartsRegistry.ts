import { use as registerEChartsComponents } from 'echarts/core';
import { GaugeChart, PieChart } from 'echarts/charts';
import { LegendComponent, TooltipComponent } from 'echarts/components';
import { SVGRenderer } from 'echarts/renderers';

registerEChartsComponents([GaugeChart, PieChart, LegendComponent, TooltipComponent, SVGRenderer]);
