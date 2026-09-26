import { useMemo, useRef, useState } from 'react';
import { Button } from 'antd';
import { createBillTabKey } from '@/domain/common/page/tab/tabKeys';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/list/ListPage';
import ListTree from '@/domain/common/page/list/ListTree';
import ListTreePanel from '@/domain/common/page/list/ListTreePanel';
import ModalEditPage from '@/domain/common/page/edit/ModalEditPage';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { usePermissionAccess } from '@/domain/common/page/access/usePermissionAccess';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useBeforeCloseGuard } from '@/domain/common/page/tab/useBeforeCloseGuard';
import { flowPost, workflowApi } from '../api';
import type { DefinitionRow, WorkflowBusinessChoice } from '../api';
import { OperationType } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { componentKeys } from '../../componentKeys';
import { definitionAccess } from './permissions';
import { useWorkflowBusinessRefSelector } from './useWorkflowBusinessRefSelector';
import {
  createWorkflowBusinessTree,
  parseWorkflowBusinessScope,
  workflowBusinessQueryKey,
} from './workflowBusinessDirectory';

const createInitialValues = { number: '', name: '', businessType: undefined };

export default function DefinitionListPage(props: PageComponentProps) {
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [keyword, setKeyword] = useState('');
  const [selectedTreeKey, setSelectedTreeKey] = useState<React.Key>('root');
  const [creating, setCreating] = useState(false);
  const [selection, setSelection] = useState<React.Key[]>([]);

  const dirty = useRef(false);
  const client = useQueryClient();
  const confirm = useOperationConfirm();
  const { can } = usePermissionAccess(definitionAccess.prefix);
  useBeforeCloseGuard(props.appNumber, props.tabKey, dirty);
  const scope = parseWorkflowBusinessScope(selectedTreeKey);
  const query = useQuery({
    queryKey: ['workflow', 'definitions', pageNum, pageSize, keyword, scope],
    queryFn: () =>
      workflowApi.definitions({
        pageNum,
        pageSize,
        keyword: keyword || undefined,
        ...scope,
      }),
    enabled: props.active,
    meta: { errorPresentation: 'local-initial' },
  });
  const directoryQuery = useQuery({
    queryKey: workflowBusinessQueryKey,
    queryFn: workflowApi.businessTypes,
    enabled: props.active,
  });
  const treeData = useMemo(
    () => createWorkflowBusinessTree(directoryQuery.data ?? [], '全部流程'),
    [directoryQuery.data],
  );
  const businessSelector = useWorkflowBusinessRefSelector();
  const createFields = useMemo<EditField[]>(
    () => [
      {
        dataIndex: 'number',
        label: '编码',
        type: 'text',
        rules: [
          {
            required: true,
            pattern: /^[a-z][a-z0-9_-]{0,39}$/,
            message: '小写字母开头，最多 40 位字母、数字、下划线或连字符',
          },
        ],
      },
      {
        dataIndex: 'name',
        label: '名称',
        type: 'text',
        rules: [{ required: true, whitespace: true, max: 100 }],
      },
      {
        dataIndex: 'businessType',
        label: '业务类型',
        type: 'ref-selector',
        fullWidth: true,
        refSelector: businessSelector,
        rules: [{ required: true }],
      },
    ],
    [businessSelector],
  );
  const selected =
    selection.length === 1 ? query.data?.records.find((row) => row.id === selection[0]) : undefined;
  const versions = [...(selected?.definitions ?? [])].sort(
    (left, right) => Number(right.version) - Number(left.version),
  );
  const version = versions[0];
  const refresh = async () => {
    await client.invalidateQueries({ queryKey: ['workflow', 'definitions'] });
  };
  const openDesigner = (id: string, readOnly: boolean) => {
    useWorkbenchStore
      .getState()
      .openBillTab(
        props.appNumber,
        componentKeys.definitionDesigner,
        id,
        readOnly ? OperationType.VIEW : OperationType.EDIT,
      );
  };
  const create = useCommandMutation({
    mutationFn: (values: Record<string, unknown>) =>
      flowPost<string>('definition/create', {
        ...values,
        businessType: (values.businessType as WorkflowBusinessChoice).key,
      }),
    successMessage: '流程定义已创建',
    onSuccess: async (id) => {
      dirty.current = false;
      setCreating(false);
      openDesigner(id, false);
      await refresh();
    },
  });
  const command = useCommandMutation({
    mutationFn: async ({ path, data }: { path: string; data: unknown }) =>
      flowPost<string>(`definition/${path}`, data),
    successMessage: '操作成功',
    onSuccess: refresh,
  });
  const closeCreate = async () => {
    if (
      dirty.current &&
      !(await confirm({
        type: 'warning',
        title: '存在未保存的流程定义',
        description: '关闭后将丢失当前填写内容。',
        confirmText: '关闭',
      }))
    )
      return;
    dirty.current = false;
    setCreating(false);
  };
  return (
    <>
      <ListPage<DefinitionRow>
        title="流程定义"
        access={definitionAccess}
        rowKey="id"
        dataSource={query.data?.records ?? []}
        total={query.data?.total}
        pageNum={pageNum}
        pageSize={pageSize}
        loading={query.isLoading}
        error={getBlockingQueryError(query)}
        onRetry={() => {
          if (props.active) void Promise.all([query.refetch(), directoryQuery.refetch()]);
        }}
        treePanel={
          <ListTreePanel>
            <ListTree
              virtual={false}
              treeData={treeData}
              showLine={false}
              blockNode
              defaultExpandAll
              selectedKeys={[selectedTreeKey]}
              onSelect={(keys) => {
                setSelectedTreeKey(keys[0] ?? 'root');
                setPageNum(1);
                setSelection([]);
              }}
            />
          </ListTreePanel>
        }
        quickSearchPlaceholder="搜索流程编码/名称"
        filterSummary={keyword ? `关键字：${keyword}` : undefined}
        onQuickSearch={(value) => {
          setKeyword(value.trim());
          setPageNum(1);
          setSelection([]);
        }}
        onPageChange={(page, size) => {
          setPageNum(page);
          setPageSize(size);
          setSelection([]);
        }}
        onRefresh={() => {
          if (props.active) void query.refetch();
        }}
        selectMode="checkbox"
        selectedRowKeys={selection}
        onSelectChange={(keys) => {
          setSelection(keys);
        }}
        toolbarActions={[
          {
            key: 'create',
            label: '新增流程',
            permission: definitionAccess.permissions.save,
            onClick: () => {
              dirty.current = false;
              setCreating(true);
            },
          },
          {
            key: 'design',
            label: version?.published ? '查看设计' : '设计',
            permission: definitionAccess.permissions.design,
            disabled: !version || command.isPending,
            onClick: () => {
              if (version)
                openDesigner(
                  version.id,
                  version.published || !can(definitionAccess.permissions.save),
                );
            },
          },
          {
            key: 'versions',
            label: '版本管理',
            disabled: !selected,
            onClick: () => {
              if (!selected) return;
              useWorkbenchStore.getState().addContentTab(props.appNumber, {
                key: createBillTabKey(componentKeys.definitionVersions, selected.id),
                label: `${selected.name} · 版本列表`,
                componentKey: componentKeys.definitionVersions,
                pageType: 'LIST',
                context: { bindingId: selected.id },
                closable: true,
              });
            },
          },
          {
            key: 'enabled',
            label: selected?.enabled ? '停用' : '启用',
            permission: definitionAccess.permissions.publish,
            disabled: !selected || command.isPending,
            onClick: () => {
              if (selected)
                command.mutate({
                  path: 'enabled',
                  data: { id: selected.id, version: selected.version, enabled: !selected.enabled },
                });
            },
          },
          { builtin: 'refresh' },
        ]}
        columns={[
          {
            title: '流程编码',
            dataIndex: 'number',
            width: 140,
            render: (value: string, row) => {
              const targetVersion =
                selected?.id === row.id
                  ? version
                  : [...row.definitions].sort(
                      (left, right) => Number(right.version) - Number(left.version),
                    )[0];
              return (
                <Button
                  type="link"
                  disabled={!targetVersion || !can(definitionAccess.permissions.design)}
                  onClick={() => {
                    if (targetVersion)
                      openDesigner(
                        targetVersion.id,
                        targetVersion.published || !can(definitionAccess.permissions.save),
                      );
                  }}
                >
                  {value}
                </Button>
              );
            },
          },
          { title: '名称', dataIndex: 'name', width: 160 },
          { title: '绑定业务', dataIndex: 'businessType', width: 200 },
          {
            title: '版本',
            key: 'versions',
            render: (_value, row) => {
              const latest = [...row.definitions].sort(
                (left, right) => Number(right.version) - Number(left.version),
              )[0];
              return latest
                ? `最新 V${latest.version} · ${latest.published ? '已发布' : '草稿'}（共 ${row.definitions.length} 个版本）`
                : '暂无版本';
            },
          },
          {
            title: '状态',
            key: 'enabled',
            width: 100,
            render: (_value, row) => (row.enabled ? '启用' : '停用'),
          },
        ]}
      />
      <ModalEditPage
        open={creating}
        title="新增流程定义"
        access={definitionAccess}
        fields={createFields}
        initialValues={createInitialValues}
        saving={create.isPending}
        onValuesChange={() => {
          dirty.current = true;
        }}
        onClose={() => {
          void closeCreate();
        }}
        onSave={async (values) => {
          await create.mutateAsync(values);
        }}
      />
    </>
  );
}
