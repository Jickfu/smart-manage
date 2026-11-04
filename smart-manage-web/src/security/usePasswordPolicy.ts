import { useQuery } from '@tanstack/react-query';
import request from '@/api/request';
import type { Result } from '@/types/api';
import {
  createPasswordPolicyRule,
  getPasswordPolicyHint,
  type PasswordPolicy,
} from './passwordPolicy';

export const passwordPolicyQueryKey = ['password-policy'] as const;
export function usePasswordPolicy(enabled: boolean) {
  const query = useQuery({
    queryKey: passwordPolicyQueryKey,
    enabled,
    staleTime: 0,
    queryFn: () =>
      request
        .get<
          Result<PasswordPolicy>
        >('/sys/base/login/password/policy', { headers: { 'Cache-Control': 'no-cache' } })
        .then((response) => response.data.data),
  });
  return {
    hint: getPasswordPolicyHint(query.data),
    rule: createPasswordPolicyRule(query.isError ? undefined : query.data),
  };
}
