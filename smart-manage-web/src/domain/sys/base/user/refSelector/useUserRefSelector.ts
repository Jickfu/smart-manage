import { createElement, useMemo, useState } from 'react';
import { Checkbox } from 'antd';
import type { RefSelectorFieldConfig } from '@/domain/common/page/EditPage';
import { defineRefSelector } from '@/domain/common/page/defineRefSelector';
import { useQuery } from '@tanstack/react-query';
import { orgApi } from '@/domain/sys/base/org/api';
import { orgQueryKeys } from '@/domain/sys/base/org/queryKeys';
import type { OrgTreeNode } from '@/domain/sys/base/org/types';
import { userApi } from '../api';
import type { UserListVO } from '../types';

const toTreeNode = (node: OrgTreeNode): Record<string, unknown> => ({
  key: node.id,
  title: node.name,
  children: node.children.map(toTreeNode),
});

/** 用户引用选择器；提交稳定用户引用，敏感联系方式由具体业务在服务端解析。 */
export function useUserRefSelector(options?: {
  multiple?: boolean;
  title?: string;
}): RefSelectorFieldConfig {
  const [includeDescendants, setIncludeDescendants] = useState(false);
  const treeQuery = useQuery({
    queryKey: orgQueryKeys.tree(false),
    queryFn: () => orgApi.tree(false),
    staleTime: 5 * 60 * 1000,
  });
  const multiple = options?.multiple ?? false;
  const title = options?.title ?? '选择用户';
  const treeData = useMemo(() => (treeQuery.data ?? []).map(toTreeNode), [treeQuery.data]);
  const defaultTreeKey = treeQuery.data?.[0]?.id;
  return useMemo(
    () =>
      defineRefSelector<UserListVO>({
        selectorKey: ['sys-base-user', multiple ? 'multiple' : 'single', includeDescendants],
        mode: multiple ? 'tree-table-multiple' : 'tree-table',
        modalTitle: title,
        fetchFn: (params) =>
          userApi.listPage({
            pageNum: params.pageNum,
            pageSize: params.pageSize,
            keyword: params.keyword,
            orgId: params.parentId ?? defaultTreeKey,
            includeDescendants,
          }),
        displayRender: (record) => record.name,
        fieldNames: { key: 'id', label: 'name' },
        treeData,
        defaultTreeKey,
        treeFooter: createElement(
          Checkbox,
          {
            checked: includeDescendants,
            onChange: (event) => setIncludeDescendants(event.target.checked),
          },
          '包含下级',
        ),
        columns: [
          { title: '工号', dataIndex: 'number', width: 140 },
          { title: '姓名', dataIndex: 'name', width: 160 },
          { title: '用户名', dataIndex: 'username' },
        ],
      }),
    [defaultTreeKey, includeDescendants, multiple, title, treeData],
  );
}
