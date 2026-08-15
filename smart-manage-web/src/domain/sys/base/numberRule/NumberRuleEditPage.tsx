import { useEffect, useMemo, useState } from 'react';
import { Button, Form, Input, InputNumber, Select, Space, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import EditPage from '@/domain/common/page/EditPage';
import type { EditField } from '@/domain/common/page/EditPage';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { usePermissionAccess } from '@/domain/common/page/usePermissionAccess';
import { createBillTabKey } from '@/domain/common/page/tabKeys';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
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

const ScopeSelector = ({
  references,
  disabled,
}: {
  references: NumberReference[];
  disabled?: boolean;
}) => {
  const form = Form.useFormInstance();
  const referenceKey = Form.useWatch('referenceKey', form) as string | undefined;
  const scopeType = Form.useWatch('scopeType', form) as NumberScopeType | undefined;
  const reference = references.find((item) => item.referenceKey === referenceKey);
  useEffect(() => {
    if (reference && (!scopeType || !reference.allowedScopes.includes(scopeType))) {
      form.setFieldValue('scopeType', reference.allowedScopes[0]);
    }
  }, [form, reference, scopeType]);
  return (
    <Form.Item name="scopeType" rules={[{ required: true, message: '请选择流水作用域' }]} noStyle>
      <Select
        className="sm-number-rule-scope"
        disabled={disabled}
        options={(reference?.allowedScopes ?? []).map((scope) => ({
          label: scopeLabels[scope],
          value: scope,
        }))}
      />
    </Form.Item>
  );
};

const SegmentEditor = ({ references }: { references: NumberReference[] }) => {
  const form = Form.useFormInstance();
  const referenceKey = Form.useWatch('referenceKey', form) as string | undefined;
  const reference = references.find((item) => item.referenceKey === referenceKey);
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
      {(fields, { add, remove, move }, { errors }) => {
        const columns: ColumnsType<(typeof fields)[number]> = [
          { title: '顺序', width: 54, render: (_, __, index) => index + 1 },
          {
            title: '值类型',
            width: 130,
            render: (_, field) => (
              <Form.Item name={[field.name, 'segmentType']} rules={[{ required: true }]} noStyle>
                <Select options={segmentTypeOptions} />
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
                      <Form.Item name={[field.name, 'value']} rules={[{ required: true }]} noStyle>
                        <Select placeholder="选择受控业务变量" options={variableOptions(type)} />
                      </Form.Item>
                    );
                  }
                  if (type === 'FIXED') {
                    return (
                      <Form.Item name={[field.name, 'value']} rules={[{ required: true }]} noStyle>
                        <Input maxLength={200} placeholder="固定文本" />
                      </Form.Item>
                    );
                  }
                  return <Input disabled placeholder="由流水计数器生成" />;
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
                      <Form.Item name={[field.name, 'format']} rules={[{ required: true }]} noStyle>
                        <Select
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
                      <Form.Item name={[field.name, 'length']} rules={[{ required: true }]} noStyle>
                        <InputNumber min={1} max={18} precision={0} />
                      </Form.Item>
                    );
                  }
                  return <Input disabled />;
                }}
              </Form.Item>
            ),
          },
          {
            title: '段后分隔符',
            width: 130,
            render: (_, field) => (
              <Form.Item name={[field.name, 'separator']} noStyle>
                <Input maxLength={10} placeholder="默认 -" />
              </Form.Item>
            ),
          },
          {
            title: '操作',
            width: 170,
            render: (_, field, index) => (
              <Space size="small">
                <Button disabled={index === 0} onClick={() => move(index, index - 1)}>
                  上移
                </Button>
                <Button
                  disabled={index === fields.length - 1}
                  onClick={() => move(index, index + 1)}
                >
                  下移
                </Button>
                <Button danger onClick={() => remove(field.name)}>
                  删除
                </Button>
              </Space>
            ),
          },
        ];
        return (
          <div className="sm-number-rule-segments">
            <Table
              rowKey="key"
              size="small"
              pagination={false}
              columns={columns}
              dataSource={fields}
            />
            <Space>
              <Button
                onClick={() => {
                  const currentSegments = (form.getFieldValue('segments') ?? []) as NumberRuleSegment[];
                  const sequenceIndex = currentSegments.findIndex(
                    (segment) => segment.segmentType === 'SEQUENCE',
                  );
                  const insertIndex = sequenceIndex >= 0 ? sequenceIndex : fields.length;
                  add(
                    {
                      segmentType: 'FIXED',
                      value: '',
                      separator: fields.length ? '-' : '',
                    },
                    insertIndex,
                  );
                }}
              >
                添加格式段
              </Button>
              <Form.ErrorList errors={errors} />
            </Space>
          </div>
        );
      }}
    </Form.List>
  );
};

const NumberPreview = () => {
  const form = Form.useFormInstance();
  const [preview, setPreview] = useState('');
  const { can } = usePermissionAccess(numberRuleAccess.prefix);
  const previewMutation = useMutation({
    mutationFn: async () => {
      const values = await form.validateFields(['referenceKey', 'segments', 'startValue']);
      const segments = (values.segments as NumberRuleSegment[]).map((segment, index) => ({
        ...segment,
        sort: index + 1,
        separator: segment.separator ?? '',
      }));
      return numberRuleApi.preview(
        String(values.referenceKey),
        segments,
        Number(values.startValue),
      );
    },
    onSuccess: setPreview,
  });
  return (
    <Space.Compact block>
      <Input readOnly value={preview} placeholder="点击预览，不消耗流水" />
      <Button
        disabled={!can(numberRuleAccess.permissions.preview)}
        loading={previewMutation.isPending}
        onClick={() => previewMutation.mutate()}
      >
        预览
      </Button>
    </Space.Compact>
  );
};

const NumberRuleEditPage = (props: PageComponentProps) => {
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
  const fields = useMemo<EditField[]>(
    () => [
      {
        label: '编号引用',
        dataIndex: 'referenceKey',
        type: 'select',
        disabled: Boolean(detail),
        options: references.map((reference) => ({
          label: `${reference.featureName} / ${reference.name}`,
          value: reference.referenceKey,
        })),
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
        content: <ScopeSelector references={references} disabled={detail?.systemPreset} />,
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
      {
        label: '编号格式',
        dataIndex: 'segmentsEditor',
        type: 'custom',
        fullWidth: true,
        content: <SegmentEditor references={references} />,
      },
      {
        label: '格式预览',
        dataIndex: 'preview',
        type: 'custom',
        fullWidth: true,
        content: <NumberPreview />,
      },
      { label: '描述', dataIndex: 'description', type: 'textarea', fullWidth: true },
    ],
    [detail, references],
  );
  const initialValues = useMemo(
    () => ({
      referenceKey: detail?.referenceKey ?? references[0]?.referenceKey,
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
      const segments = (values.segments as NumberRuleSegment[]).map((segment, index) => ({
        ...segment,
        sort: index + 1,
        separator: segment.separator ?? '',
      }));
      const savedId = await numberRuleApi.save({
        id: billId,
        version: detail?.version,
        referenceKey: String(values.referenceKey),
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
      fields={fields}
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
