import { readFileSync, readdirSync, statSync } from 'node:fs';
import { join, resolve } from 'node:path';

const webRoot = resolve(import.meta.dirname, '..');
const repoRoot = resolve(webRoot, '..');
const catalogFileArgument = process.argv.find((argument) => argument.startsWith('--catalog-file='));
const menuCatalogFileArgument = process.argv.find((argument) =>
  argument.startsWith('--menu-catalog-file='),
);

function filesUnder(directory, predicate) {
  const result = [];
  for (const name of readdirSync(directory)) {
    const path = join(directory, name);
    if (statSync(path).isDirectory()) result.push(...filesUnder(path, predicate));
    else if (predicate(path)) result.push(path);
  }
  return result;
}

const catalog = new Set();
const menuCatalog = new Set();
if (catalogFileArgument) {
  const catalogFile = catalogFileArgument.substring('--catalog-file='.length);
  for (const permission of readFileSync(catalogFile, 'utf8').split(/\r?\n/)) {
    if (permission.trim()) catalog.add(permission.trim());
  }
} else {
  // 本地快速检查没有数据库时使用迁移字面量；CI 会传入 Flyway 迁移后的最终目录。
  for (const path of filesUnder(join(repoRoot, 'db', 'migration'), (file) => file.endsWith('.sql'))) {
    const text = readFileSync(path, 'utf8');
    for (const match of text.matchAll(
      /['"]([a-z][a-z0-9-]*(?::[A-Za-z][A-Za-z0-9-]*){2,})['"]/g,
    )) {
      catalog.add(match[1]);
    }
  }
}

if (menuCatalogFileArgument) {
  const menuCatalogFile = menuCatalogFileArgument.substring('--menu-catalog-file='.length);
  for (const permission of readFileSync(menuCatalogFile, 'utf8').split(/\r?\n/)) {
    if (permission.trim()) menuCatalog.add(permission.trim());
  }
}

const used = new Set();
for (const path of filesUnder(join(webRoot, 'src'), (file) => file.endsWith('permissions.ts'))) {
  const text = readFileSync(path, 'utf8');
  const resource = text.match(/defineAccessResource\('([^']+)'\s*,\s*\{([\s\S]*?)\}\)/);
  if (!resource) continue;
  const [, prefix, entries] = resource;
  for (const match of entries.matchAll(/:\s*'([^']+)'/g)) used.add(`${prefix}:${match[1]}`);
}

const javaRoot = join(repoRoot, 'smart-manage-api', 'src', 'main', 'java');
for (const path of filesUnder(javaRoot, (file) => file.endsWith('.java'))) {
  const text = readFileSync(path, 'utf8');
  const permissionSource = path.endsWith('Permission.java')
    ? text
    : [...text.matchAll(/@SaCheckPermission\(([^)]*)\)/g)].map((match) => match[1]).join('\n');
  for (const match of permissionSource.matchAll(
    /["']([a-z][a-z0-9-]*(?::[A-Za-z][A-Za-z0-9-]*){2,})["']/g,
  )) {
    used.add(match[1]);
  }
  if (path.endsWith('Permission.java')) {
    const stringConstants = new Map();
    for (const match of text.matchAll(/String\s+(\w+)\s*=\s*"([^"]+)"\s*;/g)) {
      stringConstants.set(match[1], match[2]);
    }
    for (const match of text.matchAll(/String\s+\w+\s*=\s*(\w+)\s*\+\s*"(:[^"]+)"\s*;/g)) {
      const prefix = stringConstants.get(match[1]);
      if (prefix) used.add(`${prefix}${match[2]}`);
    }
  }
}

const missing = [...used].filter((permission) => !catalog.has(permission)).sort();
if (missing.length > 0) {
  console.error(`以下代码权限未在 Flyway 权威目录中注册：\n${missing.join('\n')}`);
  process.exit(1);
}
const unused = catalogFileArgument
  ? [...catalog]
      .filter((permission) => !used.has(permission) && !menuCatalog.has(permission))
      .sort()
  : [];
if (unused.length > 0) {
  console.warn(`以下目录权限当前没有被前后端代码引用，请确认是否为预留或待清理项：\n${unused.join('\n')}`);
}
console.log(
  `权限目录校验通过：${used.size} 个代码权限、${menuCatalog.size} 个菜单入口权限均已注册，${unused.length} 个目录权限待复核。`,
);
