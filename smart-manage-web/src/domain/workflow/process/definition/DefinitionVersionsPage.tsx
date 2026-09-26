import { useState } from 'react';
import { Button } from 'antd';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/list/ListPage';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { usePermissionAccess } from '@/domain/common/page/access/usePermissionAccess';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { OperationType, type PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { flowPost, workflowApi, type DefinitionVersion } from '../api';
import { componentKeys } from '../../componentKeys';
import { definitionAccess } from './permissions';

export default function DefinitionVersionsPage(props: PageComponentProps) {
  const bindingId = props.context?.bindingId;
  const [selection, setSelection] = useState<React.Key[]>([]);
  const [keyword, setKeyword] = useState('');
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const client = useQueryClient();
  const confirm = useOperationConfirm();
  const { can } = usePermissionAccess(definitionAccess.prefix);
  const query = useQuery({
    queryKey: ['workflow', 'definition-versions', bindingId],
    queryFn: () => workflowApi.versions(bindingId!),
    enabled: props.active && Boolean(bindingId),
    meta: { errorPresentation: 'local-initial' },
  });
  const versions = [...(query.data?.definitions ?? [])]
    .filter(
      (version) => !keyword || `v${version.version}`.toLowerCase().includes(keyword.toLowerCase()),
    )
    .sort((left, right) => Number(right.version) - Number(left.version));
  const currentPage = Math.min(pageNum, Math.max(1, Math.ceil(versions.length / pageSize)));
  const rows = versions.slice((currentPage - 1) * pageSize, currentPage * pageSize);
  const selected =
    selection.length === 1 ? rows.find((version) => version.id === selection[0]) : undefined;
  const refresh = async () => {
    await client.invalidateQueries({ queryKey: ['workflow', 'definitions'] });
    await client.invalidateQueries({ queryKey: ['workflow', 'definition-versions', bindingId] });
  };
  const command = useCommandMutation({
    mutationFn: ({ path, data }: { path: string; data: unknown }) =>
      flowPost<string>(`definition/${path}`, data),
    successMessage: '操作成功',
    onSuccess: refresh,
  });
  const openDesigner = (version: DefinitionVersion) =>
    useWorkbenchStore
      .getState()
      .openBillTab(
        props.appNumber,
        componentKeys.definitionDesigner,
        version.id,
        version.published || !can(definitionAccess.permissions.save)
          ? OperationType.VIEW
          : OperationType.EDIT,
      );
  const publish = () => {
    if (!selected) return;
    const id = selected.id;
    void confirm({
      type: 'warning',
      title: `发布 V${selected.version}`,
      description:
        '仅发布已经保存的设计。发布后该版本不可修改；后续变更需复制新版本，已运行实例继续使用原版本。',
      confirmText: '发布',
      onConfirm: async () => {
        const design = await workflowApi.design(id);
        await command.mutateAsync({
          path: 'publish',
          data: { definitionId: id, digest: design.digest },
        });
      },
    });
  };
  return (
    <ListPage<DefinitionVersion>
      title={query.data ? `${query.data.name} · 版本` : '流程版本'}
      access={definitionAccess}
      rowKey="id"
      dataSource={rows}
      total={versions.length}
      pageNum={currentPage}
      pageSize={pageSize}
      loading={query.isLoading}
      error={getBlockingQueryError(query)}
      onRetry={() => {
        if (props.active) void query.refetch();
      }}
      onRefresh={() => {
        if (props.active) void refresh();
      }}
      quickSearchPlaceholder="搜索版本号，如 V12"
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
      selectMode="checkbox"
      selectedRowKeys={selection}
      onSelectChange={setSelection}
      toolbarActions={[
        {
          key: 'design',
          label: selected?.published ? '查看设计' : '设计',
          permission: definitionAccess.permissions.design,
          disabled: !selected || command.isPending,
          onClick: () => {
            if (selected) openDesigner(selected);
          },
        },
        {
          key: 'copy',
          label: '复制新版本',
          permission: definitionAccess.permissions.save,
          disabled: !selected || command.isPending,
          onClick: () => {
            if (selected) command.mutate({ path: 'copy', data: { id: selected.id } });
          },
        },
        {
          key: 'publish',
          label: '发布',
          permission: definitionAccess.permissions.publish,
          disabled: !selected || selected.published || command.isPending,
          onClick: publish,
        },
        { builtin: 'refresh' },
      ]}
      columns={[
        {
          title: '版本号',
          dataIndex: 'version',
          width: 140,
          render: (value: string, row) => (
            <Button
              type="link"
              disabled={!can(definitionAccess.permissions.design)}
              onClick={() => openDesigner(row)}
            >
              V{value}
            </Button>
          ),
        },
        { title: '流程名称', dataIndex: 'name' },
        {
          title: '版本状态',
          key: 'status',
          width: 180,
          render: (_value, row) => (row.published ? '已发布（只读）' : '草稿'),
        },
      ]}
    />
  );
}
