import { create } from 'zustand';
import type { UserInfoVO } from '@/types/api';

interface UserState {
  userInfo: UserInfoVO | null;
  csrfToken: string | null;
  setSession: (userInfo: UserInfoVO, csrfToken: string) => void;
  setUserInfo: (info: UserInfoVO) => void;
  setThemeColor: (themeColor: string) => void;
  clearUser: () => void;
}

/** 只消费服务端身份声明；未初始化会话默认无特殊管理员能力。 */
export const selectIsAdministrator = (state: Pick<UserState, 'userInfo'>): boolean =>
  state.userInfo?.administrator === true;

export const useUserStore = create<UserState>((set) => ({
  userInfo: null,
  csrfToken: null,
  setSession: (userInfo, csrfToken) => set({ userInfo, csrfToken }),
  setUserInfo: (userInfo: UserInfoVO) => set({ userInfo }),
  setThemeColor: (themeColor: string) =>
    set((state) => ({
      userInfo: state.userInfo ? { ...state.userInfo, themeColor } : null,
    })),
  clearUser: () => {
    set({ userInfo: null, csrfToken: null });
  },
}));
