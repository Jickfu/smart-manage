import { useState } from 'react';
import { Button, Form, Input, Select } from 'antd';
import { useQueryClient } from '@tanstack/react-query';
import AppModal from '@/domain/common/component/AppModal';
import RefSelector from '@/domain/common/component/RefSelector';
import { defineRefSelector } from '@/domain/common/page/edit/defineRefSelector';
import { usePermissionAccess } from '@/domain/common/page/access/usePermissionAccess';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { flowPost } from '../api';
import type { MaintenanceTarget } from '../api';

interface CandidateOption {
  storageId: string;
  handlerCode: string;
  handlerName: string;
}

// 复用平台选择交互，候选查询仍使用工作流权限，不能要求维护人员拥有用户管理权限。
const candidateSelector = defineRefSelector<CandidateOption>({
  selectorKey: 'workflow-candidate-maintenance',
  mode: 'multiple',
  modalTitle: '选择当前任务候选人',
  fieldNames: { key: 'storageId', label: 'handlerName' },
  displayRender: (record) => record.handlerName,
  columns: [
    { title: '工号', dataIndex: 'handlerCode', width: 160 },
    { title: '姓名', dataIndex: 'handlerName' },
  ],
  fetchFn: async (params) => {
    const result = await flowPost<{ handlerAuths: { rows: CandidateOption[]; total: number } }>(
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

export default function CandidateMaintenance() {
  const [open, setOpen] = useState(false);
  const [target, setTarget] = useState<MaintenanceTarget>();
  const [candidates, setCandidates] = useState<CandidateOption[]>([]);
  const [form] = Form.useForm();
  const { can } = usePermissionAccess('workflow:process:task');
  const client = useQueryClient();
  const load = useCommandMutation({
    mutationFn: async (id: string) => {
      const value = await flowPost<MaintenanceTarget>('task/maintenance-target', { id });
      const ids = [...new Set(value.tasks.flatMap((task) => task.candidates))];
      const choices: CandidateOption[] = [];
      // 角色解析后的候选集合可能超过回显接口上限；按接口边界分批，不截断原候选集合。
      for (let offset = 0; offset < ids.length; offset += 100) {
        choices.push(
          ...(await flowPost<CandidateOption[]>('assignment/feedback', {
            storageIds: ids
              .slice(offset, offset + 100)
              .map((candidateId) => `sm:user:${candidateId}`)
              .join(','),
          })),
        );
      }
      return { value, choices };
    },
    onSuccess: ({ value, choices }) => {
      setTarget(value);
      setCandidates(choices);
      form.setFieldsValue({ taskId: undefined, candidates: [], reason: '' });
    },
  });
  const save = useCommandMutation({
    mutationFn: (values: { taskId: string; candidates: CandidateOption[]; reason: string }) =>
      flowPost('task/candidates', {
        taskId: values.taskId,
        reason: values.reason,
        candidateIds: values.candidates.map((candidate) =>
          candidate.storageId.replace('sm:user:', ''),
        ),
        instanceId: target!.id,
      }),
    successMessage: '候选人已调整，变更已记录',
    onSuccess: async () => {
      setOpen(false);
      await client.invalidateQueries({ queryKey: ['workflow'] });
    },
  });
  if (!can('workflow:process:task:maintain')) return null;
  return (
    <>
      <Button
        onClick={() => {
          setOpen(true);
          setTarget(undefined);
          form.resetFields();
        }}
      >
        维护候选人
      </Button>
      <AppModal
        open={open}
        title="维护当前任务候选人"
        onCancel={() => setOpen(false)}
        closeDisabled={save.isPending}
        footer={
          <>
            <Button onClick={() => setOpen(false)} disabled={save.isPending}>
              取消
            </Button>
            <Button
              type="primary"
              loading={save.isPending}
              disabled={!target?.tasks.length}
              onClick={() => form.submit()}
            >
              保存变更
            </Button>
          </>
        }
      >
        <p>仅变更候选人，不代审批。流程实例 ID 可由申请人提供；维护权限不授予单据详情访问权。</p>
        <Input.Search
          placeholder="流程实例 ID"
          enterButton="读取任务"
          loading={load.isPending}
          onSearch={(value) => {
            if (value.trim()) load.mutate(value.trim());
          }}
        />
        {target && (
          <>
            <p>
              单据：{target.number}（{target.tasks.length ? '审批中' : '无活动任务'}）
            </p>
            <Form form={form} layout="vertical" onFinish={(values) => save.mutate(values)}>
              <Form.Item name="taskId" label="当前任务" rules={[{ required: true }]}>
                <Select
                  options={target.tasks.map((task) => ({ value: task.id, label: task.name }))}
                  onChange={(value) =>
                    form.setFieldValue(
                      'candidates',
                      (target.tasks.find((task) => task.id === value)?.candidates ?? []).map(
                        (candidateId) =>
                          candidates.find(
                            (candidate) => candidate.storageId === `sm:user:${candidateId}`,
                          ) ?? {
                            storageId: `sm:user:${candidateId}`,
                            handlerCode: '',
                            handlerName: candidateId,
                          },
                      ),
                    )
                  }
                />
              </Form.Item>
              <Form.Item
                name="candidates"
                label="调整后的候选人"
                rules={[{ required: true, message: '至少选择一名启用用户' }]}
              >
                <RefSelector<Record<string, unknown>> {...candidateSelector} />
              </Form.Item>
              <Form.Item
                name="reason"
                label="变更原因"
                rules={[{ required: true, whitespace: true, max: 1000 }]}
              >
                <Input.TextArea rows={4} maxLength={1000} />
              </Form.Item>
            </Form>
          </>
        )}
      </AppModal>
    </>
  );
}
