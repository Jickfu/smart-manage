import assert from "node:assert/strict";
import {
  readFileSync,
  mkdtempSync,
  mkdirSync,
  writeFileSync,
  rmSync,
} from "node:fs";
import { tmpdir } from "node:os";
import { join, dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { test } from "node:test";
import { createMarkdownRenderer } from "vitepress";
import {
  excludedDocuments,
  headingSlug,
  isIncludedDocument,
  pageStatus,
  repositoryReferencesPlugin,
  renderSearchDocument,
  tokenizeSearchText,
} from "./content.mjs";

const docsRoot = fileURLToPath(new URL("../../../docs/", import.meta.url));
const markdown = await createMarkdownRenderer(docsRoot, {
  anchor: { slugify: headingSlug },
  config(renderer) {
    repositoryReferencesPlugin(renderer, docsRoot);
  },
});

test("提案默认排除，只收录统一设计建议；正式示例与移动规范职责独立", () => {
  assert.equal(pageStatus("mobile/README.md").label, "移动端规范");
  assert.equal(pageStatus("design/chart-examples.md").label, "通用示例");
  assert.equal(
    pageStatus("proposals/frontend-design-spec/README.md").kind,
    "review",
  );
  assert.equal(pageStatus("architecture/frontend.md").kind, "reference");
  assert.equal(isIncludedDocument("proposals/private-notes.md"), false);
  assert.equal(isIncludedDocument("../AGENTS.md"), false);
  const fixtureRoot = mkdtempSync(join(tmpdir(), "design-docs-"));
  try {
    mkdirSync(join(fixtureRoot, "design"));
    mkdirSync(join(fixtureRoot, "proposals/frontend-design-spec"), {
      recursive: true,
    });
    for (const documentPath of [
      "design/README.md",
      "proposals/frontend-design-spec/README.md",
      "proposals/private-notes.md",
    ]) {
      writeFileSync(join(fixtureRoot, documentPath), "# fixture");
    }
    assert.deepEqual(excludedDocuments(fixtureRoot), [
      "proposals/private-notes.md",
    ]);
  } finally {
    // 只删除本测试在系统临时目录直接创建的唯一目录。
    if (resolve(dirname(fixtureRoot)) !== resolve(tmpdir()))
      throw new Error("测试目录超出临时目录");
    rmSync(fixtureRoot, { recursive: true, force: true });
  }
});

test("中文搜索能找到句内词与规则编号，无需外部检索服务", () => {
  const tokens = tokenizeSearchText("关键金额和数量直接完整显示。F-18 必须。");
  assert.ok(tokens.includes("金额"));
  assert.ok(tokens.includes("数量"));
  assert.ok(tokens.includes("F-18"));
  assert.deepEqual(tokenizeSearchText("F-18"), ["F-18"]);
});

test("真实 Markdown 渲染保留浏览站内链接，仓库源码和排除资料不生成死链接", async () => {
  const html = markdown.render(
    "[基础](./foundations.md) · [源码](../../smart-manage-web/src/styles/theme.ts) · [记录](../proposals/private-notes.md) · [官方](https://ant.design/)",
    { relativePath: "design/README.md" },
  );
  assert.match(html, /href="\/design\/foundations\.html"/);
  assert.match(html, /href="https:\/\/ant\.design\/"/);
  assert.ok(!html.includes('href="../../smart-manage-web'));
  assert.ok(!html.includes('href="../proposals/frontend-design-runtime'));
  assert.equal((html.match(/（仓库引用）/g) ?? []).length, 2);
});

test("README 的正文引用直接指向静态产物，保留章节锚点", () => {
  const html = markdown.render(
    "[正式](./design/README.md#总原则) · [建议](./proposals/frontend-design-spec/README.md)",
    { relativePath: "design-entry.md" },
  );
  assert.match(html, /href="\/design\/index\.html#总原则"/);
  assert.match(html, /href="\/proposals\/frontend-design-spec\/index\.html"/);
  assert.ok(!html.includes("README.html"));
});

test("章节锚点与仓库检查器语义一致，中文标点不破坏跳转", async () => {
  const html = markdown.render("## 弹框、表格和视觉\n\n正文");
  assert.match(html, /id="弹框表格和视觉"/);
});

test("业务应用依赖和构建入口不引用文档工具", () => {
  const packagePath = new URL(
    "../../../smart-manage-web/package.json",
    import.meta.url,
  );
  const appPackage = JSON.parse(readFileSync(packagePath, "utf8"));
  assert.equal(appPackage.dependencies.vitepress, undefined);
  assert.equal(appPackage.devDependencies.vitepress, undefined);
  assert.ok(
    !Object.values(appPackage.scripts).join(" ").includes("design-docs"),
  );
});

test("搜索标题区分正式与候选，保留真实锚点且排除研究记录", () => {
  const source = "# 设计基础\n\n## 内容\n\n说明";
  const effective = renderSearchDocument(
    source,
    { relativePath: "design/foundations.md" },
    markdown,
  );
  const review = renderSearchDocument(
    source,
    { relativePath: "proposals/frontend-design-spec/README.md" },
    markdown,
  );
  assert.match(effective, /id="设计基础"[^>]*>前端规范 · /);
  assert.match(review, /id="设计基础"[^>]*>设计建议 · /);
  assert.equal(
    renderSearchDocument(
      source,
      { relativePath: "proposals/private-notes.md" },
      markdown,
    ),
    "",
  );
  for (const [documentPath, label] of [
    ["architecture/backend.md", "后端规范"],
    ["backend/README.md", "后端规范"],
    ["mobile/README.md", "移动端规范"],
    ["development/collaboration.md", "协作与质量"],
    ["development/verification.md", "协作与质量"],
  ]) {
    assert.ok(
      renderSearchDocument(
        source,
        { relativePath: documentPath },
        markdown,
      ).includes(`${label} · `),
    );
  }
  assert.equal(
    renderSearchDocument(
      source,
      { relativePath: "platform/base/user.md" },
      markdown,
    ),
    "",
  );
});
