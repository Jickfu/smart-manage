import { useEffect, useState } from 'react';
import dayjs from 'dayjs';
import { App, Button, DatePicker, Form, Input, Select, Table } from 'antd';
import AppModal from '@/domain/common/component/AppModal';
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

/** 本人资料使用独立登录态接口，不依赖系统建模应用、菜单或用户管理权限。 */
export default function PersonalInfoModal({
  open,
  userInfo,
  onClose,
  onSaved,
}: PersonalInfoModalProps) {
  const { message } = App.useApp();
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
      message.success('个人信息已保存');
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
      <Form form={form} layout="vertical" className="sm-personal-info-form">
        <div className="sm-personal-info-grid">
          <Form.Item label="用户名">
            <Input variant="underlined" value={userInfo?.username} disabled />
          </Form.Item>
          <Form.Item label="工号">
            <Input variant="underlined" value={userInfo?.number ?? ''} disabled />
          </Form.Item>
          <Form.Item name="name" label="姓名" rules={[{ required: true, message: '姓名不能为空' }]}>
            <Input variant="underlined" maxLength={50} />
          </Form.Item>
          <Form.Item name="gender" label="性别">
            <Select
              variant="underlined"
              allowClear
              options={[
                { value: 'MALE', label: '男' },
                { value: 'FEMALE', label: '女' },
              ]}
            />
          </Form.Item>
          <Form.Item name="birthday" label="生日">
            <DatePicker variant="underlined" />
          </Form.Item>
          <Form.Item label="公司">
            <Input variant="underlined" value={userInfo?.companyName} disabled />
          </Form.Item>
          <Form.Item label="部门">
            <Input variant="underlined" value={userInfo?.currentOrgName} disabled />
          </Form.Item>
          <Form.Item label="手机">
            <Input variant="underlined" value={userInfo?.phone ?? ''} disabled />
          </Form.Item>
          <Form.Item label="邮箱">
            <Input variant="underlined" value={userInfo?.email ?? ''} disabled />
          </Form.Item>
        </div>
      </Form>
      <div className="sm-personal-info-assignments">
        <div className="sm-personal-info-section-title">部门信息</div>
        <Table
          size="small"
          pagination={false}
          rowKey="id"
          dataSource={userInfo?.assignments ?? []}
          columns={[
            { title: '部门', dataIndex: 'orgName' },
            { title: '部门长名称', dataIndex: 'orgNamePath' },
            { title: '岗位', dataIndex: 'position', width: 140, render: (value) => value || '-' },
            {
              title: '主职',
              dataIndex: 'isPrimary',
              width: 70,
              render: (value) => (value ? '是' : '否'),
            },
          ]}
        />
      </div>
    </AppModal>
  );
}
