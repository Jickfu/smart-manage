import { Button, DatePicker, Form, Input, Select, Space } from 'antd';
import type { AuditLogFilters } from './types';
import './monitorLog.css';

interface Props<TFilters extends AuditLogFilters> {
  values: TFilters;
  eventTypeOptions?: Array<{ label: string; value: string }>;
  onFilter: (values: TFilters) => void;
}

export default function AuditLogFilter<TFilters extends AuditLogFilters>({
  values,
  eventTypeOptions,
  onFilter,
}: Props<TFilters>) {
  const [form] = Form.useForm<AuditLogFilters>();

  return (
    <Form
      form={form}
      className="sm-monitor-log-filter"
      layout="inline"
      initialValues={values}
      onFinish={(submittedValues) => onFilter(submittedValues as TFilters)}
    >
      <Form.Item name="success" label="结果">
        <Select
          className="sm-monitor-log-filter-select"
          allowClear
          placeholder="全部"
          options={[
            { label: '成功', value: true },
            { label: '失败', value: false },
          ]}
        />
      </Form.Item>
      {eventTypeOptions && (
        <Form.Item name="eventType" label="事件">
          <Select
            className="sm-monitor-log-filter-select"
            allowClear
            placeholder="全部"
            options={eventTypeOptions}
          />
        </Form.Item>
      )}
      <Form.Item name="timeRange" label="发生时间">
        <DatePicker.RangePicker showTime />
      </Form.Item>
      <Form.Item name="traceId" label="Trace ID">
        <Input className="sm-monitor-log-filter-trace" allowClear placeholder="完整 Trace ID" />
      </Form.Item>
      <Form.Item>
        <Space>
          <Button type="primary" htmlType="submit">
            查询
          </Button>
          <Button
            onClick={() => {
              form.resetFields();
              onFilter({} as TFilters);
            }}
          >
            重置
          </Button>
        </Space>
      </Form.Item>
    </Form>
  );
}
