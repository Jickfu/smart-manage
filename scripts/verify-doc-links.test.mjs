import { mkdtempSync, mkdirSync, writeFileSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { test } from "node:test";
import assert from "node:assert/strict";
import { headingAnchors, verifyDocuments } from "./verify-doc-links.mjs";

test("中文标题、重复标题、显式锚点和代码块", async () => {
  assert.deepEqual(
    [
      ...(await headingAnchors(
        '# 中文 标题\n# 中文 标题\n```md\n# 示例\n```\n<a id="explicit"></a>',
      )),
    ],
    ["中文-标题", "中文-标题-1", "explicit"],
  );
});

test("锚点取自真实标题和 HTML，排除注释、行内及缩进代码中的伪锚点", async () => {
  const source = [
    "业务说明",
    "========",
    "",
    "业务说明",
    "--------",
    "",
    "<!--",
    "# 不存在的章节",
    '<a id="comment-anchor"></a>',
    "-->",
    "",
    "    # 缩进代码",
    '    <a id="code-anchor"></a>',
    "",
    '`<a id="inline-anchor"></a>`',
    "",
    "# **配置**与 `Map<String>` [说明](target.md)",
    "",
    '<a name="real-anchor"></a>',
  ].join("\n");
  assert.deepEqual(
    [...(await headingAnchors(source))],
    ["业务说明", "业务说明-1", "配置与-mapstring-说明", "real-anchor"],
  );
});

test("HTML 锚点按真实属性解析，支持无引号、空白及字符实体", async () => {
  const source = [
    '<div data-id="fake-id" data-name="fake-name" name="not-an-anchor"></div>',
    "<a id=bare></a>",
    '<a id = "spaced-double"></a>',
    "<a name = 'spaced-single'></a>",
    "<a ID\t=\tuppercase></a>",
    '<a id="a&amp;b" title="带 > 的属性"></a>',
    '<a title="id=attribute-text" id="real-id"></a>',
    '<!-- <a id="comment-anchor"></a> -->',
    '<script>const text = "<a id=script-anchor></a>";</script>',
    "",
    "`<a id=inline-code></a>`",
    "",
    "```html",
    "<a id=fenced-code></a>",
    "```",
  ].join("\n");
  assert.deepEqual(
    [...(await headingAnchors(source))],
    ["bare", "spaced-double", "spaced-single", "uppercase", "a&b", "real-id"],
  );
});

test("data-id 不能让失效跳转通过，无引号和带空白的真实锚点可以跳转", async () => {
  const root = mkdtempSync(join(tmpdir(), "sm-doc-links-"));
  try {
    writeFileSync(
      join(root, "target.md"),
      [
        '<div data-id="fake"></div>',
        "<a id=bare></a>",
        '<a id = "spaced"></a>',
        "<a name = 'named'></a>",
        '<div name="not-anchor"></div>',
      ].join("\n"),
    );
    writeFileSync(
      join(root, "README.md"),
      [
        "[失效](target.md#fake)",
        "[有效](target.md#bare)",
        "[有效](target.md#spaced)",
        "[有效](target.md#named)",
        "[失效](target.md#not-anchor)",
      ].join("\n"),
    );
    const errors = await verifyDocuments(root, ["README.md"]);
    assert.equal(errors.length, 2);
    assert.match(errors[0], /README.md:1: 章节锚点不存在: target.md#fake/);
    assert.match(
      errors[1],
      /README.md:5: 章节锚点不存在: target.md#not-anchor/,
    );
  } finally {
    rmSync(root, { recursive: true });
  }
});

test("多行下划线式标题支持两种级别、重复锚点及容器内标题", async () => {
  for (const newline of ["\n", "\r\n"]) {
    const source = [
      "第一行",
      "第二行",
      "======",
      "",
      "第一行",
      "第二行",
      "------",
      "",
      "> 引用第一行",
      "> 引用第二行",
      "> ======",
      "",
      "- 列表第一行",
      "  列表第二行",
      "  ------",
      "",
      "**格式**第一行",
      "`Map<String>` 第二行",
      "======",
    ].join(newline);
    assert.deepEqual(
      [...(await headingAnchors(source))],
      [
        "第一行-第二行",
        "第一行-第二行-1",
        "引用第一行-引用第二行",
        "列表第一行-列表第二行",
        "格式第一行-mapstring-第二行",
      ],
    );
  }
});

test("普通段落、分隔线、注释与代码示例不生成多行标题锚点", async () => {
  const source = [
    "普通第一行",
    "普通第二行",
    "",
    "---",
    "",
    "隔开的第一行",
    "",
    "隔开的第二行",
    "======",
    "",
    "<!--",
    "注释第一行",
    "注释第二行",
    "======",
    "-->",
    "",
    "```md",
    "围栏第一行",
    "围栏第二行",
    "======",
    "```",
    "",
    "    代码第一行",
    "    代码第二行",
    "    ======",
    "",
    "`行内第一行",
    "行内第二行",
    "======`",
  ].join("\n");
  assert.deepEqual([...(await headingAnchors(source))], ["隔开的第二行"]);
});

test("多行标题的同文件和跨文件跳转通过，失效锚点仍报告真实行号", async () => {
  const root = mkdtempSync(join(tmpdir(), "sm-doc-links-"));
  try {
    writeFileSync(join(root, "target.md"), "第一行\n第二行\n======\n");
    writeFileSync(
      join(root, "README.md"),
      [
        "本页第一行",
        "本页第二行",
        "------",
        "",
        "[同文件](#本页第一行-本页第二行)",
        "[跨文件](target.md#第一行-第二行)",
        "[失效](target.md#不存在)",
      ].join("\n"),
    );
    const errors = await verifyDocuments(root, ["README.md"]);
    assert.equal(errors.length, 1);
    assert.match(errors[0], /README.md:7: 章节锚点不存在: target.md#不存在/);
  } finally {
    rmSync(root, { recursive: true });
  }
});

test("标题中的图片引用使用可见文字生成锚点", async () => {
  assert.deepEqual(
    [...(await headingAnchors("# ![图标][icon] 说明\n\n[icon]: icon.png"))],
    ["图标-说明"],
  );
});

test("下划线式标题链接通过，注释和代码中的伪标题链接被拒绝", async () => {
  const root = mkdtempSync(join(tmpdir(), "sm-doc-links-"));
  try {
    writeFileSync(
      join(root, "README.md"),
      [
        "业务说明",
        "========",
        "",
        "[跳转](#业务说明)",
        "",
        "<!--",
        "# 不存在的章节",
        "-->",
        "",
        "[错误跳转](#不存在的章节)",
        "",
        "    # 代码中的章节",
        "",
        "[错误跳转](#代码中的章节)",
      ].join("\n"),
    );
    const errors = await verifyDocuments(root, ["README.md"]);
    assert.equal(errors.length, 2);
    assert.match(errors[0], /README.md:10: 章节锚点不存在: #不存在的章节/);
    assert.match(errors[1], /README.md:14: 章节锚点不存在: #代码中的章节/);
  } finally {
    rmSync(root, { recursive: true });
  }
});

test("检查链接与引用，不访问网络或模板；缺失文件和锚点均失败", async () => {
  const root = mkdtempSync(join(tmpdir(), "sm-doc-links-"));
  try {
    mkdirSync(join(root, "文档"));
    writeFileSync(join(root, "文档", "目标 文件.md"), "# 标题\n# 标题\n");
    writeFileSync(
      join(root, "README.md"),
      [
        "[链接](<文档/目标%20文件.md#标题-1>)",
        "[ref]: <文档/目标%20文件.md#标题>",
        "[外链](https://example.invalid/no-page)",
        "[模板](domains/{领域}/module.md)",
        "```md",
        "[示例](missing.md)",
        "```",
      ].join("\n"),
    );
    assert.deepEqual(await verifyDocuments(root, ["README.md"]), []);
    writeFileSync(
      join(root, "README.md"),
      "[坏文件](missing.md)\n[坏锚点](<文档/目标%20文件.md#missing>)",
    );
    const errors = await verifyDocuments(root, ["README.md"]);
    assert.equal(errors.length, 2);
    assert.match(errors[0], /README.md:1: 目标不存在/);
    assert.match(errors[1], /README.md:2: 章节锚点不存在/);
  } finally {
    // 只清理由本测试成功创建的隔离目录。
    rmSync(root, { recursive: true });
  }
});

test("代码跨度和转义示例不检查，真实链接仍检查并保留行号", async () => {
  const root = mkdtempSync(join(tmpdir(), "sm-doc-links-"));
  try {
    const cases = [
      ["`[示例](missing.md)`", []],
      ["``[包含 ` 字符](missing.md)``", []],
      ["`跨行代码\n[示例](missing.md)` [真实链接](missing-real.md)", [2]],
      [String.raw`\[转义示例](missing.md)`, []],
      [String.raw`\\[实际链接](missing-even.md)`, [1]],
      ["`未闭合 [实际链接](missing-unclosed.md)", [1]],
      ["`未闭合\n\n[实际链接](missing-paragraph.md) `后续", [3]],
      [String.fromCharCode(92) + "`[实际链接](missing-tick.md)", [1]],
      ["[`代码标签`](missing-label.md)", [1]],
      ["`[代码](missing.md)` [真实链接](missing-real.md)", [1]],
    ];
    for (const [source, expectedLines] of cases) {
      writeFileSync(join(root, "README.md"), source);
      const errors = await verifyDocuments(root, ["README.md"]);
      assert.equal(errors.length, expectedLines.length, source);
      for (const [index, line] of expectedLines.entries()) {
        assert.ok(
          errors[index].startsWith(`README.md:${line}:`),
          errors[index],
        );
      }
    }
  } finally {
    rmSync(root, { recursive: true });
  }
});

test("标题、列表和引用会中断段落，不把后续链接误当成代码", async () => {
  const root = mkdtempSync(join(tmpdir(), "sm-doc-links-"));
  try {
    const cases = [
      ["`说明\n# 标题\n[失效](missing.md)\n`末尾", 3],
      ["`说明\n- [失效](missing.md)\n`末尾", 2],
      ["`说明\n1. [失效](missing.md)\n`末尾", 2],
      ["`说明\n> [失效](missing.md)\n`末尾", 2],
      ["`说明\n---\n[失效](missing.md)\n`末尾", 3],
    ];
    for (const [source, line] of cases) {
      writeFileSync(join(root, "README.md"), source);
      const errors = await verifyDocuments(root, ["README.md"]);
      assert.equal(errors.length, 1, source);
      assert.ok(errors[0].startsWith(`README.md:${line}:`), errors[0]);
    }
    writeFileSync(
      join(root, "README.md"),
      [
        "- `正常多行代码",
        "  [示例](missing.md)`",
        "> `引用内代码 [示例](missing.md)`",
        "", // 空行结束引用段落，之后的四空格才表示独立缩进代码块。
        "    [缩进代码](missing.md)",
        "",
        "~~~md",
        "[代码围栏](missing.md)",
        "~~~",
      ].join("\n"),
    );
    assert.deepEqual(await verifyDocuments(root, ["README.md"]), []);
  } finally {
    rmSync(root, { recursive: true });
  }
});

test("GFM 表格的代码跨度不跨单元格或行，失效链接和图片必须检出", async () => {
  const root = mkdtempSync(join(tmpdir(), "sm-doc-links-"));
  try {
    const cases = [
      [
        "| 示例 | 文档 |\n| --- | --- |\n| `未闭合 | [失效](missing.md) |\n| 收尾` | 普通 |",
        3,
      ],
      ["| 示例 | 文档 |\n| --- | --- |\n| `开头 | [失效](missing.md) ` |", 3],
      ["| `开头 | [失效](missing.md) ` |\n| --- | --- |", 1],
      [
        "| 示例 | 文档 |\n| --- | --- |\n| `[代码](missing-code.md)` | ![失效](missing.png) |",
        3,
        "missing.png",
      ],
      [
        "| 示例 | 文档 |\n| --- | --- |\n| 普通 | `[代码](missing-code.md)` [失效](missing.md) |",
        3,
      ],
    ];
    for (const [source, line, target = "missing.md"] of cases) {
      writeFileSync(join(root, "README.md"), source);
      const errors = await verifyDocuments(root, ["README.md"]);
      assert.equal(errors.length, 1, source);
      assert.ok(errors[0].endsWith(`: ${target}`), errors[0]);
      assert.ok(
        errors[0].startsWith(`README.md:${line}: 目标不存在:`),
        errors[0],
      );
    }
    writeFileSync(
      join(root, "README.md"),
      [
        "| 示例 | 文档 |",
        "| --- | --- |",
        "| `[代码](missing.md)` | ``[含反引号 ` 的代码](missing.md)`` |",
        "| `[带竖线的代码](missing.md)\\|示例` | 普通 |",
      ].join("\n"),
    );
    assert.deepEqual(await verifyDocuments(root, ["README.md"]), []);
  } finally {
    rmSync(root, { recursive: true });
  }
});

test("GFM 表格链接与跨文件锚点使用同一解析入口", async () => {
  const root = mkdtempSync(join(tmpdir(), "sm-doc-links-"));
  try {
    writeFileSync(
      join(root, "target.md"),
      "# ~~旧~~ 新\n\n多行\n标题\n======\n",
    );
    writeFileSync(
      join(root, "README.md"),
      [
        "# ~~旧~~ 新",
        "",
        "| 名称 | 跳转 |",
        "| --- | --- |",
        "| 本页 | [标题](#旧-新) |",
        "| 跨文件 | [标题](target.md#旧-新) |",
        "| 多行 | [标题](target.md#多行-标题) |",
      ].join("\n"),
    );
    assert.deepEqual(await verifyDocuments(root, ["README.md"]), []);
  } finally {
    rmSync(root, { recursive: true });
  }
});
