// 构建时显式选择领域；默认只交付底座，不推断后端配置。
export function selectedDomains(value = process.env.SMART_MANAGE_DOMAINS ?? 'sys') {
  const domains = value.split(',').map((domain) => domain.trim());
  if (
    !domains.includes('sys') ||
    domains.some((domain) => !/^[a-z][a-z0-9-]*$/.test(domain)) ||
    new Set(domains).size !== domains.length
  ) {
    throw new Error('SMART_MANAGE_DOMAINS 必须包含 sys，且领域名称有效、不重复');
  }
  return domains;
}
