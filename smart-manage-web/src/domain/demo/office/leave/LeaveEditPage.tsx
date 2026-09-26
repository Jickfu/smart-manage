import { useMemo, useRef, useState } from 'react';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import dayjs from 'dayjs';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import EditPage from '@/domain/common/page/edit/EditPage';
import { EditFormFields } from '@/domain/common/page/edit/EditFormFields';
import { BusinessAttachmentPanel } from '@/domain/common/attachment/BusinessAttachmentPanel';
import { useEditAttachments } from '@/domain/common/page/edit/useEditAttachments';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { BillStatus, OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { createBillTabKey } from '@/domain/common/page/tab/tabKeys';
import { useWorkbenchStore } from '@/stores/workbench';
import { generateUUID } from '@/utils';
import { componentKeys } from '../../componentKeys';
import { leaveApi } from './api';
import { leaveFields } from './fields';
import { leaveAccess } from './permissions';
import LeaveApprovalView from './LeaveApprovalView';

export default function LeaveEditPage(props: PageComponentProps) {
  const adding = props.operationType === OperationType.ADDNEW;
  const [clientKey] = useState(generateUUID);
  const submitIntent = useRef({ content: '', requestId: '' });
  const [today] = useState(() => dayjs().format('YYYY-MM-DD'));
  const [revision, setRevision] = useState(0);
  const [approval, setApproval] = useState(false);
  const client = useQueryClient();
  const dirty = useRef(false);
  const confirm = useOperationConfirm();
  const openApproval = async () => {
    if (
      dirty.current &&
      !(await confirm({
        type: 'warning',
        title: '单据尚未保存',
        description: '进入审批详情会丢失当前修改。',
        confirmText: '继续',
        cancelText: '留在页面',
      }))
    )
      return;
    dirty.current = false;
    setApproval(true);
  };
  const query = useQuery({
    queryKey: ['demo', 'leave', props.billId],
    queryFn: () => leaveApi.detail(props.billId!),
    enabled: !adding && Boolean(props.billId) && props.active,
    meta: { errorPresentation: 'local-initial' },
  });
  const detail = adding ? undefined : query.data;
  const attachments = useEditAttachments(
    { resourceType: 'demo.office.leave', initialAttachments: detail?.attachments },
    () => {
      dirty.current = true;
      setRevision((value) => value + 1);
    },
  );
  const initialValues = useMemo(
    () =>
      detail
        ? { ...detail }
        : { clientKey, bizDate: today, leaveType: 'PERSONAL', billStatus: 'A', days: 1 },
    [detail, clientKey, today],
  );
  const persist = async (values: Record<string, unknown>, submit: boolean) => {
    const command = {
      ...values,
      id: props.billId,
      version: detail?.version,
      clientKey: detail?.clientKey ?? clientKey,
    };
    // 同一次提交失败重试复用标识；撤回/拒绝后新轮次的版本和内容变化必须生成新标识。
    const content = JSON.stringify(command);
    if (submit && submitIntent.current.content !== content)
      submitIntent.current = { content, requestId: generateUUID() };
    const savedId = submit
      ? await leaveApi.submit({ ...command, requestId: submitIntent.current.requestId })
      : await leaveApi.save(command);
    dirty.current = false;
    if (adding || submit)
      useWorkbenchStore.getState().replaceContentTab(props.appNumber, props.tabKey, {
        key: createBillTabKey(componentKeys.leaveEdit, savedId),
        componentKey: componentKeys.leaveEdit,
        pageType: 'EDIT',
        billId: savedId,
        operationType: submit ? OperationType.VIEW : OperationType.EDIT,
        closable: true,
      });
    await client.invalidateQueries({ queryKey: ['demo', 'leave'] });
    await client.invalidateQueries({ queryKey: ['workflow'] });
  };
  const save = useCommandMutation({
    mutationFn: (values: Record<string, unknown>) => persist(values, false),
    successMessage: '请假申请已保存',
  });
  const submit = useCommandMutation({
    mutationFn: (values: Record<string, unknown>) => persist(values, true),
    successMessage: '请假申请已提交',
  });
  if (approval && detail?.currentInstanceId)
    return (
      <LeaveApprovalView
        resourceId={detail.currentInstanceId}
        context={{ businessId: detail.id, appNumber: props.appNumber, tabKey: props.tabKey }}
        active={props.active}
        onBack={() => {
          setApproval(false);
          void query.refetch();
        }}
      />
    );
  return (
    <EditPage
      onValuesChange={() => {
        dirty.current = true;
      }}
      title="请假申请"
      access={leaveAccess}
      operationType={
        props.operationType === OperationType.VIEW &&
        detail?.billStatus === 'A' &&
        (detail.lastOutcome === 'WITHDRAWN' || detail.lastOutcome === 'REJECTED')
          ? OperationType.EDIT
          : (props.operationType ?? OperationType.EDIT)
      }
      billStatus={(detail?.billStatus ?? 'A') as BillStatus}
      initialValues={initialValues}
      loading={!adding && query.isLoading}
      error={adding ? null : getBlockingQueryError(query)}
      onRetry={() => {
        if (!adding && props.active) void query.refetch();
      }}
      saving={save.isPending || submit.isPending}
      onSave={save.mutateAsync}
      onSubmit={submit.mutateAsync}
      onExit={() => {
        void useWorkbenchStore.getState().removeContentTab(props.appNumber, props.tabKey);
      }}
      closeGuard={{ appNumber: props.appNumber, tabKey: props.tabKey }}
      dirtyRevision={revision}
      transformValues={attachments.withValues}
      headerActions={[
        { builtin: 'save' },
        { builtin: 'submit' },
        ...(detail?.currentInstanceId
          ? [
              {
                key: 'approval',
                label: '查看本轮审批',
                onClick: () => {
                  void openApproval();
                },
              },
            ]
          : []),
        { builtin: 'exit' },
      ]}
      sections={[
        {
          key: 'basic',
          label: '基本信息',
          content: (editable) => <EditFormFields fields={leaveFields} editable={editable} />,
        },
        {
          key: 'attachments',
          label: '附件',
          content: (editable) => (
            <BusinessAttachmentPanel
              resourceType="demo.office.leave"
              attachments={attachments.attachments}
              retainedAttachmentIds={detail?.retainedAttachmentIds}
              editable={editable}
              onChange={attachments.update}
            />
          ),
        },
      ]}
    />
  );
}
