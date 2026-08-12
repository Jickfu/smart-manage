import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type {
  PermissionListForm,
  PermissionListVO,
  PermissionListAllVO,
  PermissionDetailVO,
  PermissionSaveForm,
  PermissionSelectVO,
  PermissionSelectForm,
} from './types';

export const permissionApi = {
  listPage: (form: PermissionListForm) =>
    request
      .post<Result<PageData<PermissionListVO>>>('/sys/base/permission/listPage', form)
      .then((res) => res.data.data),

  /** 鏉冮檺鍏ㄩ噺鏌ヨ锛堜笉鍒嗛〉锛夛紝鐢ㄤ簬瑙掕壊鏉冮檺鍒嗛厤 */
  listAll: () =>
    request
      .post<Result<PermissionListAllVO[]>>('/sys/base/permission/listAll', {})
      .then((res) => res.data.data),

  /** 鍩虹璧勬枡閫夋嫨鍣?*/
  select: (form: PermissionSelectForm) =>
    request
      .post<Result<PageData<PermissionSelectVO>>>('/sys/base/permission/select', form)
      .then((res) => res.data.data),

  detail: (id: string) =>
    request
      .post<Result<PermissionDetailVO>>('/sys/base/permission/detail', { id })
      .then((res) => res.data.data),

  save: (form: PermissionSaveForm) =>
    request.post<Result<string>>('/sys/base/permission/save', form).then((res) => res.data.data),

  delete: (id: string) =>
    request
      .post<Result<string>>('/sys/base/permission/delete', { id })
      .then((res) => res.data.data),
};
