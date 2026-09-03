import { useMemo } from 'react';
import type { RefSelectorFieldConfig } from '@/domain/common/page/edit/EditPage';
import { defineRefSelector } from '@/domain/common/page/edit/defineRefSelector';
import { emailApi } from '../api';
import type { AccountOption } from '../types';

/** 管理员手工发信可选账号引用；后端已限定为启用且允许手工选择的账号。 */
export function useEmailAccountRefSelector(): RefSelectorFieldConfig {
  return useMemo(
    () =>
      defineRefSelector<AccountOption>({
        selectorKey: ['sys-message-email-account', 'manual'],
        modalTitle: '选择发信账号',
        fetchFn: async ({ pageNum, pageSize, keyword }) => {
          const normalizedKeyword = keyword?.trim().toLowerCase();
          const options = (await emailApi.options()).filter(
            (account) =>
              !normalizedKeyword ||
              account.number.toLowerCase().includes(normalizedKeyword) ||
              account.name.toLowerCase().includes(normalizedKeyword) ||
              account.fromAddress.toLowerCase().includes(normalizedKeyword),
          );
          const start = (pageNum - 1) * pageSize;
          return { records: options.slice(start, start + pageSize), total: options.length };
        },
        displayRender: (account) =>
          `${account.name}（${account.number}）${account.defaultAccount ? '【默认】' : ''}`,
        fieldNames: { key: 'id', label: 'name' },
        columns: [
          { title: '账号编码', dataIndex: 'number', width: 160 },
          { title: '账号名称', dataIndex: 'name', width: 180 },
          { title: '发件地址', dataIndex: 'fromAddress' },
          {
            title: '默认账号',
            dataIndex: 'defaultAccount',
            width: 100,
            render: (value) => (value ? '是' : '否'),
          },
        ],
      }),
    [],
  );
}
