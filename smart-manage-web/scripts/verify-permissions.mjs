import { readFileSync, readdirSync, statSync } from 'node:fs';
import { join, resolve } from 'node:path';

const webRoot = resolve(import.meta.dirname, '..');
const repoRoot = resolve(webRoot, '..');

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
for (const path of filesUnder(join(repoRoot, 'db', 'migration'), (file) => file.endsWith('.sql'))) {
  const text = readFileSync(path, 'utf8');
  for (const match of text.matchAll(/['"]([a-z][a-z0-9-]*(?::[A-Za-z][A-Za-z0-9-]*){2,})['"]/g)) {
    catalog.add(match[1]);
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
}

const missing = [...used].filter((permission) => !catalog.has(permission)).sort();
if (missing.length > 0) {
  console.error(`以下代码权限未在 Flyway 权威目录中注册：\n${missing.join('\n')}`);
  process.exit(1);
}
console.log(`权限目录校验通过：${used.size} 个代码权限均已注册。`);
