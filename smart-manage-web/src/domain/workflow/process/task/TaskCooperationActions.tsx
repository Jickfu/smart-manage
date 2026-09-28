import { useRef, useState } from 'react';
import { Button, Form, Input, Select } from 'antd';
import { useQueryClient } from '@tanstack/react-query';
import AppModal from '@/domain/common/component/AppModal';
import RefSelector from '@/domain/common/component/RefSelector';
import { FormFieldCell, FormFieldGrid } from '@/domain/common/page/edit/FormFieldLayout';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { generateUUID } from '@/utils';
import { workflowApi } from '../api';
import type { ApprovalDetail } from '../api';
import { workflowUserRefSelector } from './workflowUserRefSelector';
import type { WorkflowUserOption } from './workflowUserRefSelector';

type Cooperation = 'TRANSFER' | 'DELEGATE' | 'ADD_SIGN' | 'REDUCE_SIGN';

export default function TaskCooperationActions({
  detail,
  onCompleted,
}: {
  detail: ApprovalDetail;
  onCompleted?: () => Promise<void>;
}) {
  const [open, setOpen] = useState(false);
  const [takeBackOpen, setTakeBackOpen] = useState(false);
  const cooperationIntent = useRef({ content: '', requestId: '' });
  const takeBackIntent = useRef({ content: '', requestId: '' });
  const [form] = Form.useForm();
  const [takeBackForm] = Form.useForm();
  const client = useQueryClient();
  const cooperation = useCommandMutation({
    mutationFn: (values: { action: Cooperation; users: WorkflowUserOption[]; reason: string }) => {
      const content = JSON.stringify([detail.id, detail.currentTaskId, values]);
      if (cooperationIntent.current.content !== content) {
        cooperationIntent.current = { content, requestId: generateUUID() };
      }
      return workflowApi.cooperate({
        instanceId: detail.id,
        taskId: detail.currentTaskId!,
        action: values.action,
        targetUserIds: values.users.map((user) => user.storageId.replace('sm:user:', '')),
        reason: values.reason,
        requestId: cooperationIntent.current.requestId,
      });
    },
    successMessage: '任务协作操作已完成',
    onSuccess: async () => {
      cooperationIntent.current = { content: '', requestId: '' };
      form.resetFields();
      setOpen(false);
      await client.invalidateQueries({ queryKey: ['workflow'] });
      await onCompleted?.();
    },
  });
  const takeBack = useCommandMutation({
    mutationFn: (values: { reason: string }) => {
      const content = JSON.stringify([detail.id, values]);
      if (takeBackIntent.current.content !== content) {
        takeBackIntent.current = { content, requestId: generateUUID() };
      }
      return workflowApi.takeBack(detail.id, values.reason, takeBackIntent.current.requestId);
    },
    successMessage: '任务已拿回',
    onSuccess: async () => {
      takeBackIntent.current = { content: '', requestId: '' };
      takeBackForm.resetFields();
      setTakeBackOpen(false);
      await client.invalidateQueries({ queryKey: ['workflow'] });
      await onCompleted?.();
    },
  });
  const closeCooperation = () => {
    if (cooperation.isPending) return;
    cooperationIntent.current = { content: '', requestId: '' };
    form.resetFields();
    setOpen(false);
  };
  const closeTakeBack = () => {
    if (takeBack.isPending) return;
    takeBackIntent.current = { content: '', requestId: '' };
    takeBackForm.resetFields();
    setTakeBackOpen(false);
  };
  return (
    <>
      <div className="sm-workflow-actions">
        {detail.currentTaskId && <Button onClick={() => setOpen(true)}>协作处理</Button>}
        {detail.canTakeBack && <Button onClick={() => setTakeBackOpen(true)}>拿回</Button>}
      </div>
      <AppModal
        open={open}
        title="当前任务协作"
        width={600}
        onCancel={closeCooperation}
        closeDisabled={cooperation.isPending}
        footer={
          <>
            <Button onClick={closeCooperation} disabled={cooperation.isPending}>
              取消
            </Button>
            <Button type="primary" loading={cooperation.isPending} onClick={() => form.submit()}>
              确定
            </Button>
          </>
        }
      >
        <Form
          form={form}
          layout="vertical"
          className="sm-edit-form"
          onFinish={(values) => cooperation.mutate(values)}
        >
          <FormFieldGrid maxColumns={1}>
            <FormFieldCell>
              <Form.Item
                className="sm-edit-field-content"
                name="action"
                label="协作方式"
                rules={[{ required: true, message: '请选择协作方式' }]}
              >
                <Select
                  variant="underlined"
                  options={[
                    { value: 'TRANSFER', label: '转办' },
                    { value: 'DELEGATE', label: '委派' },
                    { value: 'ADD_SIGN', label: '加签' },
                    { value: 'REDUCE_SIGN', label: '减签' },
                  ]}
                />
              </Form.Item>
            </FormFieldCell>
            <FormFieldCell>
              <Form.Item
                className="sm-edit-field-content"
                name="users"
                label="目标人员"
                rules={[{ required: true, message: '请选择目标人员' }]}
              >
                <RefSelector<Record<string, unknown>> {...workflowUserRefSelector} />
              </Form.Item>
            </FormFieldCell>
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
      <AppModal
        open={takeBackOpen}
        title="拿回任务"
        width={520}
        onCancel={closeTakeBack}
        closeDisabled={takeBack.isPending}
        footer={
          <>
            <Button onClick={closeTakeBack} disabled={takeBack.isPending}>
              取消
            </Button>
            <Button
              type="primary"
              loading={takeBack.isPending}
              onClick={() => takeBackForm.submit()}
            >
              确定拿回
            </Button>
          </>
        }
      >
        <Form
          form={takeBackForm}
          layout="vertical"
          className="sm-edit-form"
          onFinish={(values) => takeBack.mutate(values)}
        >
          <FormFieldGrid maxColumns={1}>
            <FormFieldCell>
              <Form.Item
                className="sm-edit-field-content"
                name="reason"
                label="拿回原因"
                rules={[
                  { required: true, whitespace: true, message: '请输入拿回原因' },
                  { max: 500, message: '拿回原因不能超过500个字符' },
                ]}
              >
                <Input.TextArea variant="underlined" rows={3} maxLength={500} showCount />
              </Form.Item>
            </FormFieldCell>
          </FormFieldGrid>
        </Form>
      </AppModal>
    </>
  );
}
