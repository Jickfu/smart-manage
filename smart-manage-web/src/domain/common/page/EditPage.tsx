import { useEffect, useState } from 'react';
import { App, Collapse, Form } from 'antd';
import type { FormInstance } from 'antd';
import type { Rule } from 'antd/es/form';
import type { ReactNode } from 'react';
import { OperationType, BillStatus } from './types';
import { EditFormFields } from './EditFormFields';
import type { AccessResource, PermissionAction } from './access';
import { PermissionActions } from './PermissionActions';
import { EditPageShell } from './EditPageShell';
import { useWorkbenchStore } from '@/stores/workbench';
import './EditPage.css';

/** 编辑字段公共属性 */
export interface EditFieldBase {
  label: string;
  dataIndex: string;
  /** antd Form 校验规则，如 [{ required: true, message: '编码不能为空' }] */
  rules?: Rule[];
  disabled?: boolean;
  /** 占位提示 */
  placeholder?: string;
  /** 是否占满整行 */
  fullWidth?: boolean;
  /** 横跨的标准字段列数；适用于类名等较长但不需要占满整行的内容。 */
  columnSpan?: 2;
}

/** RefSelector 字段配置 — type === 'ref-selector' 时必填 */
export interface RefSelectorFieldConfig {
  /** 选择器标识，用于隔离不同实例的查询缓存 */
  selectorKey: string | readonly unknown[];
  mode?: 'default' | 'multiple' | 'tree-table';
  modalTitle: string;
  /** 数据获取函数，传入分页/搜索参数，返回分页结果 */
  fetchFn: (params: {
    pageNum: number;
    pageSize: number;
    keyword?: string;
    parentId?: string;
  }) => Promise<{ records: Record<string, unknown>[]; total: number }>;
  /** 选中记录的展示渲染 */
  displayRender: (record: Record<string, unknown>) => ReactNode;
  /** 字段名映射 */
  fieldNames: { key: string; label: string };
  /** 表格列定义 */
  columns: {
    title: string;
    dataIndex: string;
    width?: number | string;
    render?: (text: unknown, record: Record<string, unknown>, index: number) => ReactNode;
  }[];
  /** 每页条数，默认 20 */
  pageSize?: number;
  /** 树表模式：树形数据 */
  treeData?: Record<string, unknown>[];
  /** 树表模式：树字段映射 */
  treeFieldNames?: { key: string; title: string; children: string };
}

/** 编辑字段定义 — 按 type 分流为判别联合类型 */
export type EditField = EditFieldBase &
  (
    | { type: 'text' }
    | { type: 'password' }
    | { type: 'date' }
    | { type: 'datetime' }
    | { type: 'number' }
    | { type: 'switch' }
    | { type: 'textarea' }
    | { type: 'select'; options?: { label: string; value: string | number }[] }
    | { type: 'custom'; content: ReactNode }
    | { type: 'readonly' }
    | { type: 'ref-selector'; refSelector: RefSelectorFieldConfig }
  );

interface EditPageProps {
  title: string;
  fields: EditField[];
  /** 初始值（详情数据回显），Form 内部通过 setFieldsValue 同步 */
  initialValues?: Record<string, unknown>;
  /** 单据状态（无状态的基础数据不传） */
  billStatus?: BillStatus;
  operationType: OperationType;
  loading?: boolean;
  saving?: boolean;
  error?: Error | null;
  onRetry?: () => void;
  /** 保存回调，接收 Form 校验通过后的字段值 */
  onSave?: (values: Record<string, unknown>) => Promise<void>;
  /** 提交回调，接收 Form 校验通过后的字段值 */
  onSubmit?: (values: Record<string, unknown>) => Promise<void>;
  onExit?: () => void;
  /** 当前领域的编辑命令权限声明 */
  access?: AccessResource<{ save: string; submit?: string }>;
  /** 提交、审核、关闭等扩展业务命令 */
  headerActions?: PermissionAction[];
  /** 额外的聚合内容，仍处于同一个 Form 中，例如主从单据明细。 */
  detailContent?: (editable: boolean) => ReactNode;
  /** 明细标题栏右侧操作区。 */
  detailExtra?: (editable: boolean) => ReactNode;
  /** 注册页签关闭前的脏数据检查。 */
  closeGuard?: { appNumber: string; tabKey: string };
  onValuesChange?: (
    changedValues: Record<string, unknown>,
    allValues: Record<string, unknown>,
    form: FormInstance,
  ) => void;
}

/** 是否可编辑：暂存或新增时允许编辑 */
function isEditable(opType: OperationType, status?: BillStatus): boolean {
  if (opType === OperationType.VIEW) return false;
  if (opType === OperationType.ADDNEW) return true;
  return status === BillStatus.SAVED || status === undefined;
}

/** 通用编辑页 — 使用 antd Form 驱动校验与字段状态 */
const EditPage = ({
  fields,
  initialValues,
  billStatus,
  operationType,
  loading = false,
  saving = false,
  error = null,
  onRetry,
  onSave,
  onSubmit,
  onExit,
  access,
  headerActions,
  detailContent,
  detailExtra,
  closeGuard,
  onValuesChange,
}: EditPageProps) => {
  const { modal } = App.useApp();
  const [form] = Form.useForm();
  const [dirty, setDirty] = useState(false);
  const [activeCollapseKeys, setActiveCollapseKeys] = useState<string[]>(
    detailContent ? ['basic', 'detail'] : ['basic'],
  );
  const editable = isEditable(operationType, billStatus);
  const detailExpanded = activeCollapseKeys.includes('detail');

  // 后端数据加载完成后同步到 Form
  useEffect(() => {
    if (!loading && initialValues) {
      form.setFieldsValue(initialValues);
    }
  }, [form, initialValues, loading]);

  useEffect(() => {
    if (!closeGuard) return;
    const store = useWorkbenchStore.getState();
    store.registerBeforeClose(closeGuard.appNumber, closeGuard.tabKey, async () => {
      if (!dirty) return true;
      return new Promise<boolean>((resolve) => {
        modal.confirm({
          title: '存在未保存的修改',
          content: '关闭页面将丢失当前修改，是否继续？',
          okText: '继续关闭',
          cancelText: '留在页面',
          onOk: () => resolve(true),
          onCancel: () => resolve(false),
        });
      });
    });
    return () => store.unregisterBeforeClose(closeGuard.appNumber, closeGuard.tabKey);
  }, [closeGuard, dirty, modal]);

  const handleSave = async () => {
    if (!onSave) return;
    try {
      const values = await form.validateFields();
      await onSave(values);
      setDirty(false);
    } catch (err) {
      // 表单校验错误由 Form 展示，命令错误由领域 Mutation 统一处理。
      if ((err as { errorFields?: unknown[] }).errorFields) return;
    }
  };

  const handleSubmit = async () => {
    if (!onSubmit) return;
    try {
      const values = await form.validateFields();
      await onSubmit(values);
      setDirty(false);
    } catch (err) {
      if ((err as { errorFields?: unknown[] }).errorFields) return;
    }
  };

  return (
    <EditPageShell
      loading={loading}
      error={error}
      onRetry={onRetry}
      actions={
        <PermissionActions
          prefix={access?.prefix}
          actions={[
            ...(editable && onSave
              ? [
                  {
                    key: 'save',
                    label: '保存',
                    permission: access?.permissions.save,
                    type: 'primary' as const,
                    loading: saving,
                    onClick: handleSave,
                  },
                ]
              : []),
            ...(editable && onSubmit
              ? [
                  {
                    key: 'submit',
                    label: '提交',
                    permission: access?.permissions.submit,
                    type: 'primary' as const,
                    loading: saving,
                    onClick: handleSubmit,
                  },
                ]
              : []),
            ...(headerActions ?? []),
            ...(onExit ? [{ key: 'exit', label: '退出', onClick: onExit }] : []),
          ]}
        />
      }
    >
      <Form
        form={form}
        layout="vertical"
        className={`sm-edit-form${editable ? '' : ' sm-edit-form--view'}`}
        onValuesChange={(changedValues, allValues) => {
          setDirty(true);
          onValuesChange?.(changedValues, allValues, form);
        }}
      >
        <Collapse
          className="sm-edit-collapse"
          collapsible="icon"
          activeKey={activeCollapseKeys}
          onChange={(keys) => setActiveCollapseKeys(Array.isArray(keys) ? keys : [keys])}
          items={[
            {
              key: 'basic',
              label: '基本信息',
              children: <EditFormFields fields={fields} editable={editable} />,
            },
            ...(detailContent
              ? [
                  {
                    key: 'detail',
                    label: '明细信息',
                    children: detailContent(editable),
                    extra: detailExpanded ? detailExtra?.(editable) : undefined,
                  },
                ]
              : []),
          ]}
        />
      </Form>
    </EditPageShell>
  );
};

export default EditPage;
