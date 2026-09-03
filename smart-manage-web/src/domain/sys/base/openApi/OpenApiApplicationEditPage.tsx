import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { useEffect, useMemo, useRef, useState } from 'react';
import type { Key } from 'react';
import type { ColumnsType } from 'antd/es/table';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import EditPage from '@/domain/common/page/edit/EditPage';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import { EditFormFields } from '@/domain/common/page/edit/EditFormFields';
import { EditableDetailTable } from '@/domain/common/component/EditableDetailTable';
import RefSelector from '@/domain/common/component/RefSelector';
import { createBillTabKey } from '@/domain/common/page/tab/tabKeys';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { usePermissionAccess } from '@/domain/common/page/access/usePermissionAccess';
import { EditSectionActionButton } from '@/domain/common/page/edit/EditSectionActionButton';
import { useWorkbenchStore } from '@/stores/workbench';
import { useUserRefSelector } from '@/domain/sys/base/user/refSelector/useUserRefSelector';
import { useOrgRefSelector } from '@/domain/sys/base/org/refSelector/useOrgRefSelector';
import { openApiPlatformApi } from './api';
import { openApiApplicationAccess } from './permissions';
import { openApiQueryKeys } from './queryKeys';
import { buildCatalogTree, catalogFilterFromTreeKey } from './catalogHierarchy';
import OpenApiCredentialSection from './OpenApiCredentialSection';
import type { OpenApiCredentialSectionRef } from './OpenApiCredentialSection';
import type {
  OpenApiApplicationForm,
  OpenApiCredential,
  OpenApiRelease,
  ReferenceRecord,
} from './types';
import './OpenApiPage.css';

type CatalogRecord = OpenApiRelease & Record<string, unknown>;

const OpenApiApplicationEditPage = (props: PageComponentProps) => {
  const { appNumber, tabKey, billId, operationType } = props;
  const isAdd = operationType === OperationType.ADDNEW;
  const queryClient = useQueryClient();
  const feedback = useOperationFeedback();
  const replaceContentTab = useWorkbenchStore((state) => state.replaceContentTab);
  const activateContentTab = useWorkbenchStore((state) => state.activateContentTab);
  const userRefSelector = useUserRefSelector({ title: '选择代理用户' });
  const orgRefSelector = useOrgRefSelector();
  const { can } = usePermissionAccess(openApiApplicationAccess.prefix);
  const credentialRef = useRef<OpenApiCredentialSectionRef>(null);
  const initializedGrantVersion = useRef<string | undefined>(undefined);
  const [selectedCredential, setSelectedCredential] = useState<OpenApiCredential>();
  const [authorizedApis, setAuthorizedApis] = useState<OpenApiRelease[]>([]);
  const [selectedGrantKeys, setSelectedGrantKeys] = useState<Key[]>([]);
  const [grantRevision, setGrantRevision] = useState(0);
  const detailQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: openApiQueryKeys.applicationDetail(billId),
    queryFn: () => openApiPlatformApi.applicationDetail(billId!),
    enabled: Boolean(billId),
  });
  const hierarchyQuery = useQuery({
    queryKey: openApiQueryKeys.catalogList({ hierarchy: true }),
    queryFn: openApiPlatformApi.catalogHierarchy,
  });
  const catalogQuery = useQuery({
    queryKey: openApiQueryKeys.catalogList({ grantOptions: true }),
    queryFn: () => openApiPlatformApi.catalogList({ pageNum: 1, pageSize: 1000 }),
  });
  const detail = detailQuery.data;

  useEffect(() => {
    if (!detail || !catalogQuery.data) return;
    const initializationKey = `${detail.id}:${detail.version}`;
    if (initializedGrantVersion.current === initializationKey) return;
    const operationKeys = new Set(detail.operationKeys ?? []);
    setAuthorizedApis(
      catalogQuery.data.records.filter((release) => operationKeys.has(release.operationKey)),
    );
    initializedGrantVersion.current = initializationKey;
  }, [catalogQuery.data, detail]);

  const basicFields = useMemo<EditField[]>(
    () => [
      {
        label: '系统编码',
        dataIndex: 'number',
        type: 'text',
        rules: [
          { required: true, message: '系统编码不能为空' },
          { pattern: /^[A-Za-z0-9._-]+$/, message: '仅允许字母、数字、点、下划线和横线' },
        ],
      },
      { label: '系统名称', dataIndex: 'name', type: 'text', rules: [{ required: true }] },
      {
        label: '代理用户',
        dataIndex: 'proxyUser',
        type: 'ref-selector',
        refSelector: userRefSelector,
        rules: [{ required: true, message: '请选择代理用户' }],
      },
      {
        label: '固定组织',
        dataIndex: 'proxyOrg',
        type: 'ref-selector',
        refSelector: orgRefSelector,
        rules: [{ required: true, message: '请选择固定组织' }],
      },
      { label: '描述', dataIndex: 'description', type: 'textarea', fullWidth: true },
    ],
    [orgRefSelector, userRefSelector],
  );
  const policyFields = useMemo<EditField[]>(
    () => [
      {
        label: '认证方式',
        dataIndex: 'authenticationType',
        type: 'select',
        options: [{ label: 'HMAC-SHA256 签名认证', value: 'HMAC_SHA256' }],
        rules: [{ required: true }],
      },
      {
        label: '报文加密',
        dataIndex: 'encryptionAlgorithm',
        type: 'select',
        options: [
          { label: '无加密', value: 'NONE' },
          { label: 'AES-256-GCM', value: 'AES_256_GCM' },
          { label: 'SM4-GCM（国密）', value: 'SM4_GCM' },
        ],
        rules: [{ required: true }],
      },
      {
        label: 'IP 访问策略',
        dataIndex: 'ipPolicyMode',
        type: 'select',
        options: [
          { label: '不限制', value: 'DISABLED' },
          { label: '白名单', value: 'WHITELIST' },
          { label: '黑名单', value: 'BLACKLIST' },
        ],
        rules: [{ required: true }],
      },
      {
        label: 'IP 地址或 CIDR',
        dataIndex: 'ipRanges',
        type: 'textarea',
        fullWidth: true,
        placeholder: '每行一个地址或网段，例如 10.10.0.0/16',
      },
    ],
    [],
  );
  const initialValues = useMemo(
    () =>
      detail
        ? { ...detail, proxyUser: detail.proxyUser, proxyOrg: detail.proxyOrg }
        : {
            authenticationType: 'HMAC_SHA256',
            encryptionAlgorithm: 'AES_256_GCM',
            ipPolicyMode: 'DISABLED',
          },
    [detail],
  );
  const save = useCommandMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const proxyUser = values.proxyUser as ReferenceRecord;
      const proxyOrg = values.proxyOrg as ReferenceRecord;
      return openApiPlatformApi.applicationSave({
        ...(values as unknown as OpenApiApplicationForm),
        id: billId,
        version: detail?.version,
        number: String(values.number).trim(),
        name: String(values.name).trim(),
        proxyUserId: proxyUser.id,
        proxyOrgId: proxyOrg.id,
        ipRanges: String(values.ipRanges ?? '').trim() || undefined,
        description: String(values.description ?? '').trim() || undefined,
        operationKeys: authorizedApis.map((release) => release.operationKey),
      });
    },
    onSuccess: async (id) => {
      if (isAdd) {
        const nextKey = createBillTabKey(props.componentKey, id);
        replaceContentTab(appNumber, tabKey, {
          key: nextKey,
          closable: true,
          componentKey: props.componentKey,
          pageType: 'EDIT',
          operationType: OperationType.EDIT,
          billId: id,
        });
        activateContentTab(appNumber, nextKey);
      }
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: openApiQueryKeys.applications() }),
        queryClient.invalidateQueries({ queryKey: openApiQueryKeys.applicationDetail(id) }),
      ]);
      feedback.success(isAdd ? '新增成功，可以继续创建访问凭据' : '保存成功');
    },
  });
  const grantColumns: ColumnsType<OpenApiRelease> = [
    { title: 'API 编码', dataIndex: 'apiNumber', width: 210 },
    { title: 'API 名称', dataIndex: 'name', width: 220 },
    { title: 'URL', dataIndex: 'path', width: 360 },
    { title: '功能', dataIndex: 'featureName', width: 140 },
    { title: '所属应用', dataIndex: 'applicationName' },
  ];
  const catalogTree = buildCatalogTree(hierarchyQuery.data ?? []);
  const grantSelector = (
    <RefSelector<CatalogRecord>
      value={authorizedApis as CatalogRecord[]}
      onChange={(value) => {
        setAuthorizedApis((value ?? []) as OpenApiRelease[]);
        setSelectedGrantKeys([]);
        setGrantRevision((current) => current + 1);
      }}
      mode="tree-table-multiple"
      selectorKey={['openapi', 'grant-selector']}
      modalTitle="增加 API 授权"
      fieldNames={{ key: 'operationKey', label: 'name' }}
      displayRender={(record) => `${record.apiNumber} ${record.name}`}
      treeData={catalogTree as unknown as Record<string, unknown>[]}
      defaultTreeKey={catalogTree[0]?.key}
      treeFieldNames={{ key: 'key', title: 'title', children: 'children' }}
      fetchFn={async ({ pageNum, pageSize, keyword, parentId }) => {
        const result = await openApiPlatformApi.catalogList({
          pageNum,
          pageSize,
          keyword,
          ...catalogFilterFromTreeKey(parentId),
        });
        return { records: result.records as CatalogRecord[], total: result.total };
      }}
      columns={[
        { title: 'API 编码', dataIndex: 'apiNumber', width: 200 },
        { title: 'API 名称', dataIndex: 'name', width: 200 },
        { title: 'URL', dataIndex: 'path', width: 340 },
        { title: '功能', dataIndex: 'featureName', width: 130 },
        { title: '所属应用', dataIndex: 'applicationName' },
      ]}
      trigger={<EditSectionActionButton>增加</EditSectionActionButton>}
    />
  );

  return (
    <EditPage
      access={openApiApplicationAccess}
      title="第三方应用"
      sections={[
        {
          key: 'basic',
          label: '基本信息',
          content: (editable) => <EditFormFields fields={basicFields} editable={editable} />,
        },
        {
          key: 'access-policy',
          label: '访问策略',
          content: (editable) => <EditFormFields fields={policyFields} editable={editable} />,
        },
        ...(billId
          ? [
              {
                key: 'credentials',
                label: '访问凭据',
                content: (editable: boolean) =>
                  detail ? (
                    <OpenApiCredentialSection
                      ref={credentialRef}
                      application={detail}
                      editable={editable && can(openApiApplicationAccess.permissions.credential)}
                      onSelectionChange={setSelectedCredential}
                    />
                  ) : null,
                extra: (editable: boolean) =>
                  detail && editable && can(openApiApplicationAccess.permissions.credential) ? (
                    <>
                      <EditSectionActionButton onClick={() => credentialRef.current?.create()}>
                        创建凭据
                      </EditSectionActionButton>
                      <EditSectionActionButton
                        disabled={!selectedCredential}
                        danger={selectedCredential?.enabled}
                        onClick={() => credentialRef.current?.toggleSelected()}
                      >
                        {selectedCredential?.enabled ? '停用' : '启用'}
                      </EditSectionActionButton>
                    </>
                  ) : undefined,
              },
            ]
          : []),
        {
          key: 'api-grants',
          label: 'API 授权',
          content: (editable) => (
            <EditableDetailTable<OpenApiRelease>
              editable={editable && can(openApiApplicationAccess.permissions.grant)}
              rowKey="operationKey"
              columns={grantColumns}
              dataSource={authorizedApis}
              selectedRowKeys={selectedGrantKeys}
              onSelectedRowKeysChange={setSelectedGrantKeys}
            />
          ),
          extra: (editable) =>
            editable && can(openApiApplicationAccess.permissions.grant) ? (
              <>
                {grantSelector}
                <EditSectionActionButton
                  danger
                  disabled={selectedGrantKeys.length === 0}
                  onClick={() => {
                    const removedKeys = new Set(selectedGrantKeys.map(String));
                    setAuthorizedApis((current) =>
                      current.filter((release) => !removedKeys.has(release.operationKey)),
                    );
                    setSelectedGrantKeys([]);
                    setGrantRevision((current) => current + 1);
                  }}
                >
                  删除
                </EditSectionActionButton>
              </>
            ) : undefined,
        },
      ]}
      initialValues={initialValues}
      operationType={operationType ?? OperationType.EDIT}
      closeGuard={{ appNumber, tabKey }}
      loading={detailQuery.isLoading}
      error={getBlockingQueryError(detailQuery) as Error | null}
      onRetry={() => detailQuery.refetch()}
      onSave={
        can(openApiApplicationAccess.permissions.save) &&
        can(openApiApplicationAccess.permissions.grant)
          ? async (values) => save.mutateAsync(values).then(() => undefined)
          : undefined
      }
      saving={save.isPending}
      dirtyRevision={grantRevision}
      onExit={() => useWorkbenchStore.getState().removeContentTab(appNumber, tabKey)}
    />
  );
};

export default OpenApiApplicationEditPage;
