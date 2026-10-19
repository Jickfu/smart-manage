import { execFileSync } from "node:child_process";
import { existsSync, readFileSync } from "node:fs";
import { createRequire } from "node:module";
import { dirname, extname, relative, resolve } from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";

// 使用锁定的 CommonMark/GFM 开发依赖，完整识别多行标题与表格单元格边界。
const require = createRequire(
  new URL("../smart-manage-web/package.json", import.meta.url),
);
const [{ fromMarkdown }, { gfm }, { gfmFromMarkdown }, { parseFragment }] =
  await Promise.all(
    [
      "mdast-util-from-markdown",
      "micromark-extension-gfm",
      "mdast-util-gfm",
      "parse5",
    ].map(
      (specifier) => import(pathToFileURL(require.resolve(specifier)).href),
    ),
  );

// 链接、当前文件锚点和跨文件锚点必须共用相同的 GFM 解析规则。
function parseMarkdown(source) {
  return fromMarkdown(source, {
    extensions: [gfm()],
    mdastExtensions: [gfmFromMarkdown()],
  });
}

function* documentNodes(node) {
  yield node;
  for (const child of node.children ?? []) yield* documentNodes(child);
}

function* documentLinks(ast) {
  for (const node of documentNodes(ast)) {
    if (["link", "image", "definition"].includes(node.type)) yield node;
  }
}

function headingText(node) {
  if (["text", "inlineCode"].includes(node.type)) return node.value;
  if (["image", "imageReference"].includes(node.type)) return node.alt ?? "";
  if (node.type === "break") return " ";
  return (node.children ?? []).map(headingText).join("");
}

function anchorsFromAst(ast) {
  const anchors = new Set();
  for (const node of documentNodes(ast)) {
    if (node.type !== "heading") continue;
    const slug = headingText(node)
      .replace(/\r?\n/g, " ")
      .toLowerCase()
      .replace(/[^\p{L}\p{N}_\- ]/gu, "")
      .replace(/ /g, "-");
    let anchor = slug;
    let occurrence = 0;
    while (anchors.has(anchor)) anchor = `${slug}-${++occurrence}`;
    anchors.add(anchor);
  }
  // 合并真实 HTML 节点后解析，保留跨节点的表格等上下文；代码示例不会进入 HTML 树。
  const html = [...documentNodes(ast)]
    .filter((node) => node.type === "html")
    .map((node) => node.value)
    .join("\n");
  function collectHtmlAnchors(node) {
    for (const attribute of node.attrs ?? []) {
      if (attribute.namespace || attribute.prefix || !attribute.value) continue;
      // id 适用于元素；兼容的 name 锚点只属于 a，不能把 data-id 或其他元素的 name 算进去。
      if (
        attribute.name === "id" ||
        (attribute.name === "name" &&
          node.tagName === "a" &&
          node.namespaceURI === "http://www.w3.org/1999/xhtml")
      ) {
        anchors.add(attribute.value);
      }
    }
    for (const child of node.childNodes ?? []) collectHtmlAnchors(child);
  }
  collectHtmlAnchors(parseFragment(html));
  return anchors;
}

export async function headingAnchors(source) {
  return anchorsFromAst(parseMarkdown(source));
}

/** 仅验证仓库文件和 Markdown 锚点；不联网，不把示例占位符当成真实文件。 */
export async function verifyDocuments(root, documents) {
  const errors = [];
  const cache = new Map();
  for (const document of documents) {
    const filename = resolve(root, document);
    const ast = parseMarkdown(readFileSync(filename, "utf8"));
    cache.set(filename, anchorsFromAst(ast));
    for (const link of documentLinks(ast)) {
      const target = link.url;
      const line = link.position.start.line;
      if (/^(?:[a-z][a-z0-9+.-]*:|\/)/i.test(target) || target.includes("{"))
        continue;
      try {
        const split = target.indexOf("#");
        const path = decodeURIComponent(
          split < 0 ? target : target.slice(0, split),
        );
        const anchor =
          split < 0 ? "" : decodeURIComponent(target.slice(split + 1));
        const destination = path ? resolve(dirname(filename), path) : filename;
        let reason;
        if (!existsSync(destination)) reason = "目标不存在";
        else if (anchor && extname(destination).toLowerCase() === ".md") {
          if (!cache.has(destination))
            cache.set(
              destination,
              await headingAnchors(readFileSync(destination, "utf8")),
            );
          if (!cache.get(destination).has(anchor)) reason = "章节锚点不存在";
        }
        if (reason)
          errors.push(
            `${relative(root, filename)}:${line}: ${reason}: ${target}`,
          );
      } catch {
        errors.push(
          `${relative(root, filename)}:${line}: 非法链接编码: ${target}`,
        );
      }
    }
  }
  return errors;
}

if (
  process.argv[1] &&
  pathToFileURL(resolve(process.argv[1])).href === import.meta.url
) {
  const root = resolve(
    process.argv[2] ?? resolve(dirname(fileURLToPath(import.meta.url)), ".."),
  );
  const documents = [
    ...new Set(
      execFileSync(
        "git",
        [
          "ls-files",
          "--cached",
          "--others",
          "--exclude-standard",
          "-z",
          "--",
          "*.md",
        ],
        { cwd: root, encoding: "utf8" },
      )
        .split("\0")
        .filter(Boolean),
    ),
  ].filter((document) => existsSync(resolve(root, document)));
  const errors = await verifyDocuments(root, documents);
  for (const error of errors) console.error(error);
  console.log(
    `Markdown: ${documents.length} files, ${errors.length} local link/anchor errors`,
  );
  process.exitCode = errors.length ? 1 : 0;
}
