import { useMemo, useRef, useState } from 'react';
import dayjs from 'dayjs';
import { App, Button } from 'antd';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { usePermissionAccess } from '@/domain/common/page/usePermissionAccess';
import { createBillTabKey } from '@/domain/common/page/tabKeys';
import EditPage from '@/domain/common/page/EditPage';
import { OperationType } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { orgApi } from '@/domain/sys/base/org/api';
import { orgQueryKeys } from '@/domain/sys/base/org/queryKeys';
import { userApi } from './api';
import { userAccess } from './permissions';
import { userQueryKeys } from './queryKeys';
import { UserProfileFields } from './UserProfileFields';
import { UserAssignmentTable } from './UserAssignmentTable';
import { resolveSensitiveContactUpdate } from './sensitiveContact';
import type { UserAssignmentTableRef } from './UserAssignmentTable';
import type { PageComponentProps } from '@/domain/common/page/types';
import type { UserAssignmentVO } from './types';
import type { OrgRefRecord } from '@/domain/sys/base/org/refSelector/useOrgRefSelector';
import './UserEditPage.css';

const UserEditPage = (props: PageComponentProps) => {
  const { message } = App.useApp();
  const assignmentTableRef = useRef<UserAssignmentTableRef>(null);
  const [hasSelectedAssignments, setHasSelectedAssignments] = useState(false);
  const queryClient = useQueryClient();
  const { can } = usePermissionAccess(userAccess.prefix);
  const canReadSensitive = can(userAccess.permissions.readSensitive);
  const { appNumber, tabKey, operationType, billId } = props;
  const isAddNew = operationType === OperationType.ADDNEW;
  const replaceContentTab = useWorkbenchStore((state) => state.replaceContentTab);
  const activateContentTab = useWorkbenchStore((state) => state.activateContentTab);
  const detailQuery = useQuery({
    queryKey: userQueryKeys.detail(billId),
    queryFn: () => userApi.detail(billId!),
    enabled: Boolean(billId),
  });
  const orgQuery = useQuery({
    queryKey: [...orgQueryKeys.all, 'options'],
    queryFn: orgApi.options,
  });
  const detail = detailQuery.data;
  const initialValues = useMemo(
    () =>
      detail
        ? {
            username: detail.username,
            name: detail.name,
            number: detail.number,
            gender: detail.gender,
            birthday: detail.birthday ? dayjs(detail.birthday) : undefined,
            email: detail.email ?? '',
            phone: detail.phone ?? '',
            emailChanged: 'false',
            phoneChanged: 'false',
            avatar: detail.avatar ?? '',
            avatarAttachmentId: detail.avatarAttachmentId,
            avatarUploadSessionId: undefined,
            assignments: (detail.assignments ?? []).map((assignment) => {
              const organization = orgQuery.data?.find((item) => item.id === assignment.orgId);
              return {
                ...assignment,
                org: {
                  id: assignment.orgId,
                  number: organization?.number ?? '',
                  name: organization?.name ?? assignment.orgName ?? assignment.orgNamePath ?? '',
                } satisfies OrgRefRecord,
              };
            }),
          }
        : { assignments: [] },
    [detail, orgQuery.data],
  );
  const saveMutation = useCommandMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const savedId = await userApi.save({
        id: billId ?? undefined,
        version: detail?.version,
        username: String(values.username).trim(),
        password: (values.password as string) || undefined,
        name: String(values.name).trim(),
        number: String(values.number).trim(),
        gender: values.gender as 'MALE' | 'FEMALE' | undefined,
        birthday: values.birthday
          ? dayjs(values.birthday as dayjs.ConfigType).format('YYYY-MM-DD')
          : undefined,
        email: resolveSensitiveContactUpdate(values.email, {
          isAddNew,
          canReadSensitive,
          changed: values.emailChanged === 'true',
        }),
        phone: resolveSensitiveContactUpdate(values.phone, {
          isAddNew,
          canReadSensitive,
          changed: values.phoneChanged === 'true',
        }),
        avatarAttachmentId: values.avatarAttachmentId as string | undefined,
        attachmentUploadSessions:
          values.avatarAttachmentId && values.avatarUploadSessionId
            ? {
                [String(values.avatarAttachmentId)]: String(values.avatarUploadSessionId),
              }
            : {},
        assignments: (
          (values.assignments as (UserAssignmentVO & { org?: OrgRefRecord })[] | undefined) ?? []
        ).map(({ org, ...assignment }) => ({
          ...assignment,
          orgId: org!.id,
        })),
      });
      if (isAddNew) {
        const nextKey = createBillTabKey(props.componentKey, savedId);
        replaceContentTab(appNumber, tabKey, {
          key: nextKey,
          closable: true,
          componentKey: props.componentKey,
          pageType: 'EDIT',
          operationType: OperationType.EDIT,
          billId: savedId,
        });
        activateContentTab(appNumber, nextKey);
      }
      await queryClient.invalidateQueries({ queryKey: userQueryKeys.all });
      message.success(isAddNew ? '新增成功' : '保存成功');
    },
  });
  return (
    <EditPage
      access={userAccess}
      title="用户"
      fields={[]}
      initialValues={initialValues}
      operationType={operationType ?? OperationType.EDIT}
      closeGuard={{ appNumber, tabKey }}
      loading={detailQuery.isLoading || orgQuery.isLoading}
      error={(detailQuery.error ?? orgQuery.error) as Error | null}
      onRetry={() => Promise.all([detailQuery.refetch(), orgQuery.refetch()])}
      onSave={saveMutation.mutateAsync}
      saving={saveMutation.isPending}
      basicContent={(editable) => (
        <UserProfileFields
          editable={editable}
          isAddNew={isAddNew}
          canReadSensitive={canReadSensitive}
        />
      )}
      detailLabel="部门信息"
      detailContent={(editable) => (
        <UserAssignmentTable
          ref={assignmentTableRef}
          editable={editable}
          organizations={orgQuery.data ?? []}
          onSelectionChange={setHasSelectedAssignments}
        />
      )}
      detailExtra={(editable) =>
        editable ? (
          <div className="sm-user-assignment-actions">
            <Button type="link" onClick={() => assignmentTableRef.current?.add()}>
              新增
            </Button>
            <Button
              type="link"
              danger
              disabled={!hasSelectedAssignments}
              onClick={() => assignmentTableRef.current?.removeSelected()}
            >
              删除
            </Button>
          </div>
        ) : undefined
      }
      onExit={() => useWorkbenchStore.getState().removeContentTab(appNumber, tabKey)}
    />
  );
};

export default UserEditPage;
