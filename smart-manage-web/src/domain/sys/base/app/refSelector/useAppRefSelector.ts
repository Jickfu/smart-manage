import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import type { RefSelectorFieldConfig } from '@/domain/common/page/EditPage';
import { defineRefSelector } from '@/domain/common/page/defineRefSelector';
import { appApi, fetchAppsAll } from '../api';
import { appQueryKeys } from '../queryKeys';
import type { AppListVO } from '../types';

/**
 * 应用基础资料选择器：左侧按领域筛选，右侧分页选择应用。
 * 业务页面只负责必填、禁用和字段联动，不再重复维护查询与展示规则。
 */
export function useAppRefSelector(): RefSelectorFieldConfig {
  const domainTreeQuery = useQuery({
    queryKey: appQueryKeys.domainAppsAll(),
    queryFn: fetchAppsAll,
    staleTime: 5 * 60 * 1000,
  });

  const treeData = useMemo(
    () => [
      {
        key: 'root',
        title: '全部应用',
        children:
          domainTreeQuery.data?.map((domain) => ({
            key: domain.id,
            title: domain.name,
            isLeaf: true,
          })) ?? [],
      },
    ],
    [domainTreeQuery.data],
  );

  return useMemo(
    () =>
      defineRefSelector<AppListVO>({
        selectorKey: 'sys-base-app',
        mode: 'tree-table',
        modalTitle: '选择应用',
        fetchFn: (params) =>
          appApi.listPage({
            pageNum: params.pageNum,
            pageSize: params.pageSize,
            keyword: params.keyword,
            // 根节点表示不限定领域，其余节点的 key 均为领域 ID。
            domainId: params.parentId === 'root' ? undefined : params.parentId,
          }),
        displayRender: (record) => record.name,
        fieldNames: { key: 'id', label: 'name' },
        treeData,
        columns: [
          { title: '编码', dataIndex: 'number', width: 160 },
          { title: '名称', dataIndex: 'name' },
          { title: '所属领域', dataIndex: 'domainName', width: 160 },
        ],
      }),
    [treeData],
  );
}
