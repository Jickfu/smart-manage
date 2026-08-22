import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useEffect, useState } from 'react';
import { Button, Form, Upload } from 'antd';
import { DeleteOutlined, EditOutlined, UploadOutlined } from '@ant-design/icons';
import type { UploadProps } from 'antd';
import AppModal from '@/domain/common/component/AppModal';
import { businessAttachmentApi } from '@/domain/common/attachment/api';
import { UserAvatar } from '@/domain/sys/base/user/UserAvatar';
import type { UserInfoVO } from '@/types/api';
import { updateCurrentUserProfile } from '@/api/user';
import '@/domain/sys/base/user/UserEditPage.css';
import CurrentLoginLogModal from './CurrentLoginLogModal';
import CurrentOperateLogModal from './CurrentOperateLogModal';
import PersonalCredentialModal from './PersonalCredentialModal';
import PersonalInfoModal from './PersonalInfoModal';

interface PersonalSettingsModalProps {
  open: boolean;
  userInfo: UserInfoVO | null;
  onClose: () => void;
  onProfileSaved: (profile: UserInfoVO) => void;
  onPasswordChanged: () => void;
}

interface ProfileValues {
  avatar?: string;
  avatarAttachmentId?: string;
  avatarUploadSessionId?: string;
}

type CredentialType = 'PHONE' | 'EMAIL' | 'PASSWORD';
const AVATAR_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp']);

export default function PersonalSettingsModal({
  open,
  userInfo,
  onClose,
  onProfileSaved,
  onPasswordChanged,
}: PersonalSettingsModalProps) {
  const feedback = useOperationFeedback();
  const [profileForm] = Form.useForm<ProfileValues>();
  const [avatarSaving, setAvatarSaving] = useState(false);
  const [credentialType, setCredentialType] = useState<CredentialType>();
  const [loginLogOpen, setLoginLogOpen] = useState(false);
  const [operateLogOpen, setOperateLogOpen] = useState(false);
  const [personalInfoOpen, setPersonalInfoOpen] = useState(false);
  const avatar = Form.useWatch('avatar', profileForm);
  const avatarAttachmentId = Form.useWatch('avatarAttachmentId', profileForm);

  useEffect(() => {
    if (!open) return;
    profileForm.setFieldsValue({
      avatar: userInfo?.avatar,
      avatarAttachmentId: userInfo?.avatarAttachmentId,
      avatarUploadSessionId: undefined,
    });
  }, [open, profileForm, userInfo]);

  const saveAvatar = async (attachmentId?: string, uploadSessionId?: string) => {
    if (!userInfo) return;
    setAvatarSaving(true);
    try {
      const profile = await updateCurrentUserProfile({
        name: userInfo.name,
        gender: userInfo.gender,
        birthday: userInfo.birthday,
        avatarAttachmentId: attachmentId,
        attachmentUploadSessions:
          attachmentId && uploadSessionId ? { [attachmentId]: uploadSessionId } : {},
      });
      onProfileSaved(profile);
      feedback.success('头像已更新');
    } finally {
      setAvatarSaving(false);
    }
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
      await saveAvatar(attachment.id, attachment.uploadSessionId);
      onSuccess?.(attachment);
    } catch (error) {
      feedback.fromError(error, '头像上传失败');
      onError?.(error as Error);
    }
  };

  const rows = [
    { label: '姓名', value: userInfo?.name || '-' },
    { label: '公司', value: userInfo?.companyName || '-' },
    { label: '部门', value: userInfo?.currentOrgName || '-' },
  ];

  return (
    <>
      <AppModal
        title="个人设置"
        open={open}
        width={780}
        bodyMode="fixed"
        className="sm-personal-settings-modal"
        closeDisabled={avatarSaving}
        onCancel={onClose}
        footer={null}
      >
        <div className="sm-personal-settings-layout">
          <nav className="sm-personal-settings-nav" aria-label="个人设置栏目">
            <button type="button" className="sm-personal-settings-nav-item--active">
              账号信息
            </button>
            <button type="button" onClick={() => setCredentialType('PASSWORD')}>
              修改密码
            </button>
          </nav>
          <section className="sm-personal-settings-content">
            <div className="sm-personal-settings-card">
              <Form form={profileForm} component={false}>
                <Form.Item name="avatar" hidden>
                  <input />
                </Form.Item>
                <Form.Item name="avatarAttachmentId" hidden>
                  <input />
                </Form.Item>
                <Form.Item name="avatarUploadSessionId" hidden>
                  <input />
                </Form.Item>
              </Form>
              <div className="sm-personal-settings-avatar">
                <div className="sm-user-avatar-upload">
                  <UserAvatar
                    size={104}
                    name={userInfo?.name}
                    username={userInfo?.username}
                    src={avatar}
                  />
                  <div className="sm-user-avatar-upload-mask sm-user-avatar-upload-mask--full">
                    <Upload
                      className="sm-user-avatar-upload-control"
                      accept="image/jpeg,image/png,image/webp"
                      disabled={avatarSaving}
                      maxCount={1}
                      showUploadList={false}
                      customRequest={uploadAvatar}
                      beforeUpload={(file) => {
                        if (AVATAR_TYPES.has(file.type)) return true;
                        feedback.warning('头像仅支持 JPG、PNG、WebP 格式');
                        return Upload.LIST_IGNORE;
                      }}
                    >
                      <button type="button">
                        <UploadOutlined /> 修改
                      </button>
                    </Upload>
                    {avatarAttachmentId && (
                      <button
                        type="button"
                        disabled={avatarSaving}
                        onClick={() => {
                          profileForm.setFieldValue('avatar', undefined);
                          profileForm.setFieldValue('avatarAttachmentId', undefined);
                          void saveAvatar();
                        }}
                      >
                        <DeleteOutlined /> 删除
                      </button>
                    )}
                  </div>
                </div>
              </div>

              <div className="sm-personal-settings-info">
                {rows.map((row) => (
                  <div className="sm-personal-settings-row" key={row.label}>
                    <span>{row.label}</span>
                    <strong>{row.value}</strong>
                  </div>
                ))}
                <EditableRow
                  label="手机"
                  value={userInfo?.phone}
                  onEdit={() => setCredentialType('PHONE')}
                />
                <EditableRow
                  label="邮箱"
                  value={userInfo?.email}
                  onEdit={() => setCredentialType('EMAIL')}
                />
                <EditableRow
                  label="密码"
                  value="••••••••"
                  onEdit={() => setCredentialType('PASSWORD')}
                />
              </div>

              <div className="sm-personal-settings-footer">
                <Button type="link" onClick={() => setPersonalInfoOpen(true)}>
                  个人信息
                </Button>
                <Button type="link" onClick={() => setLoginLogOpen(true)}>
                  登录日志
                </Button>
                <Button type="link" onClick={() => setOperateLogOpen(true)}>
                  操作日志
                </Button>
              </div>
            </div>
          </section>
        </div>
      </AppModal>
      {credentialType && (
        <PersonalCredentialModal
          key={credentialType}
          type={credentialType}
          onClose={() => setCredentialType(undefined)}
          onProfileSaved={onProfileSaved}
          onPasswordChanged={onPasswordChanged}
        />
      )}
      <CurrentLoginLogModal open={loginLogOpen} onClose={() => setLoginLogOpen(false)} />
      <CurrentOperateLogModal open={operateLogOpen} onClose={() => setOperateLogOpen(false)} />
      <PersonalInfoModal
        open={personalInfoOpen}
        userInfo={userInfo}
        onClose={() => setPersonalInfoOpen(false)}
        onSaved={onProfileSaved}
      />
    </>
  );
}

function EditableRow({
  label,
  value,
  onEdit,
}: {
  label: string;
  value?: string;
  onEdit: () => void;
}) {
  return (
    <div className="sm-personal-settings-row">
      <span>{label}</span>
      <strong>{value || '-'}</strong>
      <Button type="text" icon={<EditOutlined />} aria-label={`修改${label}`} onClick={onEdit} />
    </div>
  );
}
