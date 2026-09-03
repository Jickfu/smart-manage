import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useState } from 'react';
import { Button, Form, Input, Select } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { sm2 } from 'sm-crypto';
import AppModal from '@/domain/common/component/AppModal';
import { FormFieldCell, FormFieldGrid } from '@/domain/common/page/edit/FormFieldLayout';
import {
  getCurrentPasswordPublicKey,
  updateCurrentUserContact,
  updateCurrentUserPassword,
  requestCurrentPasswordEmailCode,
  updateCurrentUserPasswordByEmail,
  requestCurrentEmailCode,
  bindCurrentEmail,
} from '@/api/user';
import type { UserInfoVO } from '@/types/api';

type CredentialType = 'PHONE' | 'EMAIL' | 'PASSWORD';

interface PersonalCredentialModalProps {
  type: CredentialType;
  onClose: () => void;
  onProfileSaved: (profile: UserInfoVO) => void;
  onPasswordChanged: () => void;
  emailPasswordAvailable: boolean;
}

interface VerifyValues {
  verificationMethod: 'PASSWORD' | 'EMAIL';
  password?: string;
}

interface ChangeValues {
  value: string;
  confirmValue?: string;
  code?: string;
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
  emailPasswordAvailable,
}: PersonalCredentialModalProps) {
  const feedback = useOperationFeedback();
  const [verifyForm] = Form.useForm<VerifyValues>();
  const [changeForm] = Form.useForm<ChangeValues>();
  const [step, setStep] = useState<'verify' | 'change'>('verify');
  const [saving, setSaving] = useState(false);
  const [emailCodeSent, setEmailCodeSent] = useState(false);
  const verificationMethod = Form.useWatch('verificationMethod', verifyForm);

  const submit = async () => {
    const verification = await verifyForm.validateFields();
    const change = await changeForm.validateFields();
    setSaving(true);
    try {
      const publicKey = await getCurrentPasswordPublicKey();
      const encryptedPassword = verification.password
        ? sm2.doEncrypt(verification.password, publicKey, 1)
        : '';
      if (type === 'PASSWORD') {
        const encryptedNewPassword = sm2.doEncrypt(change.value, publicKey, 1);
        if (verification.verificationMethod === 'EMAIL') {
          await updateCurrentUserPasswordByEmail(change.code ?? '', encryptedNewPassword);
        } else {
          await updateCurrentUserPassword(encryptedPassword, encryptedNewPassword);
        }
        feedback.success('密码已修改，请重新登录');
        onPasswordChanged();
      } else if (type === 'EMAIL') {
        if (!emailCodeSent) {
          await requestCurrentEmailCode(encryptedPassword, change.value.trim());
          setEmailCodeSent(true);
          feedback.success('验证码已发送到新邮箱');
          return;
        }
        const profile = await bindCurrentEmail(change.value.trim(), change.code ?? '');
        onProfileSaved(profile);
        feedback.success('邮箱已验证并修改');
        onClose();
      } else {
        const profile = await updateCurrentUserContact({
          verificationMethod: 'PASSWORD',
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
          className="sm-edit-form"
          initialValues={{ verificationMethod: 'PASSWORD' }}
          hidden={step !== 'verify'}
        >
          <FormFieldGrid maxColumns={1}>
            <FormFieldCell>
              <Form.Item
                className="sm-edit-field-content"
                name="verificationMethod"
                label="验证方式"
              >
                <Select
                  variant="underlined"
                  options={[
                    { value: 'PASSWORD', label: '原密码验证' },
                    ...(type === 'PASSWORD' && emailPasswordAvailable
                      ? [{ value: 'EMAIL' as const, label: '邮箱验证码' }]
                      : []),
                  ]}
                  popupMatchSelectWidth
                />
              </Form.Item>
            </FormFieldCell>
            <Form.Item
              noStyle
              shouldUpdate={(previous, current) =>
                previous.verificationMethod !== current.verificationMethod
              }
            >
              {({ getFieldValue }) =>
                getFieldValue('verificationMethod') === 'PASSWORD' ? (
                  <FormFieldCell>
                    <Form.Item
                      className="sm-edit-field-content"
                      name="password"
                      label="密码"
                      rules={[{ required: true, message: '请输入当前密码' }]}
                    >
                      <Input.Password variant="underlined" autoComplete="current-password" />
                    </Form.Item>
                  </FormFieldCell>
                ) : (
                  <div className="sm-personal-credential-email-tip">
                    验证码将发送到当前账号已验证的邮箱。
                  </div>
                )
              }
            </Form.Item>
          </FormFieldGrid>
          <div className="sm-personal-credential-actions">
            <Button onClick={onClose}>取消</Button>
            <Button
              type="primary"
              onClick={() =>
                void verifyForm.validateFields().then(async (values) => {
                  if (type === 'PASSWORD' && values.verificationMethod === 'EMAIL') {
                    setSaving(true);
                    try {
                      await requestCurrentPasswordEmailCode();
                      feedback.success('验证码已发送到当前已验证邮箱');
                    } finally {
                      setSaving(false);
                    }
                  }
                  setStep('change');
                })
              }
              loading={saving}
            >
              下一步
            </Button>
          </div>
        </Form>
        <Form
          form={changeForm}
          layout="vertical"
          className="sm-edit-form"
          hidden={step !== 'change'}
        >
          <FormFieldGrid maxColumns={1}>
            <FormFieldCell>
              <Form.Item
                className="sm-edit-field-content"
                name="value"
                label={type === 'PHONE' ? '新手机号' : type === 'EMAIL' ? '新邮箱' : '新密码'}
                rules={[
                  { required: true, message: '请输入新值' },
                  ...(type === 'EMAIL'
                    ? [{ type: 'email' as const, message: '邮箱格式不正确' }]
                    : []),
                  ...(type === 'PASSWORD' ? [{ min: 8, message: '新密码不能少于8位' }] : []),
                ]}
              >
                {type === 'PASSWORD' ? (
                  <Input.Password variant="underlined" autoComplete="new-password" />
                ) : (
                  <Input variant="underlined" maxLength={type === 'PHONE' ? 30 : 100} />
                )}
              </Form.Item>
            </FormFieldCell>
            {type === 'EMAIL' && emailCodeSent && (
              <FormFieldCell>
                <Form.Item
                  className="sm-edit-field-content"
                  name="code"
                  label="邮箱验证码"
                  rules={[
                    { required: true, message: '请输入邮箱验证码' },
                    { pattern: /^\d{6}$/, message: '请输入6位邮箱验证码' },
                  ]}
                >
                  <Input
                    variant="underlined"
                    maxLength={6}
                    inputMode="numeric"
                    autoComplete="one-time-code"
                  />
                </Form.Item>
              </FormFieldCell>
            )}
            {type === 'PASSWORD' && (
              <>
                {verificationMethod === 'EMAIL' && (
                  <FormFieldCell>
                    <Form.Item
                      className="sm-edit-field-content"
                      name="code"
                      label="邮箱验证码"
                      rules={[
                        { required: true, message: '请输入邮箱验证码' },
                        { pattern: /^\d{6}$/, message: '请输入6位邮箱验证码' },
                      ]}
                    >
                      <Input
                        variant="underlined"
                        maxLength={6}
                        inputMode="numeric"
                        autoComplete="one-time-code"
                      />
                    </Form.Item>
                  </FormFieldCell>
                )}
                <FormFieldCell>
                  <Form.Item
                    className="sm-edit-field-content"
                    name="confirmValue"
                    label="确认新密码"
                    dependencies={['value']}
                    rules={[
                      { required: true, message: '请再次输入新密码' },
                      ({ getFieldValue }) => ({
                        validator: (_: unknown, value: string) =>
                          !value || getFieldValue('value') === value
                            ? Promise.resolve()
                            : Promise.reject(new Error('两次输入的密码不一致')),
                      }),
                    ]}
                  >
                    <Input.Password variant="underlined" autoComplete="new-password" />
                  </Form.Item>
                </FormFieldCell>
              </>
            )}
          </FormFieldGrid>
          <div className="sm-personal-credential-actions">
            <Button disabled={saving} onClick={onClose}>
              取消
            </Button>
            <Button type="primary" loading={saving} onClick={() => void submit()}>
              {type === 'EMAIL' && !emailCodeSent ? '发送验证码' : '确定'}
            </Button>
          </div>
        </Form>
      </div>
    </AppModal>
  );
}
