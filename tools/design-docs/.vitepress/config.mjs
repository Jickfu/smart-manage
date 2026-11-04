import { fileURLToPath } from "node:url";
import { createRequire } from "node:module";
import { defineConfig } from "vitepress";
import {
  excludedDocuments,
  headingSlug,
  pageStatus,
  pageRewrites,
  repositoryReferencesPlugin,
  renderSearchDocument,
  tokenizeSearchText,
} from "../lib/content.mjs";

const docsRoot = fileURLToPath(new URL("../../../docs/", import.meta.url));
const require = createRequire(import.meta.url);
const base = process.env.SM_DESIGN_DOCS_BASE ?? "/";
if (!/^\/(?:[A-Za-z0-9_-]+\/)*$/.test(base)) {
  throw new Error("SM_DESIGN_DOCS_BASE 必须为 / 或 /目录/ 形式的站点路径");
}

export default defineConfig({
  lang: "zh-CN",
  title: "Smart Manage 开发规范",
  description: "前端、后端、移动端及开发人员与 Agent 的统一规范入口",
  srcDir: docsRoot,
  srcExclude: excludedDocuments(docsRoot),
  outDir: "./dist",
  cacheDir: "./.vitepress/cache",
  base,
  appearance: false,
  useWebFonts: false,
  cleanUrls: false,
  ignoreDeadLinks: false,
  vite: {
    resolve: {
      // 正文位于工具目录之外，显式从独立文档依赖解析 Vue，避免依赖业务工程或仓库根安装。
      alias: [
        {
          find: "vue/server-renderer",
          replacement: require.resolve("vue/server-renderer"),
        },
        {
          find: "vue",
          replacement: require.resolve("vue/dist/vue.runtime.esm-bundler.js"),
        },
      ],
    },
  },
  rewrites: pageRewrites,
  markdown: {
    anchor: { slugify: headingSlug },
    config(markdown) {
      repositoryReferencesPlugin(markdown, docsRoot);
    },
  },
  transformPageData(pageData) {
    if (!pageData.filePath) return;
    const status = pageStatus(pageData.filePath);
    pageData.frontmatter.docStatus = status;
    pageData.title = `${status.label} · ${pageData.title}`;
  },
  themeConfig: {
    siteTitle: "Smart Manage · 开发规范",
    nav: [
      { text: "前端规范", link: "/design/" },
      { text: "后端规范", link: "/backend/" },
      { text: "移动端规范", link: "/mobile/" },
      { text: "协作与质量", link: "/development/collaboration" },
      { text: "设计建议", link: "/proposals/frontend-design-spec/" },
      { text: "开发参考", link: "/development/frontend-page-guide" },
    ],
    sidebar: [
      {
        text: "前端规范",
        items: [
          { text: "阅读导航", link: "/" },
          { text: "总则与适用范围", link: "/design/" },
          { text: "组件适配与升级边界", link: "/design/adaptation-boundaries" },
          { text: "设计基础", link: "/design/foundations" },
          { text: "桌面设计", link: "/design/desktop" },
          { text: "图表规范", link: "/design/charts" },
          { text: "开发与验收入口", link: "/design/implementation" },
          { text: "页面示例", link: "/design/examples" },
          { text: "图表示例", link: "/design/chart-examples" },
        ],
      },
      {
        text: "后端规范",
        collapsed: true,
        items: [
          { text: "后端规范总览", link: "/backend/" },
          { text: "分层与接口事务", link: "/architecture/backend" },
          { text: "数据库与迁移", link: "/development/database" },
          { text: "认证与安全", link: "/architecture/security" },
          { text: "功能与权限", link: "/architecture/feature-and-permission" },
          { text: "数据权限", link: "/architecture/data-permission" },
          { text: "隐私与脱敏", link: "/architecture/data-masking" },
          { text: "配置与环境", link: "/development/configuration" },
          { text: "部署与多实例", link: "/architecture/deployment" },
        ],
      },
      {
        text: "移动端规范",
        collapsed: true,
        items: [
          { text: "移动端规范总览", link: "/mobile/" },
          { text: "风险确认", link: "/mobile/#风险确认" },
          { text: "平台适配", link: "/mobile/#平台适配" },
          { text: "页面与验证", link: "/mobile/#页面与验证" },
        ],
      },
      {
        text: "协作与质量",
        collapsed: true,
        items: [
          { text: "人员与 Agent 协作", link: "/development/collaboration" },
          {
            text: "模块开发指南",
            link: "/development/module-development-guide",
          },
          { text: "模块样板", link: "/development/module-pattern-catalog" },
          {
            text: "业务聚合检查",
            link: "/development/business-aggregate-checklist",
          },
          { text: "质量验证", link: "/development/verification" },
        ],
      },
      {
        text: "设计建议",
        collapsed: true,
        items: [
          { text: "待议设计建议", link: "/proposals/frontend-design-spec/" },
        ],
      },
      {
        text: "开发参考",
        collapsed: true,
        items: [
          { text: "前端架构", link: "/architecture/frontend" },
          { text: "页面接入指南", link: "/development/frontend-page-guide" },
          { text: "模块样板目录", link: "/development/module-pattern-catalog" },
          { text: "质量验证", link: "/development/verification" },
          { text: "浏览入口维护", link: "/development/design-docs-site" },
          { text: "项目文档导航", link: "/repository" },
        ],
      },
    ],
    outline: { level: [2, 3], label: "本页目录" },
    docFooter: { prev: "上一节", next: "下一节" },
    darkModeSwitchLabel: "外观",
    sidebarMenuLabel: "章节目录",
    returnToTopLabel: "返回顶部",
    skipToContentLabel: "跳转到正文",
    search: {
      provider: "local",
      options: {
        _render: renderSearchDocument,
        miniSearch: {
          options: { tokenize: tokenizeSearchText },
          searchOptions: { prefix: true, fuzzy: false, combineWith: "AND" },
        },
        translations: {
          button: { buttonText: "搜索规范", buttonAriaLabel: "搜索开发规范" },
          modal: {
            displayDetails: "显示详细结果",
            resetButtonTitle: "清空搜索",
            backButtonTitle: "关闭搜索",
            noResultsText: "没有找到相关内容",
            footer: {
              selectText: "选择",
              navigateText: "切换",
              closeText: "关闭",
            },
          },
        },
      },
    },
  },
});
