import { useState } from 'react';
import { App, Button, Tag } from 'antd';
import type { DataNode } from 'antd/es/tree';
import type { ColumnsType } from 'antd/es/table';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/ListPage';
import ListTree from '@/domain/common/page/ListTree';
import ListTreePanel from '@/domain/common/page/ListTreePanel';
import { useListPageQuery } from '@/domain/common/page/useListPageQuery';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { useEnabledMutation } from '@/domain/common/page/useEnabledMutation';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { componentKeys } from '@/domain/common/registry/componentKeys';
import { useWorkbenchStore } from '@/stores/workbench';
import { numberRuleApi } from './api';
import { numberRuleAccess } from './permissions';
import { numberRuleQueryKeys } from './queryKeys';
import type { NumberReference, NumberRuleVO } from './types';
import type { ListColumnFeatures } from '@/domain/common/page/listQuery';

const columnFeatures: ListColumnFeatures = {
  ruleKey: { label: '规则键', filter: { type: 'string' }, sorter: true },
  name: { label: '名称', filter: { type: 'string' }, sorter: true },
  featureName: { label: '功能', filter: { type: 'string' } },
  pattern: { label: '编号格式', filter: { type: 'string' } },
  scopeType: {
    label: '作用域',
    filter: {
      type: 'enum',
      options: [
        { label: '全局', value: 'GLOBAL' },
        { label: '组织', value: 'ORG' },
        { label: '分类', value: 'CATEGORY' },
      ],
    },
  },
  usageCount: { label: '引用数', filter: { type: 'number' }, sorter: true },
  defaultRule: { label: '默认规则', filter: { type: 'boolean' } },
  enabled: { label: '状态', filter: { type: 'boolean' } },
};

type Scope =
  | { type: 'all' }
  | { type: 'domain'; id: string }
  | { type: 'app'; id: string }
  | { type: 'feature'; id: string };

const buildTree = (references: NumberReference[]): DataNode[] => {
  const domains = new Map<string, DataNode & { children: DataNode[] }>();
  const apps = new Map<string, DataNode & { children: DataNode[] }>();
  const features = new Set<string>();
  for (const reference of references) {
    let domain = domains.get(reference.domainId);
    if (!domain) {
      domain = { key: `domain:${reference.domainId}`, title: reference.domainName, children: [] };
      domains.set(reference.domainId, domain);
    }
    let application = apps.get(reference.appId);
    if (!application) {
      application = { key: `app:${reference.appId}`, title: reference.appName, children: [] };
      apps.set(reference.appId, application);
      domain.children.push(application);
    }
    if (!features.has(reference.featureId)) {
      application.children.push({
        key: `feature:${reference.featureId}`,
        title: reference.featureName,
        isLeaf: true,
      });
      features.add(reference.featureId);
    }
  }
  return [{ key: 'all', title: '全部功能', children: [...domains.values()] }];
};

const NumberRuleListPage = (props: PageComponentProps) => {
  const { modal, message } = App.useApp();
  const [scope, setScope] = useState<Scope>({ type: 'all' });
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const openAddNewTab = useWorkbenchStore((state) => state.openAddNewTab);
  const queryClient = useQueryClient();
  const referencesQuery = useQuery({
    queryKey: numberRuleQueryKeys.references(),
    queryFn: numberRuleApi.references,
  });
  const scopeParams = {
    domainId: scope.type === 'domain' ? scope.id : undefined,
    appId: scope.type === 'app' ? scope.id : undefined,
    featureId: scope.type === 'feature' ? scope.id : undefined,
  };
  const {
    records,
    total,
    pageNum,
    pageSize,
    keyword,
    query,
    onSearch,
    onPageChange,
    onRefresh,
    resetPage,
    columnQueryProps,
  } = useListPageQuery({
    queryKey: numberRuleQueryKeys.list(scopeParams),
    queryFn: (params) => numberRuleApi.listPage({ ...params, ...scopeParams }),
  });
  const deleteMutation = useCommandMutation({
    mutationFn: ({ id, version }: { id: string; version: number }) =>
      numberRuleApi.delete(id, version),
    onSuccess: async () => {
      setSelectedRowKeys([]);
      await queryClient.invalidateQueries({ queryKey: numberRuleQueryKeys.all });
      message.success('删除成功');
    },
  });
  const enabledMutation = useEnabledMutation(numberRuleApi.setEnabled, async () => {
    setSelectedRowKeys([]);
    await queryClient.invalidateQueries({ queryKey: numberRuleQueryKeys.all });
  });
  const setDefaultMutation = useCommandMutation({
    mutationFn: numberRuleApi.setDefault,
    onSuccess: async () => {
      setSelectedRowKeys([]);
      await queryClient.invalidateQueries({ queryKey: numberRuleQueryKeys.all });
      message.success('默认规则已切换');
    },
  });
  const columns: ColumnsType<NumberRuleVO> = [
    {
      title: '规则键',
      dataIndex: 'ruleKey',
      width: 260,
      render: (value, record) => (
        <Button
          type="link"
          size="small"
          onClick={() =>
            openBillTab(
              props.appNumber,
              componentKeys.numberRuleEdit,
              record.id,
              OperationType.EDIT,
            )
          }
        >
          {value}
        </Button>
      ),
    },
    { title: '名称', dataIndex: 'name', width: 150 },
    { title: '功能', dataIndex: 'featureName', width: 150 },
    { title: '编号格式', dataIndex: 'pattern' },
    { title: '作用域', dataIndex: 'scopeType', width: 90 },
    { title: '引用数', dataIndex: 'usageCount', width: 80 },
    {
      title: '默认规则',
      dataIndex: 'defaultRule',
      width: 90,
      render: (value) => (value ? <Tag color="blue">默认</Tag> : '-'),
    },
    {
      title: '状态',
      dataIndex: 'enabled',
      width: 80,
      render: (enabled) => (enabled ? <Tag color="green">启用</Tag> : <Tag>停用</Tag>),
    },
  ];
  const selectedRecords = records.filter((record) => selectedRowKeys.includes(record.id));
  return (
    <ListPage<NumberRuleVO>
      {...props}
      title="编号规则"
      access={numberRuleAccess}
      loading={query.isLoading || referencesQuery.isLoading}
      error={(query.error ?? referencesQuery.error) as Error | null}
      onRetry={() => Promise.all([query.refetch(), referencesQuery.refetch()])}
      total={total}
      pageNum={pageNum}
      pageSize={pageSize}
      quickSearchPlaceholder="搜索规则键/名称"
      filterSummary={keyword ? `关键字：${keyword}` : undefined}
      treePanel={
        <ListTreePanel>
          <ListTree
            virtual={false}
            blockNode
            treeData={buildTree(referencesQuery.data ?? [])}
            defaultExpandedKeys={['all']}
            selectedKeys={[scope.type === 'all' ? 'all' : `${scope.type}:${scope.id}`]}
            onSelect={(keys) => {
              const key = String(keys[0] ?? 'all');
              resetPage();
              if (key.startsWith('domain:')) setScope({ type: 'domain', id: key.slice(6) });
              else if (key.startsWith('app:')) setScope({ type: 'app', id: key.slice(4) });
              else if (key.startsWith('feature:')) setScope({ type: 'feature', id: key.slice(8) });
              else setScope({ type: 'all' });
            }}
          />
        </ListTreePanel>
      }
      onAddNew={() => openAddNewTab(props.appNumber, componentKeys.numberRuleEdit)}
      onEnable={() => enabledMutation.mutate({ ids: selectedRowKeys.map(String), enabled: true })}
      onDisable={() =>
        modal.confirm({
          title: '确认停用所选编号规则？',
          content: '正在使用的规则会被后端拒绝停用，请先切换对应业务引用。',
          onOk: () =>
            enabledMutation.mutateAsync({ ids: selectedRowKeys.map(String), enabled: false }),
        })
      }
      enabledCommandLoading={enabledMutation.isPending}
      toolbarActions={[
        {
          key: 'setDefault',
          label: '设为默认',
          permission: numberRuleAccess.permissions.save,
          disabled:
            selectedRecords.length !== 1 ||
            !selectedRecords[0]?.enabled ||
            selectedRecords[0]?.defaultRule,
          loading: setDefaultMutation.isPending,
          onClick: () => {
            const record = selectedRecords[0];
            if (!record) return;
            modal.confirm({
              title: '确认切换默认编号规则？',
              content: `之后新生成的编号将使用：${record.name}`,
              onOk: () => setDefaultMutation.mutateAsync(record.id),
            });
          },
        },
        {
          key: 'delete',
          label: '删除',
          permission: numberRuleAccess.permissions.delete,
          danger: true,
          disabled: selectedRowKeys.length !== 1 || selectedRecords[0]?.systemPreset,
          loading: deleteMutation.isPending,
          onClick: () => {
            const record = selectedRecords[0];
            if (!record) return;
            modal.confirm({
              title: '确认删除编号规则？',
              content: record.ruleKey,
              okButtonProps: { danger: true },
              onOk: () => deleteMutation.mutateAsync({ id: record.id, version: record.version }),
            });
          },
        },
      ]}
      onRefresh={onRefresh}
      onQuickSearch={onSearch}
      onPageChange={onPageChange}
      rowKey="id"
      columns={columns}
      columnFeatures={columnFeatures}
      {...columnQueryProps}
      dataSource={records}
      selectMode="checkbox"
      selectedRowKeys={selectedRowKeys}
      onSelectChange={setSelectedRowKeys}
    />
  );
};

export default NumberRuleListPage;
