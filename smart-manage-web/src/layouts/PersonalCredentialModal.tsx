import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useState } from 'react';
import { Button, Form, Input, Select } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { sm2 } from 'sm-crypto';
import AppModal from '@/domain/common/component/AppModal';
import {
  getCurrentPasswordPublicKey,
  updateCurrentUserContact,
  updateCurrentUserPassword,
} from '@/api/user';
import type { UserInfoVO } from '@/types/api';

type CredentialType = 'PHONE' | 'EMAIL' | 'PASSWORD';

interface PersonalCredentialModalProps {
  type: CredentialType;
  onClose: () => void;
  onProfileSaved: (profile: UserInfoVO) => void;
  onPasswordChanged: () => void;
}

interface VerifyValues {
  verificationMethod: 'PASSWORD';
  password: string;
}

interface ChangeValues {
  value: string;
  confirmValue?: string;
}

const TITLES: Record<CredentialType, string> = {
  PHONE: '更改手机号',
  EMAIL: '更改邮箱',
  PASSWORD: '更改密码',
};

export default function PersonalCredentialModal({
  type,
  onClose,
  onProfileSaved,
  onPasswordChanged,
}: PersonalCredentialModalProps) {
  const feedback = useOperationFeedback();
  const [verifyForm] = Form.useForm<VerifyValues>();
  const [changeForm] = Form.useForm<ChangeValues>();
  const [step, setStep] = useState<'verify' | 'change'>('verify');
  const [saving, setSaving] = useState(false);

  const submit = async () => {
    const verification = await verifyForm.validateFields();
    const change = await changeForm.validateFields();
    setSaving(true);
    try {
      const publicKey = await getCurrentPasswordPublicKey();
      const encryptedPassword = sm2.doEncrypt(verification.password, publicKey, 1);
      if (type === 'PASSWORD') {
        await updateCurrentUserPassword(
          encryptedPassword,
          sm2.doEncrypt(change.value, publicKey, 1),
        );
        feedback.success('密码已修改，请重新登录');
        onPasswordChanged();
      } else {
        const profile = await updateCurrentUserContact({
          verificationMethod: verification.verificationMethod,
          password: encryptedPassword,
          type,
          value: change.value.trim(),
        });
        onProfileSaved(profile);
        feedback.success(type === 'PHONE' ? '手机号已修改' : '邮箱已修改');
        onClose();
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <AppModal
      title={TITLES[type]}
      open
      width={520}
      bodyMode="natural"
      closeDisabled={saving}
      onCancel={onClose}
      footer={null}
    >
      <div className="sm-personal-credential">
        {step === 'change' && (
          <Button
            className="sm-personal-credential-back"
            type="text"
            icon={<ArrowLeftOutlined />}
            disabled={saving}
            onClick={() => setStep('verify')}
          >
            返回验证
          </Button>
        )}
        <Form
          form={verifyForm}
          layout="vertical"
          initialValues={{ verificationMethod: 'PASSWORD' }}
          hidden={step !== 'verify'}
        >
          <Form.Item name="verificationMethod" label="验证方式">
            <Select options={[{ value: 'PASSWORD', label: '密码验证' }]} popupMatchSelectWidth />
          </Form.Item>
          <Form.Item
            name="password"
            label="密码"
            rules={[{ required: true, message: '请输入当前密码' }]}
          >
            <Input.Password autoComplete="current-password" />
          </Form.Item>
          <div className="sm-personal-credential-actions">
            <Button onClick={onClose}>取消</Button>
            <Button
              type="primary"
              onClick={() => void verifyForm.validateFields().then(() => setStep('change'))}
            >
              下一步
            </Button>
          </div>
        </Form>
        <Form form={changeForm} layout="vertical" hidden={step !== 'change'}>
          <Form.Item
            name="value"
            label={type === 'PHONE' ? '新手机号' : type === 'EMAIL' ? '新邮箱' : '新密码'}
            rules={[
              { required: true, message: '请输入新值' },
              ...(type === 'EMAIL' ? [{ type: 'email' as const, message: '邮箱格式不正确' }] : []),
              ...(type === 'PASSWORD' ? [{ min: 8, message: '新密码不能少于8位' }] : []),
            ]}
          >
            {type === 'PASSWORD' ? (
              <Input.Password autoComplete="new-password" />
            ) : (
              <Input maxLength={type === 'PHONE' ? 30 : 100} />
            )}
          </Form.Item>
          {type === 'PASSWORD' && (
            <Form.Item
              name="confirmValue"
              label="确认新密码"
              dependencies={['value']}
              rules={[
                { required: true, message: '请再次输入新密码' },
                ({ getFieldValue }) => ({
                  validator: (_, value) =>
                    !value || getFieldValue('value') === value
                      ? Promise.resolve()
                      : Promise.reject(new Error('两次输入的密码不一致')),
                }),
              ]}
            >
              <Input.Password autoComplete="new-password" />
            </Form.Item>
          )}
          <div className="sm-personal-credential-actions">
            <Button disabled={saving} onClick={onClose}>
              取消
            </Button>
            <Button type="primary" loading={saving} onClick={() => void submit()}>
              确定
            </Button>
          </div>
        </Form>
      </div>
    </AppModal>
  );
}
