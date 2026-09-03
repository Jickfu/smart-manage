import { useMemo } from 'react';
import type { RefSelectorFieldConfig } from '@/domain/common/page/edit/EditPage';
import { defineRefSelector } from '@/domain/common/page/edit/defineRefSelector';
import { roleApi } from '../api';
import type { RoleSelectVO } from '../types';

interface RoleRefSelectorOptions {
  orgId?: string;
  orgName?: string;
  excludedIds: string[];
}

/** 角色引用选择器：按组织上下文隔离缓存，并由服务端排除已分配角色。 */
export function useRoleRefSelector({
  orgId,
  orgName,
  excludedIds,
}: RoleRefSelectorOptions): RefSelectorFieldConfig {
  const normalizedExcludedIds = useMemo(
    () => [...new Set(excludedIds)].sort((left, right) => left.localeCompare(right)),
    [excludedIds],
  );
  const excludedIdsKey = normalizedExcludedIds.join(',');

  return useMemo(
    () =>
      defineRefSelector<RoleSelectVO>({
        selectorKey: ['sys-base-role-assignment', orgId ?? 'none', excludedIdsKey],
        mode: 'multiple',
        modalTitle: `增加角色${orgName ? ` — ${orgName}` : ''}`,
        fetchFn: (params) =>
          roleApi.select({
            pageNum: params.pageNum,
            pageSize: params.pageSize,
            keyword: params.keyword,
            orgId,
            excludedIds: normalizedExcludedIds,
          }),
        displayRender: (record) => `${record.number} ${record.name}`,
        fieldNames: { key: 'id', label: 'name' },
        columns: [
          { title: '角色编码', dataIndex: 'number', width: 160 },
          { title: '角色名称', dataIndex: 'name', width: 180 },
          {
            title: '描述',
            dataIndex: 'description',
            render: (description) => (description as string | undefined) || '—',
          },
        ],
      }),
    [excludedIdsKey, normalizedExcludedIds, orgId, orgName],
  );
}
