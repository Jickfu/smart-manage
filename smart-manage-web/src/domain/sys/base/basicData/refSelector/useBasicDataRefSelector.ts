import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import type { RefSelectorFieldConfig } from '@/domain/common/page/EditPage';
import { defineRefSelector } from '@/domain/common/page/defineRefSelector';
import { basicDataApi } from '../api';
import { basicDataQueryKeys } from '../queryKeys';
import type { BasicDataCategory, BasicDataOption } from '../types';

type BasicDataRefCategory = Pick<BasicDataCategory, 'id' | 'name'>;

interface BasicDataRefTreeNode {
  key: string;
  title: string;
  children: BasicDataRefTreeNode[];
}

function buildRefTree(options: BasicDataOption[]): BasicDataRefTreeNode[] {
  const nodes = new Map(
    options.map((option) => [
      option.id,
      {
        key: option.id,
        title: option.name,
        children: [] as BasicDataRefTreeNode[],
      },
    ]),
  );
  const roots: BasicDataRefTreeNode[] = [];
  for (const option of options) {
    const node = nodes.get(option.id)!;
    const parentNode = option.parentId ? nodes.get(option.parentId) : undefined;
    if (parentNode) parentNode.children.push(node);
    else roots.push(node);
  }
  return roots;
}

/**
 * 基础资料引用选择器：调用方必须传入分类，只允许选择该分类内的资料。
 * excludedId 用于上级资料场景，后端会同时排除当前资料及其全部后代。
 */
export function useBasicDataRefSelector(
  category?: BasicDataRefCategory,
  excludedId?: string,
): RefSelectorFieldConfig {
  const categoryId = category?.id;
  const optionsQuery = useQuery({
    queryKey: basicDataQueryKeys.parentOptions(categoryId, excludedId),
    queryFn: () => basicDataApi.parentOptions(categoryId!, excludedId),
    enabled: Boolean(categoryId),
  });
  const treeData = useMemo(
    () => [
      {
        key: 'root',
        title: category?.name ?? '基础资料',
        children: buildRefTree(optionsQuery.data ?? []),
      },
    ],
    [category?.name, optionsQuery.data],
  );

  return useMemo(
    () =>
      defineRefSelector<BasicDataOption>({
        selectorKey: ['sys-base-basic-data', categoryId, excludedId ?? 'none'],
        mode: 'tree-table',
        modalTitle: '选择基础资料',
        fetchFn: async (params) => {
          if (!categoryId) return { records: [], total: 0 };
          const options = await basicDataApi.parentOptions(categoryId, excludedId);
          const keyword = params.keyword?.trim().toLowerCase();
          const scopedOptions =
            !keyword && params.parentId && params.parentId !== 'root'
              ? options.filter(
                  (option) => option.id === params.parentId || option.parentId === params.parentId,
                )
              : options;
          const filtered = keyword
            ? scopedOptions.filter((option) =>
                [option.number, option.name, option.namePath].some((value) =>
                  value.toLowerCase().includes(keyword),
                ),
              )
            : scopedOptions;
          const offset = (params.pageNum - 1) * params.pageSize;
          return {
            records: filtered.slice(offset, offset + params.pageSize),
            total: filtered.length,
          };
        },
        displayRender: (record) => record.namePath || record.name,
        fieldNames: { key: 'id', label: 'namePath' },
        treeData,
        columns: [
          { title: '编码', dataIndex: 'number', width: 160 },
          { title: '名称', dataIndex: 'name', width: 180 },
          { title: '长名称', dataIndex: 'namePath' },
        ],
      }),
    [categoryId, excludedId, treeData],
  );
}
