import { test } from "node:test";
import assert from "node:assert/strict";
import { init, use } from "echarts/core";
import { BarChart, LineChart, PieChart, GaugeChart } from "echarts/charts";
import {
  GridComponent,
  LegendComponent,
  TooltipComponent,
} from "echarts/components";
import { SVGRenderer } from "echarts/renderers";
import {
  reviewModel,
  exactValue,
  categoryColors,
  semanticColors,
  states,
} from "./chart-review.mjs";
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
const kinds = ["line", "bar", "pie", "gauge"];

test("无权限、失败、首次加载和无记录不泄漏图形或旧值", () => {
  for (const kind of kinds)
    for (const state of ["denied", "error", "loading", "empty"]) {
      const model = reviewModel(kind, state);
      assert.equal(model.option, null);
      assert.deepEqual(model.rows, []);
      assert.ok(model.notice);
    }
});
test("全零是有效结果，饼图不画等分扇区，仪表盘保留零", () => {
  for (const kind of kinds)
    assert.ok(reviewModel(kind, "zero").rows.every((row) => row.value === 0));
  assert.equal(reviewModel("pie", "zero").option, null);
  assert.match(reviewModel("pie", "zero").notice, /不表示没有记录/);
  assert.equal(reviewModel("gauge", "zero").option.series[0].data[0].value, 0);
});
test("缺失不补零，真实时间间隔、负数及大额保留", () => {
  const model = reviewModel("line", "edge");
  const data = model.option.series[0].data;
  assert.equal(data[1][1], null);
  assert.equal(model.option.series[0].connectNulls, false);
  assert.equal(data[2][0] - data[1][0], 3 * 86400000);
  assert.equal(data[2][1], -300.5);
  assert.equal(data[3][1], 123456789.12);
  assert.equal(exactValue(data[3][1], "元"), "123,456,789.12 元");
});
test("负值构成及越界仪表盘保留原始数据而不绘制误导图形", () => {
  const pie = reviewModel("pie", "edge");
  assert.equal(pie.option, null);
  assert.ok(pie.rows.some((row) => row.value < 0));
  const gauge = reviewModel("gauge", "edge");
  assert.equal(gauge.option, null);
  assert.equal(gauge.rows[0].value, 135);
});
test("原生色板对照与明确系列映射分离，语义色仅用于任务状态", () => {
  assert.equal(
    reviewModel("bar", "normal", "semantic").option.series[1].itemStyle.color,
    semanticColors.失败,
  );
  assert.equal(
    reviewModel("line", "single").option.series[1].itemStyle.color,
    categoryColors[1],
  );
  assert.equal(reviewModel("pie", "normal", "native").option.color, undefined);
  assert.equal(
    reviewModel("pie", "normal", "native").option.series[0].data[0].itemStyle,
    undefined,
  );
  assert.ok(
    reviewModel("bar", "edge", "semantic").rows.some((row) => row.value < 0),
  );
});
test("刷新失败保留数据及提示，正常构成按同一总体计算比例", () => {
  assert.deepEqual(
    reviewModel("line", "stale").rows,
    reviewModel("line", "normal").rows,
  );
  assert.match(reviewModel("line", "stale").notice, /刷新失败/);
  const pie = reviewModel("pie", "normal");
  assert.equal(
    pie.rows.reduce((sum, row) => sum + row.value, 0),
    10000,
  );
  assert.ok(
    Math.abs(pie.rows.reduce((sum, row) => sum + row.share, 0) - 100) < 0.00001,
  );
});
test("当前固定版本实际 SVG 渲染所有有效状态及横纵柱、饼环变体", () => {
  for (const kind of kinds)
    for (const [state] of states)
      for (const horizontal of [true, false]) {
        const model = reviewModel(
          kind,
          state,
          "candidate",
          horizontal,
          horizontal,
        );
        if (!model.option) continue;
        const chart = init(null, undefined, {
          renderer: "svg",
          ssr: true,
          width: 400,
          height: 310,
        });
        try {
          chart.setOption(model.option);
          assert.ok(chart.getOption().series.length > 0, `${kind}/${state}`);
          assert.match(chart.renderToSVGString(), /<svg/);
        } finally {
          chart.dispose();
        }
      }
});
