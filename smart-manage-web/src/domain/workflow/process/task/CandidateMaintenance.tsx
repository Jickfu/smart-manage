import { useMemo, useState } from 'react';
import { Button } from 'antd';
import { useQueryClient } from '@tanstack/react-query';
import ModalEditPage from '@/domain/common/page/edit/ModalEditPage';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import { defineAccessResource } from '@/domain/common/page/access/access';
import { usePermissionAccess } from '@/domain/common/page/access/usePermissionAccess';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { flowPost } from '../api';
import type { MaintenanceTarget } from '../api';
import { workflowUserRefSelector } from './workflowUserRefSelector';
import type { WorkflowUserOption } from './workflowUserRefSelector';

interface CandidateMaintenanceProps {
  instanceId?: string;
  taskId?: string;
}

const candidateMaintenanceAccess = defineAccessResource('workflow:process:task-monitor', {
  save: 'maintain',
});

export default function CandidateMaintenance({ instanceId, taskId }: CandidateMaintenanceProps) {
  const [open, setOpen] = useState(false);
  const [target, setTarget] = useState<MaintenanceTarget>();
  const [candidates, setCandidates] = useState<WorkflowUserOption[]>([]);
  const { can } = usePermissionAccess('workflow/process/task-monitor');
  const client = useQueryClient();
  const load = useCommandMutation({
    mutationFn: async (id: string) => {
      const value = await flowPost<MaintenanceTarget>('task/maintenance-target', { id });
      const ids = [...new Set(value.tasks.flatMap((task) => task.candidates))];
      const choices: WorkflowUserOption[] = [];
      // 角色解析后的候选集合可能超过回显接口上限；按接口边界分批，不截断原候选集合。
      for (let offset = 0; offset < ids.length; offset += 100) {
        choices.push(
          ...(await flowPost<WorkflowUserOption[]>('assignment/feedback', {
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
    },
  });
  const selectedTask =
    target?.tasks.find((task) => task.id === taskId) ??
    (target?.tasks.length === 1 ? target.tasks[0] : undefined);
  const initialValues = useMemo(
    () =>
      selectedTask
        ? {
            bill: `${target?.number ?? '-'}（审批中）`,
            task: `${selectedTask.name}（${selectedTask.id}）`,
            candidates: selectedTask.candidates.map(
              (candidateId) =>
                candidates.find(
                  (candidate) => candidate.storageId === `sm:user:${candidateId}`,
                ) ?? {
                  storageId: `sm:user:${candidateId}`,
                  handlerCode: '',
                  handlerName: candidateId,
                },
            ),
            reason: '',
          }
        : undefined,
    [candidates, selectedTask, target?.number],
  );
  const fields = useMemo<EditField[]>(
    () => [
      { dataIndex: 'bill', label: '业务单据', type: 'readonly' },
      { dataIndex: 'task', label: '当前任务', type: 'readonly' },
      {
        dataIndex: 'candidates',
        label: '候选人',
        type: 'ref-selector',
        fullWidth: true,
        tooltip: '仅调整当前活动任务的候选人，不代替候选人审批。',
        refSelector: workflowUserRefSelector,
        rules: [{ required: true, message: '至少选择一名启用用户' }],
      },
      {
        dataIndex: 'reason',
        label: '变更原因',
        type: 'textarea',
        rows: 3,
        fullWidth: true,
        rules: [
          { required: true, whitespace: true, message: '请输入变更原因' },
          { max: 1000, message: '变更原因不能超过1000个字符' },
        ],
      },
    ],
    [],
  );
  const save = useCommandMutation({
    mutationFn: (values: Record<string, unknown>) =>
      flowPost('task/candidates', {
        taskId: selectedTask!.id,
        reason: values.reason as string,
        candidateIds: (values.candidates as WorkflowUserOption[]).map((candidate) =>
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
  if (!can('workflow:process:task-monitor:maintain')) return null;
  return (
    <>
      <Button
        onClick={() => {
          setOpen(true);
          setTarget(undefined);
          setCandidates([]);
          load.reset();
          if (instanceId) load.mutate(instanceId);
        }}
      >
        维护候选人
      </Button>
      <ModalEditPage
        access={candidateMaintenanceAccess}
        open={open}
        title="当前任务候选人"
        width={600}
        fields={fields}
        initialValues={initialValues}
        loading={load.isPending}
        error={
          (load.error as Error | null) ??
          (load.isSuccess && !selectedTask ? new Error('当前任务不存在或已结束') : null)
        }
        onRetry={() => instanceId && load.mutate(instanceId)}
        onClose={() => setOpen(false)}
        onSave={async (values) => {
          await save.mutateAsync(values);
        }}
        saving={save.isPending}
      />
    </>
  );
}
