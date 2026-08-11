import { useEffect, useRef, useState } from 'react';
import { Collapse, Form } from 'antd';
import type { FormInstance } from 'antd';
import type { Rule } from 'antd/es/form';
import type { ReactNode } from 'react';
import { OperationType, BillStatus } from './types';
import { EditFormFields } from './EditFormFields';
import type { AccessResource, PermissionAction } from './access';
import { PermissionActions } from './PermissionActions';
import { EditPageShell } from './EditPageShell';
import { useBeforeCloseGuard } from './useBeforeCloseGuard';
import { BusinessAttachmentPanel } from '@/domain/common/attachment/BusinessAttachmentPanel';
import type {
  BusinessAttachment,
  BusinessAttachmentFormValues,
} from '@/domain/common/attachment/types';
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

export interface TreeSelectFieldNode {
  value: string;
  title: string;
  children?: TreeSelectFieldNode[];
}

/** 编辑字段定义 — 按 type 分流为判别联合类型 */
export type EditField = EditFieldBase &
  (
    | { type: 'text' }
    | { type: 'password' }
    | { type: 'date' }
    | { type: 'datetime' }
    | { type: 'color' }
    | { type: 'icon-selector' }
    | { type: 'number' }
    | { type: 'switch' }
    | { type: 'textarea' }
    | { type: 'select'; options?: { label: string; value: string | number }[] }
    | {
        type: 'tree-select';
        treeData?: TreeSelectFieldNode[];
      }
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
  /** 基本信息折叠面板标题。 */
  basicLabel?: ReactNode;
  /** 明细折叠面板标题。 */
  detailLabel?: ReactNode;
  /** 自定义基本信息布局，用于头像等非标准表单。 */
  basicContent?: (editable: boolean) => ReactNode;
  /** 明细标题栏右侧操作区。 */
  detailExtra?: (editable: boolean) => ReactNode;
  /** 启用业务附件面板；附件上传、删除和表单字段组装统一由通用编辑页处理。 */
  attachmentResource?: {
    resourceType: string;
    initialAttachments?: BusinessAttachment[];
  };
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
  title,
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
  basicLabel = '基本信息',
  detailLabel = '明细信息',
  basicContent,
  detailExtra,
  attachmentResource,
  closeGuard,
  onValuesChange,
}: EditPageProps) => {
  const [form] = Form.useForm();
  const revisionRef = useRef(0);
  const dirtyRef = useRef(false);
  const [activeCollapseKeys, setActiveCollapseKeys] = useState<string[]>([
    'basic',
    ...(detailContent ? ['detail'] : []),
    ...(attachmentResource ? ['attachments'] : []),
  ]);
  const [attachmentState, setAttachmentState] = useState<{
    source: BusinessAttachment[] | undefined;
    values: BusinessAttachment[];
  }>({ source: undefined, values: [] });
  const editable = isEditable(operationType, billStatus);
  const detailExpanded = activeCollapseKeys.includes('detail');
  const attachments =
    attachmentState.source === attachmentResource?.initialAttachments
      ? attachmentState.values
      : (attachmentResource?.initialAttachments ?? []);

  const withAttachmentValues = (
    values: Record<string, unknown>,
  ): Record<string, unknown> & Partial<BusinessAttachmentFormValues> => {
    if (!attachmentResource) return values;
    return {
      ...values,
      attachmentIds: attachments.map((attachment) => attachment.id),
      attachmentUploadSessions: Object.fromEntries(
        attachments
          .filter((attachment) => attachment.isTemp && attachment.uploadSessionId)
          .map((attachment) => [attachment.id, attachment.uploadSessionId!]),
      ),
    };
  };

  const updateAttachments = (values: BusinessAttachment[], changeType: 'upload' | 'delete') => {
    if (changeType === 'upload') {
      revisionRef.current += 1;
      dirtyRef.current = true;
    }
    setAttachmentState({ source: attachmentResource?.initialAttachments, values });
  };

  // 后端数据加载完成后同步到 Form
  useEffect(() => {
    if (!loading && initialValues) {
      form.setFieldsValue(initialValues);
    }
  }, [form, initialValues, loading]);

  useBeforeCloseGuard(closeGuard?.appNumber, closeGuard?.tabKey, dirtyRef);

  const handleSave = async () => {
    if (!onSave) return;
    try {
      const values = await form.validateFields();
      const savedRevision = revisionRef.current;
      await onSave(withAttachmentValues(values));
      if (revisionRef.current === savedRevision) {
        dirtyRef.current = false;
      }
    } catch (err) {
      // 表单校验错误由 Form 展示，命令错误由领域 Mutation 统一处理。
      if ((err as { errorFields?: unknown[] }).errorFields) return;
    }
  };

  const handleSubmit = async () => {
    if (!onSubmit) return;
    try {
      const values = await form.validateFields();
      const submittedRevision = revisionRef.current;
      await onSubmit(withAttachmentValues(values));
      if (revisionRef.current === submittedRevision) {
        dirtyRef.current = false;
      }
    } catch (err) {
      if ((err as { errorFields?: unknown[] }).errorFields) return;
    }
  };

  return (
    <EditPageShell
      title={title}
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
          revisionRef.current += 1;
          dirtyRef.current = true;
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
              label: basicLabel,
              children: basicContent?.(editable) ?? (
                <EditFormFields fields={fields} editable={editable} />
              ),
            },
            ...(detailContent
              ? [
                  {
                    key: 'detail',
                    label: detailLabel,
                    children: detailContent(editable),
                    extra: detailExpanded ? detailExtra?.(editable) : undefined,
                  },
                ]
              : []),
            ...(attachmentResource
              ? [
                  {
                    key: 'attachments',
                    label: '附件',
                    children: (
                      <BusinessAttachmentPanel
                        resourceType={attachmentResource.resourceType}
                        attachments={attachments}
                        editable={editable}
                        onChange={updateAttachments}
                      />
                    ),
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
