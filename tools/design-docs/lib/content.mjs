import { readdirSync } from "node:fs";
import { dirname, posix, relative } from "node:path";

// 仅明确列入评审目录的提案进入浏览站，新增研究文件不会被自动发布。
export const reviewDocuments = new Set([
  "proposals/frontend-design-spec/README.md",
]);

export const pageRewrites = {
  "design-entry.md": "index.md",
  "design/README.md": "design/index.md",
  "backend/README.md": "backend/index.md",
  "mobile/README.md": "mobile/index.md",
  "README.md": "repository.md",
  "proposals/frontend-design-spec/README.md":
    "proposals/frontend-design-spec/index.md",
};

export const backendDocuments = new Set([
  "backend/README.md",
  "architecture/backend.md",
  "architecture/security.md",
  "architecture/feature-and-permission.md",
  "architecture/data-permission.md",
  "architecture/data-masking.md",
  "architecture/bill-status.md",
  "architecture/deployment.md",
  "architecture/log-lifecycle.md",
  "architecture/monitoring.md",
  "architecture/login-protection.md",
  "development/database.md",
  "development/configuration.md",
]);

export const collaborationDocuments = new Set([
  "development/collaboration.md",
  "development/module-development-guide.md",
  "development/module-pattern-catalog.md",
  "development/business-aggregate-checklist.md",
  "development/verification.md",
]);

const searchReferences = new Set([
  "README.md",
  "design-entry.md",
  "architecture/frontend.md",
  "development/frontend-page-guide.md",
  "development/module-pattern-catalog.md",
  "development/module-development-guide.md",
  "development/verification.md",
  "development/design-docs-site.md",
]);

export function isIncludedDocument(documentPath) {
  const normalizedPath = documentPath.replaceAll("\\", "/");
  return (
    normalizedPath.endsWith(".md") &&
    !normalizedPath.startsWith("../") &&
    !normalizedPath.startsWith("/") &&
    (!normalizedPath.startsWith("proposals/") ||
      reviewDocuments.has(normalizedPath))
  );
}

export function excludedDocuments(docsRoot) {
  return readdirSync(docsRoot, { recursive: true, withFileTypes: true })
    .filter((entry) => entry.isFile() && entry.name.endsWith(".md"))
    .map((entry) =>
      relative(docsRoot, `${entry.parentPath}/${entry.name}`).replaceAll(
        "\\",
        "/",
      ),
    )
    .filter((documentPath) => !isIncludedDocument(documentPath));
}

export function pageStatus(documentPath) {
  const normalizedPath = documentPath.replaceAll("\\", "/");
  if (normalizedPath === "design-entry.md") {
    return {
      kind: "navigation",
      label: "阅读导航",
      description: "按任务阅读前端、后端、移动端及协作要求；候选建议单独标记。",
    };
  }
  if (
    ["design/examples.md", "design/chart-examples.md"].includes(normalizedPath)
  ) {
    return {
      kind: "reference",
      label: "通用示例",
      description:
        "示例说明通用能力的用法；具体要求以对应规范为准，不照搬业务字段或演示参数。",
    };
  }
  if (normalizedPath.startsWith("mobile/")) {
    return {
      kind: "effective",
      label: "移动端规范",
      description:
        "手机浏览器与小程序的共通要求及接入框架；未确定事项以正文说明为准。",
    };
  }
  if (
    normalizedPath.startsWith("backend/") ||
    backendDocuments.has(normalizedPath)
  ) {
    return {
      kind: "effective",
      label: "后端规范",
      description: "后端架构与开发要求。共享安全和部署章节按各自适用范围使用。",
    };
  }
  if (collaborationDocuments.has(normalizedPath)) {
    return {
      kind: "effective",
      label: "协作与质量",
      description: "开发人员与 Agent 的任务流程、模块指南和质量要求。",
    };
  }
  if (normalizedPath.startsWith("design/")) {
    return {
      kind: "effective",
      label: "前端规范",
      description:
        "统一的前端设计与开发要求。存量页面的适配情况以各章节说明为准。",
    };
  }
  if (reviewDocuments.has(normalizedPath)) {
    return {
      kind: "review",
      label: "设计建议",
      description:
        "本页维护未采纳的设计选择，不作为开发硬要求；正式约束按对应规范执行。",
    };
  }
  return {
    kind: "reference",
    label: "开发参考",
    description:
      "按本文所属的架构、开发或领域职责使用；本页不改变原有权威职责。",
  };
}

export function headingSlug(text) {
  return text
    .replace(/\r?\n/g, " ")
    .toLowerCase()
    .replace(/[^\p{L}\p{N}_\- ]/gu, "")
    .replace(/ /g, "-");
}

// 本地中文分词，不将检索内容发送到外部搜索服务。
export function tokenizeSearchText(text) {
  const trimmedText = text.trim();
  if (/^[FDMCA]-\d{2}$/i.test(trimmedText)) return [trimmedText];
  const segmenter = new Intl.Segmenter("zh-CN", { granularity: "word" });
  return [
    ...new Set([
      ...Array.from(segmenter.segment(text))
        .filter((segment) => segment.isWordLike)
        .map((segment) => segment.segment),
      ...(text.match(/\b[FDMCA]-\d{2}\b/g) ?? []),
    ]),
  ];
}

export function renderSearchDocument(source, environment, markdown) {
  const documentPath = environment.relativePath;
  if (
    !isIncludedDocument(documentPath) ||
    (!documentPath.startsWith("design/") &&
      !documentPath.startsWith("mobile/") &&
      !documentPath.startsWith("backend/") &&
      !backendDocuments.has(documentPath) &&
      !collaborationDocuments.has(documentPath) &&
      !reviewDocuments.has(documentPath) &&
      !searchReferences.has(documentPath))
  ) {
    return "";
  }
  const html = markdown.render(source, environment);
  if (environment.frontmatter?.search === false) return "";
  const status = pageStatus(environment.relativePath);
  // 搜索索引从 HTML 标题提取层级；只给索引标题加性质，不改正文或章节 ID。
  return html.replace(
    /(<h1\b[^>]*>)/,
    `$1${markdown.utils.escapeHtml(status.label)} · `,
  );
}

export function isRepositoryReference(href, sourcePath) {
  if (/^(?:[a-z][a-z0-9+.-]*:|\/|#)/i.test(href)) return false;
  const targetPath = href.split(/[?#]/, 1)[0];
  if (!targetPath) return false;
  const resolvedPath = posix.normalize(
    posix.join(
      dirname(sourcePath).replaceAll("\\", "/"),
      decodeURIComponent(targetPath),
    ),
  );
  return (
    resolvedPath.startsWith("../") ||
    (resolvedPath.endsWith(".md") && !isIncludedDocument(resolvedPath)) ||
    /\.(?:tsx?|jsx?|css|java|ps1|ya?ml|json)$/.test(resolvedPath)
  );
}

export function repositoryReferencesPlugin(markdown, docsRoot) {
  markdown.core.ruler.push("repository-references", (state) => {
    const sourcePath = state.env.realPath
      ? relative(docsRoot, state.env.realPath).replaceAll("\\", "/")
      : state.env.relativePath;
    if (!sourcePath) return;
    for (const block of state.tokens) {
      if (!block.children) continue;
      const children = block.children;
      for (let index = 0; index < children.length; index++) {
        const opening = children[index];
        if (opening.type !== "link_open") continue;
        const href = opening.attrGet("href");
        if (!href) continue;
        if (!isRepositoryReference(href, sourcePath)) {
          // 显式处理 README 路由；正文引用保持原样，静态站内不依赖服务器回退。
          if (!/^(?:[a-z][a-z0-9+.-]*:|\/|#)/i.test(href)) {
            const [targetPath] = href.split(/[?#]/, 1);
            if (targetPath.endsWith(".md")) {
              const resolvedPath = posix.normalize(
                posix.join(
                  dirname(sourcePath).replaceAll("\\", "/"),
                  decodeURIComponent(targetPath),
                ),
              );
              const outputPath = pageRewrites[resolvedPath] ?? resolvedPath;
              opening.attrSet(
                "href",
                `/${outputPath.replace(/\.md$/, ".html")}${href.slice(targetPath.length)}`,
              );
            }
          }
          continue;
        }
        const closing = children
          .slice(index + 1)
          .find((token) => token.type === "link_close");
        if (!closing) continue;
        // 源码和未收录文档留在仓库，不导出文件，也不生成浏览器死链接。
        opening.type = "html_inline";
        opening.content = `<span class="repository-reference" title="仓库引用：${markdown.utils.escapeHtml(href)}">`;
        closing.type = "html_inline";
        closing.content =
          '<span class="repository-reference-label">（仓库引用）</span></span>';
      }
    }
  });
}
