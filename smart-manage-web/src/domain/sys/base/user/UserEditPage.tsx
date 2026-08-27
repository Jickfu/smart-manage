import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useMemo, useRef, useState } from 'react';
import dayjs from 'dayjs';
import { Button, DatePicker, Form, Input, Select, Table } from 'antd';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { usePermissionAccess } from '@/domain/common/page/usePermissionAccess';
import { createBillTabKey } from '@/domain/common/page/tabKeys';
import EditPage from '@/domain/common/page/EditPage';
import { OperationType } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { userApi } from './api';
import { userAccess } from './permissions';
import { userQueryKeys } from './queryKeys';
import { UserProfileFields } from './UserProfileFields';
import { FormFieldCell, FormFieldGrid } from '@/domain/common/page/FormFieldLayout';
import { UserAssignmentTable } from './UserAssignmentTable';
import { resolveSensitiveContactUpdate } from './sensitiveContact';
import type { UserAssignmentTableRef } from './UserAssignmentTable';
import type { PageComponentProps } from '@/domain/common/page/types';
import type { UserAssignmentVO } from './types';
import type { OrgRefRecord } from '@/domain/sys/base/org/refSelector/useOrgRefSelector';
import './UserEditPage.css';
import { updateCurrentUserProfile } from '@/api/user';
import { useUserStore } from '@/stores/user';

const UserEditPage = (props: PageComponentProps) => {
  const feedback = useOperationFeedback();
  const assignmentTableRef = useRef<UserAssignmentTableRef>(null);
  const [hasSelectedAssignments, setHasSelectedAssignments] = useState(false);
  const queryClient = useQueryClient();
  const { can } = usePermissionAccess(userAccess.prefix);
  const canReadSensitive = can(userAccess.permissions.readSensitive);
  const { appNumber, tabKey, operationType, billId } = props;
  const selfMode = props.context?.mode === 'self';
  const currentUser = useUserStore((state) => state.userInfo);
  const isAddNew = operationType === OperationType.ADDNEW;
  const replaceContentTab = useWorkbenchStore((state) => state.replaceContentTab);
  const activateContentTab = useWorkbenchStore((state) => state.activateContentTab);
  const detailQuery = useQuery({
    queryKey: userQueryKeys.detail(billId),
    queryFn: () => userApi.detail(billId!),
    enabled: Boolean(billId) && !selfMode,
  });
  const detail = detailQuery.data;
  const initialValues = useMemo(
    () =>
      selfMode && currentUser
        ? {
            username: currentUser.username,
            name: currentUser.name,
            number: currentUser.number ?? '',
            gender: currentUser.gender,
            birthday: currentUser.birthday ? dayjs(currentUser.birthday) : undefined,
            companyName: currentUser.companyName,
            currentOrgName: currentUser.currentOrgName,
            email: currentUser.email ?? '',
            phone: currentUser.phone ?? '',
            assignments: currentUser.assignments,
          }
        : detail
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
              assignments: detail.assignments ?? [],
            }
          : { assignments: [] },
    [currentUser, detail, selfMode],
  );
  const saveMutation = useCommandMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      if (selfMode && currentUser) {
        const profile = await updateCurrentUserProfile({
          name: String(values.name).trim(),
          gender: values.gender as 'MALE' | 'FEMALE' | undefined,
          birthday: values.birthday
            ? dayjs(values.birthday as dayjs.ConfigType).format('YYYY-MM-DD')
            : undefined,
          avatarAttachmentId: currentUser.avatarAttachmentId,
          attachmentUploadSessions: {},
        });
        useUserStore.getState().setUserInfo({ ...currentUser, ...profile });
        feedback.success('个人信息已保存');
        return;
      }
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
      feedback.success(isAddNew ? '新增成功' : '保存成功');
    },
  });
  return (
    <EditPage
      access={selfMode ? undefined : userAccess}
      title={selfMode ? '个人信息' : '用户'}
      fields={[]}
      initialValues={initialValues}
      operationType={operationType ?? OperationType.EDIT}
      closeGuard={{ appNumber, tabKey }}
      loading={selfMode ? false : detailQuery.isLoading}
      error={selfMode ? null : (detailQuery.error as Error | null)}
      onRetry={() => detailQuery.refetch()}
      onSave={saveMutation.mutateAsync}
      saving={saveMutation.isPending}
      basicContent={(editable) =>
        selfMode ? (
          <FormFieldGrid>
            <FormFieldCell>
              <Form.Item className="sm-edit-field-content" name="username" label="用户名">
                <Input variant="underlined" disabled />
              </Form.Item>
            </FormFieldCell>
            <FormFieldCell>
              <Form.Item className="sm-edit-field-content" name="number" label="工号">
                <Input variant="underlined" disabled />
              </Form.Item>
            </FormFieldCell>
            <FormFieldCell>
              <Form.Item
                className="sm-edit-field-content"
                name="name"
                label="姓名"
                rules={[{ required: true, message: '姓名不能为空' }]}
              >
                <Input variant="underlined" disabled={!editable} maxLength={50} />
              </Form.Item>
            </FormFieldCell>
            <FormFieldCell>
              <Form.Item className="sm-edit-field-content" name="gender" label="性别">
                <Select
                  variant="underlined"
                  disabled={!editable}
                  allowClear
                  options={[
                    { value: 'MALE', label: '男' },
                    { value: 'FEMALE', label: '女' },
                  ]}
                />
              </Form.Item>
            </FormFieldCell>
            <FormFieldCell>
              <Form.Item className="sm-edit-field-content" name="birthday" label="生日">
                <DatePicker
                  className="sm-edit-control-full"
                  variant="underlined"
                  disabled={!editable}
                />
              </Form.Item>
            </FormFieldCell>
            {[
              ['companyName', '公司'],
              ['currentOrgName', '部门'],
              ['phone', '手机'],
              ['email', '邮箱'],
            ].map(([name, label]) => (
              <FormFieldCell key={name}>
                <Form.Item className="sm-edit-field-content" name={name} label={label}>
                  <Input variant="underlined" disabled />
                </Form.Item>
              </FormFieldCell>
            ))}
          </FormFieldGrid>
        ) : (
          <UserProfileFields
            editable={editable}
            isAddNew={isAddNew}
            canReadSensitive={canReadSensitive}
          />
        )
      }
      detailLabel="部门信息"
      detailContent={(editable) =>
        selfMode ? (
          <Table
            size="small"
            pagination={false}
            rowKey="id"
            dataSource={currentUser?.assignments ?? []}
            columns={[
              { title: '部门', render: (_, assignment) => assignment.org.name },
              { title: '部门长名称', dataIndex: 'orgNamePath' },
              { title: '岗位', dataIndex: 'position', width: 160, render: (value) => value || '-' },
              {
                title: '主职',
                dataIndex: 'isPrimary',
                width: 80,
                render: (value) => (value ? '是' : '否'),
              },
            ]}
          />
        ) : (
          <UserAssignmentTable
            ref={assignmentTableRef}
            editable={editable}
            onSelectionChange={setHasSelectedAssignments}
          />
        )
      }
      detailExtra={(editable) =>
        !selfMode && editable ? (
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
