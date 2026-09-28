import { defineRefSelector } from '@/domain/common/page/edit/defineRefSelector';
import { flowPost } from '../api';

export interface WorkflowUserOption {
  storageId: string;
  handlerCode: string;
  handlerName: string;
}

// 工作流人员选择使用自己的候选查询权限，不要求审批人拥有平台用户管理权限。
export const workflowUserRefSelector = defineRefSelector<WorkflowUserOption>({
  selectorKey: 'workflow-user',
  mode: 'multiple',
  modalTitle: '选择流程参与人',
  fieldNames: { key: 'storageId', label: 'handlerName' },
  displayRender: (record) => record.handlerName,
  columns: [
    { title: '工号', dataIndex: 'handlerCode', width: 160 },
    { title: '姓名', dataIndex: 'handlerName' },
  ],
  fetchFn: async (params) => {
    const result = await flowPost<{ handlerAuths: { rows: WorkflowUserOption[]; total: number } }>(
      'assignment/candidates',
      {
        handlerType: '用户',
        handlerName: params.keyword,
        pageNum: params.pageNum,
        pageSize: params.pageSize,
      },
    );
    return { records: result.handlerAuths.rows, total: result.handlerAuths.total };
  },
});
