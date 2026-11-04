import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Table } from 'antd';
import EditPage from '@/domain/common/page/edit/EditPage';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import { editFormSection } from '@/domain/common/page/edit/editPageSection';
import { OperationType, type PageComponentProps } from '@/domain/common/page/types';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { useWorkbenchStore } from '@/stores/workbench';
import { singletonApi } from './api';
import { singletonQueryKeys } from './queryKeys';
import { singletonStateLabels } from './types';
const fields: EditField[] = [
  { label: '任务编码', dataIndex: 'number', type: 'text' },
  { label: '任务名称', dataIndex: 'name', type: 'text' },
  { label: '所属应用', dataIndex: 'appNumber', type: 'text' },
  { label: '期望状态', dataIndex: 'requestedLabel', type: 'text' },
  { label: '实际状态', dataIndex: 'stateLabel', type: 'text' },
  { label: '执行实例', dataIndex: 'ownerInstanceId', type: 'text' },
  { label: '运行身份', dataIndex: 'runId', type: 'text', columnSpan: 2 },
  { label: '执行代际', dataIndex: 'generation', type: 'text' },
  { label: '开始时间', dataIndex: 'startedAt', type: 'datetime' },
  { label: '状态更新时间', dataIndex: 'heartbeatAt', type: 'datetime' },
  { label: '下次重试', dataIndex: 'retryAt', type: 'datetime' },
  { label: '最近失败', dataIndex: 'lastError', type: 'text' },
  { label: '当前实例装配', dataIndex: 'availableLabel', type: 'text' },
  { label: '任务说明', dataIndex: 'description', type: 'textarea', fullWidth: true },
];
const eventLabels: Record<string, string> = {
  ACQUIRED: '取得执行权',
  START_REQUESTED: '请求启动',
  STOP_REQUESTED: '请求停止',
  STOPPED: '执行结束',
  FAILED: '执行失败',
  RUNNING: '业务就绪',
  STOPPING: '资源收束',
};
export default function SingletonDetailPage(props: PageComponentProps) {
  const detail = useQuery({
    queryKey: singletonQueryKeys.detail(props.billId),
    queryFn: () => singletonApi.detail(props.billId!),
    enabled: Boolean(props.billId),
    refetchInterval: props.active ? 5000 : false,
    meta: { errorPresentation: 'local-initial' },
  });
  const events = useQuery({
    queryKey: singletonQueryKeys.events(props.billId),
    queryFn: () => singletonApi.events(props.billId!),
    enabled: Boolean(props.billId),
    refetchInterval: props.active ? 5000 : false,
    meta: { errorPresentation: 'local-initial' },
  });
  const values = useMemo(
    () =>
      detail.data
        ? {
            ...detail.data,
            stateLabel: singletonStateLabels[detail.data.state],
            requestedLabel: detail.data.requested ? '运行' : '停止',
            availableLabel: detail.data.available ? '已装配' : '未装配，当前节点不能控制',
          }
        : {},
    [detail.data],
  );
  return (
    <EditPage
      title="常驻任务"
      operationType={OperationType.VIEW}
      initialValues={values}
      loading={detail.isLoading || events.isLoading}
      error={(getBlockingQueryError(detail) || getBlockingQueryError(events)) as Error | null}
      onRetry={() => void Promise.all([detail.refetch(), events.refetch()])}
      onExit={() => useWorkbenchStore.getState().removeContentTab(props.appNumber, props.tabKey)}
      sections={[
        editFormSection('state', '运行状态', fields),
        {
          key: 'events',
          label: '近期事件（最多200条）',
          content: () => (
            <Table
              size="small"
              rowKey="id"
              dataSource={events.data ?? []}
              pagination={{ pageSize: 10 }}
              columns={[
                { title: '时间', dataIndex: 'occurredAt', ellipsis: true, width: 190 },
                {
                  title: '事件',
                  dataIndex: 'eventType',
                  ellipsis: true,
                  width: 140,
                  render: (value: string) => eventLabels[value] ?? value,
                },
                { title: '实例', dataIndex: 'instanceId', ellipsis: true, width: 160 },
                { title: '运行身份', dataIndex: 'runId', ellipsis: true, width: 320 },
                { title: '原因', dataIndex: 'reason', ellipsis: true },
              ]}
            />
          ),
        },
      ]}
    />
  );
}
