import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import ModalEditPage from '@/domain/common/page/ModalEditPage';
import type { EditField } from '@/domain/common/page/EditPage';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { orgApi } from './api';
import { orgAccess } from './permissions';
import { orgQueryKeys } from './queryKeys';
import { useOrgRefSelector } from './refSelector/useOrgRefSelector';
import type { OrgRefRecord } from './refSelector/useOrgRefSelector';
import type { OrgSaveForm, OrgTreeNode, OrgType } from './types';

interface Props {
  open: boolean;
  orgId: string | null;
  defaultParentId?: string;
  onClose: () => void;
  onSaved: () => void;
}

const flattenTree = (nodes: OrgTreeNode[]): OrgTreeNode[] => {
  const result: OrgTreeNode[] = [];
  const visit = (items: OrgTreeNode[]) => {
    for (const item of items) {
      result.push(item);
      visit(item.children);
    }
  };
  visit(nodes);
  return result;
};

const OrgEditModal = ({ open, orgId, defaultParentId, onClose, onSaved }: Props) => {
  const detailQuery = useQuery({
    queryKey: orgQueryKeys.detail(orgId),
    queryFn: () => orgApi.detail(orgId!),
    enabled: Boolean(open && orgId),
    staleTime: 0,
  });
  const treeQuery = useQuery({
    queryKey: orgQueryKeys.tree(),
    queryFn: () => orgApi.tree(false),
    enabled: open,
  });
  const parentRefSelector = useOrgRefSelector(orgId ?? undefined);
  const flatTreeNodes = useMemo(() => flattenTree(treeQuery.data ?? []), [treeQuery.data]);
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
    const parentId = detail?.parentId ?? defaultParentId;
    const parent = flatTreeNodes.find((node) => node.id === parentId);
    return {
      number: detail?.number ?? '',
      name: detail?.name ?? '',
      parentOrg: parent
        ? ({ id: parent.id, number: parent.number, name: parent.name } satisfies OrgRefRecord)
        : null,
      orgType: detail?.orgType ?? ('DEPARTMENT' as OrgType),
      sort: detail?.sort ?? 99,
      description: detail?.description ?? '',
    };
  }, [defaultParentId, detail, flatTreeNodes]);
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
      loading={detailQuery.isLoading || treeQuery.isLoading}
      error={(detailQuery.error ?? treeQuery.error) as Error | null}
      onRetry={() => Promise.all([detailQuery.refetch(), treeQuery.refetch()])}
      access={orgAccess}
    />
  );
};

export default OrgEditModal;
