import { useRef, useState } from 'react';
import { generateUUID } from '@/utils';
import { Button, Form, Input, Select, Tabs } from 'antd';
import { useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { workflowActionLabels, workflowApi, stateLabels } from '../api';
import type { ApprovalDetail } from '../api';
import '../workflow.css';
import TaskCooperationActions from './TaskCooperationActions';

export default function ApprovalPanel({
  detail,
  onDirty,
  onCompleted,
  initialTab = 'task',
}: {
  detail: ApprovalDetail;
  onDirty?: (dirty: boolean) => void;
  onCompleted?: () => Promise<void>;
  initialTab?: 'task' | 'history';
}) {
  const [form] = Form.useForm();
  const [tab, setTab] = useState(initialTab);
  const client = useQueryClient();
  const intent = useRef({ content: '', requestId: '' });
  const mutation = useCommandMutation({
    mutationFn: (values: { action: string; opinion?: string }) => {
      const data = { ...values, instanceId: detail.id, taskId: detail.currentTaskId! };
      const content = JSON.stringify(data);
      if (intent.current.content !== content)
        intent.current = { content, requestId: generateUUID() };
      return workflowApi.approve({ ...data, requestId: intent.current.requestId });
    },
    successMessage: '审批处理成功',
    onSuccess: async () => {
      form.resetFields();
      onDirty?.(false);
      await client.invalidateQueries({ queryKey: ['workflow'] });
      await onCompleted?.();
    },
  });
  return (
    <div className="sm-workflow-panel">
      <Tabs
        activeKey={tab}
        onChange={(value) => setTab(value === 'history' ? 'history' : 'task')}
        items={[
          {
            key: 'task',
            label: '任务处理',
            children: (
              <>
                <p>{stateLabels[detail.run.state]}</p>
                {detail.run.tasks.map((task) => (
                  <p key={task.id}>
                    {task.name}：
                    {task.candidates.map((id) => detail.actorNames[id] ?? id).join('、')}
                  </p>
                ))}
                {detail.currentTaskId ? (
                  <Form
                    form={form}
                    layout="vertical"
                    initialValues={{ action: 'APPROVE' }}
                    onValuesChange={() => onDirty?.(true)}
                    onFinish={(values) => mutation.mutate(values)}
                  >
                    <div className="sm-workflow-form-scroll">
                      <Form.Item name="action" label="处理结果" rules={[{ required: true }]}>
                        <Select
                          options={[
                            { value: 'APPROVE', label: '同意' },
                            { value: 'REJECT', label: '拒绝并结束本轮' },
                          ]}
                        />
                      </Form.Item>
                      <Form.Item
                        name="opinion"
                        label="审批意见"
                        dependencies={['action']}
                        rules={[
                          ({ getFieldValue }) => ({
                            validator: async (_rule, value: string | undefined) => {
                              if (getFieldValue('action') === 'REJECT' && !value?.trim())
                                throw new Error('拒绝时必须填写审批意见');
                            },
                          }),
                        ]}
                      >
                        <Input.TextArea maxLength={1000} rows={6} showCount />
                      </Form.Item>
                    </div>
                    <Button
                      className="sm-workflow-submit"
                      type="primary"
                      htmlType="submit"
                      loading={mutation.isPending}
                    >
                      提交处理
                    </Button>
                  </Form>
                ) : (
                  <p>当前没有需要您处理的任务。</p>
                )}
                <TaskCooperationActions detail={detail} onCompleted={onCompleted} />
              </>
            ),
          },
          {
            key: 'history',
            label: '审批记录',
            children: (
              <div className="sm-workflow-history-scroll">
                <ol className="sm-workflow-history">
                  {detail.run.history.map((item) => (
                    <li key={item.id}>
                      <strong>
                        {item.nodeName} · {workflowActionLabels[item.action] ?? item.action}
                      </strong>
                      <p>
                        {item.actorId ? (detail.actorNames[item.actorId] ?? item.actorId) : '系统'}{' '}
                        · {item.time}
                      </p>
                      <p>{item.opinion}</p>
                    </li>
                  ))}
                </ol>
              </div>
            ),
          },
        ]}
      />
    </div>
  );
}
