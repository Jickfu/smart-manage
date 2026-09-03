import { useState } from 'react';
import {
  Button,
  Form,
  Input,
  InputNumber,
  Select,
  Space,
  Switch,
  Table,
  Tag,
  Typography,
} from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { PageComponentProps } from '@/domain/common/page/types';
import { EditPageShell } from '@/domain/common/page/edit/EditPageShell';
import { FormFieldCell, FormFieldGrid } from '@/domain/common/page/edit/FormFieldLayout';
import AppModal from '@/domain/common/component/AppModal';
import RefSelector from '@/domain/common/component/RefSelector';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { usePermissionAccess } from '@/domain/common/page/usePermissionAccess';
import { useUserRefSelector } from '@/domain/sys/base/user/refSelector/useUserRefSelector';
import { monitorAlertApi } from './api';
import { monitorAlertQueryKeys as keys } from './queryKeys';
import { monitorAlertAccess } from './permissions';
import type { AlertRule, UserRef } from './types';
import './monitorAlert.css';

interface RuleFormValues {
  enabled: boolean;
  severity: string;
  threshold: number;
  durationSeconds: number;
  recoveryThreshold?: number;
  repeatIntervalSeconds: number;
  emailEnabled: boolean;
  recipientUsers: UserRef[];
  description?: string;
}
const severityColor: Record<string, string> = {
  INFO: 'blue',
  WARNING: 'warning',
  CRITICAL: 'error',
};
const displayValue = (rule: AlertRule | undefined, value?: number) =>
  value == null ? undefined : rule?.valueKind === 'RATIO' ? value * 100 : value;
const persistedValue = (rule: AlertRule, value?: number) =>
  value == null ? undefined : rule.valueKind === 'RATIO' ? value / 100 : value;
export default function MonitorAlertPage({ active }: PageComponentProps) {
  const [editing, setEditing] = useState<AlertRule>();
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [status, setStatus] = useState<string>();
  const [form] = Form.useForm<RuleFormValues>();
  const [selectedRuleId, setSelectedRuleId] = useState<string>();
  const queryClient = useQueryClient();
  const feedback = useOperationFeedback();
  const { can } = usePermissionAccess(monitorAlertAccess.prefix);
  const recipientSelector = useUserRefSelector({ multiple: true, title: '选择告警邮件接收人' });
  const rules = useQuery({
    queryKey: keys.rules(),
    queryFn: monitorAlertApi.rules,
    enabled: active,
  });
  const incidents = useQuery({
    queryKey: keys.incidents(page, pageSize, status),
    queryFn: () => monitorAlertApi.incidents({ pageNum: page, pageSize, status }),
    enabled: active,
  });
  const save = useMutation({
    mutationFn: monitorAlertApi.saveRule,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: keys.rules() });
      setEditing(undefined);
      feedback.success('告警规则已保存');
    },
    onError: (error) => feedback.fromError(error, '告警规则保存失败'),
  });
  const open = (rule: AlertRule) => {
    setEditing(rule);
    form.setFieldsValue({
      enabled: rule.enabled,
      severity: rule.severity,
      threshold: displayValue(rule, Number(rule.threshold))!,
      durationSeconds: rule.durationSeconds,
      recoveryThreshold: displayValue(
        rule,
        rule.recoveryThreshold == null ? undefined : Number(rule.recoveryThreshold),
      ),
      repeatIntervalSeconds: rule.repeatIntervalSeconds,
      emailEnabled: rule.emailEnabled,
      recipientUsers: rule.recipientUsers ?? [],
      description: rule.description,
    });
  };
  const submit = async () => {
    if (!editing) return;
    const values = await form.validateFields();
    await save.mutateAsync({
      id: editing.id,
      version: editing.version,
      enabled: values.enabled,
      severity: values.severity,
      threshold: persistedValue(editing, values.threshold)!,
      durationSeconds: values.durationSeconds,
      recoveryThreshold: persistedValue(editing, values.recoveryThreshold),
      repeatIntervalSeconds: values.repeatIntervalSeconds,
      emailEnabled: values.emailEnabled,
      recipientUserIds: (values.recipientUsers ?? []).map((item) => item.id),
      description: values.description,
    });
  };
  const selectedRule = rules.data?.find((rule) => rule.id === selectedRuleId);
  return (
    <EditPageShell
      title="监控告警"
      loading={rules.isLoading}
      error={rules.error}
      onRetry={() => void rules.refetch()}
      actions={
        <Space>
          <Select
            allowClear
            placeholder="事件状态"
            value={status}
            options={['PENDING', 'FIRING', 'RECOVERED', 'CLOSED'].map((value) => ({
              value,
              label: value,
            }))}
            onChange={(value) => {
              setStatus(value);
              setPage(1);
            }}
          />
          <Button
            type="primary"
            disabled={!selectedRule || !can(monitorAlertAccess.permissions.manage)}
            onClick={() => selectedRule && open(selectedRule)}
          >
            配置规则
          </Button>
          <Button
            type="primary"
            onClick={() => {
              void rules.refetch();
              void incidents.refetch();
            }}
          >
            刷新
          </Button>
        </Space>
      }
    >
      <div className="sm-monitor-alert">
        <section>
          <div className="sm-monitor-alert-title">
            <Typography.Title level={5}>告警规则</Typography.Title>
            <Typography.Text type="secondary">
              仅支持系统预定义类型，不提供表达式 DSL
            </Typography.Text>
          </div>
          <Table
            size="small"
            pagination={false}
            rowKey="id"
            dataSource={rules.data ?? []}
            rowSelection={{
              type: 'checkbox',
              selectedRowKeys: selectedRuleId ? [selectedRuleId] : [],
              onChange: (keys) =>
                setSelectedRuleId(keys.length === 1 ? String(keys[0]) : undefined),
            }}
            columns={[
              { title: '规则', dataIndex: 'name' },
              { title: '对象', dataIndex: 'scopeType', width: 100 },
              {
                title: '级别',
                dataIndex: 'severity',
                width: 110,
                render: (value: string) => <Tag color={severityColor[value]}>{value}</Tag>,
              },
              {
                title: '触发 / 恢复阈值',
                width: 180,
                render: (_, rule) =>
                  `${displayValue(rule, rule.threshold)} / ${displayValue(rule, rule.recoveryThreshold) ?? '-'} ${rule.displayUnit}`,
              },
              {
                title: '持续时间',
                dataIndex: 'durationSeconds',
                width: 120,
                render: (value: number) => `${value}s`,
              },
              {
                title: '邮件',
                dataIndex: 'emailEnabled',
                width: 100,
                render: (value: boolean) => (
                  <Tag color={value ? 'success' : 'default'}>{value ? '启用' : '关闭'}</Tag>
                ),
              },
              {
                title: '接收人数',
                width: 100,
                render: (_, rule) => rule.recipientUsers?.length ?? 0,
              },
              {
                title: '状态',
                dataIndex: 'enabled',
                width: 100,
                render: (value: boolean) => (
                  <Tag color={value ? 'success' : 'default'}>{value ? '启用' : '停用'}</Tag>
                ),
              },
            ]}
          />
        </section>
        <section>
          <div className="sm-monitor-alert-title">
            <Typography.Title level={5}>告警事件</Typography.Title>
            <Typography.Text type="secondary">
              异常、触发、恢复和重复通知均由后台状态机记录
            </Typography.Text>
          </div>
          <Table
            size="small"
            rowKey="id"
            loading={incidents.isFetching}
            dataSource={incidents.data?.records ?? []}
            pagination={{
              current: page,
              pageSize,
              total: incidents.data?.total ?? 0,
              onChange: (next, nextSize) => {
                setPage(next);
                setPageSize(nextSize);
              },
            }}
            columns={[
              { title: '开始时间', dataIndex: 'startedAt', width: 180 },
              { title: '规则', dataIndex: 'ruleName', width: 180 },
              { title: '对象', render: (_, item) => `${item.scopeType} / ${item.scopeId}` },
              {
                title: '级别',
                dataIndex: 'severity',
                width: 110,
                render: (value: string) => <Tag color={severityColor[value]}>{value}</Tag>,
              },
              {
                title: '状态',
                dataIndex: 'status',
                width: 120,
                render: (value: string) => (
                  <Tag
                    color={
                      value === 'FIRING' ? 'error' : value === 'RECOVERED' ? 'success' : 'warning'
                    }
                  >
                    {value}
                  </Tag>
                ),
              },
              {
                title: '最新 / 峰值',
                width: 150,
                render: (_, item) => `${item.lastValueDisplay} / ${item.peakValueDisplay}`,
              },
              { title: '摘要', dataIndex: 'summary' },
            ]}
          />
        </section>
      </div>
      <AppModal
        title={editing ? `配置规则：${editing.name}` : '配置告警规则'}
        open={Boolean(editing)}
        onCancel={() => setEditing(undefined)}
        width={760}
        footer={
          <>
            <Button onClick={() => setEditing(undefined)}>取消</Button>
            <Button type="primary" loading={save.isPending} onClick={() => void submit()}>
              保存
            </Button>
          </>
        }
      >
        <Form form={form} layout="vertical" variant="underlined" className="sm-edit-form">
          {editing && (
            <Typography.Paragraph type="secondary">
              指标类型：{editing.valueKind}；允许范围：
              {displayValue(editing, editing.minValue)} ～
              {displayValue(editing, editing.maxValue) ?? '不限'} {editing.displayUnit}
              ；推荐触发值：
              {displayValue(editing, editing.recommendedThreshold)} {editing.displayUnit}
            </Typography.Paragraph>
          )}
          <FormFieldGrid maxColumns={2}>
            <FormFieldCell>
              <Form.Item
                className="sm-edit-field-content"
                name="enabled"
                label="启用规则"
                valuePropName="checked"
              >
                <Switch />
              </Form.Item>
            </FormFieldCell>
            <FormFieldCell>
              <Form.Item
                className="sm-edit-field-content"
                name="severity"
                label="严重程度"
                rules={[{ required: true }]}
              >
                <Select
                  options={['INFO', 'WARNING', 'CRITICAL'].map((value) => ({
                    value,
                    label: value,
                  }))}
                />
              </Form.Item>
            </FormFieldCell>
            <FormFieldCell>
              <Form.Item
                className="sm-edit-field-content"
                name="threshold"
                label="触发阈值"
                rules={[{ required: true }]}
              >
                <InputNumber
                  className="sm-edit-control-full"
                  disabled={editing?.valueKind === 'BOOLEAN'}
                  min={displayValue(editing, editing?.minValue)}
                  max={displayValue(editing, editing?.maxValue)}
                  suffix={editing?.displayUnit}
                />
              </Form.Item>
            </FormFieldCell>
            <FormFieldCell>
              <Form.Item
                className="sm-edit-field-content"
                name="recoveryThreshold"
                label="恢复阈值"
              >
                <InputNumber
                  className="sm-edit-control-full"
                  disabled={editing?.valueKind === 'BOOLEAN'}
                  min={displayValue(editing, editing?.minValue)}
                  max={displayValue(editing, editing?.maxValue)}
                  suffix={editing?.displayUnit}
                />
              </Form.Item>
            </FormFieldCell>
            <FormFieldCell>
              <Form.Item
                className="sm-edit-field-content"
                name="durationSeconds"
                label="持续时间（秒）"
                rules={[{ required: true }]}
              >
                <InputNumber className="sm-edit-control-full" min={0} max={86400} />
              </Form.Item>
            </FormFieldCell>
            <FormFieldCell>
              <Form.Item
                className="sm-edit-field-content"
                name="repeatIntervalSeconds"
                label="重复通知间隔（秒）"
                rules={[{ required: true }]}
              >
                <InputNumber className="sm-edit-control-full" min={60} max={604800} />
              </Form.Item>
            </FormFieldCell>
            <FormFieldCell>
              <Form.Item
                className="sm-edit-field-content"
                name="emailEnabled"
                label="邮件通知"
                valuePropName="checked"
              >
                <Switch />
              </Form.Item>
            </FormFieldCell>
            <FormFieldCell fullWidth>
              <Form.Item
                className="sm-edit-field-content"
                name="recipientUsers"
                label="邮件接收人"
                dependencies={['emailEnabled']}
                rules={[
                  ({ getFieldValue }) => ({
                    validator: (_, value) =>
                      getFieldValue('emailEnabled') && (!value || value.length === 0)
                        ? Promise.reject(new Error('启用邮件通知时必须选择接收人'))
                        : Promise.resolve(),
                  }),
                ]}
              >
                <RefSelector<Record<string, unknown>> {...recipientSelector} />
              </Form.Item>
            </FormFieldCell>
            <FormFieldCell fullWidth>
              <Form.Item className="sm-edit-field-content" name="description" label="描述">
                <Input.TextArea rows={2} maxLength={500} />
              </Form.Item>
            </FormFieldCell>
          </FormFieldGrid>
        </Form>
      </AppModal>
    </EditPageShell>
  );
}
