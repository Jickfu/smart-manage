import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useEffect, useState } from 'react';
import dayjs from 'dayjs';
import { Button, DatePicker, Form, Input, Select, Table } from 'antd';
import AppModal from '@/domain/common/component/AppModal';
import { FormFieldCell, FormFieldGrid } from '@/domain/common/page/FormFieldLayout';
import { updateCurrentUserProfile } from '@/api/user';
import type { UserInfoVO } from '@/types/api';

interface PersonalInfoModalProps {
  open: boolean;
  userInfo: UserInfoVO | null;
  onClose: () => void;
  onSaved: (profile: UserInfoVO) => void;
}

interface PersonalInfoValues {
  name: string;
  gender?: 'MALE' | 'FEMALE';
  birthday?: dayjs.Dayjs;
}

/** 本人资料使用独立登录态接口，不依赖系统管理应用、菜单或用户管理权限。 */
export default function PersonalInfoModal({
  open,
  userInfo,
  onClose,
  onSaved,
}: PersonalInfoModalProps) {
  const feedback = useOperationFeedback();
  const [form] = Form.useForm<PersonalInfoValues>();
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!open || !userInfo) return;
    form.setFieldsValue({
      name: userInfo.name,
      gender: userInfo.gender,
      birthday: userInfo.birthday ? dayjs(userInfo.birthday) : undefined,
    });
  }, [form, open, userInfo]);

  const save = async () => {
    if (!userInfo) return;
    const values = await form.validateFields();
    setSaving(true);
    try {
      const profile = await updateCurrentUserProfile({
        name: values.name.trim(),
        gender: values.gender,
        birthday: values.birthday?.format('YYYY-MM-DD'),
        avatarAttachmentId: userInfo.avatarAttachmentId,
        attachmentUploadSessions: {},
      });
      onSaved(profile);
      feedback.success('个人信息已保存');
      onClose();
    } finally {
      setSaving(false);
    }
  };

  return (
    <AppModal
      title="个人信息"
      open={open}
      width={760}
      bodyMode="fixed"
      className="sm-personal-info-modal"
      closeDisabled={saving}
      onCancel={onClose}
      footer={
        <>
          <Button disabled={saving} onClick={onClose}>
            取消
          </Button>
          <Button type="primary" loading={saving} onClick={() => void save()}>
            保存
          </Button>
        </>
      }
    >
      <Form form={form} layout="vertical" className="sm-edit-form sm-personal-info-form">
        <FormFieldGrid maxColumns={2}>
          <FormFieldCell>
            <Form.Item className="sm-edit-field-content" label="用户名">
              <Input variant="underlined" value={userInfo?.username} disabled />
            </Form.Item>
          </FormFieldCell>
          <FormFieldCell>
            <Form.Item className="sm-edit-field-content" label="工号">
              <Input variant="underlined" value={userInfo?.number ?? ''} disabled />
            </Form.Item>
          </FormFieldCell>
          <FormFieldCell>
            <Form.Item
              className="sm-edit-field-content"
              name="name"
              label="姓名"
              rules={[{ required: true, message: '姓名不能为空' }]}
            >
              <Input variant="underlined" maxLength={50} />
            </Form.Item>
          </FormFieldCell>
          <FormFieldCell>
            <Form.Item className="sm-edit-field-content" name="gender" label="性别">
              <Select
                variant="underlined"
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
              <DatePicker className="sm-edit-control-full" variant="underlined" />
            </Form.Item>
          </FormFieldCell>
          {[
            { label: '公司', value: userInfo?.companyName },
            { label: '部门', value: userInfo?.currentOrgName },
            { label: '手机', value: userInfo?.phone ?? '' },
            { label: '邮箱', value: userInfo?.email ?? '' },
          ].map((field) => (
            <FormFieldCell key={field.label}>
              <Form.Item className="sm-edit-field-content" label={field.label}>
                <Input variant="underlined" value={field.value} disabled />
              </Form.Item>
            </FormFieldCell>
          ))}
        </FormFieldGrid>
      </Form>
      <div className="sm-personal-info-assignments">
        <div className="sm-personal-info-section-title">部门信息</div>
        <Table
          className="sm-list-table sm-personal-info-assignment-table"
          size="small"
          pagination={false}
          tableLayout="fixed"
          rowKey="id"
          dataSource={userInfo?.assignments ?? []}
          columns={[
            {
              title: '#',
              width: 44,
              align: 'center',
              className: 'sm-list-sequence-column',
              render: (_, __, index) => index + 1,
            },
            { title: '部门', dataIndex: 'orgName', ellipsis: true },
            { title: '部门长名称', dataIndex: 'orgNamePath', ellipsis: true },
            { title: '岗位', dataIndex: 'position', width: 140, render: (value) => value || '-' },
            {
              title: '主职',
              dataIndex: 'isPrimary',
              width: 70,
              align: 'center',
              render: (value) => (value ? '是' : '否'),
            },
          ]}
        />
      </div>
    </AppModal>
  );
}
