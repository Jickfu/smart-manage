import { useMemo } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { defineRefSelector } from '@/domain/common/page/edit/defineRefSelector';
import { workflowApi, type WorkflowBusinessChoice } from '../api';
import {
  createWorkflowBusinessTree,
  parseWorkflowBusinessScope,
  workflowBusinessQueryKey,
} from './workflowBusinessDirectory';

/** 仅查询发行包实际装配的业务接入点；表单保留引用记录，提交时提取稳定 key。 */
export function useWorkflowBusinessRefSelector() {
  const client = useQueryClient();
  // 与参照表格共享查询结果；打开参照时才加载，失败由参照表格统一展示和重试。
  const directory = useQuery({
    queryKey: workflowBusinessQueryKey,
    queryFn: workflowApi.businessTypes,
    enabled: false,
  });
  const treeData = useMemo(
    () => createWorkflowBusinessTree(directory.data ?? [], '全部审批业务'),
    [directory.data],
  );
  return useMemo(
    () =>
      defineRefSelector<WorkflowBusinessChoice>({
        selectorKey: 'workflow-business',
        mode: 'tree-table',
        treeData,
        defaultTreeKey: 'root',
        modalTitle: '选择审批业务类型',
        fieldNames: { key: 'key', label: 'name' },
        displayRender: (record) => record.name,
        columns: [
          { title: '业务编码', dataIndex: 'key', width: 260 },
          { title: '业务名称', dataIndex: 'name' },
          { title: '所属应用', dataIndex: 'appName', width: 140 },
        ],
        fetchFn: async ({ keyword, pageNum, pageSize, parentId }) => {
          const choices = await client.fetchQuery({
            queryKey: workflowBusinessQueryKey,
            queryFn: workflowApi.businessTypes,
          });
          const search = keyword?.trim().toLowerCase();
          const scope = parseWorkflowBusinessScope(parentId);
          const records = choices.filter(
            (choice) =>
              (!scope.domainId || choice.domainId === scope.domainId) &&
              (!scope.appId || choice.appId === scope.appId) &&
              (!search || `${choice.key} ${choice.name}`.toLowerCase().includes(search)),
          );
          return {
            records: records.slice((pageNum - 1) * pageSize, pageNum * pageSize),
            total: records.length,
          };
        },
      }),
    [client, treeData],
  );
}
