import { forwardRef, useImperativeHandle, useState } from 'react';
import type { Key } from 'react';
import { Alert, Button, Form, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useQueryClient } from '@tanstack/react-query';
import AppModal from '@/domain/common/component/AppModal';
import { EditableDetailTable } from '@/domain/common/component/EditableDetailTable';
import { EditFormFields } from '@/domain/common/page/edit/EditFormFields';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { openApiPlatformApi } from './api';
import { formatCredentialExpiresAt } from './credentialForm';
import { openApiQueryKeys } from './queryKeys';
import type { OneTimeCredential, OpenApiApplication, OpenApiCredential } from './types';

export interface OpenApiCredentialSectionRef {
  create: () => void;
  toggleSelected: () => void;
}

interface Props {
  application: OpenApiApplication;
  editable: boolean;
  onSelectionChange: (credential?: OpenApiCredential) => void;
}

const credentialFields: EditField[] = [
  {
    label: '凭据名称',
    dataIndex: 'name',
    type: 'text',
    rules: [{ required: true, message: '凭据名称不能为空' }],
  },
  { label: '过期时间', dataIndex: 'expiresAt', type: 'datetime' },
];

const OpenApiCredentialSection = forwardRef<OpenApiCredentialSectionRef, Props>(
  function OpenApiCredentialSection({ application, editable, onSelectionChange }, ref) {
    const [modalOpen, setModalOpen] = useState(false);
    const [secret, setSecret] = useState<OneTimeCredential>();
    const [selectedKeys, setSelectedKeys] = useState<Key[]>([]);
    const [form] = Form.useForm();
    const queryClient = useQueryClient();
    const feedback = useOperationFeedback();
    const selected =
      selectedKeys.length === 1
        ? application.credentials?.find((credential) => credential.id === selectedKeys[0])
        : undefined;
    const createCredential = useCommandMutation({
      mutationFn: async () => {
        const values = await form.validateFields();
        return openApiPlatformApi.credentialCreate({
          applicationId: application.id,
          name: String(values.name).trim(),
          expiresAt: formatCredentialExpiresAt(values.expiresAt),
        });
      },
      onSuccess: async (credential) => {
        setModalOpen(false);
        form.resetFields();
        setSecret(credential);
        await queryClient.invalidateQueries({
          queryKey: openApiQueryKeys.applicationDetail(application.id),
        });
        feedback.success(
          application.encryptionAlgorithm === 'NONE'
            ? '凭据创建成功，请立即安全保存签名密钥'
            : '凭据创建成功，请立即安全保存三个密钥',
        );
      },
    });
    const toggleCredential = useCommandMutation({
      mutationFn: () =>
        openApiPlatformApi.credentialEnable({
          id: selected!.id,
          version: selected!.version,
          enabled: !selected!.enabled,
        }),
      onSuccess: async () => {
        setSelectedKeys([]);
        onSelectionChange(undefined);
        await queryClient.invalidateQueries({
          queryKey: openApiQueryKeys.applicationDetail(application.id),
        });
        feedback.success('凭据状态更新成功');
      },
    });
    useImperativeHandle(ref, () => ({
      create: () => setModalOpen(true),
      toggleSelected: () => selected && toggleCredential.mutate(),
    }));
    const columns: ColumnsType<OpenApiCredential> = [
      { title: '凭据名称', dataIndex: 'name', width: 180 },
      { title: 'Key ID', dataIndex: 'keyId', width: 260 },
      {
        title: '状态',
        dataIndex: 'enabled',
        width: 90,
        render: (enabled: boolean) => (
          <Tag color={enabled ? 'success' : 'default'}>{enabled ? '启用' : '停用'}</Tag>
        ),
      },
      {
        title: '过期时间',
        dataIndex: 'expiresAt',
        width: 180,
        render: (value) => value ?? '长期有效',
      },
      { title: '最近使用', dataIndex: 'lastUsedAt', render: (value) => value ?? '—' },
    ];
    return (
      <>
        <EditableDetailTable<OpenApiCredential>
          editable={editable}
          rowKey="id"
          columns={columns}
          dataSource={application.credentials ?? []}
          selectedRowKeys={selectedKeys}
          onSelectedRowKeysChange={(keys) => {
            setSelectedKeys(keys);
            onSelectionChange(
              keys.length === 1
                ? application.credentials?.find((credential) => credential.id === keys[0])
                : undefined,
            );
          }}
        />
        <AppModal
          open={modalOpen}
          title="创建凭据"
          onCancel={() => setModalOpen(false)}
          footer={
            <>
              <Button onClick={() => setModalOpen(false)}>取消</Button>
              <Button
                type="primary"
                loading={createCredential.isPending}
                onClick={() => createCredential.mutate()}
              >
                创建
              </Button>
            </>
          }
        >
          <Form form={form} layout="vertical">
            <EditFormFields fields={credentialFields} maxColumns={2} />
          </Form>
        </AppModal>
        <AppModal
          open={Boolean(secret)}
          title="一次性凭据"
          onCancel={() => setSecret(undefined)}
          footer={
            <Button type="primary" onClick={() => setSecret(undefined)}>
              我已安全保存
            </Button>
          }
        >
          <Alert type="warning" showIcon message="关闭后无法再次查看，请立即安全保存。" />
          {secret && (
            <div className="sm-openapi-secret-list">
              {[
                ['Key ID', secret.keyId],
                ['签名密钥（Base64）', secret.signingSecret],
                ...(secret.requestEncryptionKey
                  ? ([['请求加密密钥（Base64）', secret.requestEncryptionKey]] as const)
                  : []),
                ...(secret.responseEncryptionKey
                  ? ([['响应加密密钥（Base64）', secret.responseEncryptionKey]] as const)
                  : []),
              ].map(([label, value]) => (
                <div key={label}>
                  <Typography.Text type="secondary">{label}</Typography.Text>
                  <Typography.Text className="sm-openapi-secret-value" code copyable>
                    {value}
                  </Typography.Text>
                </div>
              ))}
            </div>
          )}
        </AppModal>
      </>
    );
  },
);

export default OpenApiCredentialSection;
