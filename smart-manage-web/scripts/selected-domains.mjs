import { readdirSync } from 'node:fs';

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
  return domains;
}
