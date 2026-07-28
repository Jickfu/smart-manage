import { useMemo, useRef, useState } from 'react';
import type { Key } from 'react';
import { Button, DatePicker, Form, Input, InputNumber, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { FormListFieldData, FormListOperation } from 'antd/es/form';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import EditPage from '@/domain/common/page/EditPage';
import type { EditField } from '@/domain/common/page/EditPage';
import {
  getDatePickerValueProps,
  normalizeDatePickerValue,
} from '@/domain/common/page/dateFormValue';
import { BillStatus, OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { useWorkbenchStore } from '@/stores/workbench';
import { purchaseRequisitionApi } from './api';
import { purchaseRequisitionAccess } from './permissions';
import { purchaseRequisitionQueryKeys } from './queryKeys';
import type {
  PurchaseRequisitionCreateNewDataVO,
  PurchaseRequisitionDetailVO,
  PurchaseRequisitionEntry,
  PurchaseRequisitionSaveForm,
} from './types';
import './PurchaseRequisitionEditPage.css';

const fields: EditField[] = [
  {
    label: '编码',
    dataIndex: 'number',
    type: 'text',
    rules: [{ required: true, message: '编码不能为空' }],
  },
  {
    label: '主题',
    dataIndex: 'subject',
    type: 'text',
    rules: [{ required: true, message: '主题不能为空' }],
  },
  {
    label: '业务日期',
    dataIndex: 'bizDate',
    type: 'date',
    disabled: true,
    placeholder: 'YYYY-MM-DD',
    rules: [{ required: true, message: '业务日期不能为空' }],
  },
  { label: '需求日期', dataIndex: 'requiredDate', type: 'date', placeholder: 'YYYY-MM-DD' },
  {
    label: '单据状态',
    dataIndex: 'billStatus',
    type: 'select',
    disabled: true,
    options: [
      { label: '暂存', value: BillStatus.SAVED },
      { label: '已提交', value: BillStatus.SUBMITTED },
      { label: '审核通过', value: BillStatus.AUDITED },
      { label: '已关闭', value: BillStatus.CLOSED },
    ],
  },
  { label: '申请原因', dataIndex: 'reason', type: 'textarea', fullWidth: true },
];

function isDetail(
  source: PurchaseRequisitionDetailVO | PurchaseRequisitionCreateNewDataVO,
): source is PurchaseRequisitionDetailVO {
  return 'id' in source;
}

const PurchaseRequisitionEditPage = (props: PageComponentProps) => {
  const [selectedEntryKeys, setSelectedEntryKeys] = useState<Key[]>([]);
  const entryOperationsRef = useRef<FormListOperation | null>(null);
  const { appNumber, tabKey, billId, operationType } = props;
  const isAddNew = operationType === OperationType.ADDNEW;
  const queryClient = useQueryClient();
  const replaceContentTab = useWorkbenchStore((state) => state.replaceContentTab);
  const activateContentTab = useWorkbenchStore((state) => state.activateContentTab);
  const sourceQuery = useQuery<PurchaseRequisitionDetailVO | PurchaseRequisitionCreateNewDataVO>({
    queryKey: isAddNew
      ? purchaseRequisitionQueryKeys.createNewData(tabKey)
      : purchaseRequisitionQueryKeys.detail(billId),
    queryFn: () =>
      isAddNew ? purchaseRequisitionApi.createNewData() : purchaseRequisitionApi.detail(billId!),
    enabled: isAddNew || Boolean(billId),
  });
  const source = sourceQuery.data;
  const detail = source && isDetail(source) ? source : undefined;
  const initialValues = useMemo(
    () =>
      source
        ? {
            number: detail?.number ?? '',
            subject: detail?.subject ?? '',
            bizDate: source.bizDate,
            requiredDate: detail?.requiredDate ?? '',
            reason: detail?.reason ?? '',
            billStatus: source.billStatus,
            entrys: source.entrys ?? [],
          }
        : {},
    [detail, source],
  );

  const persist = async (values: Record<string, unknown>, submit: boolean) => {
    const form: PurchaseRequisitionSaveForm = {
      id: billId,
      version: detail?.version,
      number: String(values.number).trim(),
      subject: String(values.subject).trim(),
      bizDate: String(values.bizDate),
      requiredDate: values.requiredDate ? String(values.requiredDate) : undefined,
      reason: values.reason ? String(values.reason) : undefined,
      entrys: (values.entrys as PurchaseRequisitionEntry[]).map((entry, index) => ({
        ...entry,
        materialName: entry.materialName.trim(),
        unit: entry.unit.trim(),
        sort: index + 1,
      })),
    };
    // 提交接口接收完整聚合，在一个后端事务中完成保存和状态推进。
    const savedId = submit
      ? await purchaseRequisitionApi.submit(form)
      : await purchaseRequisitionApi.save(form);
    const nextKey = `bill:${props.componentKey}:${savedId}`;
    if (isAddNew || submit) {
      replaceContentTab(appNumber, tabKey, {
        key: nextKey,
        label: form.number,
        closable: true,
        componentKey: props.componentKey,
        pageType: 'EDIT',
        operationType: submit ? OperationType.VIEW : OperationType.EDIT,
        billId: savedId,
      });
      activateContentTab(appNumber, nextKey);
    }
    await queryClient.invalidateQueries({ queryKey: purchaseRequisitionQueryKeys.all });
  };

  const saveMutation = useCommandMutation({
    mutationFn: (values: Record<string, unknown>) => persist(values, false),
    successMessage: isAddNew ? '新增成功' : '保存成功',
  });
  const submitMutation = useCommandMutation({
    mutationFn: (values: Record<string, unknown>) => persist(values, true),
    successMessage: '提交成功',
  });

  const renderEntrys = (editable: boolean) => (
    <Form.List
      name="entrys"
      rules={[
        {
          validator: async (_rule, entrys: PurchaseRequisitionEntry[] | undefined) => {
            if (!entrys?.length) throw new Error('至少需要一条明细');
          },
        },
      ]}
    >
      {(entryFields, { add, remove, move }, { errors }) => {
        entryOperationsRef.current = { add, remove, move };
        const columns: ColumnsType<FormListFieldData> = [
          {
            title: '物料名称',
            dataIndex: 'materialName',
            width: 200,
            render: (_value, field) => (
              <Form.Item
                name={[field.name, 'materialName']}
                rules={[{ required: true, message: '请输入物料名称' }]}
              >
                <Input disabled={!editable} />
              </Form.Item>
            ),
          },
          {
            title: '规格型号',
            width: 160,
            render: (_value, field) => (
              <Form.Item name={[field.name, 'specification']}>
                <Input disabled={!editable} />
              </Form.Item>
            ),
          },
          {
            title: '单位',
            width: 100,
            render: (_value, field) => (
              <Form.Item
                name={[field.name, 'unit']}
                rules={[{ required: true, message: '请输入单位' }]}
              >
                <Input disabled={!editable} />
              </Form.Item>
            ),
          },
          {
            title: '数量',
            width: 140,
            render: (_value, field) => (
              <Form.Item
                name={[field.name, 'quantity']}
                rules={[{ required: true, message: '请输入数量' }]}
              >
                <InputNumber min={0.000001} precision={6} disabled={!editable} />
              </Form.Item>
            ),
          },
          {
            title: '需求日期',
            width: 140,
            render: (_value, field) => (
              <Form.Item
                name={[field.name, 'requiredDate']}
                getValueProps={getDatePickerValueProps}
                normalize={normalizeDatePickerValue}
              >
                <DatePicker
                  className="sm-purchase-entry-date-picker"
                  placeholder="YYYY-MM-DD"
                  variant="borderless"
                  disabled={!editable}
                />
              </Form.Item>
            ),
          },
          {
            title: '备注',
            width: 180,
            render: (_value, field) => (
              <Form.Item name={[field.name, 'remark']}>
                <Input disabled={!editable} />
              </Form.Item>
            ),
          },
        ];
        return (
          <div className="sm-purchase-requisition-entrys">
            <Table
              rowKey="name"
              columns={columns}
              dataSource={entryFields}
              pagination={false}
              size="small"
              scroll={{ x: 'max-content' }}
              rowSelection={
                editable
                  ? {
                      selectedRowKeys: selectedEntryKeys,
                      onChange: (keys) => setSelectedEntryKeys(keys),
                    }
                  : undefined
              }
            />
            {errors.length > 0 && (
              <div className="sm-purchase-entry-error">
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
      <div className="sm-purchase-entry-actions">
        <Button type="link" onClick={() => entryOperationsRef.current?.add({ quantity: 1 })}>
          新增
        </Button>
        <Button
          type="link"
          danger
          disabled={selectedEntryKeys.length === 0}
          onClick={() => {
            entryOperationsRef.current?.remove(selectedEntryKeys.map(Number));
            setSelectedEntryKeys([]);
          }}
        >
          删除
        </Button>
      </div>
    ) : undefined;

  return (
    <EditPage
      access={purchaseRequisitionAccess}
      title="采购申请"
      fields={fields}
      initialValues={initialValues}
      billStatus={source?.billStatus as BillStatus | undefined}
      operationType={operationType ?? OperationType.EDIT}
      closeGuard={{ appNumber, tabKey }}
      loading={sourceQuery.isLoading}
      error={sourceQuery.error as Error | null}
      onRetry={() => sourceQuery.refetch()}
      onSave={saveMutation.mutateAsync}
      onSubmit={submitMutation.mutateAsync}
      saving={saveMutation.isPending || submitMutation.isPending}
      detailContent={renderEntrys}
      detailExtra={renderEntryActions}
      onExit={() => useWorkbenchStore.getState().removeContentTab(appNumber, tabKey)}
    />
  );
};

export default PurchaseRequisitionEditPage;
