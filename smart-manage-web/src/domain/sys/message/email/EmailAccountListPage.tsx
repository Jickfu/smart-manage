import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useState } from 'react';
import { Button, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useQueryClient } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/list/ListPage';
import { useListPageQuery } from '@/domain/common/page/list/useListPageQuery';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import type { ListColumnFeatures } from '@/domain/common/page/list/listQuery';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { emailApi } from './api';
import { accountAccess } from './permissions';
import { emailAccountQueryKeys } from './queryKeys';
import type { EmailAccount } from './types';

const EDIT_KEY = 'sys/message/email-account/edit';
const columnFeatures: ListColumnFeatures = {
  number: { label: '编码', filter: { type: 'string' }, sorter: true },
  name: { label: '名称', filter: { type: 'string' }, sorter: true },
  fromAddress: { label: '发件地址', filter: { type: 'string' } },
  securityMode: {
    label: '安全模式',
    filter: {
      type: 'enum',
      options: [
        { label: '无加密', value: 'NONE' },
        { label: 'STARTTLS', value: 'STARTTLS' },
        { label: 'SSL/TLS', value: 'SSL_TLS' },
      ],
    },
  },
  enabled: { label: '状态', filter: { type: 'boolean' }, sorter: true },
  defaultAccount: { label: '默认账号', filter: { type: 'boolean' } },
};

const EmailAccountListPage = (props: PageComponentProps) => {
  const feedback = useOperationFeedback();
  const confirmOperation = useOperationConfirm();
  const [selectedKeys, setSelectedKeys] = useState<React.Key[]>([]);
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const openAddNewTab = useWorkbenchStore((state) => state.openAddNewTab);
  const queryClient = useQueryClient();
  const list = useListPageQuery({
    queryKey: emailAccountQueryKeys.list({}),
    queryFn: emailApi.accountList,
  });
  const selected =
    selectedKeys.length === 1
      ? list.records.find((record) => record.id === selectedKeys[0])
      : undefined;
  const command = useCommandMutation({
    mutationFn: async (type: 'test' | 'toggle' | 'delete') => {
      if (!selected) throw new Error('请先选择账号');
      if (type === 'test') return emailApi.accountTest({ accountId: selected.id });
      if (type === 'toggle')
        return emailApi.accountEnable({
          id: selected.id,
          version: selected.version,
          enabled: !selected.enabled,
        });
      return emailApi.accountDelete({ id: selected.id, version: selected.version });
    },
    onSuccess: async (result) => {
      setSelectedKeys([]);
      await queryClient.invalidateQueries({ queryKey: emailAccountQueryKeys.all });
      feedback.success(typeof result === 'string' ? result : '操作成功');
    },
  });
  const columns: ColumnsType<EmailAccount> = [
    {
      title: '编码',
      dataIndex: 'number',
      width: 160,
      render: (value: string, record) => (
        <Button
          type="link"
          size="small"
          onClick={() => openBillTab(props.appNumber, EDIT_KEY, record.id, OperationType.EDIT)}
        >
          {value}
        </Button>
      ),
    },
    { title: '名称', dataIndex: 'name', width: 180 },
    { title: '发件地址', dataIndex: 'fromAddress' },
    { title: '安全模式', dataIndex: 'securityMode', width: 110 },
    {
      title: '状态',
      dataIndex: 'enabled',
      width: 90,
      render: (value: boolean) => (
        <Tag color={value ? 'success' : 'default'}>{value ? '启用' : '停用'}</Tag>
      ),
    },
    {
      title: '默认账号',
      dataIndex: 'defaultAccount',
      width: 100,
      render: (value: boolean) => (value ? '是' : '否'),
    },
  ];
  return (
    <ListPage<EmailAccount>
      {...props}
      title="发信账号"
      access={accountAccess}
      loading={list.query.isLoading}
      error={getBlockingQueryError(list.query) as Error | null}
      onRetry={() => list.query.refetch()}
      total={list.total}
      pageNum={list.pageNum}
      pageSize={list.pageSize}
      quickSearchPlaceholder="搜索编码/名称"
      onAddNew={() => openAddNewTab(props.appNumber, EDIT_KEY)}
      toolbarActions={[
        {
          key: 'test',
          label: '测试连接',
          permission: accountAccess.permissions.test,
          disabled: !selected,
          loading: command.isPending,
          onClick: () => command.mutate('test'),
        },
        {
          key: 'toggle',
          label: selected?.enabled ? '停用' : '启用',
          permission: accountAccess.permissions.enable,
          disabled: !selected,
          loading: command.isPending,
          onClick: () => command.mutate('toggle'),
        },
        {
          key: 'delete',
          label: '删除',
          permission: accountAccess.permissions.delete,
          danger: true,
          disabled: !selected,
          loading: command.isPending,
          onClick: () =>
            selected &&
            void confirmOperation({
              type: 'delete',
              title: '确认删除发信账号？',
              description: selected.name,
              confirmText: '删除',
              onConfirm: () => command.mutateAsync('delete'),
            }),
        },
      ]}
      onRefresh={list.onRefresh}
      onQuickSearch={list.onSearch}
      onPageChange={list.onPageChange}
      rowKey="id"
      columns={columns}
      columnFeatures={columnFeatures}
      {...list.columnQueryProps}
      dataSource={list.records}
      selectMode="checkbox"
      selectedRowKeys={selectedKeys}
      onSelectChange={setSelectedKeys}
    />
  );
};
export default EmailAccountListPage;
