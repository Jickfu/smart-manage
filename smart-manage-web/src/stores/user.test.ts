import { afterEach, expect, it } from 'vitest';
import { selectIsAdministrator, useUserStore } from './user';
import type { UserInfoVO } from '@/types/api';

afterEach(() => useUserStore.getState().clearUser());

it('uses only the server identity flag across initialization, profile refresh and logout', () => {
  expect(selectIsAdministrator(useUserStore.getState())).toBe(false);
  const user = { username: 'administrator', administrator: false } as UserInfoVO;
  useUserStore.getState().setSession(user, 'test-csrf');
  expect(selectIsAdministrator(useUserStore.getState())).toBe(false);
  useUserStore.getState().setUserInfo({ ...user, username: 'display-name', administrator: true });
  expect(selectIsAdministrator(useUserStore.getState())).toBe(true);
  useUserStore.getState().clearUser();
  expect(selectIsAdministrator(useUserStore.getState())).toBe(false);
});
