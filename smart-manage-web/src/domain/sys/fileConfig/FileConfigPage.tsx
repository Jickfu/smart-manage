import { useEffect, useState } from 'react';
import { App, Form, Input, InputNumber, Radio, Switch } from 'antd';
import { useMutation, useQuery } from '@tanstack/react-query';
import { EditPageShell } from '@/domain/common/page/EditPageShell';
import { PermissionActions } from '@/domain/common/page/PermissionActions';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useConfigDirtyGuard } from '@/domain/sys/configCommon/useConfigDirtyGuard';
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
    });
  }, [form, query.data]);
  const storageType = Form.useWatch('storageType', form) ?? 'LOCAL';
  const saveMutation = useMutation({
    mutationFn: async () => {
      const values = await form.validateFields();
      await fileConfigApi.save({
        ...values,
        id: query.data?.id,
        version: query.data?.version,
        ftpPassword: values.ftpPassword?.trim() || undefined,
      });
      await query.refetch();
      setDirty(false);
      message.success('文件配置保存成功');
    },
    onError: (error) => message.error(error instanceof Error ? error.message : '保存失败'),
  });
  const testMutation = useMutation({
    mutationFn: async () => {
      const values = await form.validateFields([
        'ftpHost',
        'ftpPort',
        'ftpUsername',
        'ftpPassword',
        'ftpDir',
        'ftpPassiveMode',
      ]);
      if (!values.ftpPassword?.trim()) {
        throw new Error('测试连接时必须重新输入 FTP 密码');
      }
      return fileConfigApi.testFtp({
        ftpHost: values.ftpHost,
        ftpPort: values.ftpPort,
        ftpUsername: values.ftpUsername,
        ftpPassword: values.ftpPassword,
        ftpDir: values.ftpDir,
        ftpPassiveMode: values.ftpPassiveMode,
      });
    },
    onSuccess: (result) => message.success(result || 'FTP 连接成功'),
    onError: (error) => message.error(error instanceof Error ? error.message : 'FTP 连接失败'),
  });
  return (
    <EditPageShell
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
              onClick: () => saveMutation.mutate(),
            },
            ...(storageType === 'FTP'
              ? [
                  {
                    key: 'test',
                    label: '测试 FTP 连接',
                    permission: fileConfigAccess.permissions.save,
                    loading: testMutation.isPending,
                    onClick: () => testMutation.mutate(),
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
        className="sm-file-config-form"
        onValuesChange={() => setDirty(true)}
      >
        <Form.Item name="storageType" label="存储方式" rules={[{ required: true }]}>
          <Radio.Group
            options={[
              { label: '本地存储', value: 'LOCAL' },
              { label: 'FTP 存储', value: 'FTP' },
            ]}
            optionType="button"
          />
        </Form.Item>
        {storageType === 'LOCAL' ? (
          <Form.Item
            name="localDir"
            label="本地存储目录"
            rules={[{ required: true, message: '本地存储目录不能为空' }]}
          >
            <Input variant="underlined" placeholder="例如 E:/upload/" />
          </Form.Item>
        ) : (
          <div className="sm-file-config-grid">
            <Form.Item
              name="ftpHost"
              label="FTP 主机"
              rules={[{ required: true, message: 'FTP 主机不能为空' }]}
            >
              <Input variant="underlined" />
            </Form.Item>
            <Form.Item
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
              name="ftpUsername"
              label="FTP 用户名"
              rules={[{ required: true, message: 'FTP 用户名不能为空' }]}
            >
              <Input variant="underlined" autoComplete="off" />
            </Form.Item>
            <Form.Item
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
            <Form.Item name="ftpDir" label="FTP 远程目录">
              <Input variant="underlined" />
            </Form.Item>
            <Form.Item name="ftpPassiveMode" label="被动模式" valuePropName="checked">
              <Switch />
            </Form.Item>
          </div>
        )}
      </Form>
    </EditPageShell>
  );
};

export default FileConfigPage;
