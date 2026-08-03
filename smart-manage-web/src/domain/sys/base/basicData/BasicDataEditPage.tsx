import { useMemo, useRef, useState } from 'react';
import type { Key } from 'react';
import { Button, Form, Input, InputNumber, Switch, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { FormListFieldData, FormListOperation } from 'antd/es/form';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import EditPage from '@/domain/common/page/EditPage';
import type { EditField } from '@/domain/common/page/EditPage';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { basicDataApi } from './api';
import { basicDataAccess } from './permissions';
import { basicDataQueryKeys } from './queryKeys';
import type {
  BasicDataCreateNewDataVO,
  BasicDataDetailVO,
  BasicDataEntry,
  BasicDataSaveForm,
} from './types';
import './BasicDataEditPage.css';

const fields: EditField[] = [
  {
    label: '编码',
    dataIndex: 'number',
    type: 'text',
    rules: [
      { required: true, whitespace: true, message: '编码不能为空' },
      { max: 64, message: '编码不能超过64个字符' },
    ],
  },
  {
    label: '名称',
    dataIndex: 'name',
    type: 'text',
    rules: [
      { required: true, whitespace: true, message: '名称不能为空' },
      { max: 128, message: '名称不能超过128个字符' },
    ],
  },
  { label: '备注', dataIndex: 'remark', type: 'textarea', fullWidth: true },
  { label: '创建时间', dataIndex: 'createTime', type: 'readonly' },
  { label: '更新时间', dataIndex: 'updateTime', type: 'readonly' },
];

function isDetail(
  source: BasicDataDetailVO | BasicDataCreateNewDataVO,
): source is BasicDataDetailVO {
  return 'id' in source;
}

const BasicDataEditPage = (props: PageComponentProps) => {
  const [selectedEntryKeys, setSelectedEntryKeys] = useState<Key[]>([]);
  const entryOperationsRef = useRef<FormListOperation | null>(null);
  const entryFieldsRef = useRef<FormListFieldData[]>([]);
  const { appNumber, tabKey, billId, operationType } = props;
  const isAddNew = operationType === OperationType.ADDNEW;
  const queryClient = useQueryClient();
  const replaceContentTab = useWorkbenchStore((state) => state.replaceContentTab);
  const activateContentTab = useWorkbenchStore((state) => state.activateContentTab);
  const sourceQuery = useQuery<BasicDataDetailVO | BasicDataCreateNewDataVO>({
    queryKey: isAddNew
      ? basicDataQueryKeys.createNewData(tabKey)
      : basicDataQueryKeys.detail(billId),
    queryFn: () => (isAddNew ? basicDataApi.createNewData() : basicDataApi.detail(billId!)),
    enabled: isAddNew || Boolean(billId),
  });
  const source = sourceQuery.data;
  const detail = source && isDetail(source) ? source : undefined;
  const initialValues = useMemo(
    () =>
      source
        ? {
            number: detail?.number ?? '',
            name: detail?.name ?? '',
            remark: detail?.remark ?? '',
            createTime: detail?.createTime ?? '',
            updateTime: detail?.updateTime ?? '',
            entrys: source.entrys ?? [],
          }
        : {},
    [detail, source],
  );
  const saveMutation = useCommandMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const saveForm: BasicDataSaveForm = {
        id: billId,
        version: detail?.version,
        number: String(values.number).trim(),
        name: String(values.name).trim(),
        remark: values.remark ? String(values.remark).trim() : undefined,
        entrys: (values.entrys as BasicDataEntry[]).map((entry) => ({
          ...entry,
          number: entry.number.trim(),
          name: entry.name.trim(),
          sort: entry.sort ?? 0,
          enabled: entry.enabled ?? true,
        })),
      };
      const savedId = await basicDataApi.save(saveForm);
      await queryClient.invalidateQueries({ queryKey: basicDataQueryKeys.all });
      if (isAddNew) {
        const nextKey = `bill:${props.componentKey}:${savedId}`;
        replaceContentTab(appNumber, tabKey, {
          key: nextKey,
          label: saveForm.name,
          closable: true,
          componentKey: props.componentKey,
          pageType: 'EDIT',
          operationType: OperationType.EDIT,
          billId: savedId,
        });
        activateContentTab(appNumber, nextKey);
      }
    },
    successMessage: isAddNew ? '新增成功' : '保存成功',
  });

  const renderEntrys = (editable: boolean) => (
    <Form.List
      name="entrys"
      rules={[
        {
          validator: async (_rule, entrys: BasicDataEntry[] | undefined) => {
            const numbers = (entrys ?? []).map((entry) => entry.number?.trim()).filter(Boolean);
            if (new Set(numbers).size !== numbers.length) {
              throw new Error('明细编码不能重复');
            }
          },
        },
      ]}
    >
      {(entryFields, { add, remove, move }, { errors }) => {
        entryOperationsRef.current = { add, remove, move };
        entryFieldsRef.current = entryFields;
        const columns: ColumnsType<FormListFieldData> = [
          {
            title: '编码',
            width: 200,
            render: (_value, field) => (
              <>
                <Form.Item name={[field.name, 'id']} hidden>
                  <Input />
                </Form.Item>
                <Form.Item
                  name={[field.name, 'number']}
                  rules={[
                    { required: true, whitespace: true, message: '请输入编码' },
                    { max: 64, message: '编码不能超过64个字符' },
                  ]}
                >
                  <Input disabled={!editable} />
                </Form.Item>
              </>
            ),
          },
          {
            title: '名称',
            render: (_value, field) => (
              <Form.Item
                name={[field.name, 'name']}
                rules={[
                  { required: true, whitespace: true, message: '请输入名称' },
                  { max: 128, message: '名称不能超过128个字符' },
                ]}
              >
                <Input disabled={!editable} />
              </Form.Item>
            ),
          },
          {
            title: '排序',
            width: 120,
            render: (_value, field) => (
              <Form.Item name={[field.name, 'sort']}>
                <InputNumber precision={0} disabled={!editable} />
              </Form.Item>
            ),
          },
          {
            title: '启用',
            width: 90,
            align: 'center',
            render: (_value, field) => (
              <Form.Item name={[field.name, 'enabled']} valuePropName="checked">
                <Switch size="small" disabled={!editable} />
              </Form.Item>
            ),
          },
        ];
        return (
          <div className="sm-basic-data-entrys">
            <Table
              rowKey="key"
              columns={columns}
              dataSource={entryFields}
              pagination={false}
              size="small"
              scroll={{ x: 'max-content' }}
              rowSelection={
                editable
                  ? {
                      selectedRowKeys: selectedEntryKeys,
                      onChange: setSelectedEntryKeys,
                    }
                  : undefined
              }
            />
            {errors.length > 0 && (
              <div className="sm-basic-data-entry-error">
                <Form.ErrorList errors={errors} />
              </div>
            )}
          </div>
        );
      }}
    </Form.List>
  );
  const renderEntryActions = (editable: boolean) =>
    editable ? (
      <div className="sm-basic-data-entry-actions">
        <Button
          type="link"
          onClick={() => entryOperationsRef.current?.add({ sort: 0, enabled: true })}
        >
          新增
        </Button>
        <Button
          type="link"
          danger
          disabled={selectedEntryKeys.length === 0}
          onClick={() => {
            const indexes = entryFieldsRef.current
              .filter((field) => selectedEntryKeys.includes(field.key))
              .map((field) => field.name);
            entryOperationsRef.current?.remove(indexes);
            setSelectedEntryKeys([]);
          }}
        >
          删除
        </Button>
      </div>
    ) : undefined;

  return (
    <EditPage
      access={basicDataAccess}
      title="基础数据管理"
      fields={fields}
      initialValues={initialValues}
      operationType={operationType ?? OperationType.EDIT}
      closeGuard={{ appNumber, tabKey }}
      loading={sourceQuery.isLoading}
      error={sourceQuery.error as Error | null}
      onRetry={() => sourceQuery.refetch()}
      onSave={saveMutation.mutateAsync}
      saving={saveMutation.isPending}
      detailContent={renderEntrys}
      detailExtra={renderEntryActions}
      onExit={() => useWorkbenchStore.getState().removeContentTab(appNumber, tabKey)}
    />
  );
};

export default BasicDataEditPage;
