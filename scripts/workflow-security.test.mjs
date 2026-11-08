import { test } from "node:test";
import assert from "node:assert/strict";
import { readdirSync, readFileSync } from "node:fs";
import { createRequire } from "node:module";

// 复用前端锁定的 YAML 解析器，按结构检查，避免注释或 run 脚本伪造声明。
const require = createRequire(
  new URL("../smart-manage-web/package.json", import.meta.url),
);
const { parsers } = require("prettier/plugins/yaml");

function scalar(node) {
  assert.ok(
    ["plain", "quoteDouble", "quoteSingle"].includes(node?.type),
    "必须使用显式标量",
  );
  return node.value;
}

function entries(node) {
  assert.ok(
    ["mapping", "flowMapping"].includes(node?.type),
    "必须使用显式映射",
  );
  const result = new Map();
  for (const item of node.children) {
    const key = scalar(item.children[0].children[0]);
    assert.notEqual(key, "<<", "安全声明不允许 YAML 合并键");
    assert.ok(!result.has(key), `重复的 YAML 键: ${key}`);
    result.set(key, item.children[1].children[0]);
  }
  return result;
}

function checkPermissions(node) {
  const permissions = entries(node);
  assert.deepEqual(
    [...permissions.keys()],
    ["contents"],
    "质量门禁只允许 contents 权限",
  );
  assert.equal(
    scalar(permissions.get("contents")),
    "read",
    "质量门禁只允许只读令牌",
  );
}

function checkWorkflow(source) {
  const tree = parsers.yaml.parse(source);
  assert.equal(tree.children.length, 1, "工作流必须只有一个 YAML 文档");
  const body = tree.children[0].children.find(
    (node) => node.type === "documentBody",
  );
  const workflow = entries(body.children[0]);
  checkPermissions(workflow.get("permissions"));

  function visit(node) {
    // 不解析隐式继承，防止安全字段通过锚点、别名或合并绕过检查。
    assert.ok(
      !node.anchor && node.type !== "alias",
      "工作流安全检查不允许 YAML 锚点或别名",
    );
    if (["mapping", "flowMapping"].includes(node.type)) {
      for (const [key, value] of entries(node)) {
        if (key === "uses") {
          assert.match(
            scalar(value),
            /^[\w-]+\/[\w.-]+(?:\/[\w./-]+)?@[a-f0-9]{40}$/,
            "外部 Action 必须固定完整提交 SHA",
          );
        }
        if (key === "permissions") checkPermissions(value);
      }
    }
    for (const child of node.children ?? []) visit(child);
  }
  visit(body);
}

const validWorkflow = `
name: Fixture
on: pull_request
permissions: { contents: read }
jobs:
  verify:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@${"a".repeat(40)} # v4.4.0
`;

test("仓库全部 GitHub 工作流固定 Action SHA 并使用显式只读权限", () => {
  const directory = new URL("../.github/workflows/", import.meta.url);
  const files = readdirSync(directory).filter((name) => /\.ya?ml$/.test(name));
  assert.ok(files.length > 0);
  for (const name of files)
    checkWorkflow(readFileSync(new URL(name, directory), "utf8"));
});

test("拒绝可变标签、分支、短 SHA 和动态引用", () => {
  for (const reference of [
    "v4",
    "main",
    "a".repeat(7),
    "${{ inputs.action-ref }}",
  ]) {
    assert.throws(() =>
      checkWorkflow(validWorkflow.replace("a".repeat(40), reference)),
    );
  }
});

test("拒绝缺失权限、工作流写权限及作业扩大权限", () => {
  assert.throws(() =>
    checkWorkflow(validWorkflow.replace("permissions: { contents: read }", "")),
  );
  assert.throws(() =>
    checkWorkflow(validWorkflow.replace("contents: read", "contents: write")),
  );
  assert.throws(() =>
    checkWorkflow(
      validWorkflow.replace(
        "    runs-on:",
        "    permissions: write-all\n    runs-on:",
      ),
    ),
  );
  assert.throws(() =>
    checkWorkflow(
      validWorkflow.replace("contents: read", "contents: read, actions: write"),
    ),
  );
});

test("注释不能伪造权限，脚本内的 uses 文本不作为 Action", () => {
  assert.throws(() =>
    checkWorkflow(validWorkflow.replace("permissions:", "# permissions:")),
  );
  checkWorkflow(
    validWorkflow +
      '      - run: |\n          echo "uses: actions/checkout@v4"\n',
  );
});

test("拒绝重复权限键和 YAML 别名继承", () => {
  assert.throws(() =>
    checkWorkflow(
      validWorkflow.replace(
        "contents: read",
        "contents: read, contents: write",
      ),
    ),
  );
  assert.throws(() =>
    checkWorkflow(
      validWorkflow.replace(
        "permissions: { contents: read }",
        "permissions: &policy { contents: read }",
      ),
    ),
  );
  assert.throws(() =>
    checkWorkflow(
      validWorkflow.replace("contents: read", "<<: { contents: read }"),
    ),
  );
});
