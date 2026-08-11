import { App, DatePicker, Form, Input, Select, Upload } from 'antd';
import type { UploadProps } from 'antd';
import { businessAttachmentApi } from '@/domain/common/attachment/api';
import { UserAvatar } from './UserAvatar';

interface UserProfileFieldsProps {
  editable: boolean;
  isAddNew: boolean;
}

const AVATAR_ACCEPT = 'image/jpeg,image/png,image/webp';
const ALLOWED_AVATAR_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp']);
export function UserProfileFields({ editable, isAddNew }: UserProfileFieldsProps) {
  const { message } = App.useApp();
  const form = Form.useFormInstance();
  const name = Form.useWatch('name', form) as string | undefined;
  const number = Form.useWatch('number', form) as string | undefined;
  const username = Form.useWatch('username', form) as string | undefined;
  const avatar = Form.useWatch('avatar', form) as string | undefined;
  const avatarAttachmentId = Form.useWatch('avatarAttachmentId', form) as string | undefined;
  const customRequest: UploadProps['customRequest'] = async ({ file, onSuccess, onError }) => {
    try {
      const sourceFile = file as File;
      const attachment = await businessAttachmentApi.upload('sys.base.user-avatar', sourceFile);
      const reader = new FileReader();
      reader.onload = () => form.setFieldValue('avatar', String(reader.result));
      reader.readAsDataURL(sourceFile);
      form.setFieldValue('avatarAttachmentId', attachment.id);
      form.setFieldValue('avatarUploadSessionId', attachment.uploadSessionId);
      onSuccess?.(attachment);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '头像上传失败');
      onError?.(error as Error);
    }
  };
  const beforeUpload: UploadProps['beforeUpload'] = (file) => {
    if (ALLOWED_AVATAR_TYPES.has(file.type)) return true;
    message.error('头像仅支持 JPG、PNG、WebP 格式');
    return Upload.LIST_IGNORE;
  };

  return (
    <div className="sm-user-profile-layout">
      <Form.Item name="avatar" hidden>
        <Input />
      </Form.Item>
      <Form.Item name="avatarAttachmentId" hidden>
        <Input />
      </Form.Item>
      <Form.Item name="avatarUploadSessionId" hidden>
        <Input />
      </Form.Item>
      <div className="sm-user-profile-avatar">
        {editable ? (
          <div className="sm-user-avatar-upload">
            <UserAvatar name={name} number={number} username={username} src={avatar} size={120} />
            {avatarAttachmentId ? (
              <div className="sm-user-avatar-upload-mask">
                <button
                  type="button"
                  onClick={() => {
                    form.setFieldValue('avatar', undefined);
                    form.setFieldValue('avatarAttachmentId', undefined);
                    form.setFieldValue('avatarUploadSessionId', undefined);
                  }}
                >
                  删除头像
                </button>
              </div>
            ) : (
              <div className="sm-user-avatar-upload-mask sm-user-avatar-upload-mask--full">
                <Upload
                  className="sm-user-avatar-upload-control sm-user-avatar-upload-control--full"
                  accept={AVATAR_ACCEPT}
                  beforeUpload={beforeUpload}
                  maxCount={1}
                  showUploadList={false}
                  customRequest={customRequest}
                >
                  <button type="button">上传头像</button>
                </Upload>
              </div>
            )}
          </div>
        ) : (
          <UserAvatar name={name} number={number} username={username} src={avatar} size={120} />
        )}
      </div>
      <div className="sm-user-profile-sections">
        <section className="sm-user-profile-section">
          <h3 className="sm-user-profile-section-title">人员信息</h3>
          <div className="sm-user-profile-fields">
            <div className="sm-user-profile-field">
              <Form.Item
                name="name"
                label="姓名"
                rules={[{ required: true, message: '姓名不能为空' }]}
              >
                <Input variant="underlined" disabled={!editable} />
              </Form.Item>
            </div>
            <div className="sm-user-profile-field">
              <Form.Item
                name="number"
                label="工号"
                rules={[{ required: true, message: '工号不能为空' }]}
              >
                <Input variant="underlined" disabled={!editable} />
              </Form.Item>
            </div>
            <div className="sm-user-profile-field">
              <Form.Item name="gender" label="性别">
                <Select
                  variant="underlined"
                  allowClear
                  disabled={!editable}
                  options={[
                    { label: '男', value: 'MALE' },
                    { label: '女', value: 'FEMALE' },
                  ]}
                />
              </Form.Item>
            </div>
            <div className="sm-user-profile-field">
              <Form.Item name="birthday" label="生日">
                <DatePicker
                  className="sm-user-profile-control"
                  variant="underlined"
                  disabled={!editable}
                />
              </Form.Item>
            </div>
          </div>
        </section>
        <section className="sm-user-profile-section">
          <h3 className="sm-user-profile-section-title">账号信息</h3>
          <div className="sm-user-profile-fields">
            <div className="sm-user-profile-field">
              <Form.Item
                name="username"
                label="用户名"
                rules={[{ required: true, message: '用户名不能为空' }]}
              >
                <Input variant="underlined" disabled={!editable || !isAddNew} />
              </Form.Item>
            </div>
            {isAddNew && (
              <div className="sm-user-profile-field">
                <Form.Item
                  name="password"
                  label="初始密码"
                  rules={[{ required: true, message: '初始密码不能为空' }]}
                >
                  <Input.Password variant="underlined" disabled={!editable} />
                </Form.Item>
              </div>
            )}
            <div className="sm-user-profile-field">
              <Form.Item
                name="phone"
                label="手机"
                rules={[{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }]}
              >
                <Input variant="underlined" disabled={!editable} />
              </Form.Item>
            </div>
            <div className="sm-user-profile-field">
              <Form.Item
                name="email"
                label="邮箱"
                rules={[{ type: 'email', message: '邮箱格式不正确' }]}
              >
                <Input variant="underlined" disabled={!editable} />
              </Form.Item>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}
