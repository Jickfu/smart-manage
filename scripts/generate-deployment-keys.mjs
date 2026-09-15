import { createECDH, randomBytes } from "node:crypto";
import { openSync, closeSync, writeFileSync, unlinkSync } from "node:fs";
import { resolve, dirname, relative, isAbsolute } from "node:path";
import { fileURLToPath } from "node:url";
import { spawnSync } from "node:child_process";

// 只生成新环境配置，不连接数据库、不读取现有密钥，也不执行轮换。
function generate(outputPath) {
  const repository = resolve(dirname(fileURLToPath(import.meta.url)), "..");
  const location = relative(repository, outputPath);
  if (
    !location.startsWith(".." + (process.platform === "win32" ? "\\" : "/")) &&
    !isAbsolute(location)
  ) {
    throw new Error("密钥文件必须位于仓库外");
  }
  const keyPair = createECDH("SM2");
  keyPair.generateKeys();
  const privateKey = keyPair.getPrivateKey();
  const publicKey = keyPair.getPublicKey(undefined, "uncompressed");
  const storageKey = randomBytes(16);
  let descriptor;
  try {
    // wx 拒绝覆盖现有文件；POSIX 在创建时即限制为仅当前用户可读写。
    descriptor = openSync(outputPath, "wx", 0o600);
    if (process.platform === "win32") {
      // Windows 必须在写入密钥前移除继承 ACL，只授权当前用户。
      const permissionResult = spawnSync(
        "powershell.exe",
        [
          "-NoProfile",
          "-NonInteractive",
          "-Command",
          `
        $ErrorActionPreference = 'Stop'
        $keyAcl = New-Object System.Security.AccessControl.FileSecurity
        $keyAcl.SetAccessRuleProtection($true, $false)
        $keyOwner = [System.Security.Principal.WindowsIdentity]::GetCurrent().User
        $keyAcl.SetOwner($keyOwner)
        $keyRule = New-Object System.Security.AccessControl.FileSystemAccessRule($keyOwner, 'FullControl', 'Allow')
        $keyAcl.AddAccessRule($keyRule)
        [System.IO.File]::SetAccessControl($env:SMART_MANAGE_KEY_OUTPUT, $keyAcl)
      `,
        ],
        {
          env: { ...process.env, SMART_MANAGE_KEY_OUTPUT: outputPath },
          windowsHide: true,
          stdio: "pipe",
        },
      );
      if (permissionResult.status !== 0)
        throw new Error("无法设置密钥文件访问权限");
    }
    writeFileSync(
      descriptor,
      [
        "# 仅用于新环境初始化；已有数据库禁止直接替换 SM4 密钥。",
        `SMART_MANAGE_SM2_PRIVATE_KEY=${privateKey.toString("hex").padStart(64, "0")}`,
        `SMART_MANAGE_SM2_PUBLIC_KEY=${publicKey.toString("hex")}`,
        `SMART_MANAGE_SM4_KEY=${storageKey.toString("base64")}`,
        "",
      ].join("\n"),
      "utf8",
    );
  } catch (error) {
    if (descriptor !== undefined) {
      closeSync(descriptor);
      descriptor = undefined;
      // 只清理本次排他创建的未完成文件，不触碰用户原有文件。
      unlinkSync(outputPath);
    }
    throw error;
  } finally {
    if (descriptor !== undefined) closeSync(descriptor);
    privateKey.fill(0);
    storageKey.fill(0);
  }
}

const args = process.argv.slice(2);
if (args.length !== 2 || args[0] !== "--output" || !args[1]) {
  console.error(
    "用法：node scripts/generate-deployment-keys.mjs --output <仓库外的现有目录/deployment-keys.env>",
  );
  process.exitCode = 1;
} else {
  try {
    const outputPath = resolve(args[1]);
    generate(outputPath);
    console.log(`已生成密钥配置文件：${outputPath}`);
    console.log(
      "文件仅当前用户可访问；部署时显式授权服务账号。密钥内容未输出到终端。",
    );
  } catch (error) {
    console.error(
      error.code === "EEXIST"
        ? "目标文件已存在，拒绝覆盖。"
        : "生成失败，请检查输出目录、运行时 SM2 支持和文件权限。",
    );
    process.exitCode = 1;
  }
}
