import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { createRequire } from "node:module";
import { resolve } from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";
import { test } from "node:test";

const repositoryRoot = fileURLToPath(new URL("../", import.meta.url));
const frontendRoot = resolve(repositoryRoot, "smart-manage-web");
const require = createRequire(resolve(frontendRoot, "package.json"));
const typescript = require("typescript");
const { fromMarkdown } = await import(
  pathToFileURL(require.resolve("mdast-util-from-markdown")).href
);
const configPath = resolve(frontendRoot, "tsconfig.json");
const config = typescript.readConfigFile(configPath, typescript.sys.readFile);
assert.equal(config.error, undefined);
const parsedConfig = typescript.parseJsonConfigFileContent(
  config.config,
  typescript.sys,
  frontendRoot,
);
assert.deepEqual(parsedConfig.errors, []);

// 只补充文中明确由调用方提供的上下文，不替换公共组件类型或模拟实现。
const actionContext = `
import type { ComponentProps } from 'react';
import ListPage from '@/domain/common/page/list/ListPage';
import EditPage from '@/domain/common/page/edit/EditPage';
import type { PermissionAction } from '@/domain/common/page/access/access';
declare const listProps: ComponentProps<typeof ListPage>;
declare const editProps: ComponentProps<typeof EditPage>;
declare const registerAction: PermissionAction;
declare const renewAction: PermissionAction;
declare const deleteAction: PermissionAction;
declare const previewAction: PermissionAction;
declare const handleRefresh: () => void;
declare const handleSave: (values: Record<string, unknown>) => Promise<void>;
declare const handleExit: () => void;
`;

function contractExamples(relativePath) {
  const markdown = readFileSync(resolve(repositoryRoot, relativePath), "utf8");
  const examples = new Map();
  function visit(node) {
    if (node.type === "code" && node.meta?.startsWith("contract=")) {
      assert.equal(node.lang, "tsx");
      const contractName = node.meta.slice("contract=".length);
      assert.ok(!examples.has(contractName), `重复示例标记: ${contractName}`);
      examples.set(contractName, node.value);
    }
    node.children?.forEach(visit);
  }
  visit(fromMarkdown(markdown));
  return examples;
}

const examples = new Map([
  ...contractExamples("docs/development/frontend-page-guide.md"),
  ...contractExamples("docs/design/charts.md"),
]);

function exampleDiagnostics(source) {
  // 虚拟文件只存在于编译器内存，保持真实别名解析且不写入业务源码目录。
  const virtualPath = resolve(
    frontendRoot,
    "src/__documentation_contract__.tsx",
  );
  const options = {
    ...parsedConfig.options,
    noEmit: true,
  };
  const host = typescript.createCompilerHost(options);
  const originalGetSourceFile = host.getSourceFile.bind(host);
  host.getSourceFile = (fileName, languageVersion, onError, shouldCreate) =>
    resolve(fileName) === virtualPath
      ? typescript.createSourceFile(fileName, source, languageVersion, true)
      : originalGetSourceFile(fileName, languageVersion, onError, shouldCreate);
  const program = typescript.createProgram(
    [virtualPath, resolve(frontendRoot, "src/vite-env.d.ts")],
    options,
    host,
  );
  return typescript.getPreEmitDiagnostics(program);
}

function formatDiagnostics(diagnostics) {
  return typescript.formatDiagnosticsWithColorAndContext(diagnostics, {
    getCanonicalFileName: (fileName) => fileName,
    getCurrentDirectory: () => frontendRoot,
    getNewLine: () => "\n",
  });
}

test("文档接入片段使用真实列表、编辑、确认反馈及图表类型", () => {
  assert.deepEqual([...examples.keys()].sort(), [
    "operation-confirm",
    "page-actions",
    "sm-chart",
  ]);
  const source = [actionContext, ...examples.values()].join("\n");
  const diagnostics = exampleDiagnostics(source);
  assert.equal(diagnostics.length, 0, formatDiagnostics(diagnostics));
});

test("拒绝虚构的图表属性和误用的编辑字段入口", () => {
  const source = `${actionContext}
import SmChart from '@/domain/common/chart/SmChart';
<SmChart option={{}} ariaLabel="示例" height={240} />;
<EditPage {...editProps} fields={[]} />;
`;
  const diagnostics = exampleDiagnostics(source);
  assert.ok(
    diagnostics.some((diagnostic) =>
      typescript
        .flattenDiagnosticMessageText(diagnostic.messageText, " ")
        .includes("height"),
    ),
    "不存在的 SmChart.height 必须被拒绝",
  );
  assert.ok(
    diagnostics.some((diagnostic) =>
      typescript
        .flattenDiagnosticMessageText(diagnostic.messageText, " ")
        .includes("fields"),
    ),
    "EditPage.fields 必须被拒绝",
  );
});
