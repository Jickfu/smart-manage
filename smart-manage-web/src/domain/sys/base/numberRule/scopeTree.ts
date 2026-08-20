export type NumberRuleScope =
  | { type: 'all' }
  | { type: 'domain'; id: string }
  | { type: 'app'; id: string }
  | { type: 'feature'; id: string };

export const parseNumberRuleScopeKey = (key: React.Key | undefined): NumberRuleScope => {
  const [type, id] = String(key ?? 'all').split(':');
  if (type === 'domain' && id) return { type: 'domain', id };
  if (type === 'app' && id) return { type: 'app', id };
  if (type === 'feature' && id) return { type: 'feature', id };
  return { type: 'all' };
};
