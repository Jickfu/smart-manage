import { useMemo } from 'react';
import { App } from 'antd';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import ModalEditPage from '@/domain/common/page/ModalEditPage';
import type { EditField } from '@/domain/common/page/EditPage';
import { defineRefSelector } from '@/domain/common/page/defineRefSelector';
import { permissionApi } from './api';
import { permissionAccess } from './permissions';
import { permissionQueryKeys } from './queryKeys';
import { appApi } from '@/domain/sys/base/app/api';
import type { AppListVO } from '@/domain/sys/base/app/types';

interface Props {
  open: boolean;
  permissionId: string | null;
  onClose: () => void;
  onSaved: () => void;
}

/** 权限编辑字段定义 */
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
    label: '所属应用',
    dataIndex: 'app',
    type: 'ref-selector',
    rules: [{ required: true, message: '所属应用不能为空' }],
    refSelector: defineRefSelector<AppListVO>({
      selectorKey: 'sys-app-permission',
      modalTitle: '选择应用',
      fetchFn: (params) =>
        appApi.listPage({
          pageNum: params.pageNum,
          pageSize: params.pageSize,
          keyword: params.keyword,
        }),
      displayRender: (record) => record.name,
      fieldNames: { key: 'id', label: 'name' },
      columns: [
        { title: '编码', dataIndex: 'number', width: 160 },
        { title: '名称', dataIndex: 'name', width: 200 },
      ],
    }),
  },
];

/** 权限编辑弹框 */
const PermissionEditPage = ({ open, permissionId, onClose, onSaved }: Props) => {
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const isAddNew = permissionId === null;

  const detailQuery = useQuery({
    queryKey: permissionQueryKeys.detail(permissionId),
    queryFn: () => permissionApi.detail(permissionId!),
    enabled: Boolean(open && permissionId),
    staleTime: 0,
  });

  const detail = detailQuery.data;

  const initialValues = useMemo(() => {
    if (!detail) return {};
    // 详情 VO 中 appId 始终保持字符串，RefSelector 回显需要完整对象。
    const appId = detail.appId;
    return {
      number: detail.number ?? '',
      name: detail.name ?? '',
      app: appId != null ? { id: appId } : null,
    };
  }, [detail]);

  const handleSave = async (values: Record<string, unknown>) => {
    const app = values.app as { id: string } | null;
    if (!app?.id) throw new Error('所属应用不能为空');
    await permissionApi.save({
      id: permissionId ?? undefined,
      version: detail?.version,
      name: (values.name as string).trim(),
      number: (values.number as string).trim(),
      appId: app.id,
    });
    await queryClient.invalidateQueries({ queryKey: permissionQueryKeys.all });
    message.success(isAddNew ? '新增成功' : '保存成功');
    onSaved();
  };
  const saveMutation = useCommandMutation({
    mutationFn: handleSave,
  });

  return (
    <ModalEditPage
      access={permissionAccess}
      title={isAddNew ? '新增权限' : '编辑权限'}
      open={open}
      onClose={onClose}
      fields={fields}
      initialValues={initialValues}
      onSave={saveMutation.mutateAsync}
      saving={saveMutation.isPending}
      loading={detailQuery.isLoading}
      error={detailQuery.error as Error | null}
      onRetry={() => detailQuery.refetch()}
    />
  );
};

export default PermissionEditPage;
