import { useEffect, useState } from 'react';
import { App, Collapse, Form, Input, InputNumber, Select, Switch } from 'antd';
import { useMutation, useQuery } from '@tanstack/react-query';
import { EditPageShell } from '@/domain/common/page/EditPageShell';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { PermissionActions } from '@/domain/common/page/PermissionActions';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useConfigDirtyGuard } from '@/domain/sys/base/configCommon/useConfigDirtyGuard';
import { fileConfigApi } from './api';
import { fileConfigAccess } from './permissions';
import type { FileConfigSaveForm } from './types';
import './FileConfigPage.css';

const FileConfigPage = ({ appNumber, tabKey }: PageComponentProps) => {
  const { message } = App.useApp();
  const [form] = Form.useForm<FileConfigSaveForm>();
  const [dirty, setDirty] = useState(false);
  const query = useQuery({
    queryKey: ['sys', 'file-config', 'singleton'],
    queryFn: fileConfigApi.singleton,
  });
  useConfigDirtyGuard(appNumber, tabKey, dirty);
  useEffect(() => {
    if (!query.data) return;
    form.setFieldsValue({
      storageType: query.data.storageType,
      localDir: query.data.localDir,
      ftpHost: query.data.ftpHost,
      ftpPort: query.data.ftpPort ?? 21,
      ftpUsername: query.data.ftpUsername,
      ftpPassword: '',
      ftpDir: query.data.ftpDir,
      ftpPassiveMode: query.data.ftpPassiveMode ?? true,
      s3Endpoint: query.data.s3Endpoint,
      s3Region: query.data.s3Region ?? 'us-east-1',
      s3Bucket: query.data.s3Bucket,
      s3AccessKey: query.data.s3AccessKey,
      s3SecretKey: '',
      s3PathStyle: query.data.s3PathStyle ?? true,
    });
  }, [form, query.data]);
  const storageType = Form.useWatch('storageType', form) ?? 'LOCAL';
  const saveMutation = useCommandMutation({
    mutationFn: async (values: FileConfigSaveForm) => {
      await fileConfigApi.save({
        ...values,
        id: query.data?.id,
        version: query.data?.version,
        ftpPassword: values.ftpPassword?.trim() || undefined,
        s3SecretKey: values.s3SecretKey?.trim() || undefined,
      });
      await query.refetch();
      setDirty(false);
      message.success('文件配置保存成功');
    },
  });
  const testMutation = useMutation({
    mutationFn: (values: FileConfigSaveForm) =>
      fileConfigApi.testFtp({
        ftpHost: values.ftpHost,
        ftpPort: values.ftpPort,
        ftpUsername: values.ftpUsername,
        ftpPassword: values.ftpPassword,
        ftpDir: values.ftpDir,
        ftpPassiveMode: values.ftpPassiveMode,
      }),
    onSuccess: (result) => message.success(result || 'FTP 连接成功'),
    onError: (error) => message.error(error instanceof Error ? error.message : 'FTP 连接失败'),
  });
  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      saveMutation.mutate(values);
    } catch (error) {
      // 表单校验错误由字段自身展示，校验通过前不得进入后端请求的加载状态。
      if (!(error as { errorFields?: unknown[] }).errorFields) {
        message.error(error instanceof Error ? error.message : '表单校验失败');
      }
    }
  };
  const handleTestFtp = async () => {
    try {
      const values = await form.validateFields([
        'ftpHost',
        'ftpPort',
        'ftpUsername',
        'ftpPassword',
        'ftpDir',
        'ftpPassiveMode',
      ]);
      if (!values.ftpPassword?.trim()) {
        form.setFields([
          {
            name: 'ftpPassword',
            errors: ['测试连接时必须重新输入 FTP 密码'],
          },
        ]);
        return;
      }
      testMutation.mutate(values);
    } catch (error) {
      if (!(error as { errorFields?: unknown[] }).errorFields) {
        message.error(error instanceof Error ? error.message : '表单校验失败');
      }
    }
  };
  return (
    <EditPageShell
      title="文件配置"
      loading={query.isLoading}
      error={query.error as Error | null}
      onRetry={() => query.refetch()}
      actions={
        <PermissionActions
          prefix={fileConfigAccess.prefix}
          actions={[
            {
              key: 'save',
              label: '保存',
              permission: fileConfigAccess.permissions.save,
              type: 'primary',
              loading: saveMutation.isPending,
              onClick: handleSave,
            },
            ...(storageType === 'FTP'
              ? [
                  {
                    key: 'test',
                    label: '测试 FTP 连接',
                    permission: fileConfigAccess.permissions.save,
                    loading: testMutation.isPending,
                    onClick: handleTestFtp,
                  },
                ]
              : []),
          ]}
        />
      }
    >
      <Form
        form={form}
        layout="vertical"
        className="sm-edit-form sm-file-config-form"
        onValuesChange={() => setDirty(true)}
      >
        <Collapse
          className="sm-edit-collapse"
          collapsible="icon"
          defaultActiveKey={['storage']}
          items={[
            {
              key: 'storage',
              label: '存储设置',
              children: (
                <>
                  <div className="sm-edit-fields">
                    <Form.Item
                      className="sm-edit-field"
                      name="storageType"
                      label="当前存储类型"
                      extra="保存后，系统文件将使用该存储类型"
                      rules={[{ required: true, message: '请选择存储类型' }]}
                    >
                      <Select
                        className="sm-file-config-full"
                        variant="underlined"
                        placeholder="请选择存储类型"
                        showSearch={false}
                        options={[
                          { label: '本地存储', value: 'LOCAL' },
                          { label: 'FTP 存储', value: 'FTP' },
                          { label: 'S3 / MinIO', value: 'S3' },
                        ]}
                      />
                    </Form.Item>
                    {storageType === 'LOCAL' ? (
                      <Form.Item
                        className="sm-edit-field"
                        name="localDir"
                        label="本地存储目录"
                        rules={[{ required: true, message: '本地存储目录不能为空' }]}
                      >
                        <Input variant="underlined" placeholder="例如 /data/smart-manage/upload/" />
                      </Form.Item>
                    ) : storageType === 'FTP' ? (
                      <>
                        <Form.Item
                          className="sm-edit-field"
                          name="ftpHost"
                          label="FTP 主机"
                          rules={[{ required: true, message: 'FTP 主机不能为空' }]}
                        >
                          <Input variant="underlined" />
                        </Form.Item>
                        <Form.Item
                          className="sm-edit-field"
                          name="ftpPort"
                          label="FTP 端口"
                          rules={[{ required: true, message: 'FTP 端口不能为空' }]}
                        >
                          <InputNumber
                            className="sm-file-config-full"
                            min={1}
                            max={65535}
                            variant="underlined"
                          />
                        </Form.Item>
                        <Form.Item
                          className="sm-edit-field"
                          name="ftpUsername"
                          label="FTP 用户名"
                          rules={[{ required: true, message: 'FTP 用户名不能为空' }]}
                        >
                          <Input variant="underlined" autoComplete="off" />
                        </Form.Item>
                        <Form.Item
                          className="sm-edit-field"
                          name="ftpPassword"
                          label="FTP 密码"
                          extra={
                            query.data?.ftpPasswordConfigured
                              ? '密码已配置；留空保存时保留原密码，测试连接时必须重新输入'
                              : '尚未配置密码'
                          }
                          rules={[
                            {
                              validator: (_, value) =>
                                query.data?.ftpPasswordConfigured || value
                                  ? Promise.resolve()
                                  : Promise.reject(new Error('FTP 密码不能为空')),
                            },
                          ]}
                        >
                          <Input.Password variant="underlined" autoComplete="new-password" />
                        </Form.Item>
                        <Form.Item className="sm-edit-field" name="ftpDir" label="FTP 远程目录">
                          <Input variant="underlined" />
                        </Form.Item>
                        <Form.Item
                          className="sm-edit-field"
                          name="ftpPassiveMode"
                          label="被动模式"
                          valuePropName="checked"
                        >
                          <Switch />
                        </Form.Item>
                      </>
                    ) : (
                      <>
                        <Form.Item
                          className="sm-edit-field"
                          name="s3Endpoint"
                          label="S3 Endpoint"
                          rules={[{ required: true, message: 'S3 Endpoint 不能为空' }]}
                        >
                          <Input
                            variant="underlined"
                            placeholder="例如 https://minio.example.com"
                          />
                        </Form.Item>
                        <Form.Item
                          className="sm-edit-field"
                          name="s3Region"
                          label="S3 Region"
                          rules={[{ required: true, message: 'S3 Region 不能为空' }]}
                        >
                          <Input variant="underlined" />
                        </Form.Item>
                        <Form.Item
                          className="sm-edit-field"
                          name="s3Bucket"
                          label="私有 Bucket"
                          rules={[{ required: true, message: 'Bucket 不能为空' }]}
                        >
                          <Input variant="underlined" />
                        </Form.Item>
                        <Form.Item
                          className="sm-edit-field"
                          name="s3AccessKey"
                          label="Access Key"
                          rules={[{ required: true, message: 'Access Key 不能为空' }]}
                        >
                          <Input variant="underlined" autoComplete="off" />
                        </Form.Item>
                        <Form.Item
                          className="sm-edit-field"
                          name="s3SecretKey"
                          label="Secret Key"
                          extra={
                            query.data?.s3SecretKeyConfigured
                              ? '密钥已配置；留空保存时保留原密钥'
                              : '尚未配置密钥'
                          }
                          rules={[
                            {
                              validator: (_, value) =>
                                query.data?.s3SecretKeyConfigured || value
                                  ? Promise.resolve()
                                  : Promise.reject(new Error('Secret Key 不能为空')),
                            },
                          ]}
                        >
                          <Input.Password variant="underlined" autoComplete="new-password" />
                        </Form.Item>
                        <Form.Item
                          className="sm-edit-field"
                          name="s3PathStyle"
                          label="Path Style（MinIO 通常启用）"
                          valuePropName="checked"
                        >
                          <Switch />
                        </Form.Item>
                      </>
                    )}
                  </div>
                </>
              ),
            },
          ]}
        />
      </Form>
    </EditPageShell>
  );
};

export default FileConfigPage;
