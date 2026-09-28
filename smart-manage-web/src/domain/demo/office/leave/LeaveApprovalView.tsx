import { useMemo, useRef, useState } from 'react';
import { Button, Select } from 'antd';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import type { DomainViewProps } from '@/domain/common/registry/domainExtensions';
import EditPage from '@/domain/common/page/edit/EditPage';
import { EditFormFields } from '@/domain/common/page/edit/EditFormFields';
import { BusinessAttachmentPanel } from '@/domain/common/attachment/BusinessAttachmentPanel';
import { OperationType } from '@/domain/common/page/types';
import { standardBillStatusCatalog } from '@/domain/common/bill/billStatus';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { useBeforeCloseGuard } from '@/domain/common/page/tab/useBeforeCloseGuard';
import AppModal from '@/domain/common/component/AppModal';
import {
  ApprovalPanel,
  DesignerFrame,
  stateLabels,
  workflowApi,
} from '@/domain/workflow/contract/approval';
import { leaveApi } from './api';
import { leaveFields } from './fields';
import { generateUUID } from '@/utils';

export default function LeaveApprovalView({
  resourceId,
  context,
  active,
  onBack,
  onDirtyChange,
}: DomainViewProps) {
  const businessAuthorized = context?.businessAuthorized === 'true';
  const initialMode = context?.initialMode === 'workflow' ? 'workflow' : 'opinions';
  const [selectedInstanceId, setSelectedInstanceId] = useState(resourceId);
  const [chart, setChart] = useState(initialMode === 'workflow');
  const [roundSelector, setRoundSelector] = useState(false);
  const [dirty, setDirty] = useState(false);
  const changeDirty = (value: boolean) => {
    setDirty(value);
    onDirtyChange?.(value);
  };
  const withdrawIntent = useRef({ instanceId: '', requestId: '' });
  const confirm = useOperationConfirm();
  const client = useQueryClient();
  const run = useQuery({
    queryKey: ['workflow', 'instance', selectedInstanceId],
    queryFn: () => workflowApi.detail(selectedInstanceId!),
    enabled: active && Boolean(selectedInstanceId),
    meta: { errorPresentation: 'local-initial' },
  });
  const businessId = context?.businessId ?? run.data?.businessId;
  const rounds = useQuery({
    queryKey: ['demo', 'leave', 'workflow-rounds', businessId],
    queryFn: () => leaveApi.workflowRounds(businessId!),
    enabled: active && businessAuthorized && Boolean(businessId),
    meta: { errorPresentation: 'local-initial' },
  });
  const snapshot = useQuery({
    queryKey: ['demo', 'leave', 'snapshot', businessId, selectedInstanceId, businessAuthorized],
    queryFn: () =>
      businessAuthorized
        ? leaveApi.businessApproval(businessId!, selectedInstanceId!)
        : leaveApi.approval(businessId!, selectedInstanceId!),
    enabled: active && Boolean(businessId && selectedInstanceId),
    meta: { errorPresentation: 'local-initial' },
  });
  const initialValues = useMemo(
    () =>
      snapshot.data
        ? {
            ...snapshot.data,
            billStatusName: standardBillStatusCatalog.require(snapshot.data.billStatus).label,
          }
        : {},
    [snapshot.data],
  );
  useBeforeCloseGuard(context?.appNumber, context?.tabKey, dirty);
  const withdraw = useCommandMutation({
    mutationFn: () => {
      if (withdrawIntent.current.instanceId !== selectedInstanceId) {
        withdrawIntent.current = { instanceId: selectedInstanceId!, requestId: generateUUID() };
      }
      return workflowApi.withdraw(selectedInstanceId!, withdrawIntent.current.requestId);
    },
    successMessage: '本轮流程已撤回',
    onSuccess: async () => {
      withdrawIntent.current = { instanceId: '', requestId: '' };
      changeDirty(false);
      await client.invalidateQueries({ queryKey: ['workflow'] });
      await client.invalidateQueries({ queryKey: ['demo', 'leave'] });
    },
  });
  const back = async () => {
    if (
      dirty &&
      !(await confirm({
        type: 'warning',
        title: '审批意见尚未提交',
        description: '返回会丢失当前填写的审批意见。',
        confirmText: '返回',
      }))
    )
      return;
    changeDirty(false);
    onBack?.();
  };
  return (
    <>
      <EditPage
        title="请假审批"
        operationType={OperationType.VIEW}
        initialValues={initialValues}
        loading={run.isLoading || snapshot.isLoading}
        error={getBlockingQueryError(run) ?? getBlockingQueryError(snapshot)}
        onRetry={() => {
          if (active) {
            void run.refetch();
            if (businessId) void snapshot.refetch();
          }
        }}
        headerActions={[
          {
            key: 'back',
            label: '返回',
            onClick: () => {
              void back();
            },
          },
          { key: 'chart', label: '流程图', disabled: !run.data, onClick: () => setChart(true) },
          ...(businessAuthorized && (rounds.data?.length ?? 0) > 1
            ? [
                {
                  key: 'rounds',
                  label: `审批轮次（${rounds.data?.length ?? 0}）`,
                  onClick: () => setRoundSelector(true),
                },
              ]
            : []),
          ...(run.data?.canWithdraw
            ? [
                {
                  key: 'withdraw',
                  label: '撤回流程',
                  danger: true,
                  loading: withdraw.isPending,
                  onClick: () => {
                    void confirm({
                      type: 'warning',
                      title: '撤回本轮流程',
                      description: '仅尚无人完成审批时允许撤回，撤回后本轮结束。',
                      confirmText: '撤回',
                      onConfirm: () => withdraw.mutateAsync(),
                    });
                  },
                },
              ]
            : []),
        ]}
        sections={[
          {
            key: 'basic',
            label: '本轮提交的单据详情',
            content: () => <EditFormFields fields={leaveFields} editable={false} />,
          },
          {
            key: 'attachments',
            label: '本轮附件',
            content: () => (
              <BusinessAttachmentPanel
                resourceType="demo.office.leave"
                attachments={snapshot.data?.attachments ?? []}
                editable={false}
                onChange={() => undefined}
              />
            ),
          },
        ]}
        sidePanel={
          run.data ? (
            <ApprovalPanel
              key={selectedInstanceId}
              detail={run.data}
              initialTab={initialMode === 'opinions' ? 'history' : 'task'}
              onDirty={changeDirty}
              onCompleted={() => client.invalidateQueries({ queryKey: ['demo', 'leave'] })}
            />
          ) : undefined
        }
        sidePanelLabel="审批功能区"
      />
      <AppModal
        open={chart}
        title="本轮流程图"
        width="90vw"
        bodyMode="natural"
        onCancel={() => setChart(false)}
        footer={<Button onClick={() => setChart(false)}>关闭</Button>}
      >
        {chart && <DesignerFrame instanceId={selectedInstanceId} readOnly />}
      </AppModal>
      <AppModal
        open={roundSelector}
        title="选择审批轮次"
        onCancel={() => setRoundSelector(false)}
        footer={<Button onClick={() => setRoundSelector(false)}>关闭</Button>}
      >
        <Select
          className="sm-leave-workflow-round-selector"
          value={selectedInstanceId}
          options={(rounds.data ?? []).map((round, index, values) => ({
            value: round.instanceId,
            label: `第 ${values.length - index} 轮 · ${stateLabels[round.state]} · ${round.createTime}`,
          }))}
          onChange={(value) => {
            setSelectedInstanceId(value);
            setRoundSelector(false);
          }}
        />
      </AppModal>
    </>
  );
}
