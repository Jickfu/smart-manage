import { useEffect, useRef } from 'react';
import { Spin, Form } from 'antd';
import { RequestErrorState } from '@/domain/common/component/RequestErrorState';
import AppModal from '@/domain/common/component/AppModal';
import type { EditField } from './EditPage';
import { EditFormFields } from './EditFormFields';
import type { AccessResource } from '../access/access';
import { PermissionActions } from '../access/PermissionActions';
import '../pageLayout.css';
import './ModalEditPage.css';

interface ModalEditPageProps {
  title: string;
  open: boolean;
  onClose: () => void;
  fields: EditField[];
  /** 从服务端数据稳定派生的初始值；引用变化代表新快照。 */
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
  const lastAppliedInitialValues = useRef<typeof initialValues>(undefined);
  const commandBlocked = useRef(Boolean(error) || loading || saving || !open);

  useEffect(() => {
    commandBlocked.current = Boolean(error) || loading || saving || !open;
  }, [error, loading, saving, open]);

  // Modal 打开且数据加载完成后同步到 Form
  useEffect(() => {
    if (
      open &&
      !loading &&
      !error &&
      initialValues &&
      initialValues !== lastAppliedInitialValues.current
    ) {
      form.setFieldsValue(initialValues);
      lastAppliedInitialValues.current = initialValues;
    }
  }, [form, initialValues, loading, open, error]);

  // Modal 关闭时重置 Form（处理新增场景，避免旧数据残留）
  const handleClose = () => {
    if (saving) return;
    onClose();
  };

  const handleSave = async () => {
    if (error || loading || saving) return;
    try {
      const values = await form.validateFields();
      if (commandBlocked.current) return;
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
          // reset 后必须允许同一缓存引用再次回显。
          lastAppliedInitialValues.current = undefined;
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
              disabled: Boolean(error) || loading,
              onClick: handleSave,
            },
          ]}
        />
      }
    >
      {error && <RequestErrorState error={error} onRetry={onRetry} />}
      <div className="sm-modal-edit-content" hidden={Boolean(error)} inert={Boolean(error)}>
        <Spin spinning={loading}>
          <Form form={form} layout="vertical" className="sm-edit-form">
            <EditFormFields fields={fields} maxColumns={2} />
          </Form>
        </Spin>
      </div>
    </AppModal>
  );
};

export default ModalEditPage;
