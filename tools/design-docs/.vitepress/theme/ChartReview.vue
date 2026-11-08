<script setup>
import { computed, ref } from "vue";
import ReviewChart from "./ReviewChart.vue";
import {
  reviewModel,
  states,
  exactValue,
  categoryColors,
} from "../../lib/chart-review.mjs";
const state = ref("normal");
const palette = ref("semantic");
const horizontal = ref(true);
const ring = ref(true);
const models = computed(() =>
  ["line", "bar", "pie", "gauge"].map((kind) =>
    reviewModel(kind, state.value, palette.value, horizontal.value, ring.value),
  ),
);
</script>
<template>
  <section class="chart-review" aria-label="图表示例">
    <p class="review-note">通用图表示例 · 固定模拟数据 · 不调用业务接口</p>
    <div class="review-controls">
      <label
        >状态
        <select v-model="state">
          <option v-for="[value, label] in states" :key="value" :value="value">
            {{ label }}
          </option>
        </select></label
      >
      <label
        >配色
        <select v-model="palette">
          <option value="candidate">项目分类色</option>
          <option value="semantic">任务采用语义色</option>
          <option value="native">ECharts 原生色板</option>
        </select></label
      >
      <label><input v-model="horizontal" type="checkbox" /> 横向条形</label>
      <label><input v-model="ring" type="checkbox" /> 环形</label>
    </div>
    <ul class="review-palette" aria-label="分类色板">
      <li v-for="color in categoryColors" :key="color">
        <span :style="{ backgroundColor: color }" aria-hidden="true"></span
        >{{ color }}
      </li>
    </ul>
    <p class="review-context">
      模拟口径：2026-01-01 至
      01-10（UTC）；图表各自单位见标题与数据表。正常数据模拟更新时间：2026-01-10
      12:00 UTC。任务图三类可选择语义色，其余图保留分类策略。
    </p>
    <div class="review-grid">
      <article v-for="model in models" :key="model.kind" class="review-card">
        <h3>{{ model.title }}</h3>
        <p v-if="model.notice" class="review-message" role="status">
          {{ model.notice }}
        </p>
        <button
          v-if="state === 'error' || state === 'stale'"
          type="button"
          @click="state = 'normal'"
        >
          模拟重试成功
        </button>
        <ReviewChart
          :option="model.option"
          :label="`${model.title}，单位${model.unit}；完整数值见下方数据表`"
        />
        <p v-if="model.summary">{{ model.summary }}</p>
        <details v-if="model.rows.length" open>
          <summary>完整数据（{{ model.rows.length }} 项）</summary>
          <div class="review-table">
            <table>
              <caption>
                {{
                  model.title
                }}原值；图例隐藏仅影响图形，不改变此表口径
              </caption>
              <thead>
                <tr>
                  <th>维度</th>
                  <th>指标</th>
                  <th>完整值</th>
                  <th v-if="model.kind === 'pie'">占比</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, index) in model.rows" :key="index">
                  <td>{{ row.dimension }}</td>
                  <td>{{ row.name }}</td>
                  <td>{{ exactValue(row.value, row.unit) }}</td>
                  <td v-if="model.kind === 'pie'">
                    {{
                      row.share == null ? "不适用" : row.share.toFixed(1) + "%"
                    }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </details>
      </article>
    </div>
  </section>
</template>
<style scoped>
.chart-review {
  color: #212121;
}
.review-note {
  padding: 12px;
  background: #fff7e6;
  border-left: 3px solid #b45309;
}
.review-controls {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin: 16px 0;
}
.review-controls label {
  display: flex;
  align-items: center;
  gap: 6px;
}
.review-controls select,
.review-card button {
  border: 1px solid #d9d9d9;
  border-radius: 2px;
  padding: 5px 8px;
  background: white;
  color: #212121;
}
.review-controls :focus-visible,
summary:focus-visible,
button:focus-visible {
  outline: 2px solid #0e5fd8;
  outline-offset: 2px;
}
.review-palette {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0;
  list-style: none;
  font-size: 12px;
}
.review-palette li {
  display: flex;
  align-items: center;
  gap: 4px;
}
.review-palette span {
  display: inline-block;
  width: 14px;
  height: 14px;
  border-radius: 2px;
}
.review-context {
  color: #666;
  font-size: 12px;
}
.review-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.review-card {
  min-height: 400px;
  min-width: 0;
  border: 1px solid #e5e5e5;
  padding: 12px;
  background: #fff;
}
.review-card h3 {
  margin: 0 0 8px;
  font-size: 16px;
}
.review-card p {
  font-size: 12px;
  line-height: 1.7;
}
.review-message {
  padding: 10px;
  border: 1px solid #e5e5e5;
  background: #f5f5f5;
  min-height: 64px;
}
.review-table {
  overflow-x: auto;
}
.review-table table {
  display: table;
  width: 100%;
  font-size: 12px;
  margin: 8px 0;
}
.review-table caption {
  text-align: left;
  font-size: 12px;
  color: #666;
}
.review-table td:last-child {
  white-space: nowrap;
}
@media (max-width: 1100px) {
  .review-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
