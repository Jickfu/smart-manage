import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useMemo, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { createBillTabKey } from '@/domain/common/page/tab/tabKeys';
import EditPage from '@/domain/common/page/edit/EditPage';
import { editFormSection } from '@/domain/common/page/edit/editPageSection';
import { OperationType } from '@/domain/common/page/types';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import { defineRefSelector } from '@/domain/common/page/edit/defineRefSelector';
import { useWorkbenchStore } from '@/stores/workbench';
import { menuApi } from './api';
import { menuAccess } from './permissions';
import { menuQueryKeys } from './queryKeys';
import { useAppRefSelector } from '@/domain/sys/base/app/refSelector';
import { useFeatureRefSelector } from '@/domain/sys/base/feature/refSelector/index';
import { permissionApi } from '@/domain/sys/base/permission/api';
import type { PermissionSelectVO } from '@/domain/sys/base/permission/types';
import type { ExternalOpenMode, MenuSelectVO, MenuTargetType } from './types';
import type { PageComponentProps } from '@/domain/common/page/types';

const MENU_LEVEL_CATEGORY = 0;
const MENU_LEVEL_PAGE = 1;
const INTERNAL_PAGE: MenuTargetType = 'INTERNAL_PAGE';
const EXTERNAL_LINK: MenuTargetType = 'EXTERNAL_LINK';

/** 前端提供即时反馈，后端仍负责最终 URL 安全校验。 */
function validateExternalUrl(_: unknown, rawValue: unknown) {
  const value = typeof rawValue === 'string' ? rawValue.trim() : '';
  if (!value) return Promise.reject(new Error('外部链接不能为空'));
  try {
    const url = new URL(value);
    if (!['http:', 'https:'].includes(url.protocol)) {
      return Promise.reject(new Error('外部链接必须使用 HTTP 或 HTTPS 协议'));
    }
    if (url.username || url.password) {
      return Promise.reject(new Error('外部链接不能包含账号密码'));
    }
    return Promise.resolve();
  } catch {
    return Promise.reject(new Error('请输入完整有效的外部链接'));
  }
}

/** 菜单编辑页 — 全页 Tab，3 个 RefSelector + 层级联动 */
const MenuEditPage = (props: PageComponentProps) => {
  const feedback = useOperationFeedback();
  const queryClient = useQueryClient();
  const { appNumber, tabKey, operationType, billId } = props;
  const isAddNew = operationType === OperationType.ADDNEW;
  const replaceContentTab = useWorkbenchStore((s) => s.replaceContentTab);
  const activateContentTab = useWorkbenchStore((s) => s.activateContentTab);

  const detailQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: menuQueryKeys.detail(billId),
    queryFn: () => menuApi.detail(billId!),
    enabled: !!billId,
  });
  const appRefSelector = useAppRefSelector();

  const detail = detailQuery.data;
  const [appSelection, setAppSelection] = useState<{
    billId?: string;
    appId?: string;
  }>();
  const [featureSelection, setFeatureSelection] = useState<{
    billId?: string;
    featureId?: string;
  }>();
  const [menuShapeSelection, setMenuShapeSelection] = useState<{
    billId?: string;
    level?: number;
    targetType?: MenuTargetType;
  }>();
  const selectedAppId =
    appSelection && appSelection.billId === billId ? appSelection.appId : detail?.app?.id;
  const featureRefSelector = useFeatureRefSelector(selectedAppId);
  const selectedFeatureId =
    featureSelection && featureSelection.billId === billId
      ? featureSelection.featureId
      : detail?.feature?.id;
  const selectedLevel =
    menuShapeSelection && menuShapeSelection.billId === billId
      ? menuShapeSelection.level
      : detail?.level;
  const selectedTargetType =
    menuShapeSelection && menuShapeSelection.billId === billId
      ? menuShapeSelection.targetType
      : detail?.targetType;
  const initialValues = useMemo(() => {
    if (!detail) {
      return { sort: 99 };
    }
    return {
      number: detail.number ?? '',
      name: detail.name ?? '',
      level: detail.level ?? undefined,
      app: detail.app ?? null,
      feature: detail.feature ?? null,
      parent: detail.parent ?? null,
      permission: detail.permission ?? null,
      path: detail.path ?? '',
      component: detail.component ?? '',
      targetType: detail.targetType ?? undefined,
      externalUrl: detail.externalUrl ?? '',
      externalOpenMode: detail.externalOpenMode ?? undefined,
      icon: detail.icon ?? '',
      description: detail.description ?? '',
      sort: detail.sort ?? undefined,
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
      disabled: !isAddNew,
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
        { label: '分组', value: MENU_LEVEL_CATEGORY },
        { label: '页面', value: MENU_LEVEL_PAGE },
      ],
      rules: [{ required: true, message: '层级不能为空' }],
    },
    {
      label: '所属应用',
      dataIndex: 'app',
      type: 'ref-selector',
      rules: [{ required: true, message: '所属应用不能为空' }],
      refSelector: appRefSelector,
    },
    ...(selectedLevel === MENU_LEVEL_PAGE
      ? [
          {
            label: '所属功能',
            dataIndex: 'feature',
            type: 'ref-selector' as const,
            disabled: !selectedAppId,
            placeholder: selectedAppId ? '请选择所属功能' : '请先选择所属应用',
            rules: [{ required: true, message: '页面菜单的所属功能不能为空' }],
            refSelector: featureRefSelector,
          },
          {
            label: '父分组（可选）',
            dataIndex: 'parent',
            type: 'ref-selector' as const,
            disabled: !selectedAppId,
            placeholder: selectedAppId ? '留空则显示在应用根级' : '请先选择所属应用',
            refSelector: defineRefSelector<MenuSelectVO>({
              selectorKey: ['sys-menu-parent', selectedAppId],
              modalTitle: '选择父分组',
              fetchFn: (params) =>
                menuApi.select({
                  pageNum: params.pageNum,
                  pageSize: params.pageSize,
                  keyword: params.keyword,
                  appId: selectedAppId!,
                  level: 0,
                  excludeId: billId ?? undefined,
                }),
              displayRender: (record) => record.name,
              fieldNames: { key: 'id', label: 'name' },
              columns: [
                { title: '编码', dataIndex: 'number', width: 120 },
                { title: '名称', dataIndex: 'name' },
                {
                  title: '层级',
                  dataIndex: 'level',
                  width: 60,
                  render: (val: unknown) => (Number(val) === 0 ? '分组' : '页面'),
                },
              ],
            }),
          },
        ]
      : []),
    {
      label: '权限',
      dataIndex: 'permission',
      type: 'ref-selector',
      disabled: !selectedAppId,
      placeholder: selectedFeatureId ? '请选择功能权限' : '请选择应用级权限',
      rules: [{ required: true, message: '权限不能为空' }],
      refSelector: defineRefSelector<PermissionSelectVO>({
        selectorKey: ['sys-perm-menu', selectedAppId, selectedFeatureId],
        modalTitle: '选择权限',
        fetchFn: (params) =>
          permissionApi.select({
            pageNum: params.pageNum,
            pageSize: params.pageSize,
            keyword: params.keyword,
            appId: selectedAppId,
            featureId: selectedFeatureId,
            appLevel: !selectedFeatureId,
          }),
        displayRender: (record) => record.name,
        fieldNames: { key: 'id', label: 'name' },
        columns: [
          { title: '编码', dataIndex: 'number', width: 200 },
          { title: '名称', dataIndex: 'name' },
        ],
      }),
    },
    ...(selectedLevel === MENU_LEVEL_PAGE
      ? [
          {
            label: '页面目标',
            dataIndex: 'targetType',
            type: 'select' as const,
            options: [
              { label: '内部页面', value: INTERNAL_PAGE },
              { label: '外部链接', value: EXTERNAL_LINK },
            ],
            rules: [{ required: true, message: '页面目标不能为空' }],
          },
        ]
      : []),
    ...(selectedLevel === MENU_LEVEL_PAGE && selectedTargetType === INTERNAL_PAGE
      ? [
          {
            label: '路径',
            dataIndex: 'path',
            type: 'text' as const,
            rules: [{ required: true, message: '路径不能为空' }],
          },
          {
            label: '组件',
            dataIndex: 'component',
            type: 'text' as const,
            rules: [{ required: true, message: '组件不能为空' }],
          },
        ]
      : []),
    ...(selectedLevel === MENU_LEVEL_PAGE && selectedTargetType === EXTERNAL_LINK
      ? [
          {
            label: '外部链接',
            dataIndex: 'externalUrl',
            type: 'text' as const,
            placeholder: '例如：https://x.com/home',
            fullWidth: true,
            rules: [{ required: true, validator: validateExternalUrl }],
          },
          {
            label: '打开方式',
            dataIndex: 'externalOpenMode',
            type: 'select' as const,
            options: [
              { label: '新浏览器标签页', value: 'NEW_TAB' },
              { label: '工作台内嵌页（iframe）', value: 'IFRAME' },
            ],
            rules: [{ required: true, message: '打开方式不能为空' }],
          },
        ]
      : []),
    { label: '图标', dataIndex: 'icon', type: 'icon-selector' },
    { label: '排序', dataIndex: 'sort', type: 'number' },
    { label: '描述', dataIndex: 'description', type: 'textarea', fullWidth: true },
  ];

  const handleSave = async (values: Record<string, unknown>) => {
    const name = (values.name as string).trim();
    const app = values.app as { id: string } | null;
    const feature = values.feature as { id: string } | null;
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
      featureId: feature?.id,
      parentId: parent?.id ?? undefined,
      permissionId: permission?.id ?? undefined,
      path: (values.path as string | undefined)?.trim() || undefined,
      component: (values.component as string | undefined)?.trim() || undefined,
      targetType: values.targetType as MenuTargetType | undefined,
      externalUrl: (values.externalUrl as string | undefined)?.trim() || undefined,
      externalOpenMode: values.externalOpenMode as ExternalOpenMode | undefined,
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
    feedback.success(isAddNew ? '新增成功' : '保存成功');
  };
  const saveMutation = useCommandMutation({
    mutationFn: handleSave,
  });

  return (
    <EditPage
      access={menuAccess}
      title="菜单"
      sections={[editFormSection('basic', '基本信息', fields)]}
      initialValues={initialValues}
      operationType={operationType ?? OperationType.EDIT}
      closeGuard={{ appNumber, tabKey }}
      loading={detailQuery.isLoading}
      error={getBlockingQueryError(detailQuery) as Error | null}
      onRetry={() => detailQuery.refetch()}
      onSave={saveMutation.mutateAsync}
      saving={saveMutation.isPending}
      onValuesChange={(changedValues, _allValues, form) => {
        if (Object.hasOwn(changedValues, 'level')) {
          const nextLevel = changedValues.level as number | undefined;
          const nextTargetType = nextLevel === MENU_LEVEL_PAGE ? INTERNAL_PAGE : undefined;
          form.setFieldValue('targetType', nextTargetType);
          form.setFieldValue('path', undefined);
          form.setFieldValue('component', undefined);
          form.setFieldValue('externalUrl', undefined);
          form.setFieldValue('externalOpenMode', undefined);
          if (nextLevel === MENU_LEVEL_CATEGORY) {
            // 分组是应用级导航容器，不归属功能，也不能沿用功能级入口权限。
            form.setFieldValue('parent', null);
            form.setFieldValue('feature', null);
            form.setFieldValue('permission', null);
            setFeatureSelection({ billId, featureId: undefined });
          }
          setMenuShapeSelection({ billId, level: nextLevel, targetType: nextTargetType });
        }
        if (Object.hasOwn(changedValues, 'targetType')) {
          const nextTargetType = changedValues.targetType as MenuTargetType | undefined;
          form.setFieldValue('path', undefined);
          form.setFieldValue('component', undefined);
          form.setFieldValue('externalUrl', undefined);
          form.setFieldValue('externalOpenMode', undefined);
          setMenuShapeSelection({ billId, level: MENU_LEVEL_PAGE, targetType: nextTargetType });
        }
        if (Object.hasOwn(changedValues, 'feature')) {
          // 权限必须属于当前功能，功能变化后原权限引用不再有效。
          form.setFieldValue('permission', null);
          const nextFeature = changedValues.feature as { id?: string } | null;
          setFeatureSelection({ billId, featureId: nextFeature?.id });
        }
        if (!Object.hasOwn(changedValues, 'app')) return;
        const nextApp = changedValues.app as { id?: string } | null;
        const nextAppId = nextApp?.id;
        if (nextAppId !== selectedAppId) {
          // 父分组必须属于当前应用，应用变化后原引用不再有效。
          form.setFieldValue('parent', null);
          form.setFieldValue('feature', null);
          form.setFieldValue('permission', null);
        }
        setAppSelection({ billId, appId: nextAppId });
      }}
      onExit={() => {
        useWorkbenchStore.getState().removeContentTab(appNumber, tabKey);
      }}
    />
  );
};

export default MenuEditPage;
