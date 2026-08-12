import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { defineRefSelector } from '@/domain/common/page/defineRefSelector';
import { fetchAppsAll } from '@/domain/sys/base/app/api';
import { appQueryKeys } from '@/domain/sys/base/app/queryKeys';
import { featureApi } from '../api';
import type { FeatureVO } from '../types';

/**
 * 功能基础资料选择器：未限定应用时按“领域 → 应用”树筛选右侧功能表格。
 * 菜单等已明确所属应用的场景继续使用普通表格，避免展示无效的跨应用筛选入口。
 */
export function useFeatureRefSelector(appId?: string) {
  const appsQuery = useQuery({
    queryKey: appQueryKeys.cloudAppsAll(),
    queryFn: fetchAppsAll,
    enabled: !appId,
    staleTime: 5 * 60 * 1000,
  });

  const treeData = useMemo(
    () => [
      {
        key: 'root',
        title: '全部功能',
        children:
          appsQuery.data?.map((cloud) => ({
            key: `cloud:${cloud.id}`,
            title: cloud.name,
            children: cloud.appList.map((application) => ({
              key: `app:${application.id}`,
              title: application.name,
              isLeaf: true,
            })),
          })) ?? [],
      },
    ],
    [appsQuery.data],
  );

  return useMemo(
    () =>
      defineRefSelector<FeatureVO>({
        selectorKey: ['sys-feature', appId],
        mode: appId ? 'default' : 'tree-table',
        modalTitle: '选择功能',
        fetchFn: (params) => {
          const [scopeType, scopeId] = params.parentId?.split(':') ?? [];
          return featureApi.listPage({
            pageNum: params.pageNum,
            pageSize: params.pageSize,
            keyword: params.keyword,
            appId: appId ?? (scopeType === 'app' ? scopeId : undefined),
            cloudId: !appId && scopeType === 'cloud' ? scopeId : undefined,
          });
        },
        displayRender: (record) => record.name,
        fieldNames: { key: 'id', label: 'name' },
        treeData: appId ? undefined : treeData,
        columns: [
          { title: '功能标识', dataIndex: 'featureKey', width: 220 },
          { title: '名称', dataIndex: 'name' },
          { title: '应用', dataIndex: 'appName', width: 140 },
        ],
      }),
    [appId, treeData],
  );
}
