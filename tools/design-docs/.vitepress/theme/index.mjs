import { h, defineAsyncComponent } from "vue";
import DefaultTheme from "vitepress/theme-without-fonts";
import { useData } from "vitepress";
import "./style.css";

export default {
  extends: DefaultTheme,
  enhanceApp({ app }) {
    // 评审试样独立异步加载，不改变业务前端依赖或图表组件。
    app.component(
      "ChartReview",
      defineAsyncComponent(() => import("./ChartReview.vue")),
    );
  },
  Layout() {
    const { frontmatter } = useData();
    return h(DefaultTheme.Layout, null, {
      "doc-before": () => {
        const status = frontmatter.value.docStatus;
        if (!status) return null;
        return h(
          "aside",
          { class: ["document-status", `document-status-${status.kind}`] },
          [h("strong", status.label), h("p", status.description)],
        );
      },
    });
  },
};
