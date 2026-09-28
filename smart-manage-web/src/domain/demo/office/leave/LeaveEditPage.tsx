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
import { OperationType } from '@/domain/common/page/types';
import { isStandardBillEditable, standardBillStatusCatalog } from '@/domain/common/bill/billStatus';
import type { PageComponentProps } from '@/domain/common/page/types';
import { createBillTabKey } from '@/domain/common/page/tab/tabKeys';
import { useWorkbenchStore } from '@/stores/workbench';
import { generateUUID } from '@/utils';
import { componentKeys } from '../../componentKeys';
import { leaveApi } from './api';
import { leaveCreateFields, leaveFields } from './fields';
import { leaveAccess } from './permissions';
import LeaveApprovalView from './LeaveApprovalView';

export default function LeaveEditPage(props: PageComponentProps) {
  const adding = props.operationType === OperationType.ADDNEW;
  const [clientKey] = useState(generateUUID);
  const submitIntent = useRef({ content: '', requestId: '' });
  const [today] = useState(() => dayjs().format('YYYY-MM-DD'));
  const [revision, setRevision] = useState(0);
  const [approvalMode, setApprovalMode] = useState<'workflow' | 'opinions'>();
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
    setApprovalMode('opinions');
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
        ? {
            ...detail,
            billStatusName: standardBillStatusCatalog.require(detail.billStatus).label,
          }
        : { clientKey, bizDate: today, leaveType: 'PERSONAL', days: 1 },
    [detail, clientKey, today],
  );
  const persist = async (values: Record<string, unknown>, submit: boolean) => {
    const editableValues = { ...values };
    delete editableValues.billStatusName;
    const command = {
      ...editableValues,
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
  if (approvalMode && detail?.currentInstanceId)
    return (
      <LeaveApprovalView
        key={approvalMode}
        resourceId={detail.currentInstanceId}
        context={{
          businessId: detail.id,
          appNumber: props.appNumber,
          tabKey: props.tabKey,
          businessAuthorized: 'true',
          initialMode: approvalMode,
        }}
        active={props.active}
        onBack={() => {
          setApprovalMode(undefined);
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
        isStandardBillEditable(detail?.billStatus) &&
        (detail?.lastOutcome === 'WITHDRAWN' || detail?.lastOutcome === 'REJECTED')
          ? OperationType.EDIT
          : (props.operationType ?? OperationType.EDIT)
      }
      editable={adding || isStandardBillEditable(detail?.billStatus)}
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
                key: 'workflow',
                label: '查看工作流',
                onClick: () => {
                  void (async () => {
                    if (
                      dirty.current &&
                      !(await confirm({
                        type: 'warning',
                        title: '单据尚未保存',
                        description: '进入工作流详情会丢失当前修改。',
                        confirmText: '继续',
                        cancelText: '留在页面',
                      }))
                    )
                      return;
                    dirty.current = false;
                    setApprovalMode('workflow');
                  })();
                },
              },
              {
                key: 'approval-opinions',
                label: '审批意见',
                onClick: () => void openApproval(),
              },
            ]
          : []),
        { builtin: 'exit' },
      ]}
      sections={[
        {
          key: 'basic',
          label: '基本信息',
          content: (editable) => (
            <EditFormFields fields={adding ? leaveCreateFields : leaveFields} editable={editable} />
          ),
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
