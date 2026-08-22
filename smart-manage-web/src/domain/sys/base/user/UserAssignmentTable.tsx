import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { forwardRef, useImperativeHandle, useRef, useState } from 'react';
import type { Key, ReactNode } from 'react';
import { Form, Input, Switch, Table } from 'antd';
import type { FormListOperation } from 'antd/es/form';
import type { ColumnsType } from 'antd/es/table';
import RefSelector from '@/domain/common/component/RefSelector';
import { useOrgRefSelector } from '@/domain/sys/base/org/refSelector/useOrgRefSelector';
import type { OrgRefRecord } from '@/domain/sys/base/org/refSelector/useOrgRefSelector';

interface UserAssignmentTableProps {
  editable: boolean;
  onSelectionChange?: (hasSelection: boolean) => void;
}

interface AssignmentRow {
  key: number;
  name: number;
}

export interface UserAssignmentTableRef {
  add: () => void;
  removeSelected: () => void;
}

function RequiredTitle({ children }: { children: ReactNode }) {
  return (
    <span>
      <span className="sm-user-assignment-required">*</span>
      {children}
    </span>
  );
}

function OrganizationLongName({ fieldName }: { fieldName: number }) {
  const form = Form.useFormInstance();
  const org = Form.useWatch(['assignments', fieldName, 'org'], form) as OrgRefRecord | undefined;
  const assignments = form.getFieldValue('assignments') as Array<{ orgNamePath?: string }>;
  return org ? (org.namePath ?? assignments[fieldName]?.orgNamePath ?? '') : '';
}

export const UserAssignmentTable = forwardRef<UserAssignmentTableRef, UserAssignmentTableProps>(
  function UserAssignmentTable({ editable, onSelectionChange }, ref) {
    const feedback = useOperationFeedback();
    const form = Form.useFormInstance();
    const orgRefSelector = useOrgRefSelector();
    const operationsRef = useRef<FormListOperation | null>(null);
    const indexByKeyRef = useRef(new Map<Key, number>());
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);

    useImperativeHandle(ref, () => ({
      add: () => {
        const assignments = form.getFieldValue('assignments') ?? [];
        operationsRef.current?.add({ isOrgLeader: false, isPrimary: assignments.length === 0 });
      },
      removeSelected: () => {
        const selectedIndexes = selectedRowKeys
          .map((key) => indexByKeyRef.current.get(key))
          .filter((index): index is number => index !== undefined);
        const assignments = form.getFieldValue('assignments') ?? [];
        const removesPrimary = selectedIndexes.some((index) => assignments[index]?.isPrimary);
        if (removesPrimary && selectedIndexes.length < assignments.length) {
          feedback.warning('请先将其他任职设为主职');
          return;
        }
        operationsRef.current?.remove(selectedIndexes);
        setSelectedRowKeys([]);
        onSelectionChange?.(false);
      },
    }));

    return (
      <Form.List name="assignments">
        {(fields, operations, { errors }) => {
          operationsRef.current = operations;
          indexByKeyRef.current = new Map(fields.map((field) => [field.key, field.name]));
          const rows = fields.map((field) => ({ key: field.key, name: field.name }));
          const columns: ColumnsType<AssignmentRow> = [
            {
              title: '#',
              width: 56,
              align: 'center',
              render: (_value, _row, index) => index + 1,
            },
            {
              title: <RequiredTitle>部门</RequiredTitle>,
              width: 240,
              render: (_, row) => (
                <Form.Item
                  name={[row.name, 'org']}
                  rules={[{ required: true, message: '请选择部门' }]}
                >
                  <RefSelector<Record<string, unknown>>
                    {...orgRefSelector}
                    modalTitle="选择部门"
                    placeholder="请选择部门"
                    disabled={!editable}
                  />
                </Form.Item>
              ),
            },
            {
              title: '部门长名称',
              render: (_, row) => <OrganizationLongName fieldName={row.name} />,
            },
            {
              title: <RequiredTitle>职位</RequiredTitle>,
              width: 180,
              render: (_, row) => (
                <Form.Item
                  name={[row.name, 'position']}
                  rules={[{ required: true, message: '职位不能为空' }]}
                >
                  <Input variant="borderless" disabled={!editable} />
                </Form.Item>
              ),
            },
            {
              title: '负责人',
              width: 90,
              align: 'center',
              render: (_, row) => (
                <Form.Item name={[row.name, 'isOrgLeader']} valuePropName="checked">
                  <Switch disabled={!editable} />
                </Form.Item>
              ),
            },
            {
              title: '主职',
              width: 80,
              align: 'center',
              render: (_, row) => (
                <Form.Item name={[row.name, 'isPrimary']} valuePropName="checked">
                  <Switch
                    disabled={!editable}
                    onChange={(checked) => {
                      if (!checked) {
                        form.setFieldValue(['assignments', row.name, 'isPrimary'], true);
                        feedback.warning('请直接将其他任职设为主职');
                        return;
                      }
                      const assignments = (form.getFieldValue('assignments') ?? []).map(
                        (assignment: Record<string, unknown>, index: number) => ({
                          ...assignment,
                          isPrimary: index === row.name,
                        }),
                      );
                      form.setFieldValue('assignments', assignments);
                    }}
                  />
                </Form.Item>
              ),
            },
          ];
          return (
            <div className="sm-user-assignment-editor">
              <Table
                rowKey="key"
                size="small"
                pagination={false}
                columns={columns}
                dataSource={rows}
                scroll={{ x: 'max-content' }}
                rowSelection={
                  editable
                    ? {
                        selectedRowKeys,
                        onChange: (keys) => {
                          setSelectedRowKeys(keys);
                          onSelectionChange?.(keys.length > 0);
                        },
                      }
                    : undefined
                }
              />
              {errors.length > 0 && (
                <div className="sm-user-assignment-error">
                  <Form.ErrorList errors={errors} />
                </div>
              )}
            </div>
          );
        }}
      </Form.List>
    );
  },
);
