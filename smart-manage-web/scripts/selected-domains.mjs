import { readdirSync, existsSync, readFileSync } from 'node:fs';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';

// 默认装配当前源码树中的全部领域；显式值只用于 platform-only 等发行裁剪。
export function selectedDomains(
  value = process.env.SMART_MANAGE_DOMAINS,
  domainRoot = new URL('../src/domain/', import.meta.url),
) {
  const domains =
    value === undefined || value === 'all'
      ? readdirSync(domainRoot, { withFileTypes: true })
          .filter((entry) => entry.isDirectory() && entry.name !== 'common')
          .map((entry) => entry.name)
          .sort((left, right) => {
            if (left === 'sys') return -1;
            if (right === 'sys') return 1;
            return left.localeCompare(right);
          })
      : value.split(',').map((domain) => domain.trim());
  if (
    !Array.isArray(domains) ||
    !domains.includes('sys') ||
    domains.some(
      (domain) =>
        typeof domain !== 'string' ||
        !/^[a-z][a-z0-9-]*$/.test(domain) ||
        domain === 'all' ||
        domain === 'common',
    ) ||
    new Set(domains).size !== domains.length
  ) {
    throw new Error('领域清单必须包含 sys，且领域名称有效、不重复，不得使用 all 或 common');
  }
  const rootPath = domainRoot instanceof URL ? fileURLToPath(domainRoot) : domainRoot;
  for (const domain of domains) {
    const manifest = join(rootPath, domain, 'dependencies.json');
    if (!existsSync(manifest)) continue;
    const dependencies = JSON.parse(readFileSync(manifest, 'utf8'));
    if (
      !Array.isArray(dependencies) ||
      dependencies.some((dependency) => typeof dependency !== 'string')
    ) {
      throw new Error(`领域 ${domain} 的依赖声明必须是字符串数组`);
    }
    for (const dependency of dependencies) {
      if (!domains.includes(dependency))
        throw new Error(`领域 ${domain} 依赖未装配的领域 ${dependency}`);
    }
  }
  return domains;
}
