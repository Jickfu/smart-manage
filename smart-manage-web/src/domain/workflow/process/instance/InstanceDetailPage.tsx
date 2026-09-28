import { useRef, useState } from 'react';
import { Button, Card, Descriptions, Form, Input, Radio, Select, Table, Tabs } from 'antd';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import AppModal from '@/domain/common/component/AppModal';
import { EditPageShell } from '@/domain/common/page/EditPageShell';
import { FormFieldCell, FormFieldGrid } from '@/domain/common/page/edit/FormFieldLayout';
import { usePermissionAccess } from '@/domain/common/page/access/usePermissionAccess';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { flowPost, stateLabels, workflowActionLabels, workflowApi } from '../api';
import type { InstanceAdminDetail } from '../api';
import CandidateMaintenance from '../task/CandidateMaintenance';
import { instanceAccess } from './permissions';
import '../workflow.css';

type Command = 'suspend' | 'resume' | 'terminate' | 'jump' | 'variables' | 'script-retry';
type CommandValues = {
  reason: string;
  taskId?: string;
  action?: 'JUMP' | 'RETURN';
  targetNodeCode?: string;
  changes?: string;
  removals?: string;
};

const commandTitles: Record<Command, string> = {
  suspend: '挂起流程实例',
  resume: '恢复流程实例',
  terminate: '终止流程实例',
  jump: '调整流程节点',
  variables: '修改流程变量',
  'script-retry': '重试脚本节点',
};

export default function InstanceDetailPage(props: PageComponentProps) {
  const [command, setCommand] = useState<Command>();
  const [commandTaskId, setCommandTaskId] = useState<string>();
  const commandIntent = useRef({ content: '', requestId: '' });
  const [form] = Form.useForm<CommandValues>();
  const client = useQueryClient();
  const confirm = useOperationConfirm();
  const { can, loading: permissionsLoading } = usePermissionAccess(instanceAccess.prefix);
  const query = useQuery({
    queryKey: ['workflow', 'instance-admin-detail', props.billId],
    queryFn: () => workflowApi.instanceAdminDetail(props.billId!),
    enabled: props.active && Boolean(props.billId),
    meta: { errorPresentation: 'local-initial' },
  });
  const mutation = useCommandMutation({
    mutationFn: async (values: CommandValues) => {
      if (!command || !props.billId) return;
      const content = JSON.stringify([command, props.billId, commandTaskId, values]);
      if (commandIntent.current.content !== content) {
        commandIntent.current = { content, requestId: crypto.randomUUID() };
      }
      const base = {
        instanceId: props.billId,
        reason: values.reason,
        requestId: commandIntent.current.requestId,
      };
      if (command === 'jump') {
        return flowPost(`instance/${command}`, {
          ...base,
          taskId: values.taskId,
          action: values.action,
          targetNodeCode: values.targetNodeCode,
        });
      }
      if (command === 'script-retry') {
        return flowPost(`instance/${command}`, { ...base, taskId: commandTaskId });
      }
      if (command === 'variables') {
        let changes: Record<string, unknown> = {};
        if (values.changes?.trim()) changes = JSON.parse(values.changes) as Record<string, unknown>;
        return flowPost(`instance/${command}`, {
          ...base,
          changes,
          removals:
            values.removals
              ?.split(/[,\n]/)
              .map((value) => value.trim())
              .filter(Boolean) ?? [],
        });
      }
      return flowPost(`instance/${command}`, base);
    },
    successMessage: '流程操作已完成并记录审计',
    onSuccess: async () => {
      commandIntent.current = { content: '', requestId: '' };
      form.resetFields();
      setCommand(undefined);
      setCommandTaskId(undefined);
      await Promise.all([
        client.invalidateQueries({ queryKey: ['workflow', 'instance-admin-detail', props.billId] }),
        client.invalidateQueries({ queryKey: ['workflow', 'instances'] }),
        client.invalidateQueries({ queryKey: ['workflow', 'task-monitor'] }),
        client.invalidateQueries({ queryKey: ['workflow', 'flow-logs'] }),
      ]);
    },
  });
  const detail = query.data;
  const openCommand = (next: Command, taskId?: string) => {
    form.resetFields();
    const onlyTaskId = query.data?.run.tasks.length === 1 ? query.data.run.tasks[0]?.id : undefined;
    form.setFieldsValue({
      action: 'JUMP',
      changes: '{}',
      removals: '',
      taskId: taskId ?? onlyTaskId,
    });
    setCommand(next);
    setCommandTaskId(taskId);
  };
  const closeCommand = () => {
    if (mutation.isPending) return;
    commandIntent.current = { content: '', requestId: '' };
    form.resetFields();
    setCommand(undefined);
    setCommandTaskId(undefined);
  };
  const submit = async (values: CommandValues) => {
    if (command === 'terminate') {
      const accepted = await confirm({
        type: 'destructive',
        title: '确认终止当前流程实例',
        description: '终止后当前审批轮次结束且不能恢复，业务单据将自行处理流程结果。',
        confirmText: '终止流程',
      });
      if (!accepted) return;
    }
    await mutation.mutateAsync(values);
  };
  return (
    <div className="sm-workflow-instance-detail">
      <EditPageShell
        title={detail ? `${detail.number} · 流程实例` : '流程实例'}
        loading={query.isLoading || permissionsLoading}
        error={getBlockingQueryError(query)}
        onRetry={() => props.active && void query.refetch()}
        actions={
          detail && (
            <>
              {detail.run.state === 'APPROVING' &&
                detail.run.active &&
                can(instanceAccess.permissions.suspend) && (
                  <Button type="primary" onClick={() => openCommand('suspend')}>
                    挂起
                  </Button>
                )}
              {detail.run.state === 'APPROVING' &&
                !detail.run.active &&
                can(instanceAccess.permissions.resume) && (
                  <Button type="primary" onClick={() => openCommand('resume')}>
                    恢复
                  </Button>
                )}
              {detail.run.state === 'APPROVING' && can(instanceAccess.permissions.jump) && (
                <Button type="primary" onClick={() => openCommand('jump')}>
                  节点调整
                </Button>
              )}
              {detail.run.state === 'APPROVING' &&
                !detail.run.active &&
                can(instanceAccess.permissions.variables) && (
                  <Button type="primary" onClick={() => openCommand('variables')}>
                    修改变量
                  </Button>
                )}
              {detail.run.state === 'APPROVING' && can(instanceAccess.permissions.terminate) && (
                <Button danger onClick={() => openCommand('terminate')}>
                  终止
                </Button>
              )}
              <Button
                onClick={() =>
                  void useWorkbenchStore.getState().removeContentTab(props.appNumber, props.tabKey)
                }
              >
                关闭
              </Button>
            </>
          )
        }
      >
        {detail && <InstanceContent detail={detail} onCommand={openCommand} can={can} />}
      </EditPageShell>
      <AppModal
        open={Boolean(command)}
        title={command ? commandTitles[command] : ''}
        width={600}
        onCancel={closeCommand}
        closeDisabled={mutation.isPending}
        footer={
          <>
            <Button onClick={closeCommand} disabled={mutation.isPending}>
              取消
            </Button>
            <Button
              type="primary"
              danger={command === 'terminate'}
              loading={mutation.isPending}
              onClick={() => form.submit()}
            >
              确定
            </Button>
          </>
        }
      >
        <Form
          form={form}
          layout="vertical"
          className="sm-edit-form"
          onFinish={(values) => void submit(values)}
        >
          <FormFieldGrid maxColumns={1}>
            {command === 'jump' && (
              <>
                <FormFieldCell>
                  <Form.Item
                    className="sm-edit-field-content"
                    name="taskId"
                    label="当前任务"
                    rules={[{ required: true, message: '请选择要调整的当前任务' }]}
                  >
                    <Select
                      variant="underlined"
                      options={(query.data?.run.tasks ?? []).map((task) => ({
                        value: task.id,
                        label: `${task.name}（${task.nodeCode}）`,
                      }))}
                    />
                  </Form.Item>
                </FormFieldCell>
                <FormFieldCell>
                  <Form.Item
                    className="sm-edit-field-content"
                    name="action"
                    label="调整方向"
                    rules={[{ required: true, message: '请选择调整方向' }]}
                  >
                    <Radio.Group
                      options={[
                        { label: '跳转到未经过节点', value: 'JUMP' },
                        { label: '退回节点', value: 'RETURN' },
                      ]}
                    />
                  </Form.Item>
                </FormFieldCell>
                <Form.Item noStyle shouldUpdate={(before, after) => before.action !== after.action}>
                  {({ getFieldValue }) => {
                    const visited = new Set(
                      query.data?.run.history.map((item) => item.nodeCode) ?? [],
                    );
                    const backward = getFieldValue('action') === 'RETURN';
                    return (
                      <FormFieldCell>
                        <Form.Item
                          className="sm-edit-field-content"
                          name="targetNodeCode"
                          label="目标节点"
                          rules={[{ required: true, message: '请选择目标节点' }]}
                        >
                          <Select
                            variant="underlined"
                            options={(query.data?.run.nodeTargets ?? [])
                              .filter((target) => backward === visited.has(target.code))
                              .map((target) => ({
                                value: target.code,
                                label: `${target.name}（${target.code}${target.script ? ' · 脚本' : ''}）`,
                              }))}
                          />
                        </Form.Item>
                      </FormFieldCell>
                    );
                  }}
                </Form.Item>
              </>
            )}
            {command === 'variables' && (
              <>
                <FormFieldCell>
                  <Form.Item
                    className="sm-edit-field-content"
                    name="changes"
                    label="新增或修改变量（JSON 对象）"
                    rules={[
                      {
                        validator: async (_rule, value: string) => {
                          try {
                            if (value?.trim()) {
                              const parsed = JSON.parse(value) as unknown;
                              if (!parsed || Array.isArray(parsed) || typeof parsed !== 'object')
                                throw new Error();
                            }
                          } catch {
                            throw new Error('请输入有效的 JSON 对象');
                          }
                        },
                      },
                    ]}
                  >
                    <Input.TextArea variant="underlined" rows={8} />
                  </Form.Item>
                </FormFieldCell>
                <FormFieldCell>
                  <Form.Item
                    className="sm-edit-field-content"
                    name="removals"
                    label="删除变量名（逗号或换行分隔）"
                  >
                    <Input.TextArea variant="underlined" rows={3} />
                  </Form.Item>
                </FormFieldCell>
              </>
            )}
            <FormFieldCell>
              <Form.Item
                className="sm-edit-field-content"
                name="reason"
                label="操作原因"
                rules={[
                  { required: true, whitespace: true, message: '请输入操作原因' },
                  { max: 500, message: '操作原因不能超过500个字符' },
                ]}
              >
                <Input.TextArea variant="underlined" rows={3} maxLength={500} showCount />
              </Form.Item>
            </FormFieldCell>
          </FormFieldGrid>
        </Form>
      </AppModal>
    </div>
  );
}

function InstanceContent({
  detail,
  onCommand,
  can,
}: {
  detail: InstanceAdminDetail;
  onCommand: (command: Command, taskId?: string) => void;
  can: (permission: string) => boolean;
}) {
  const taskColumns = [
    { title: '节点编码', dataIndex: 'nodeCode', width: 180 },
    { title: '节点名称', dataIndex: 'name' },
    {
      title: '候选人',
      dataIndex: 'candidates',
      render: (values: string[]) => values.map((id) => detail.actorNames[id] ?? id).join('、'),
    },
    {
      title: '维护',
      key: 'maintain',
      width: 120,
      render: (_value: unknown, task: InstanceAdminDetail['run']['tasks'][number]) => (
        <>
          {task.script && !detail.run.active && can(instanceAccess.permissions.scriptRetry) && (
            <Button type="link" onClick={() => onCommand('script-retry', task.id)}>
              重试脚本
            </Button>
          )}
          {!task.script && <CandidateMaintenance instanceId={detail.id} taskId={task.id} />}
        </>
      ),
    },
  ];
  return (
    <div className="sm-workflow-instance-layout">
      <Card className="sm-workflow-instance-main" title="流程与单据信息">
        <Descriptions column={2} size="small">
          <Descriptions.Item label="实例 ID">{detail.id}</Descriptions.Item>
          <Descriptions.Item label="状态">{stateLabels[detail.run.state]}</Descriptions.Item>
          <Descriptions.Item label="业务类型">{detail.businessType}</Descriptions.Item>
          <Descriptions.Item label="业务单据 ID">{detail.businessId}</Descriptions.Item>
          <Descriptions.Item label="申请人">
            {detail.actorNames[detail.applicantId] ?? detail.applicantId}
          </Descriptions.Item>
          <Descriptions.Item label="所属组织">{detail.orgId}</Descriptions.Item>
        </Descriptions>
        <Card size="small" title="当前任务">
          <Table
            rowKey="id"
            size="small"
            pagination={false}
            dataSource={detail.run.tasks}
            columns={taskColumns}
          />
        </Card>
        <Card size="small" title="流程变量">
          <pre className="sm-workflow-variable-json">
            {JSON.stringify(detail.run.variables, null, 2)}
          </pre>
        </Card>
      </Card>
      <Card className="sm-workflow-instance-side">
        <Tabs
          items={[
            {
              key: 'history',
              label: '流转轨迹',
              children: (
                <Table
                  rowKey="id"
                  size="small"
                  pagination={false}
                  dataSource={detail.run.history}
                  columns={[
                    { title: '节点', dataIndex: 'nodeName' },
                    {
                      title: '动作',
                      dataIndex: 'action',
                      width: 140,
                      render: (action: string) => workflowActionLabels[action] ?? action,
                    },
                    {
                      title: '处理人',
                      dataIndex: 'actorId',
                      width: 100,
                      render: (id?: string) => (id ? (detail.actorNames[id] ?? id) : '-'),
                    },
                    { title: '意见', dataIndex: 'opinion' },
                    { title: '时间', dataIndex: 'time', width: 170 },
                  ]}
                />
              ),
            },
            {
              key: 'candidate-changes',
              label: '候选人维护',
              children: (
                <Table
                  rowKey="id"
                  size="small"
                  pagination={false}
                  dataSource={detail.candidateChanges}
                  columns={[
                    {
                      title: '操作人',
                      dataIndex: 'operatorId',
                      width: 100,
                      render: (id: string) => detail.actorNames[id] ?? id,
                    },
                    {
                      title: '变更前',
                      dataIndex: 'beforeCandidates',
                      render: (ids: string[]) =>
                        ids.map((id) => detail.actorNames[id] ?? id).join('、'),
                    },
                    {
                      title: '变更后',
                      dataIndex: 'afterCandidates',
                      render: (ids: string[]) =>
                        ids.map((id) => detail.actorNames[id] ?? id).join('、'),
                    },
                    { title: '原因', dataIndex: 'reason' },
                    { title: '时间', dataIndex: 'time', width: 170 },
                  ]}
                />
              ),
            },
            {
              key: 'operations',
              label: '管理记录',
              children: (
                <Table
                  rowKey="id"
                  size="small"
                  pagination={false}
                  dataSource={detail.operations}
                  columns={[
                    {
                      title: '动作',
                      dataIndex: 'action',
                      width: 140,
                      render: (action: string) => workflowActionLabels[action] ?? action,
                    },
                    {
                      title: '操作人',
                      dataIndex: 'operatorId',
                      width: 100,
                      render: (id: string) => detail.actorNames[id] ?? id,
                    },
                    { title: '原因', dataIndex: 'reason' },
                    { title: '时间', dataIndex: 'time', width: 170 },
                  ]}
                />
              ),
            },
          ]}
        />
      </Card>
    </div>
  );
}
