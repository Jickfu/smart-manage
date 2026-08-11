import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import type { RefSelectorFieldConfig } from '@/domain/common/page/EditPage';
import { defineRefSelector } from '@/domain/common/page/defineRefSelector';
import { orgApi } from '../api';
import { orgQueryKeys } from '../queryKeys';
import type { OrgListVO, OrgTreeNode, OrgType } from '../types';

export interface OrgRefRecord {
  id: string;
  number: string;
  name: string;
}

const ORG_TYPE_LABELS: Record<OrgType, string> = {
  GROUP: '集团',
  COMPANY: '公司',
  DEPARTMENT: '部门',
};

const toSelectableTreeNode = (
  node: OrgTreeNode,
  excludedId?: string,
): Record<string, unknown> | null => {
  // 当前组织、其全部后代和已封存分支都不能成为新的上级组织。
  if (node.id === excludedId || node.archived) return null;
  return {
    key: node.id,
    title: node.name,
    children: node.children
      .map((child) => toSelectableTreeNode(child, excludedId))
      .filter((child): child is Record<string, unknown> => child !== null),
  };
};

/** 组织引用选择器：左侧行政组织树，右侧展示当前节点及其直接下级。 */
export function useOrgRefSelector(excludedId?: string): RefSelectorFieldConfig {
  const treeQuery = useQuery({
    queryKey: orgQueryKeys.tree(),
    queryFn: () => orgApi.tree(false),
    staleTime: 5 * 60 * 1000,
  });
  const treeData = useMemo(
    () =>
      (treeQuery.data ?? [])
        .map((node) => toSelectableTreeNode(node, excludedId))
        .filter((node): node is Record<string, unknown> => node !== null),
    [excludedId, treeQuery.data],
  );

  return useMemo(
    () =>
      defineRefSelector<OrgListVO>({
        selectorKey: ['sys-base-org-parent', excludedId ?? 'new'],
        mode: 'tree-table',
        modalTitle: '选择上级组织',
        fetchFn: (params) =>
          orgApi.parentListPage({
            pageNum: params.pageNum,
            pageSize: params.pageSize,
            keyword: params.keyword,
            parentId: params.parentId,
            excludedId,
          }),
        displayRender: (record) => record.name,
        fieldNames: { key: 'id', label: 'name' },
        treeData,
        columns: [
          { title: '编码', dataIndex: 'number', width: 160 },
          { title: '名称', dataIndex: 'name', width: 180 },
          { title: '长名称', dataIndex: 'namePath' },
          {
            title: '组织类型',
            dataIndex: 'orgType',
            width: 100,
            render: (value) => ORG_TYPE_LABELS[value as OrgType],
          },
        ],
      }),
    [excludedId, treeData],
  );
}
