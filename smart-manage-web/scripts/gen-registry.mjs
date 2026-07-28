/**
 * 自动发现 domain 目录下的 pageRegistration.ts(x)，生成模块页面清单导入文件。
 * 页面键、类型和组件完全由清单显式声明，生成器不解析业务源码。
 */
import { writeFileSync } from 'node:fs';
import { readdir } from 'node:fs/promises';
import { dirname, join, relative } from 'node:path';
import { fileURLToPath } from 'node:url';

const scriptDir = dirname(fileURLToPath(import.meta.url));
const rootDir = join(scriptDir, '..');
const sourceDir = join(rootDir, 'src');
const outputFile = join(sourceDir, 'domain', 'common', 'registry', 'registry.gen.ts');

function toPosixPath(filePath) {
  return filePath.replace(/\\/g, '/');
}

const header = `/**
 * 页面注册清单导入文件，由 pnpm gen:registry 自动生成，禁止手动修改。
 */
`;

async function discoverRegistrations(directory) {
  const results = [];
  const entries = await readdir(directory, { withFileTypes: true });
  for (const entry of entries) {
    const fullPath = join(directory, entry.name);
    if (entry.isDirectory()) {
      if (entry.name === 'node_modules' || entry.name === 'common') continue;
      results.push(...(await discoverRegistrations(fullPath)));
    } else if (entry.name === 'pageRegistration.ts' || entry.name === 'pageRegistration.tsx') {
      results.push(fullPath);
    }
  }
  return results;
}

async function main() {
  console.log('[gen:registry] 扫描模块页面注册清单...');
  const files = await discoverRegistrations(join(sourceDir, 'domain'));
  files.sort();

  if (files.length === 0) {
    throw new Error('未发现 pageRegistration.ts(x)，页面注册清单不能为空。');
  }

  const lines = [header];
  const moduleNames = [];
  for (const [index, filePath] of files.entries()) {
    const importPath = toPosixPath(relative(dirname(outputFile), filePath)).replace(/\.tsx?$/, '');
    const normalizedImportPath = importPath.startsWith('.') ? importPath : `./${importPath}`;
    const moduleName = `pageRegistrationModule${index + 1}`;
    moduleNames.push(moduleName);
    lines.push(`// ${toPosixPath(relative(rootDir, filePath))}`);
    lines.push(`import ${moduleName} from '${normalizedImportPath}';`);
    lines.push('');
  }
  lines.push("import { registerPageRegistrationModules } from './componentRegistry';");
  lines.push('');
  lines.push('registerPageRegistrationModules([');
  for (const moduleName of moduleNames) {
    lines.push(`  ${moduleName},`);
  }
  lines.push(']);');
  writeFileSync(outputFile, `${lines.join('\n')}\n`, 'utf-8');

  console.log(
    `[gen:registry] 已生成 ${toPosixPath(relative(rootDir, outputFile))}，共 ${files.length} 个模块清单`,
  );
  for (const filePath of files) {
    console.log(`  - ${toPosixPath(relative(rootDir, filePath))}`);
  }
}

main().catch((error) => {
  console.error('[gen:registry] 错误：', error);
  process.exit(1);
});
