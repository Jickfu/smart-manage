import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import ModalEditPage from '@/domain/common/page/edit/ModalEditPage';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { orgApi } from './api';
import { orgAccess } from './permissions';
import { orgQueryKeys } from './queryKeys';
import { useOrgRefSelector } from './refSelector/useOrgRefSelector';
import type { OrgRefRecord } from './refSelector/useOrgRefSelector';
import type { OrgSaveForm, OrgType } from './types';

interface Props {
  open: boolean;
  orgId: string | null;
  defaultParent?: OrgRefRecord;
  onClose: () => void;
  onSaved: () => void;
}

const OrgEditModal = ({ open, orgId, defaultParent, onClose, onSaved }: Props) => {
  const detailQuery = useQuery({
    queryKey: orgQueryKeys.detail(orgId),
    queryFn: () => orgApi.detail(orgId!),
    enabled: Boolean(open && orgId),
    staleTime: 0,
  });
  const parentRefSelector = useOrgRefSelector(orgId ?? undefined);
  const fields = useMemo<EditField[]>(
    () => [
      {
        label: '编码',
        dataIndex: 'number',
        type: 'text',
        rules: [
          { required: true, whitespace: true, message: '编码不能为空' },
          { max: 100, message: '编码不能超过100个字符' },
          { pattern: /^[^/]+$/, message: '编码不能包含 /' },
        ],
      },
      {
        label: '名称',
        dataIndex: 'name',
        type: 'text',
        rules: [
          { required: true, whitespace: true, message: '名称不能为空' },
          { max: 100, message: '名称不能超过100个字符' },
          { pattern: /^[^/]+$/, message: '名称不能包含 /' },
        ],
      },
      {
        label: '上级组织',
        dataIndex: 'parentOrg',
        type: 'ref-selector',
        refSelector: parentRefSelector,
        placeholder: '不选择表示顶级组织',
      },
      {
        label: '组织类型',
        dataIndex: 'orgType',
        type: 'select',
        options: [
          { label: '集团', value: 'GROUP' },
          { label: '公司', value: 'COMPANY' },
          { label: '部门', value: 'DEPARTMENT' },
        ],
        rules: [{ required: true, message: '组织类型不能为空' }],
      },
      {
        label: '排序',
        dataIndex: 'sort',
        type: 'number',
        rules: [
          { required: true, message: '排序不能为空' },
          { type: 'integer', min: 0, max: 999999, message: '排序必须是0至999999的整数' },
        ],
      },
      {
        label: '描述',
        dataIndex: 'description',
        type: 'textarea',
        fullWidth: true,
        rules: [{ max: 500, message: '描述不能超过500个字符' }],
      },
    ],
    [parentRefSelector],
  );
  const detail = detailQuery.data;
  const initialValues = useMemo(() => {
    return {
      number: detail?.number ?? '',
      name: detail?.name ?? '',
      parentOrg: detail?.parent ?? defaultParent ?? null,
      orgType: detail?.orgType ?? ('DEPARTMENT' as OrgType),
      sort: detail?.sort ?? 99,
      description: detail?.description ?? '',
    };
  }, [defaultParent, detail]);
  const saveMutation = useCommandMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const form: OrgSaveForm = {
        id: orgId ?? undefined,
        version: detail?.version,
        number: String(values.number).trim(),
        name: String(values.name).trim(),
        parentId: (values.parentOrg as OrgRefRecord | null)?.id,
        orgType: String(values.orgType) as OrgType,
        sort: Number(values.sort),
        description: values.description ? String(values.description).trim() : undefined,
      };
      await orgApi.save(form);
      onSaved();
    },
    successMessage: orgId ? '组织保存成功' : '组织新增成功',
  });

  return (
    <ModalEditPage
      title={orgId ? '编辑组织' : '新增组织'}
      open={open}
      onClose={onClose}
      fields={fields}
      initialValues={initialValues}
      onSave={saveMutation.mutateAsync}
      saving={saveMutation.isPending}
      loading={detailQuery.isLoading}
      error={detailQuery.error as Error | null}
      onRetry={() => detailQuery.refetch()}
      access={orgAccess}
    />
  );
};

export default OrgEditModal;
