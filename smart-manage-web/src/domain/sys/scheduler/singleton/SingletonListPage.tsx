import { useEffect, useState } from 'react';
import { Button, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useQueryClient } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/list/ListPage';
import { useListPageQuery } from '@/domain/common/page/list/useListPageQuery';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { OperationType, type PageComponentProps } from '@/domain/common/page/types';
import { componentKeys } from '@/domain/common/registry/componentKeys';
import { useWorkbenchStore } from '@/stores/workbench';
import { singletonApi } from './api';
import { singletonAccess } from './permissions';
import { singletonQueryKeys } from './queryKeys';
import {
  canStart,
  canStop,
  singletonStateLabels,
  type SingletonTask,
  type SingletonCommand,
} from './types';

export default function SingletonListPage(props: PageComponentProps) {
  const [selectedKeys, setSelectedKeys] = useState<React.Key[]>([]);
  const list = useListPageQuery({
    queryKey: singletonQueryKeys.lists(),
    queryFn: singletonApi.listPage,
  });
  const client = useQueryClient();
  const feedback = useOperationFeedback();
  const confirm = useOperationConfirm();
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const selected =
    selectedKeys.length === 1
      ? list.records.find((record) => record.number === selectedKeys[0])
      : undefined;
  const mutation = useCommandMutation({
    mutationFn: (command: SingletonCommand) => singletonApi.control(command),
    onSuccess: async () => {
      setSelectedKeys([]);
      feedback.success('运行期望已更新，请查看实际运行状态');
      await client.invalidateQueries({ queryKey: singletonQueryKeys.all });
    },
  });
  const refetch = list.query.refetch;
  useEffect(() => {
    if (!props.active) return;
    const timer = window.setInterval(() => {
      if (document.visibilityState === 'visible') void refetch();
    }, 5000);
    return () => window.clearInterval(timer);
  }, [props.active, refetch]);
  const control = (requested: boolean) => {
    if (!selected) return;
    const command = { number: selected.number, version: selected.version, requested };
    void confirm({
      type: 'normal',
      title: requested ? '启动常驻任务' : '停止常驻任务',
      description: `${selected.name}（${selected.number}）：${requested ? '由集群中的一个实例执行，故障后自动接管。' : '停止继续派发工作，等待当前任务释放资源；提交成功不代表已经停止。'}`,
      confirmText: requested ? '启动' : '停止',
      onConfirm: () => mutation.mutateAsync(command),
    });
  };
  const columns: ColumnsType<SingletonTask> = [
    {
      title: '任务名称',
      dataIndex: 'name',
      ellipsis: true,
      width: 180,
      fixed: 'left',
      render: (name: string, record) => (
        <Button
          type="link"
          size="small"
          onClick={() =>
            openBillTab(
              props.appNumber,
              componentKeys.schedulerSingletonDetail,
              record.number,
              OperationType.VIEW,
            )
          }
        >
          {name}
        </Button>
      ),
    },
    { title: '任务编码', dataIndex: 'number', ellipsis: true, width: 220 },
    { title: '所属应用', dataIndex: 'appNumber', ellipsis: true, width: 130 },
    {
      title: '期望状态',
      dataIndex: 'requested',
      ellipsis: true,
      width: 100,
      render: (requested: boolean) => (requested ? '运行' : '停止'),
    },
    {
      title: '实际状态',
      dataIndex: 'state',
      ellipsis: true,
      width: 120,
      render: (state: SingletonTask['state']) => (
        <Tag
          color={
            state === 'RUNNING'
              ? 'success'
              : state === 'FAILED' || state === 'UNKNOWN'
                ? 'warning'
                : 'default'
          }
        >
          {singletonStateLabels[state]}
        </Tag>
      ),
    },
    { title: '执行实例', dataIndex: 'ownerInstanceId', ellipsis: true, width: 170 },
    { title: '状态更新时间', dataIndex: 'heartbeatAt', ellipsis: true, width: 180 },
    { title: '下次重试', dataIndex: 'retryAt', ellipsis: true, width: 180 },
    { title: '最近失败', dataIndex: 'lastError', ellipsis: true },
  ];
  return (
    <ListPage<SingletonTask>
      {...props}
      title="常驻任务"
      access={singletonAccess}
      loading={list.query.isLoading}
      error={getBlockingQueryError(list.query) as Error | null}
      onRetry={() => void refetch()}
      total={list.total}
      pageNum={list.pageNum}
      pageSize={list.pageSize}
      rowKey="number"
      columns={columns}
      dataSource={list.records}
      quickSearchPlaceholder="搜索任务编码或名称"
      onQuickSearch={(value) => {
        setSelectedKeys([]);
        list.onSearch(value);
      }}
      onPageChange={(page, size) => {
        setSelectedKeys([]);
        list.onPageChange(page, size);
      }}
      onRefresh={() => {
        setSelectedKeys([]);
        void refetch();
      }}
      selectMode="radio"
      selectedRowKeys={selectedKeys}
      onSelectChange={setSelectedKeys}
      toolbarActions={[
        { builtin: 'refresh' },
        {
          key: 'start',
          label: '启动',
          permission: singletonAccess.permissions.save,
          disabled: !canStart(selected),
          loading: mutation.isPending,
          onClick: () => control(true),
        },
        {
          key: 'stop',
          label: '停止',
          permission: singletonAccess.permissions.save,
          disabled: !canStop(selected),
          loading: mutation.isPending,
          onClick: () => control(false),
        },
      ]}
    />
  );
}
