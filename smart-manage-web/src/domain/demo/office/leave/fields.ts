import type { EditField } from '@/domain/common/page/edit/EditPage';
export const leaveFields: EditField[] = [
  {
    label: '单据编号',
    dataIndex: 'number',
    type: 'text',
    disabled: true,
    placeholder: '保存时生成',
  },
  { label: '业务日期', dataIndex: 'bizDate', type: 'date', rules: [{ required: true }] },
  {
    label: '请假类型',
    dataIndex: 'leaveType',
    type: 'select',
    options: [
      { value: 'ANNUAL', label: '年假' },
      { value: 'SICK', label: '病假' },
      { value: 'PERSONAL', label: '事假' },
      { value: 'OTHER', label: '其他' },
    ],
    rules: [{ required: true }],
  },
  {
    label: '请假天数',
    dataIndex: 'days',
    type: 'number',
    tooltip: '人工填写，最多两位小数；不自动计算节假日或余额。',
    rules: [
      { required: true, type: 'number', min: 0.01, max: 9999.99 },
      {
        validator: async (_rule, value: number | undefined) => {
          if (value !== undefined && Math.abs(value * 100 - Math.round(value * 100)) > 0.000001)
            throw new Error('最多填写两位小数');
        },
      },
    ],
  },
  { label: '开始时间', dataIndex: 'startTime', type: 'datetime', rules: [{ required: true }] },
  {
    label: '结束时间',
    dataIndex: 'endTime',
    type: 'datetime',
    rules: [
      { required: true },
      ({ getFieldValue }) => ({
        validator: async (_rule, value: string | undefined) => {
          if (value && getFieldValue('startTime') && value <= getFieldValue('startTime'))
            throw new Error('结束时间必须晚于开始时间');
        },
      }),
    ],
  },
  {
    label: '单据状态',
    dataIndex: 'billStatus',
    type: 'select',
    disabled: true,
    options: [
      { value: 'A', label: '草稿' },
      { value: 'B', label: '审批中' },
      { value: 'C', label: '审批通过' },
    ],
  },
  {
    label: '请假事由',
    dataIndex: 'reason',
    type: 'textarea',
    fullWidth: true,
    rows: 4,
    rules: [{ required: true, whitespace: true, max: 2000 }],
  },
];
