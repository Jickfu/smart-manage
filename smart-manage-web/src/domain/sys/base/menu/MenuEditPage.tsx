import { useMemo } from 'react';
import { App } from 'antd';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { createBillTabKey } from '@/domain/common/page/tabKeys';
import EditPage from '@/domain/common/page/EditPage';
import { OperationType } from '@/domain/common/page/types';
import type { EditField } from '@/domain/common/page/EditPage';
import { defineRefSelector } from '@/domain/common/page/defineRefSelector';
import { useWorkbenchStore } from '@/stores/workbench';
import { menuApi } from './api';
import { menuAccess } from './permissions';
import { menuQueryKeys } from './queryKeys';
import { appApi } from '@/domain/sys/base/app/api';
import { permissionApi } from '@/domain/sys/base/permission/api';
import type { AppListVO } from '@/domain/sys/base/app/types';
import type { PermissionSelectVO } from '@/domain/sys/base/permission/types';
import type { MenuSelectVO } from './types';
import type { PageComponentProps } from '@/domain/common/page/types';

/** 菜单编辑页 — 全页 Tab，3 个 RefSelector + 层级联动 */
const MenuEditPage = (props: PageComponentProps) => {
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const { appNumber, tabKey, operationType, billId } = props;
  const isAddNew = operationType === OperationType.ADDNEW;
  const replaceContentTab = useWorkbenchStore((s) => s.replaceContentTab);
  const activateContentTab = useWorkbenchStore((s) => s.activateContentTab);

  const detailQuery = useQuery({
    queryKey: menuQueryKeys.detail(billId),
    queryFn: () => menuApi.detail(billId!),
    enabled: !!billId,
  });

  const detail = detailQuery.data;

  const initialValues = useMemo(() => {
    if (!detail) return {};
    return {
      number: detail.number ?? '',
      name: detail.name ?? '',
      level: detail.level ?? undefined,
      app: detail.appId != null ? { id: detail.appId } : null,
      parent: detail.parent ?? null,
      permission: detail.permissionId != null ? { id: detail.permissionId } : null,
      path: detail.path ?? '',
      component: detail.component ?? '',
      icon: detail.icon ?? '',
      description: detail.description ?? '',
      sort: detail.sort ?? undefined,
      createTime: detail.createTime ?? '',
      updateTime: detail.updateTime ?? '',
    };
  }, [detail]);

  const fields: EditField[] = [
    {
      label: '名称',
      dataIndex: 'name',
      type: 'text',
      rules: [{ required: true, message: '名称不能为空' }],
    },
    {
      label: '编码',
      dataIndex: 'number',
      type: 'text',
      placeholder: '例如：user 或 base_management',
      rules: [
        { required: true, message: '编码不能为空' },
        {
          pattern: /^[a-z][a-z0-9]*(?:_[a-z0-9]+)*$/,
          message: '编码必须为小写字母、数字和下划线，且以小写字母开头',
        },
      ],
    },
    {
      label: '层级',
      dataIndex: 'level',
      type: 'select',
      options: [
        { label: '分组', value: 0 },
        { label: '页面', value: 1 },
      ],
      rules: [{ required: true, message: '层级不能为空' }],
    },
    {
      label: '所属应用',
      dataIndex: 'app',
      type: 'ref-selector',
      rules: [{ required: true, message: '所属应用不能为空' }],
      refSelector: defineRefSelector<AppListVO>({
        selectorKey: 'sys-app-menu',
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
    {
      label: '父菜单',
      dataIndex: 'parent',
      type: 'ref-selector',
      refSelector: defineRefSelector<MenuSelectVO>({
        selectorKey: 'sys-menu-parent',
        modalTitle: '选择父菜单',
        fetchFn: (params) =>
          menuApi.select({
            pageNum: params.pageNum,
            pageSize: params.pageSize,
            keyword: params.keyword,
            excludeId: billId ?? undefined,
          }),
        displayRender: (record) => `${record.number ?? '-'} / ${record.name}`,
        fieldNames: { key: 'id', label: 'name' },
        columns: [
          { title: '编码', dataIndex: 'number', width: 120 },
          { title: '名称', dataIndex: 'name', width: 180 },
          {
            title: '层级',
            dataIndex: 'level',
            width: 60,
            render: (val: unknown) => (Number(val) === 0 ? '分组' : '页面'),
          },
        ],
      }),
    },
    {
      label: '权限',
      dataIndex: 'permission',
      type: 'ref-selector',
      refSelector: defineRefSelector<PermissionSelectVO>({
        selectorKey: 'sys-perm-menu',
        modalTitle: '选择权限',
        fetchFn: (params) =>
          permissionApi.select({
            pageNum: params.pageNum,
            pageSize: params.pageSize,
            keyword: params.keyword,
          }),
        displayRender: (record) => `${record.number} / ${record.name}`,
        fieldNames: { key: 'id', label: 'name' },
        columns: [
          { title: '编码', dataIndex: 'number', width: 200 },
          { title: '名称', dataIndex: 'name', width: 200 },
        ],
      }),
    },
    { label: '路径', dataIndex: 'path', type: 'text' },
    { label: '组件', dataIndex: 'component', type: 'text' },
    { label: '图标', dataIndex: 'icon', type: 'text', placeholder: 'Ant Design 图标名' },
    { label: '描述', dataIndex: 'description', type: 'textarea', fullWidth: true },
    { label: '排序', dataIndex: 'sort', type: 'number' },
    { label: '创建时间', dataIndex: 'createTime', type: 'readonly' },
    { label: '更新时间', dataIndex: 'updateTime', type: 'readonly' },
  ];

  const handleSave = async (values: Record<string, unknown>) => {
    const name = (values.name as string).trim();
    const app = values.app as { id: string } | null;
    const parent = values.parent as { id: string; number?: string; name?: string } | null;
    const permission = values.permission as { id: string } | null;

    if (!app?.id) throw new Error('所属应用不能为空');

    const savedId = await menuApi.save({
      id: billId ?? undefined,
      version: detail?.version,
      name,
      number: (values.number as string).trim(),
      level: values.level as number,
      appId: app.id,
      parentId: parent?.id ?? undefined,
      permissionId: permission?.id ?? undefined,
      path: (values.path as string) ?? undefined,
      component: (values.component as string) ?? undefined,
      icon: (values.icon as string) ?? undefined,
      description: (values.description as string) ?? undefined,
      sort: (values.sort as number) ?? undefined,
    });

    if (isAddNew && tabKey !== createBillTabKey(props.componentKey, savedId)) {
      replaceContentTab(appNumber, tabKey, {
        key: createBillTabKey(props.componentKey, savedId),
        closable: true,
        componentKey: props.componentKey,
        pageType: 'EDIT',
        operationType: OperationType.EDIT,
        billId: String(savedId),
      });
      activateContentTab(appNumber, createBillTabKey(props.componentKey, savedId));
    }
    await queryClient.invalidateQueries({ queryKey: menuQueryKeys.all });
    message.success(isAddNew ? '新增成功' : '保存成功');
  };
  const saveMutation = useCommandMutation({
    mutationFn: handleSave,
  });

  return (
    <EditPage
      access={menuAccess}
      title="菜单"
      fields={fields}
      initialValues={initialValues}
      operationType={operationType ?? OperationType.EDIT}
      closeGuard={{ appNumber, tabKey }}
      loading={detailQuery.isLoading}
      error={detailQuery.error as Error | null}
      onRetry={() => detailQuery.refetch()}
      onSave={saveMutation.mutateAsync}
      saving={saveMutation.isPending}
      onExit={() => {
        useWorkbenchStore.getState().removeContentTab(appNumber, tabKey);
      }}
    />
  );
};

export default MenuEditPage;
