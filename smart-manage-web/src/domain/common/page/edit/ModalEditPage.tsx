import { useEffect } from 'react';
import { Spin, Button, Result, Form } from 'antd';
import AppModal from '@/domain/common/component/AppModal';
import type { EditField } from './EditPage';
import { EditFormFields } from './EditFormFields';
import type { AccessResource } from '../access';
import { PermissionActions } from '../PermissionActions';
import '../pageLayout.css';
import './ModalEditPage.css';

interface ModalEditPageProps {
  title: string;
  open: boolean;
  onClose: () => void;
  fields: EditField[];
  /** 初始值（详情数据回显），Form 内部通过 setFieldsValue 同步 */
  initialValues?: Record<string, unknown>;
  /** 保存回调，接收 Form 校验通过后的字段值 */
  onSave: (values: Record<string, unknown>) => Promise<void>;
  loading?: boolean;
  saving?: boolean;
  error?: Error | null;
  onRetry?: () => void;
  width?: number;
  access?: AccessResource<{ save: string }>;
}

/** 通用 Modal 编辑模板 — 三段式布局：标题栏 + 可滚动字段区 + 底部按钮，使用 antd Form 驱动校验 */
const ModalEditPage = ({
  title,
  open,
  onClose,
  fields,
  initialValues,
  onSave,
  loading = false,
  saving = false,
  error = null,
  onRetry,
  width = 600,
  access,
}: ModalEditPageProps) => {
  const [form] = Form.useForm();

  // Modal 打开且数据加载完成后同步到 Form
  useEffect(() => {
    if (open && !loading && initialValues) {
      form.setFieldsValue(initialValues);
    }
  }, [form, initialValues, loading, open]);

  // Modal 关闭时重置 Form（处理新增场景，避免旧数据残留）
  const handleClose = () => {
    if (saving) return;
    onClose();
  };

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      await onSave(values);
    } catch (err) {
      // 表单校验错误由 Form 展示，命令错误由领域 Mutation 统一处理。
      if ((err as { errorFields?: unknown[] }).errorFields) return;
    }
  };

  return (
    <AppModal
      title={title}
      open={open}
      onCancel={handleClose}
      afterOpenChange={(visible) => {
        if (!visible) {
          form.resetFields();
        }
      }}
      className="sm-modal-edit"
      closeDisabled={saving}
      width={width}
      footer={
        <PermissionActions
          prefix={access?.prefix}
          actions={[
            { key: 'cancel', label: '取消', disabled: saving, onClick: onClose },
            {
              key: 'save',
              label: '保存',
              permission: access?.permissions.save,
              type: 'primary',
              loading: saving,
              onClick: handleSave,
            },
          ]}
        />
      }
    >
      {error ? (
        <Result
          status="error"
          title="加载失败"
          subTitle={error.message || '请检查网络连接后重试'}
          extra={
            onRetry && (
              <Button type="primary" onClick={onRetry}>
                重试
              </Button>
            )
          }
        />
      ) : (
        <Spin spinning={loading}>
          <Form form={form} layout="vertical" className="sm-edit-form">
            <EditFormFields fields={fields} maxColumns={2} />
          </Form>
        </Spin>
      )}
    </AppModal>
  );
};

export default ModalEditPage;
