import { useMemo } from 'react';
import { App, Form } from 'antd';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import EditPage from '@/domain/common/page/EditPage';
import type { EditField } from '@/domain/common/page/EditPage';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { createBillTabKey } from '@/domain/common/page/tabKeys';
import type { PageComponentProps } from '@/domain/common/page/types';
import { OperationType } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { scriptApi } from './api';
import { scriptAccess } from './permissions';
import { scriptQueryKeys } from './queryKeys';
import ScriptEditor from './ScriptEditor';
import './scriptConsole.css';

const fields: EditField[] = [
  {
    label: '编码',
    dataIndex: 'number',
    type: 'text',
    rules: [{ required: true, message: '编码不能为空' }],
  },
  {
    label: '名称',
    dataIndex: 'name',
    type: 'text',
    rules: [{ required: true, message: '名称不能为空' }],
  },
  { label: '备注', dataIndex: 'remark', type: 'textarea', fullWidth: true },
  { label: '创建时间', dataIndex: 'createTime', type: 'readonly' },
  { label: '更新时间', dataIndex: 'updateTime', type: 'readonly' },
];

export default function ScriptEditPage(props: PageComponentProps) {
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const isAddNew = props.operationType === OperationType.ADDNEW;
  const replaceContentTab = useWorkbenchStore((state) => state.replaceContentTab);
  const activateContentTab = useWorkbenchStore((state) => state.activateContentTab);
  const detailQuery = useQuery({
    queryKey: isAddNew ? scriptQueryKeys.createNewData() : scriptQueryKeys.detail(props.billId),
    queryFn: () => (isAddNew ? scriptApi.createNewData() : scriptApi.detail(props.billId!)),
  });
  const detail = detailQuery.data;
  const initialValues = useMemo(
    () => ({
      number: detail?.number ?? '',
      name: detail?.name ?? '',
      remark: detail?.remark ?? '',
      content: detail?.content ?? '',
      createTime: detail?.createTime ?? '',
      updateTime: detail?.updateTime ?? '',
    }),
    [detail],
  );
  const saveMutation = useCommandMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const name = String(values.name).trim();
      const savedId = await scriptApi.save({
        id: props.billId,
        version: detail?.version,
        number: String(values.number).trim(),
        name,
        remark: String(values.remark ?? ''),
        content: String(values.content ?? ''),
      });
      if (isAddNew && props.tabKey !== savedId) {
        const nextKey = createBillTabKey(props.componentKey, savedId);
        replaceContentTab(props.appNumber, props.tabKey, {
          key: nextKey,
          closable: true,
          componentKey: props.componentKey,
          pageType: 'EDIT',
          operationType: OperationType.EDIT,
          billId: savedId,
        });
        activateContentTab(props.appNumber, nextKey);
      }
      await queryClient.invalidateQueries({ queryKey: scriptQueryKeys.all });
      message.success(isAddNew ? '新增成功' : '保存成功');
    },
  });
  return (
    <EditPage
      title="脚本"
      access={scriptAccess}
      fields={fields}
      initialValues={initialValues}
      operationType={props.operationType ?? OperationType.EDIT}
      closeGuard={{ appNumber: props.appNumber, tabKey: props.tabKey }}
      loading={detailQuery.isLoading}
      error={detailQuery.error as Error | null}
      onRetry={() => detailQuery.refetch()}
      onSave={saveMutation.mutateAsync}
      saving={saveMutation.isPending}
      detailContent={(editable) => (
        <Form.Item
          name="content"
          label="JavaScript"
          rules={[{ required: true, message: '脚本内容不能为空' }]}
        >
          <ScriptEditor disabled={!editable} className="sm-script-edit-code" />
        </Form.Item>
      )}
      onExit={() => useWorkbenchStore.getState().removeContentTab(props.appNumber, props.tabKey)}
    />
  );
}
