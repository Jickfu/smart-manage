import { useMemo, useState } from 'react';
import type { Key } from 'react';
import { Button, Form, Input, InputNumber, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { FormListFieldData } from 'antd/es/form';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import EditPage from '@/domain/common/page/EditPage';
import type { EditField } from '@/domain/common/page/EditPage';
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
    label: '申请日期',
    dataIndex: 'applyDate',
    type: 'text',
    placeholder: 'YYYY-MM-DD',
    rules: [{ required: true, message: '申请日期不能为空' }],
  },
  { label: '需求日期', dataIndex: 'requiredDate', type: 'text', placeholder: 'YYYY-MM-DD' },
  { label: '申请原因', dataIndex: 'reason', type: 'textarea', fullWidth: true },
  { label: '单据状态', dataIndex: 'billStatusName', type: 'readonly' },
  { label: '创建时间', dataIndex: 'createTime', type: 'readonly' },
  { label: '更新时间', dataIndex: 'updateTime', type: 'readonly' },
];

function statusName(status?: string) {
  if (status === BillStatus.SAVED) return '暂存';
  if (status === BillStatus.SUBMITTED) return '已提交';
  if (status === BillStatus.AUDITED) return '审核通过';
  if (status === BillStatus.CLOSED) return '已关闭';
  return '';
}

function isDetail(
  source: PurchaseRequisitionDetailVO | PurchaseRequisitionCreateNewDataVO,
): source is PurchaseRequisitionDetailVO {
  return 'id' in source;
}

const PurchaseRequisitionEditPage = (props: PageComponentProps) => {
  const [selectedEntryKeys, setSelectedEntryKeys] = useState<Key[]>([]);
  const { appNumber, tabKey, billId, operationType } = props;
  const isAddNew = operationType === OperationType.ADDNEW;
  const queryClient = useQueryClient();
  const replaceContentTab = useWorkbenchStore((state) => state.replaceContentTab);
  const activateContentTab = useWorkbenchStore((state) => state.activateContentTab);
  const sourceQuery = useQuery<PurchaseRequisitionDetailVO | PurchaseRequisitionCreateNewDataVO>({
    queryKey: isAddNew
      ? purchaseRequisitionQueryKeys.createNewData()
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
            applyDate: source.applyDate,
            requiredDate: detail?.requiredDate ?? '',
            reason: detail?.reason ?? '',
            billStatusName: statusName(source.billStatus),
            createTime: detail?.createTime ?? '',
            updateTime: detail?.updateTime ?? '',
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
      applyDate: String(values.applyDate),
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
      {(entryFields, { add, remove }, { errors }) => {
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
              <Form.Item name={[field.name, 'requiredDate']}>
                <Input placeholder="YYYY-MM-DD" disabled={!editable} />
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
            {editable && (
              <div className="sm-purchase-entry-actions">
                <Button onClick={() => add({ quantity: 1 })}>新增</Button>
                <Button
                  danger
                  disabled={selectedEntryKeys.length === 0}
                  onClick={() => {
                    const selectedNames = entryFields
                      .filter((field) => selectedEntryKeys.includes(field.key))
                      .map((field) => field.name);
                    remove(selectedNames);
                    setSelectedEntryKeys([]);
                  }}
                >
                  删除
                </Button>
              </div>
            )}
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
              <div className="sm-purchase-entry-error">
                <Form.ErrorList errors={errors} />
              </div>
            )}
          </div>
        );
      }}
    </Form.List>
  );

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
      onExit={() => useWorkbenchStore.getState().removeContentTab(appNumber, tabKey)}
    />
  );
};

export default PurchaseRequisitionEditPage;
