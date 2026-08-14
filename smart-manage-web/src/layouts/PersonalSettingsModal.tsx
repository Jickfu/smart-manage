import { useEffect, useState } from 'react';
import { App, Button, Form, Input, Upload } from 'antd';
import type { UploadProps } from 'antd';
import { sm2 } from 'sm-crypto';
import AppModal from '@/domain/common/component/AppModal';
import { businessAttachmentApi } from '@/domain/common/attachment/api';
import { UserAvatar } from '@/domain/sys/base/user/UserAvatar';
import type { UserInfoVO } from '@/types/api';
import {
  getCurrentPasswordPublicKey,
  updateCurrentUserPassword,
  updateCurrentUserProfile,
} from '@/api/user';
import '@/domain/sys/base/user/UserEditPage.css';
import CurrentLoginLogModal from './CurrentLoginLogModal';

interface PersonalSettingsModalProps {
  open: boolean;
  userInfo: UserInfoVO | null;
  onClose: () => void;
  onProfileSaved: (profile: UserInfoVO) => void;
  onPasswordChanged: () => void;
}

interface ProfileValues {
  name: string;
  avatar?: string;
  avatarAttachmentId?: string;
  avatarUploadSessionId?: string;
}

interface PasswordValues {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

const AVATAR_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp']);

export default function PersonalSettingsModal({
  open,
  userInfo,
  onClose,
  onProfileSaved,
  onPasswordChanged,
}: PersonalSettingsModalProps) {
  const { message } = App.useApp();
  const [profileForm] = Form.useForm<ProfileValues>();
  const [passwordForm] = Form.useForm<PasswordValues>();
  const [profileSaving, setProfileSaving] = useState(false);
  const [passwordSaving, setPasswordSaving] = useState(false);
  const [activeSection, setActiveSection] = useState<'account' | 'password'>('account');
  const [loginLogOpen, setLoginLogOpen] = useState(false);
  const avatar = Form.useWatch('avatar', profileForm);
  const avatarAttachmentId = Form.useWatch('avatarAttachmentId', profileForm);
  const name = Form.useWatch('name', profileForm);

  useEffect(() => {
    if (!open) return;
    profileForm.setFieldsValue({
      name: userInfo?.name,
      avatar: userInfo?.avatar,
      avatarAttachmentId: userInfo?.avatarAttachmentId,
      avatarUploadSessionId: undefined,
    });
    passwordForm.resetFields();
  }, [open, passwordForm, profileForm, userInfo]);

  const closeModal = () => {
    setActiveSection('account');
    onClose();
  };

  const uploadAvatar: UploadProps['customRequest'] = async ({ file, onSuccess, onError }) => {
    try {
      const sourceFile = file as File;
      const attachment = await businessAttachmentApi.upload('sys.base.user-avatar', sourceFile);
      const reader = new FileReader();
      reader.onload = () => profileForm.setFieldValue('avatar', String(reader.result));
      reader.readAsDataURL(sourceFile);
      profileForm.setFieldValue('avatarAttachmentId', attachment.id);
      profileForm.setFieldValue('avatarUploadSessionId', attachment.uploadSessionId);
      onSuccess?.(attachment);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '头像上传失败');
      onError?.(error as Error);
    }
  };

  const saveProfile = async () => {
    const values = await profileForm.validateFields();
    setProfileSaving(true);
    try {
      const result = await updateCurrentUserProfile({
        name: values.name,
        avatarAttachmentId: values.avatarAttachmentId,
        attachmentUploadSessions:
          values.avatarAttachmentId && values.avatarUploadSessionId
            ? { [values.avatarAttachmentId]: values.avatarUploadSessionId }
            : {},
      });
      onProfileSaved(result);
      message.success('个人资料已更新');
      closeModal();
    } finally {
      setProfileSaving(false);
    }
  };

  const changePassword = async () => {
    const values = await passwordForm.validateFields();
    setPasswordSaving(true);
    try {
      const publicKey = await getCurrentPasswordPublicKey();
      await updateCurrentUserPassword(
        sm2.doEncrypt(values.currentPassword, publicKey, 1),
        sm2.doEncrypt(values.newPassword, publicKey, 1),
      );
      message.success('密码已修改，请重新登录');
      onPasswordChanged();
    } finally {
      setPasswordSaving(false);
    }
  };

  return (
    <AppModal
      title="个人设置"
      open={open}
      width={780}
      bodyMode="fixed"
      className="sm-personal-settings-modal"
      closeDisabled={profileSaving || passwordSaving}
      onCancel={closeModal}
      footer={null}
    >
      <div className="sm-personal-settings-layout">
        <nav className="sm-personal-settings-nav" aria-label="个人设置栏目">
          <button
            type="button"
            className={activeSection === 'account' ? 'sm-personal-settings-nav-item--active' : ''}
            onClick={() => setActiveSection('account')}
          >
            账号信息
          </button>
          <button
            type="button"
            className={activeSection === 'password' ? 'sm-personal-settings-nav-item--active' : ''}
            onClick={() => setActiveSection('password')}
          >
            密码修改
          </button>
        </nav>
        <div className="sm-personal-settings-content">
          <section hidden={activeSection !== 'account'}>
            <Form form={profileForm} layout="vertical" className="sm-personal-settings-form">
              <Form.Item name="avatar" hidden>
                <Input />
              </Form.Item>
              <Form.Item name="avatarAttachmentId" hidden>
                <Input />
              </Form.Item>
              <Form.Item name="avatarUploadSessionId" hidden>
                <Input />
              </Form.Item>
              <div className="sm-personal-settings-avatar">
                <div className="sm-user-avatar-upload">
                  <UserAvatar size={120} name={name} username={userInfo?.username} src={avatar} />
                  {avatarAttachmentId ? (
                    <div className="sm-user-avatar-upload-mask">
                      <button
                        type="button"
                        onClick={() => {
                          profileForm.setFieldValue('avatar', undefined);
                          profileForm.setFieldValue('avatarAttachmentId', undefined);
                          profileForm.setFieldValue('avatarUploadSessionId', undefined);
                        }}
                      >
                        删除头像
                      </button>
                    </div>
                  ) : (
                    <div className="sm-user-avatar-upload-mask sm-user-avatar-upload-mask--full">
                      <Upload
                        className="sm-user-avatar-upload-control sm-user-avatar-upload-control--full"
                        accept="image/jpeg,image/png,image/webp"
                        maxCount={1}
                        showUploadList={false}
                        customRequest={uploadAvatar}
                        beforeUpload={(file) => {
                          if (AVATAR_TYPES.has(file.type)) return true;
                          message.error('头像仅支持 JPG、PNG、WebP 格式');
                          return Upload.LIST_IGNORE;
                        }}
                      >
                        <button type="button">上传头像</button>
                      </Upload>
                    </div>
                  )}
                </div>
              </div>
              <Form.Item
                name="name"
                label="姓名"
                rules={[{ required: true, message: '姓名不能为空' }]}
              >
                <Input variant="underlined" maxLength={50} />
              </Form.Item>
              <Form.Item label="用户名">
                <Input variant="underlined" value={userInfo?.username} disabled />
              </Form.Item>
              <Form.Item label="工号">
                <Input variant="underlined" value={userInfo?.number} disabled />
              </Form.Item>
              <Form.Item label="手机">
                <Input variant="underlined" value={userInfo?.phone} disabled />
              </Form.Item>
              <Form.Item label="邮箱">
                <Input variant="underlined" value={userInfo?.email} disabled />
              </Form.Item>
              <div className="sm-personal-settings-actions">
                <Button onClick={() => setLoginLogOpen(true)}>登录日志</Button>
                <Button onClick={closeModal}>取消</Button>
                <Button type="primary" loading={profileSaving} onClick={() => void saveProfile()}>
                  保存
                </Button>
              </div>
            </Form>
          </section>
          <section hidden={activeSection !== 'password'}>
            <Form form={passwordForm} layout="vertical" className="sm-personal-settings-form">
              <Form.Item
                name="currentPassword"
                label="原密码"
                rules={[{ required: true, message: '请输入原密码' }]}
              >
                <Input.Password variant="underlined" autoComplete="current-password" />
              </Form.Item>
              <Form.Item
                name="newPassword"
                label="新密码"
                rules={[{ required: true, min: 8, message: '新密码不能少于8位' }]}
              >
                <Input.Password variant="underlined" autoComplete="new-password" />
              </Form.Item>
              <Form.Item
                name="confirmPassword"
                label="确认新密码"
                dependencies={['newPassword']}
                rules={[
                  { required: true, message: '请再次输入新密码' },
                  ({ getFieldValue }) => ({
                    validator: (_, value) =>
                      !value || getFieldValue('newPassword') === value
                        ? Promise.resolve()
                        : Promise.reject(new Error('两次输入的密码不一致')),
                  }),
                ]}
              >
                <Input.Password variant="underlined" autoComplete="new-password" />
              </Form.Item>
              <div className="sm-personal-settings-actions">
                <Button onClick={closeModal}>取消</Button>
                <Button
                  type="primary"
                  loading={passwordSaving}
                  onClick={() => void changePassword()}
                >
                  修改密码
                </Button>
              </div>
            </Form>
          </section>
        </div>
      </div>
      <CurrentLoginLogModal open={loginLogOpen} onClose={() => setLoginLogOpen(false)} />
    </AppModal>
  );
}
