import { useEffect, useRef, useState } from 'react';
import { Form } from 'antd';
import type { FormInstance } from 'antd';
import type { Rule } from 'antd/es/form';
import type { ReactNode } from 'react';
import { OperationType, BillStatus } from '../types';
import type { AccessResource, PermissionAction } from '../access/access';
import { PermissionActions } from '../access/PermissionActions';
import { EditPageShell } from '../EditPageShell';
import { EditSectionCollapse } from './EditSectionCollapse';
import { useBeforeCloseGuard } from '../tab/useBeforeCloseGuard';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import type { EditPageSection } from './editPageSection';
import '../pageLayout.css';

/** 编辑字段公共属性 */
export interface EditFieldBase {
  label: string;
  dataIndex: string;
  /** antd Form 校验规则，如 [{ required: true, message: '编码不能为空' }] */
  rules?: Rule[];
  disabled?: boolean;
  /** 占位提示 */
  placeholder?: string;
  /** 是否延伸至第四列标准控件的右边缘 */
  fullWidth?: boolean;
  /** 占用的标准字段列数；控件延伸至对应列标准控件的右边缘。 */
  columnSpan?: 2 | 3;
}

/** RefSelector 字段配置 — type === 'ref-selector' 时必填 */
export interface RefSelectorFieldConfig {
  /** 选择器标识，用于隔离不同实例的查询缓存 */
  selectorKey: string | readonly unknown[];
  mode?: 'default' | 'multiple' | 'tree-table' | 'tree-table-multiple';
  modalTitle: string;
  /** 数据获取函数，传入分页/搜索参数，返回分页结果 */
  fetchFn: (params: {
    pageNum: number;
    pageSize: number;
    keyword?: string;
    parentId?: string;
  }) => Promise<{ records: Record<string, unknown>[]; total: number }>;
  /** 选中记录的文本展示，用于输入框查看与多选值拼接。 */
  displayRender: (record: Record<string, unknown>) => string;
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
  /** 是否允许拖动选择弹框，默认关闭。 */
  modalDraggable?: boolean;
  /** 是否允许调整选择弹框大小，默认关闭。 */
  modalResizable?: boolean;
  /** 树表模式：树形数据 */
  treeData?: Record<string, unknown>[];
  /** 树表模式：默认选中的根节点。 */
  defaultTreeKey?: string;
  /** 树表模式：树字段映射 */
  treeFieldNames?: { key: string; title: string; children: string };
  /** 树表模式：固定在左树下方的范围或状态控件。 */
  treeFooter?: ReactNode;
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
    | { type: 'textarea'; rows?: number }
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
  /** 页面正文卡片完全由调用方声明，EditPage 只负责统一壳层、Form 与折叠布局。 */
  sections: EditPageSection[];
  /** 从服务端数据稳定派生的初始值；引用变化代表新快照，不得内联构造等价对象。 */
  initialValues?: Record<string, unknown>;
  /** 单据状态（无状态的基础数据不传） */
  billStatus?: BillStatus;
  operationType: OperationType;
  loading?: boolean;
  saving?: boolean;
  error?: Error | null;
  onRetry?: () => void;
  /** 保存回调，接收 Form 校验通过后的字段值 */
  /** 返回 false 表示命令被用户取消，页面应保留脏状态。 */
  onSave?: (values: Record<string, unknown>) => Promise<void | boolean>;
  /** 无状态命令型表单可覆盖默认“保存”文案，例如“发送”。 */
  saveLabel?: string;
  /** 提交回调，接收 Form 校验通过后的字段值 */
  onSubmit?: (values: Record<string, unknown>) => Promise<void>;
  onExit?: () => void;
  /** 当前领域的编辑命令权限声明 */
  access?: AccessResource<{ save: string; submit?: string }>;
  /** 提交、审核、关闭等扩展业务命令 */
  headerActions?: PermissionAction[];
  /** 注册页签关闭前的脏数据检查。 */
  closeGuard?: { appNumber: string; tabKey: string };
  onValuesChange?: (
    changedValues: Record<string, unknown>,
    allValues: Record<string, unknown>,
    form: FormInstance,
  ) => void;
  /** 保存或提交前对已校验值做领域级组装，例如合并附件上传会话。 */
  transformValues?: (values: Record<string, unknown>) => Record<string, unknown>;
  /** Form 外部卡片内容的用户编辑修订号，用于统一脏数据关闭保护。 */
  dirtyRevision?: number;
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
  sections,
  initialValues,
  billStatus,
  operationType,
  loading = false,
  saving = false,
  error = null,
  onRetry,
  onSave,
  saveLabel = '保存',
  onSubmit,
  onExit,
  access,
  headerActions,
  closeGuard,
  onValuesChange,
  transformValues = (values) => values,
  dirtyRevision = 0,
}: EditPageProps) => {
  const [form] = Form.useForm();
  const feedback = useOperationFeedback();
  const revisionRef = useRef(0);
  const dirtyRef = useRef(false);
  const dirtyRevisionRef = useRef(dirtyRevision);
  const lastAppliedInitialValues = useRef<typeof initialValues>(undefined);
  const commandBlocked = useRef(Boolean(error) || loading || saving);
  const [activeCollapseKeys, setActiveCollapseKeys] = useState<string[]>(
    sections.map((section) => section.key),
  );
  const editable = isEditable(operationType, billStatus);

  useEffect(() => {
    commandBlocked.current = Boolean(error) || loading || saving;
  }, [error, loading, saving]);

  // 仅应用新的成功快照；失败/重试的状态切换不重复灌入旧值，成功新版本仍保持既有同步语义。
  useEffect(() => {
    if (!loading && !error && initialValues && initialValues !== lastAppliedInitialValues.current) {
      form.setFieldsValue(initialValues);
      lastAppliedInitialValues.current = initialValues;
    }
  }, [form, initialValues, loading, error]);

  useBeforeCloseGuard(closeGuard?.appNumber, closeGuard?.tabKey, dirtyRef);

  useEffect(() => {
    if (dirtyRevisionRef.current !== dirtyRevision) {
      dirtyRevisionRef.current = dirtyRevision;
      revisionRef.current += 1;
      dirtyRef.current = true;
    }
  }, [dirtyRevision]);

  const prepareValues = async () => {
    let values: Record<string, unknown>;
    try {
      values = await form.validateFields();
    } catch (err) {
      const firstError = (
        err as { errorFields?: Array<{ name: (string | number)[]; errors: string[] }> } | null
      )?.errorFields?.[0];
      if (firstError) {
        feedback.warning(firstError.errors[0] ?? '请检查表单中的必填项');
        form.scrollToField(firstError.name, { focus: true });
      } else {
        feedback.fromError(err, '表单校验失败，请检查输入后重试');
      }
      return undefined;
    }
    try {
      return transformValues(values);
    } catch (err) {
      // 此处尚未进入领域 Mutation，本地组装异常必须由本层负责反馈。
      feedback.fromError(err, '表单数据组装失败，请检查输入后重试');
      return undefined;
    }
  };

  const finishSave = (revision: number) => {
    if (revisionRef.current !== revision) return;
    dirtyRef.current = false;
  };

  const handleSave = async () => {
    if (!onSave || error || loading || saving) return;
    const values = await prepareValues();
    // 异步校验期间资源可能被撤权，调用命令前再次检查最新状态。
    if (!values || commandBlocked.current) return;
    const savedRevision = revisionRef.current;
    try {
      const completed = await onSave(values);
      if (completed !== false) finishSave(savedRevision);
    } catch {
      // 进入回调后由领域 Mutation 展示失败，保留脏状态，不二次提示。
    }
  };

  const handleSubmit = async () => {
    if (!onSubmit || error || loading || saving) return;
    const values = await prepareValues();
    if (!values || commandBlocked.current) return;
    const submittedRevision = revisionRef.current;
    try {
      await onSubmit(values);
      finishSave(submittedRevision);
    } catch {
      // 提交失败同样由领域 Mutation 负责，不能清除用户修改。
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
                    label: saveLabel,
                    permission: access?.permissions.save,
                    type: 'primary' as const,
                    loading: saving,
                    disabled: Boolean(error) || loading,
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
                    disabled: Boolean(error) || loading,
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
        <EditSectionCollapse
          activeKeys={activeCollapseKeys}
          onActiveKeysChange={setActiveCollapseKeys}
          items={sections.map((section) => ({
            key: section.key,
            label: section.label,
            children: section.content(editable),
            extra: (expanded: boolean) => (expanded ? section.extra?.(editable) : undefined),
          }))}
        />
      </Form>
    </EditPageShell>
  );
};

export default EditPage;
