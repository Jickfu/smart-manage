import { existsSync, readFileSync, readdirSync } from "node:fs";
import { join, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { parse } from "parse5";
import { isIncludedDocument, pageRewrites } from "./content.mjs";

const outputRoot = fileURLToPath(new URL("../dist/", import.meta.url));
const base = process.env.SM_DESIGN_DOCS_BASE ?? "/";
const errors = [];
const htmlFiles = readdirSync(outputRoot, {
  recursive: true,
  withFileTypes: true,
})
  .filter((entry) => entry.isFile() && entry.name.endsWith(".html"))
  .map((entry) => join(entry.parentPath, entry.name));
const documents = new Map();

function* nodes(node) {
  yield node;
  for (const child of node.childNodes ?? []) yield* nodes(child);
}

function readDocument(filename) {
  if (!documents.has(filename)) {
    const source = readFileSync(filename, "utf8");
    const tree = parse(source);
    const anchors = new Set(
      [...nodes(tree)].flatMap((node) =>
        (node.attrs ?? [])
          .filter((attribute) => attribute.name === "id")
          .map((attribute) => attribute.value),
      ),
    );
    documents.set(filename, { tree, anchors, source });
  }
  return documents.get(filename);
}

for (const filename of htmlFiles) {
  const document = readDocument(filename);
  const relativeFile = filename.slice(outputRoot.length).replaceAll("\\", "/");
  for (const node of nodes(document.tree)) {
    for (const attribute of node.attrs ?? []) {
      if (!["href", "src"].includes(attribute.name)) continue;
      const target = attribute.value;
      if (/^(?:[a-z][a-z0-9+.-]*:|\/\/)/i.test(target)) continue;
      const targetUrl = new URL(
        target,
        `https://docs.invalid${base}${relativeFile}`,
      );
      const pathname = decodeURIComponent(targetUrl.pathname);
      if (!pathname.startsWith(base)) {
        errors.push(`${relativeFile}: 引用未使用部署前缀: ${target}`);
        continue;
      }
      const relativeTarget = pathname.slice(base.length);
      const targetFile = resolve(
        outputRoot,
        relativeTarget.endsWith("/") || !relativeTarget
          ? `${relativeTarget}index.html`
          : relativeTarget,
      );
      if (!existsSync(targetFile)) {
        errors.push(`${relativeFile}: 静态引用不存在: ${target}`);
      } else if (targetFile.endsWith(".html") && targetUrl.hash) {
        const anchor = decodeURIComponent(targetUrl.hash.slice(1));
        if (!readDocument(targetFile).anchors.has(anchor)) {
          errors.push(`${relativeFile}: 章节锚点不存在: ${target}`);
        }
      }
    }
  }
}

// 所有产物页面必须来自收录的源文档，禁止提案因配置遗漏意外导出。
const docsRoot = fileURLToPath(new URL("../../../docs/", import.meta.url));
const allowedPages = new Set(["404.html"]);
for (const entry of readdirSync(docsRoot, {
  recursive: true,
  withFileTypes: true,
})) {
  if (!entry.isFile()) continue;
  const sourcePath = join(entry.parentPath, entry.name)
    .slice(docsRoot.length)
    .replaceAll("\\", "/");
  if (isIncludedDocument(sourcePath))
    allowedPages.add(
      (pageRewrites[sourcePath] ?? sourcePath).replace(/\.md$/, ".html"),
    );
}
for (const filename of htmlFiles) {
  const outputPath = filename.slice(outputRoot.length).replaceAll("\\", "/");
  if (!allowedPages.has(outputPath))
    errors.push(`未收录的产物页面: ${outputPath}`);
}

for (const error of errors) console.error(error);
console.log(
  `Design docs: ${htmlFiles.length} HTML pages, ${errors.length} static reference/content errors`,
);
process.exitCode = errors.length ? 1 : 0;
