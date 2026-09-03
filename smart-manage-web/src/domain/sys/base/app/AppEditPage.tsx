import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useMemo } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { useEditTabLifecycle } from '@/domain/common/page/edit/useEditTabLifecycle';
import {
  getEditSavePostCommitFeedback,
  runEditSavePostCommit,
} from '@/domain/common/page/edit/editSavePostCommit';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import EditPage from '@/domain/common/page/edit/EditPage';
import { editFormSection } from '@/domain/common/page/edit/editPageSection';
import type { PageComponentProps } from '@/domain/common/page/types';
import { OperationType } from '@/domain/common/page/types';
import { defineRefSelector } from '@/domain/common/page/edit/defineRefSelector';
import { appApi } from './api';
import { appAccess } from './permissions';
import { appQueryKeys } from './queryKeys';
import { domainApi } from '@/domain/sys/base/domain/api';
import type { DomainSelectVO } from '@/domain/sys/base/domain/types';

/** 应用编辑字段定义 */
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
  {
    label: '所属领域',
    dataIndex: 'domain',
    type: 'ref-selector',
    rules: [{ required: true, message: '所属领域不能为空' }],
    refSelector: defineRefSelector<DomainSelectVO>({
      selectorKey: 'sys-domain',
      modalTitle: '选择所属领域',
      // 使用专用的 select 接口（区别于列表页的 listPage）
      fetchFn: (params) =>
        domainApi.select({
          pageNum: params.pageNum,
          pageSize: params.pageSize,
          keyword: params.keyword,
        }),
      displayRender: (record) => record.name,
      fieldNames: { key: 'id', label: 'name' },
      columns: [
        { title: '编码', dataIndex: 'number', width: 160 },
        { title: '名称', dataIndex: 'name' },
      ],
    }),
  },
  { label: '图标', dataIndex: 'icon', type: 'icon-selector' },
  { label: '图标颜色', dataIndex: 'iconColor', type: 'color' },
  { label: '排序', dataIndex: 'seq', type: 'number' },
  { label: '描述', dataIndex: 'description', type: 'textarea', fullWidth: true },
  { label: '创建时间', dataIndex: 'createTime', type: 'readonly' },
  { label: '更新时间', dataIndex: 'updateTime', type: 'readonly' },
];

/** 应用编辑页 — 独立页形态，无单据状态 */
const AppEditPage = (props: PageComponentProps) => {
  const feedback = useOperationFeedback();
  const queryClient = useQueryClient();
  const { appNumber, tabKey, operationType, billId } = props;
  const { isAddNew, promoteToPersistedTab, exit } = useEditTabLifecycle(props);

  // 详情查询（仅编辑模式）
  const detailQuery = useQuery({
    queryKey: appQueryKeys.detail(billId),
    queryFn: () => appApi.detail(billId!),
    enabled: !!billId,
  });

  const detail = detailQuery.data;

  // Form 初始值，从详情数据派生
  const initialValues = useMemo(() => {
    if (!detail) return {};
    return {
      number: detail.number ?? '',
      name: detail.name ?? '',
      // RefSelector 传整个 domain 对象（包含 id/number/name 供 displayRender 使用）
      domain: detail.domain ?? null,
      icon: detail.icon ?? '',
      iconColor: detail.iconColor ?? '',
      seq: detail.seq ?? undefined,
      description: detail.description ?? '',
      createTime: detail.createTime ?? '',
      updateTime: detail.updateTime ?? '',
    };
  }, [detail]);

  const handleSave = async (values: Record<string, unknown>) => {
    const name = (values.name as string).trim();
    const number = (values.number as string).trim();
    // RefSelector 传整个对象，从中提取 domain
    const domain = values.domain as { id: string } | null;
    if (!domain?.id) throw new Error('所属领域不能为空');
    const savedId = await appApi.save({
      id: billId ?? undefined,
      version: detail?.version,
      name,
      number,
      icon: (values.icon as string) ?? '',
      iconColor: (values.iconColor as string) ?? '',
      seq: (values.seq as number) ?? 0,
      description: (values.description as string) ?? '',
      // 雪花 ID 保持字符串，前端不转 Number
      domainId: domain.id,
    });
    return savedId;
  };
  const saveMutation = useCommandMutation({
    mutationFn: handleSave,
    onSuccess: async (savedId) => {
      const result = await runEditSavePostCommit({
        syncTab: () => promoteToPersistedTab(savedId),
        refreshCache: () =>
          queryClient.invalidateQueries({ queryKey: appQueryKeys.all }, { throwOnError: true }),
      });
      const resultFeedback = getEditSavePostCommitFeedback(result, isAddNew);
      feedback[resultFeedback.type](resultFeedback.message);
    },
  });

  return (
    <EditPage
      access={appAccess}
      title="应用"
      sections={[editFormSection('basic', '基本信息', fields)]}
      initialValues={initialValues}
      operationType={operationType ?? OperationType.EDIT}
      closeGuard={{ appNumber, tabKey }}
      loading={detailQuery.isLoading}
      error={detailQuery.error as Error | null}
      onRetry={() => detailQuery.refetch()}
      onSave={async (values) => {
        await saveMutation.mutateAsync(values);
      }}
      saving={saveMutation.isPending}
      onExit={exit}
    />
  );
};

export default AppEditPage;
