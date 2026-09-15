import { test } from "node:test";
import assert from "node:assert/strict";
import { createECDH } from "node:crypto";
import { mkdtempSync, readFileSync, rmSync, statSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { spawnSync } from "node:child_process";

test("生成独立且匹配的密钥，保护文件并拒绝覆盖", () => {
  const directory = mkdtempSync(join(tmpdir(), "sm-deployment-keys-"));
  try {
    const output = join(directory, "deployment-keys.env");
    const run = () =>
      spawnSync(
        process.execPath,
        ["scripts/generate-deployment-keys.mjs", "--output", output],
        { encoding: "utf8" },
      );
    const generated = run();
    assert.equal(generated.status, 0);
    const content = readFileSync(output, "utf8");
    const values = Object.fromEntries(
      content
        .split("\n")
        .filter((line) => line.startsWith("SMART_MANAGE_"))
        .map((line) => {
          const separator = line.indexOf("=");
          return [line.slice(0, separator), line.slice(separator + 1)];
        }),
    );
    assert.equal(values.SMART_MANAGE_SM2_PRIVATE_KEY.length, 64);
    assert.equal(values.SMART_MANAGE_SM2_PUBLIC_KEY.length, 130);
    assert.equal(Buffer.from(values.SMART_MANAGE_SM4_KEY, "base64").length, 16);
    const derived = createECDH("SM2");
    derived.setPrivateKey(
      Buffer.from(values.SMART_MANAGE_SM2_PRIVATE_KEY, "hex"),
    );
    // 断言失败也不输出密钥值。
    assert.ok(
      derived.getPublicKey("hex") === values.SMART_MANAGE_SM2_PUBLIC_KEY,
    );
    for (const value of Object.values(values))
      assert.ok(!generated.stdout.includes(value));
    assert.equal(run().status, 1);
    assert.ok(readFileSync(output, "utf8") === content);
    if (process.platform !== "win32")
      assert.equal(statSync(output).mode & 0o777, 0o600);
    else {
      const acl = spawnSync(
        "powershell.exe",
        [
          "-NoProfile",
          "-NonInteractive",
          "-Command",
          "$acl = [System.IO.File]::GetAccessControl($env:SMART_MANAGE_KEY_OUTPUT); if (-not $acl.AreAccessRulesProtected -or @($acl.Access).Count -ne 1) { exit 1 }",
        ],
        {
          env: { ...process.env, SMART_MANAGE_KEY_OUTPUT: output },
          windowsHide: true,
        },
      );
      assert.equal(acl.status, 0);
    }
    const second = join(directory, "second.env");
    assert.equal(
      spawnSync(process.execPath, [
        "scripts/generate-deployment-keys.mjs",
        "--output",
        second,
      ]).status,
      0,
    );
    assert.ok(readFileSync(second, "utf8") !== content);
  } finally {
    // mkdtemp 创建的本次测试专属绝对目录，包含的均为一次性测试密钥。
    rmSync(directory, { recursive: true });
  }
});
