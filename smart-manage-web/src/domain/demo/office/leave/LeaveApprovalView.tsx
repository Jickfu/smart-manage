import { useMemo, useState } from 'react';
import { Button } from 'antd';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import type { DomainViewProps } from '@/domain/common/registry/domainExtensions';
import EditPage from '@/domain/common/page/edit/EditPage';
import { EditFormFields } from '@/domain/common/page/edit/EditFormFields';
import { BusinessAttachmentPanel } from '@/domain/common/attachment/BusinessAttachmentPanel';
import { OperationType, BillStatus } from '@/domain/common/page/types';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { useBeforeCloseGuard } from '@/domain/common/page/tab/useBeforeCloseGuard';
import AppModal from '@/domain/common/component/AppModal';
import { ApprovalPanel, DesignerFrame, workflowApi } from '@/domain/workflow/contract/approval';
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
  const [chart, setChart] = useState(false);
  const [dirty, setDirty] = useState(false);
  const changeDirty = (value: boolean) => {
    setDirty(value);
    onDirtyChange?.(value);
  };
  const [withdrawRequestId] = useState(generateUUID);
  const confirm = useOperationConfirm();
  const client = useQueryClient();
  const run = useQuery({
    queryKey: ['workflow', 'instance', resourceId],
    queryFn: () => workflowApi.detail(resourceId!),
    enabled: active && Boolean(resourceId),
    meta: { errorPresentation: 'local-initial' },
  });
  const businessId = context?.businessId ?? run.data?.businessId;
  const snapshot = useQuery({
    queryKey: ['demo', 'leave', 'snapshot', businessId, resourceId],
    queryFn: () => leaveApi.approval(businessId!, resourceId!),
    enabled: active && Boolean(businessId && resourceId),
    meta: { errorPresentation: 'local-initial' },
  });
  const initialValues = useMemo(() => (snapshot.data ? { ...snapshot.data } : {}), [snapshot.data]);
  useBeforeCloseGuard(context?.appNumber, context?.tabKey, dirty);
  const withdraw = useCommandMutation({
    mutationFn: () => workflowApi.withdraw(resourceId!, withdrawRequestId),
    successMessage: '本轮流程已撤回',
    onSuccess: async () => {
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
        billStatus={BillStatus.SUBMITTED}
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
              detail={run.data}
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
        {chart && <DesignerFrame instanceId={resourceId} readOnly />}
      </AppModal>
    </>
  );
}
