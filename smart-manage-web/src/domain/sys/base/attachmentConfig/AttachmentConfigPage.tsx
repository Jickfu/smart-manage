import { useEffect, useState } from 'react';
import type { Key } from 'react';
import { Button, Collapse, Form, Input, InputNumber, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { FormListFieldData } from 'antd/es/form';
import { useQuery } from '@tanstack/react-query';
import { EditPageShell } from '@/domain/common/page/EditPageShell';
import { PermissionActions } from '@/domain/common/page/PermissionActions';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { useConfigDirtyGuard } from '@/domain/sys/base/configCommon/useConfigDirtyGuard';
import { attachmentConfigApi } from './api';
import { attachmentConfigAccess } from './permissions';
import type { AttachmentConfigSaveForm } from './types';
import './AttachmentConfigPage.css';

interface StringRuleTableProps {
  name: 'allowedExtensions' | 'allowedMimeTypes';
  title: string;
  columnTitle: string;
  placeholder: string;
  emptyMessage: string;
}

function StringRuleTable({
  name,
  title,
  columnTitle,
  placeholder,
  emptyMessage,
}: StringRuleTableProps) {
  const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
  return (
    <Form.List
      name={name}
      rules={[
        {
          validator: async (_rule, values: string[] | undefined) => {
            if (!values?.length) throw new Error(emptyMessage);
          },
        },
      ]}
    >
      {(fields, { add, remove }, { errors }) => {
        const columns: ColumnsType<FormListFieldData> = [
          {
            title: '序号',
            key: 'sequence',
            width: 64,
            align: 'center',
            render: (_value, _field, index) => index + 1,
          },
          {
            title: columnTitle,
            render: (_value, field) => (
              <Form.Item
                name={field.name}
                rules={[{ required: true, whitespace: true, message: `${columnTitle}不能为空` }]}
              >
                <Input variant="underlined" placeholder={placeholder} />
              </Form.Item>
            ),
          },
        ];
        return (
          <section className="sm-attachment-config-rule-table">
            <div className="sm-attachment-config-rule-toolbar">
              <span className="sm-attachment-config-rule-title">{title}</span>
              <div className="sm-attachment-config-rule-actions">
                <Button type="link" onClick={() => add('')}>
                  新增
                </Button>
                <Button
                  type="link"
                  danger
                  disabled={selectedRowKeys.length === 0}
                  onClick={() => {
                    const selectedIndexes = fields
                      .filter((field) => selectedRowKeys.includes(field.key))
                      .map((field) => field.name);
                    remove(selectedIndexes);
                    setSelectedRowKeys([]);
                  }}
                >
                  删除
                </Button>
              </div>
            </div>
            <Table
              rowKey={(field) => field.key}
              columns={columns}
              dataSource={fields}
              pagination={false}
              size="small"
              rowSelection={{
                selectedRowKeys,
                onChange: setSelectedRowKeys,
              }}
            />
            <Form.ErrorList errors={errors} />
          </section>
        );
      }}
    </Form.List>
  );
}

const AttachmentConfigPage = ({ appNumber, tabKey }: PageComponentProps) => {
  const [form] = Form.useForm<AttachmentConfigSaveForm>();
  const [dirty, setDirty] = useState(false);
  const query = useQuery({
    queryKey: ['sys', 'attachment-config', 'singleton'],
    queryFn: attachmentConfigApi.singleton,
  });
  useConfigDirtyGuard(appNumber, tabKey, dirty);
  useEffect(() => {
    if (query.data) form.setFieldsValue(query.data);
  }, [form, query.data]);
  const saveMutation = useCommandMutation({
    mutationFn: attachmentConfigApi.save,
    successMessage: '附件全局限制已保存',
    onSuccess: async () => {
      setDirty(false);
      await query.refetch();
    },
  });
  return (
    <EditPageShell
      title="附件配置"
      loading={query.isLoading}
      error={query.error as Error | null}
      onRetry={() => query.refetch()}
      actions={
        <PermissionActions
          prefix={attachmentConfigAccess.prefix}
          actions={[
            {
              key: 'save',
              label: '保存',
              type: 'primary',
              permission: attachmentConfigAccess.permissions.save,
              loading: saveMutation.isPending,
              onClick: () => form.submit(),
            },
          ]}
        />
      }
    >
      <Form
        form={form}
        layout="vertical"
        className="sm-edit-form sm-attachment-config-form"
        onFinish={(values) => saveMutation.mutateAsync(values)}
        onValuesChange={() => setDirty(true)}
      >
        <Form.Item name="id" hidden>
          <input />
        </Form.Item>
        <Form.Item name="version" hidden>
          <input />
        </Form.Item>
        <Collapse
          className="sm-edit-collapse"
          collapsible="icon"
          defaultActiveKey={['upload-limit', 'file-types']}
          items={[
            {
              key: 'upload-limit',
              label: '上传限制',
              children: (
                <div className="sm-edit-fields">
                  <Form.Item
                    className="sm-edit-field"
                    label="单文件最大大小（MB）"
                    name="maxUploadBytes"
                    getValueProps={(value) => ({
                      value: value ? value / 1024 / 1024 : undefined,
                    })}
                    normalize={(value) => Math.round(Number(value) * 1024 * 1024)}
                    rules={[{ required: true, message: '请输入单文件最大大小' }]}
                  >
                    <InputNumber
                      className="sm-edit-control-full"
                      min={1}
                      max={1024}
                      precision={0}
                      variant="underlined"
                    />
                  </Form.Item>
                  <Form.Item
                    className="sm-edit-field"
                    label="临时附件有效期（小时）"
                    name="tempExpireHours"
                    rules={[{ required: true, message: '请输入临时附件有效期' }]}
                  >
                    <InputNumber
                      className="sm-edit-control-full"
                      min={1}
                      max={168}
                      precision={0}
                      variant="underlined"
                    />
                  </Form.Item>
                </div>
              ),
            },
            {
              key: 'file-types',
              label: '文件类型白名单',
              children: (
                <div className="sm-attachment-config-rule-tables">
                  <StringRuleTable
                    name="allowedExtensions"
                    title="允许扩展名"
                    columnTitle="扩展名"
                    placeholder="例如 pdf（不带点）"
                    emptyMessage="请至少配置一个扩展名"
                  />
                  <StringRuleTable
                    name="allowedMimeTypes"
                    title="允许 MIME 类型"
                    columnTitle="MIME 类型"
                    placeholder="例如 application/pdf"
                    emptyMessage="请至少配置一个 MIME 类型"
                  />
                </div>
              ),
            },
          ]}
        />
      </Form>
    </EditPageShell>
  );
};

export default AttachmentConfigPage;
