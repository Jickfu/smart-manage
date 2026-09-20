import { readFileSync } from 'node:fs';

// 完整发行清单属于前端装配配置；CI 只引用 all，不维护领域名称。
export function selectedDomains(
  value = process.env.SMART_MANAGE_DOMAINS ?? 'sys',
  manifest = new URL('../domains.json', import.meta.url),
) {
  const domains =
    value === 'all'
      ? JSON.parse(readFileSync(manifest, 'utf8'))
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
