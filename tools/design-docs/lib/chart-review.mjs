// 仅用于文档示例的固定样本，不供业务页面导入。
export const categoryColors = [
  "#0E5FD8",
  "#0F766E",
  "#8B5CF6",
  "#B45309",
  "#BE185D",
  "#475569",
];
export const semanticColors = {
  成功: "#1BA854",
  失败: "#FB2323",
  跳过: "#FF991C",
};
export const states = [
  ["normal", "正常"],
  ["single", "单项"],
  ["zero", "全零"],
  ["empty", "无记录"],
  ["loading", "首次加载"],
  ["error", "请求失败"],
  ["stale", "旧数据刷新失败"],
  ["denied", "无权限"],
  ["edge", "边界样本"],
];
export function exactValue(value, unit) {
  if (value == null) return "缺失";
  return `${new Intl.NumberFormat("zh-CN", { minimumFractionDigits: unit === "元" ? 2 : 0, maximumFractionDigits: unit === "元" ? 2 : 1 }).format(value)} ${unit}`;
}
function baseOption(palette) {
  return {
    animation: false,
    useUTC: true,
    backgroundColor: "transparent",
    textStyle: { color: "#212121", fontSize: 12 },
    ...(palette === "native" ? {} : { color: categoryColors }),
    legend: { bottom: 0, textStyle: { color: "#666666", fontSize: 12 } },
    tooltip: { trigger: "axis", confine: true, renderMode: "richText" },
    grid: {
      left: 12,
      right: 20,
      top: 38,
      bottom: 50,
      outerBoundsMode: "same",
      outerBoundsContain: "all",
    },
  };
}
const axisStyle = {
  axisLabel: { color: "#666666", fontSize: 12 },
  splitLine: { lineStyle: { color: "#E5E5E5" } },
};
export function reviewModel(
  kind,
  state,
  palette = "candidate",
  horizontal = true,
  ring = true,
) {
  const unit =
    kind === "line" || kind === "pie" || (kind === "bar" && state === "edge")
      ? "元"
      : kind === "bar"
        ? "次"
        : "%";
  const title = {
    line: "业务金额趋势",
    bar: "各部门任务执行",
    pie: "业务金额构成",
    gauge: "目标达成率",
  }[kind];
  const model = {
    kind,
    title,
    unit,
    rows: [],
    option: null,
    notice: "",
    summary: "",
  };
  const notices = {
    loading: "正在加载当前条件的数据…",
    empty: "当前条件下没有记录，请调整范围。",
    error: "数据加载失败，请重试。",
    denied: "没有该指标的查看权限，不显示历史数据。",
  };
  if (notices[state]) return { ...model, notice: notices[state] };
  if (state === "stale")
    model.notice = "刷新失败，当前保留的是上次成功数据（模拟）。";
  const zero = state === "zero";
  const single = state === "single";
  const edge = state === "edge";
  const option = baseOption(palette);
  if (kind === "line") {
    const dates = [
      "2026-01-01",
      "2026-01-02",
      "2026-01-05",
      "2026-01-10",
    ].slice(0, single ? 1 : 4);
    const values = edge
      ? [1250.25, null, -300.5, 123456789.12]
      : [1250.25, 1620, 1480.5, 2350.75];
    const names = ["本期", "上期"];
    option.xAxis = {
      ...axisStyle,
      type: "time",
      axisLabel: {
        ...axisStyle.axisLabel,
        hideOverlap: true,
        formatter: (value) => new Date(value).toISOString().slice(5, 10),
      },
    };
    option.yAxis = { ...axisStyle, type: "value", name: "金额（元）" };
    option.tooltip.valueFormatter = (value) => exactValue(value, unit);
    option.series = names.map((name, index) => {
      const data = dates.map((date, position) => {
        const raw = zero ? 0 : values[position];
        const value =
          raw == null
            ? null
            : index === 0
              ? raw
              : Math.round(raw * 0.8 * 100) / 100;
        model.rows.push({ dimension: date, name, value, unit });
        return [Date.parse(`${date}T00:00:00Z`), value];
      });
      return {
        name,
        type: "line",
        data,
        connectNulls: false,
        smooth: false,
        symbol: index === 0 ? "circle" : "diamond",
        symbolSize: 6,
        lineStyle: { width: 2, type: index === 0 ? "solid" : "dashed" },
        ...(palette === "native"
          ? {}
          : { itemStyle: { color: categoryColors[index] } }),
      };
    });
    model.summary = edge
      ? "保留真实日期间隔；01-02 缺失断开，负值与大额不裁掉。完整金额见数据表。"
      : "真实日期间隔；本期实线圆点，上期虚线菱形。";
  } else if (kind === "bar") {
    const labels = (
      edge
        ? [
            "华东区域客户服务与运营支持部门",
            "华南区域交付与售后保障部门",
            "西部区域联合服务部门",
          ]
        : ["运营部", "交付部", "服务部"]
    ).slice(0, single ? 1 : 3);
    const category = {
      ...axisStyle,
      type: "category",
      data: labels,
      axisLabel: {
        ...axisStyle.axisLabel,
        interval: 0,
        formatter: (label) => label.match(/.{1,7}/gu).join("\n"),
      },
    };
    const numeric = {
      ...axisStyle,
      type: "value",
      name: edge ? "金额（元）" : "执行（次）",
      ...(edge ? {} : { min: 0, minInterval: 1 }),
    };
    option.xAxis = horizontal ? numeric : category;
    option.yAxis = horizontal ? { ...category, inverse: true } : numeric;
    option.tooltip.valueFormatter = (value) => exactValue(value, unit);
    option.grid.bottom = horizontal ? 50 : edge ? 95 : 50;
    const seriesNames = edge ? ["净变动", "预算"] : ["成功", "失败", "跳过"];
    const sampleValues = edge
      ? [
          [123456789.12, -2400.5, 0],
          [1000000, 3200.25, 0],
        ]
      : [
          [32, 28, 24],
          [4, 2, 3],
          [2, 3, 1],
        ];
    if (edge) model.title = "各部门金额净变动";
    option.series = seriesNames.map((name, index) => {
      const values = sampleValues[index]
        .slice(0, labels.length)
        .map((value) => (zero ? 0 : value));
      values.forEach((value, position) =>
        model.rows.push({ dimension: labels[position], name, value, unit }),
      );
      return {
        name,
        type: "bar",
        barMaxWidth: 20,
        data: values,
        ...(palette === "native"
          ? {}
          : {
              itemStyle: {
                color:
                  palette === "semantic" && !edge
                    ? semanticColors[name]
                    : categoryColors[index],
              },
            }),
      };
    });
    model.summary = edge
      ? "净变动允许负值，按零基线两侧表达；原值及长部门名称见数据表。"
      : "按业务部门顺序比较，数量轴从零开始；长名称换行，完整名称见数据表。";
  } else if (kind === "pie") {
    const names = ["产品服务", "技术支持", "其他"].slice(0, single ? 1 : 3);
    const values = (edge ? [1200, -200, 0.01] : [6200.25, 2400.5, 1399.25])
      .slice(0, names.length)
      .map((value) => (zero ? 0 : value));
    model.rows = names.map((name, index) => ({
      dimension: name,
      name: "金额",
      value: values[index],
      unit,
    }));
    const total = values.reduce((sum, value) => sum + value, 0);
    model.summary = `总体金额：${exactValue(total, unit)}。构成占比使用相同总体。`;
    if (values.some((value) => value < 0))
      return {
        ...model,
        summary: `原始值代数合计：${exactValue(total, unit)}；含负值，不计算构成占比。`,
        notice: "存在负值，停止绘制构成图；保留原值，建议改用条形图。",
      };
    if (zero)
      return {
        ...model,
        notice: "金额总计为 0；不绘制等分扇区，不表示没有记录。",
      };
    model.rows.forEach((row) => {
      row.share = (row.value / total) * 100;
    });
    option.tooltip = {
      trigger: "item",
      confine: true,
      renderMode: "richText",
      formatter: (item) =>
        `${item.name}\n${exactValue(item.value, unit)}\n占比 ${item.percent}%`,
    };
    option.series = [
      {
        type: "pie",
        radius: ring ? ["42%", "66%"] : "66%",
        center: ["50%", "42%"],
        stillShowZeroSum: false,
        label: { show: false },
        data: names.map((name, index) => ({
          name,
          value: values[index],
          ...(palette === "native"
            ? {}
            : { itemStyle: { color: categoryColors[index] } }),
        })),
      },
    ];
  } else {
    const value = zero ? 0 : edge ? 135 : 76;
    model.rows = [{ dimension: "当前达成率", name: "达成率", value, unit }];
    model.summary = `实际值 ${exactValue(value, unit)}；范围 0–100%，目标 80%。此阈值仅为试样。`;
    if (value < 0 || value > 100)
      return {
        ...model,
        notice: "实际值超出量程：保留 135%，停止绘制指针，不截成 100%。",
      };
    option.legend = undefined;
    option.tooltip = {
      trigger: "item",
      confine: true,
      renderMode: "richText",
      valueFormatter: (value) => exactValue(value, unit),
    };
    option.series = [
      {
        name: "达成率",
        type: "gauge",
        min: 0,
        max: 100,
        startAngle: 210,
        endAngle: -30,
        splitNumber: 5,
        radius: "82%",
        axisLine: { lineStyle: { width: 10, color: [[1, "#E5E5E5"]] } },
        progress: {
          show: true,
          width: 10,
          itemStyle: {
            color: palette === "native" ? undefined : categoryColors[0],
          },
        },
        pointer: { width: 4 },
        axisLabel: { color: "#666666", distance: 14 },
        axisTick: { show: false },
        splitLine: { length: 8, lineStyle: { color: "#999999" } },
        detail: {
          formatter: "{value}%",
          fontSize: 28,
          color: "#212121",
          offsetCenter: [0, "62%"],
        },
        data: [{ value, name: "达成率" }],
        title: { offsetCenter: [0, "35%"], color: "#666666" },
      },
    ];
  }
  return { ...model, option };
}
