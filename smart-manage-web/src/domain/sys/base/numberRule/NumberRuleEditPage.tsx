import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useEffect, useMemo, useRef, useState } from 'react';
import type { Key, RefObject } from 'react';
import { Button, Form, Input, InputNumber, Select } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { FormListOperation } from 'antd/es/form';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import EditPage from '@/domain/common/page/edit/EditPage';
import { editFormSection } from '@/domain/common/page/edit/editPageSection';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import { defineRefSelector } from '@/domain/common/page/edit/defineRefSelector';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { usePermissionAccess } from '@/domain/common/page/access/usePermissionAccess';
import { EditSectionActionButton } from '@/domain/common/page/edit/EditSectionActionButton';
import { createBillTabKey } from '@/domain/common/page/tab/tabKeys';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import {
  EditableDetailTable,
  RequiredDetailColumnTitle,
} from '@/domain/common/component/EditableDetailTable';
import { numberRuleApi } from './api';
import { numberRuleAccess } from './permissions';
import { numberRuleQueryKeys } from './queryKeys';
import type {
  NumberReference,
  NumberResetPeriod,
  NumberRuleSegment,
  NumberScopeType,
  NumberSegmentType,
} from './types';
import './NumberRuleEditPage.css';

const scopeLabels: Record<NumberScopeType, string> = {
  GLOBAL: '全局',
  ORG: '按组织',
  CATEGORY: '按基础资料分类',
};

const resetOptions = [
  { label: '不重置', value: 'NEVER' },
  { label: '每年', value: 'YEAR' },
  { label: '每月', value: 'MONTH' },
  { label: '每天', value: 'DAY' },
];

const segmentTypeOptions = [
  { label: '固定值', value: 'FIXED' },
  { label: '业务变量', value: 'VARIABLE' },
  { label: '日期', value: 'DATE' },
  { label: '顺序号', value: 'SEQUENCE' },
];

const ScopeSelector = ({ disabled }: { disabled?: boolean }) => {
  const form = Form.useFormInstance();
  const selectedReference = Form.useWatch('reference', form) as NumberReference | undefined;
  const scopeType = Form.useWatch('scopeType', form) as NumberScopeType | undefined;
  useEffect(() => {
    if (selectedReference && (!scopeType || !selectedReference.allowedScopes.includes(scopeType))) {
      form.setFieldValue('scopeType', selectedReference.allowedScopes[0]);
    }
  }, [form, scopeType, selectedReference]);
  return (
    <Form.Item name="scopeType" rules={[{ required: true, message: '请选择流水作用域' }]} noStyle>
      <Select
        className="sm-number-rule-scope"
        variant="underlined"
        disabled={disabled}
        options={(selectedReference?.allowedScopes ?? []).map((scope) => ({
          label: scopeLabels[scope],
          value: scope,
        }))}
      />
    </Form.Item>
  );
};

interface SegmentEditorProps {
  editable: boolean;
  selectedRowKeys: Key[];
  onSelectedRowKeysChange: (keys: Key[]) => void;
  onSelectedIndexChange: (index: number | undefined) => void;
  operationsRef: RefObject<FormListOperation | null>;
  indexByKeyRef: RefObject<Map<Key, number>>;
}

const SegmentEditor = ({
  editable,
  selectedRowKeys,
  onSelectedRowKeysChange,
  onSelectedIndexChange,
  operationsRef,
  indexByKeyRef,
}: SegmentEditorProps) => {
  const form = Form.useFormInstance();
  const reference = Form.useWatch('reference', form) as NumberReference | undefined;
  const variableOptions = (type: 'VARIABLE' | 'DATE') =>
    reference?.variables
      .filter((variable) => variable.segmentType === type)
      .map((variable) => ({ label: variable.name, value: variable.key })) ?? [];

  return (
    <Form.List
      name="segments"
      rules={[
        {
          validator: async (_, value: NumberRuleSegment[]) => {
            const sequenceCount = value?.filter(
              (segment) => segment.segmentType === 'SEQUENCE',
            ).length;
            if (!value?.length) throw new Error('请至少添加一个编号格式段');
            if (sequenceCount !== 1) throw new Error('编号格式必须且只能包含一个顺序号段');
          },
        },
      ]}
    >
      {(fields, { add, remove, move }) => {
        operationsRef.current = { add, remove, move };
        indexByKeyRef.current = new Map(fields.map((field) => [field.key, field.name]));
        const columns: ColumnsType<(typeof fields)[number]> = [
          { title: '顺序', width: 54, render: (_, __, index) => index + 1 },
          {
            title: <RequiredDetailColumnTitle>值类型</RequiredDetailColumnTitle>,
            width: 130,
            render: (_, field) => (
              <Form.Item
                name={[field.name, 'segmentType']}
                rules={[{ required: true, message: '请选择值类型' }]}
              >
                <Select variant="borderless" disabled={!editable} options={segmentTypeOptions} />
              </Form.Item>
            ),
          },
          {
            title: '值/来源',
            render: (_, field) => (
              <Form.Item noStyle shouldUpdate>
                {() => {
                  const type = form.getFieldValue(['segments', field.name, 'segmentType']) as
                    | NumberSegmentType
                    | undefined;
                  if (type === 'VARIABLE' || type === 'DATE') {
                    return (
                      <Form.Item
                        name={[field.name, 'value']}
                        rules={[{ required: true, message: '请选择值或来源' }]}
                      >
                        <Select
                          variant="borderless"
                          disabled={!editable}
                          placeholder="选择受控业务变量"
                          options={variableOptions(type)}
                        />
                      </Form.Item>
                    );
                  }
                  if (type === 'FIXED') {
                    return (
                      <Form.Item
                        name={[field.name, 'value']}
                        rules={[{ required: true, whitespace: true, message: '请输入固定值' }]}
                      >
                        <Input
                          variant="borderless"
                          disabled={!editable}
                          maxLength={200}
                          placeholder="固定文本"
                        />
                      </Form.Item>
                    );
                  }
                  return <Input variant="borderless" disabled placeholder="由流水计数器生成" />;
                }}
              </Form.Item>
            ),
          },
          {
            title: '格式/长度',
            width: 150,
            render: (_, field) => (
              <Form.Item noStyle shouldUpdate>
                {() => {
                  const type = form.getFieldValue(['segments', field.name, 'segmentType']) as
                    | NumberSegmentType
                    | undefined;
                  if (type === 'DATE') {
                    return (
                      <Form.Item
                        name={[field.name, 'format']}
                        rules={[{ required: true, message: '请选择日期格式' }]}
                      >
                        <Select
                          variant="borderless"
                          disabled={!editable}
                          options={[
                            { label: 'yyyy', value: 'yyyy' },
                            { label: 'yyyyMM', value: 'yyyyMM' },
                            { label: 'yyyyMMdd', value: 'yyyyMMdd' },
                          ]}
                        />
                      </Form.Item>
                    );
                  }
                  if (type === 'SEQUENCE') {
                    return (
                      <Form.Item
                        name={[field.name, 'length']}
                        rules={[{ required: true, message: '请输入流水号位数' }]}
                      >
                        <InputNumber
                          variant="borderless"
                          disabled={!editable}
                          min={1}
                          max={18}
                          precision={0}
                        />
                      </Form.Item>
                    );
                  }
                  return <Input variant="borderless" disabled />;
                }}
              </Form.Item>
            ),
          },
          {
            title: '段后分隔符',
            width: 130,
            render: (_, field) => (
              <Form.Item name={[field.name, 'separator']}>
                <Input
                  variant="borderless"
                  disabled={!editable}
                  maxLength={10}
                  placeholder="默认 -"
                />
              </Form.Item>
            ),
          },
        ];
        return (
          <div className="sm-number-rule-segments">
            <EditableDetailTable
              editable={editable}
              showIndexColumn={false}
              rowKey="key"
              columns={columns}
              dataSource={fields}
              tableLayout="fixed"
              scroll={{ x: 800 }}
              selectedRowKeys={selectedRowKeys}
              onSelectedRowKeysChange={(keys) => {
                onSelectedRowKeysChange(keys);
                onSelectedIndexChange(
                  keys.length === 1 ? indexByKeyRef.current.get(keys[0]!) : undefined,
                );
              }}
            />
            <NumberPreview />
          </div>
        );
      }}
    </Form.List>
  );
};

interface SegmentActionsProps {
  selectedRowKeys: Key[];
  selectedIndex: number | undefined;
  onSelectedRowKeysChange: (keys: Key[]) => void;
  onSelectedIndexChange: (index: number | undefined) => void;
  operationsRef: RefObject<FormListOperation | null>;
  indexByKeyRef: RefObject<Map<Key, number>>;
}

const SegmentActions = ({
  selectedRowKeys,
  selectedIndex,
  onSelectedRowKeysChange,
  onSelectedIndexChange,
  operationsRef,
  indexByKeyRef,
}: SegmentActionsProps) => {
  const form = Form.useFormInstance();
  const segments = Form.useWatch('segments', form) as NumberRuleSegment[] | undefined;
  const segmentCount = segments?.length ?? 0;
  return (
    <div className="sm-number-rule-segment-actions">
      <EditSectionActionButton
        onClick={() => {
          const currentSegments = (form.getFieldValue('segments') ?? []) as NumberRuleSegment[];
          const sequenceIndex = currentSegments.findIndex(
            (segment) => segment.segmentType === 'SEQUENCE',
          );
          operationsRef.current?.add(
            {
              segmentType: 'FIXED',
              value: '',
              separator: currentSegments.length ? '-' : '',
            },
            sequenceIndex >= 0 ? sequenceIndex : currentSegments.length,
          );
        }}
      >
        添加格式段
      </EditSectionActionButton>
      <EditSectionActionButton
        disabled={selectedIndex === undefined || selectedIndex === 0}
        onClick={() => {
          if (selectedIndex === undefined) return;
          operationsRef.current?.move(selectedIndex, selectedIndex - 1);
          onSelectedIndexChange(selectedIndex - 1);
        }}
      >
        上移
      </EditSectionActionButton>
      <EditSectionActionButton
        disabled={selectedIndex === undefined || selectedIndex === segmentCount - 1}
        onClick={() => {
          if (selectedIndex === undefined) return;
          operationsRef.current?.move(selectedIndex, selectedIndex + 1);
          onSelectedIndexChange(selectedIndex + 1);
        }}
      >
        下移
      </EditSectionActionButton>
      <EditSectionActionButton
        danger
        disabled={selectedRowKeys.length === 0}
        onClick={() => {
          const selectedIndexes = selectedRowKeys
            .map((key) => indexByKeyRef.current.get(key))
            .filter((index): index is number => index !== undefined);
          operationsRef.current?.remove(selectedIndexes);
          onSelectedRowKeysChange([]);
          onSelectedIndexChange(undefined);
        }}
      >
        删除
      </EditSectionActionButton>
    </div>
  );
};

const NumberPreview = () => {
  const form = Form.useFormInstance();
  const feedback = useOperationFeedback();
  const [preview, setPreview] = useState('');
  const { can } = usePermissionAccess(numberRuleAccess.prefix);
  const previewMutation = useMutation({
    mutationFn: async () => {
      const values = await form.validateFields(['reference', 'segments', 'startValue']);
      const reference = values.reference as NumberReference;
      const segments = (values.segments as NumberRuleSegment[]).map((segment, index) => ({
        ...segment,
        sort: index + 1,
        separator: segment.separator ?? '',
      }));
      return numberRuleApi.preview(reference.referenceKey, segments, Number(values.startValue));
    },
    onSuccess: setPreview,
    onError: (error) => {
      const validationError = error as {
        errorFields?: { name: (string | number)[]; errors: string[] }[];
      };
      const firstError = validationError.errorFields?.[0];
      if (!firstError) return;
      void feedback.warning(firstError.errors[0] || '请完善编号格式后再生成预览');
      form.scrollToField(firstError.name, { focus: true });
    },
  });
  return (
    <div className="sm-number-rule-preview">
      <div className="sm-number-rule-preview-content">
        <span className="sm-number-rule-preview-label">格式预览</span>
        <code className={preview ? '' : 'sm-number-rule-preview-placeholder'}>
          {preview || '点击右侧按钮生成示例编号'}
        </code>
        <span className="sm-number-rule-preview-hint">仅模拟生成，不消耗实际流水号</span>
      </div>
      <Button
        type="primary"
        disabled={!can(numberRuleAccess.permissions.preview)}
        loading={previewMutation.isPending}
        onClick={() => previewMutation.mutate()}
      >
        生成预览
      </Button>
    </div>
  );
};

const NumberRuleEditPage = (props: PageComponentProps) => {
  const [selectedSegmentKeys, setSelectedSegmentKeys] = useState<Key[]>([]);
  const [selectedSegmentIndex, setSelectedSegmentIndex] = useState<number>();
  const segmentOperationsRef = useRef<FormListOperation | null>(null);
  const segmentIndexByKeyRef = useRef(new Map<Key, number>());
  const queryClient = useQueryClient();
  const { appNumber, tabKey, operationType, billId } = props;
  const isAddNew = operationType === OperationType.ADDNEW;
  const replaceContentTab = useWorkbenchStore((state) => state.replaceContentTab);
  const activateContentTab = useWorkbenchStore((state) => state.activateContentTab);
  const detailQuery = useQuery({
    queryKey: numberRuleQueryKeys.detail(billId),
    queryFn: () => numberRuleApi.detail(billId!),
    enabled: Boolean(billId),
  });
  const referencesQuery = useQuery({
    queryKey: numberRuleQueryKeys.references(),
    queryFn: numberRuleApi.references,
  });
  const detail = detailQuery.data;
  const references = useMemo(() => referencesQuery.data ?? [], [referencesQuery.data]);
  const referenceSelector = useMemo(
    () =>
      defineRefSelector<NumberReference>({
        selectorKey: numberRuleQueryKeys.references(),
        modalTitle: '选择编号引用',
        fieldNames: { key: 'referenceKey', label: 'name' },
        displayRender: (reference) => `${reference.featureName} / ${reference.name}`,
        columns: [
          { title: '所属领域', dataIndex: 'domainName', width: 140 },
          { title: '所属应用', dataIndex: 'appName', width: 160 },
          { title: '功能', dataIndex: 'featureName', width: 180 },
          { title: '编号引用', dataIndex: 'name' },
        ],
        fetchFn: async ({ pageNum, pageSize, keyword }) => {
          const normalizedKeyword = keyword?.trim().toLowerCase();
          const matchedReferences = normalizedKeyword
            ? references.filter((reference) =>
                [
                  reference.referenceKey,
                  reference.name,
                  reference.featureName,
                  reference.appName,
                  reference.domainName,
                ].some((value) => value.toLowerCase().includes(normalizedKeyword)),
              )
            : references;
          const startIndex = (pageNum - 1) * pageSize;
          return {
            records: matchedReferences.slice(startIndex, startIndex + pageSize),
            total: matchedReferences.length,
          };
        },
      }),
    [references],
  );
  const fields = useMemo<EditField[]>(
    () => [
      {
        label: '编号引用',
        dataIndex: 'reference',
        type: 'ref-selector',
        disabled: Boolean(detail),
        placeholder: '请选择编号引用',
        refSelector: referenceSelector,
        rules: [{ required: true, message: '请选择编号引用' }],
      },
      {
        label: '规则键',
        dataIndex: 'ruleKey',
        type: 'text',
        disabled: Boolean(detail),
        rules: [{ required: true, whitespace: true, message: '规则键不能为空' }],
      },
      {
        label: '名称',
        dataIndex: 'name',
        type: 'text',
        rules: [{ required: true, whitespace: true, message: '名称不能为空' }],
      },
      {
        label: '流水作用域',
        dataIndex: 'scopeTypeEditor',
        type: 'custom',
        content: <ScopeSelector disabled={detail?.systemPreset} />,
      },
      {
        label: '重置周期',
        dataIndex: 'resetPeriod',
        type: 'select',
        options: resetOptions,
        rules: [{ required: true, message: '请选择重置周期' }],
      },
      {
        label: '起始流水值',
        dataIndex: 'startValue',
        type: 'number',
        rules: [{ required: true, type: 'number', min: 1, message: '起始流水值不能小于1' }],
      },
      { label: '描述', dataIndex: 'description', type: 'textarea', fullWidth: true },
    ],
    [detail, referenceSelector],
  );
  const initialValues = useMemo(
    () => ({
      reference:
        references.find((reference) => reference.referenceKey === detail?.referenceKey) ??
        references[0],
      ruleKey: detail?.ruleKey ?? '',
      name: detail?.name ?? '',
      scopeType: detail?.scopeType ?? references[0]?.allowedScopes[0] ?? 'GLOBAL',
      resetPeriod: detail?.resetPeriod ?? 'NEVER',
      startValue: detail?.startValue ?? 1,
      segments: detail?.segments ?? [
        { sort: 1, segmentType: 'FIXED', value: '', separator: '-' },
        { sort: 2, segmentType: 'SEQUENCE', length: 5, separator: '' },
      ],
      description: detail?.description ?? '',
    }),
    [detail, references],
  );
  const saveMutation = useCommandMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const reference = values.reference as NumberReference;
      const segments = (values.segments as NumberRuleSegment[]).map((segment, index) => ({
        ...segment,
        sort: index + 1,
        separator: segment.separator ?? '',
      }));
      const savedId = await numberRuleApi.save({
        id: billId,
        version: detail?.version,
        referenceKey: reference.referenceKey,
        ruleKey: String(values.ruleKey).trim(),
        name: String(values.name).trim(),
        scopeType: values.scopeType as NumberScopeType,
        resetPeriod: values.resetPeriod as NumberResetPeriod,
        startValue: Number(values.startValue),
        segments,
        description: values.description ? String(values.description).trim() : undefined,
      });
      if (isAddNew) {
        const nextKey = createBillTabKey(props.componentKey, savedId);
        replaceContentTab(appNumber, tabKey, {
          key: nextKey,
          closable: true,
          componentKey: props.componentKey,
          pageType: 'EDIT',
          operationType: OperationType.EDIT,
          billId: savedId,
        });
        activateContentTab(appNumber, nextKey);
      }
      await queryClient.invalidateQueries({ queryKey: numberRuleQueryKeys.all });
    },
    successMessage: isAddNew ? '新增成功' : '保存成功',
  });
  return (
    <EditPage
      access={numberRuleAccess}
      title="编号规则"
      sections={[
        editFormSection('basic', '基本信息', fields),
        {
          key: 'segments',
          label: '编号格式',
          content: (editable) => (
            <SegmentEditor
              editable={editable}
              selectedRowKeys={selectedSegmentKeys}
              onSelectedRowKeysChange={setSelectedSegmentKeys}
              onSelectedIndexChange={setSelectedSegmentIndex}
              operationsRef={segmentOperationsRef}
              indexByKeyRef={segmentIndexByKeyRef}
            />
          ),
          extra: (editable) =>
            editable ? (
              <SegmentActions
                selectedRowKeys={selectedSegmentKeys}
                selectedIndex={selectedSegmentIndex}
                onSelectedRowKeysChange={setSelectedSegmentKeys}
                onSelectedIndexChange={setSelectedSegmentIndex}
                operationsRef={segmentOperationsRef}
                indexByKeyRef={segmentIndexByKeyRef}
              />
            ) : null,
        },
      ]}
      initialValues={initialValues}
      operationType={operationType ?? OperationType.EDIT}
      closeGuard={{ appNumber, tabKey }}
      loading={detailQuery.isLoading || referencesQuery.isLoading}
      error={(detailQuery.error ?? referencesQuery.error) as Error | null}
      onRetry={() => Promise.all([detailQuery.refetch(), referencesQuery.refetch()])}
      onSave={saveMutation.mutateAsync}
      saving={saveMutation.isPending}
      onExit={() => useWorkbenchStore.getState().removeContentTab(appNumber, tabKey)}
    />
  );
};

export default NumberRuleEditPage;
