<script setup>
import { onMounted, onBeforeUnmount, ref, watch } from "vue";
import { init, use } from "echarts/core";
import { BarChart, LineChart, PieChart, GaugeChart } from "echarts/charts";
import {
  GridComponent,
  LegendComponent,
  TooltipComponent,
} from "echarts/components";
import { SVGRenderer } from "echarts/renderers";
use([
  BarChart,
  LineChart,
  PieChart,
  GaugeChart,
  GridComponent,
  LegendComponent,
  TooltipComponent,
  SVGRenderer,
]);
const props = defineProps({ option: Object, label: String });
const host = ref(null);
let instance;
let observer;
function update() {
  if (!host.value) return;
  if (!props.option) {
    instance?.dispose();
    instance = undefined;
    return;
  }
  instance ??= init(host.value, undefined, { renderer: "svg" });
  instance.setOption(props.option, { notMerge: true });
  instance.resize();
}
onMounted(() => {
  update();
  observer = new ResizeObserver(() => instance?.resize());
  observer.observe(host.value);
});
watch(() => props.option, update, { flush: "post" });
onBeforeUnmount(() => {
  observer?.disconnect();
  instance?.dispose();
});
</script>
<template>
  <div
    v-show="option"
    ref="host"
    class="review-chart"
    role="img"
    :aria-label="label"
  />
</template>
<style scoped>
.review-chart {
  width: 100%;
  height: 310px;
  min-width: 0;
}
</style>
