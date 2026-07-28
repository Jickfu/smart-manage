import { DatePicker, Form, Input, InputNumber, Select, Switch } from 'antd';
import RefSelector from '@/domain/common/component/RefSelector';
import type { EditField } from './EditPage';
import { getDatePickerValueProps, normalizeDatePickerValue } from './dateFormValue';

const { TextArea } = Input;

interface EditFormFieldsProps {
  fields: EditField[];
  editable?: boolean;
}

/** 只读字段展示组件，由 Form.Item 注入 value。 */
function ReadonlyText({ value }: { value?: unknown }) {
  return <span className="sm-edit-readonly">{value != null ? String(value) : '-'}</span>;
}

/** 统一渲染普通编辑页与弹窗编辑页的字段控件。 */
function renderFormControl(field: EditField, disabled: boolean) {
  switch (field.type) {
    case 'text':
      return <Input variant="underlined" placeholder={field.placeholder} disabled={disabled} />;
    case 'password':
      return (
        <Input.Password variant="underlined" placeholder={field.placeholder} disabled={disabled} />
      );
    case 'date':
      return (
        <DatePicker
          variant="underlined"
          className="sm-edit-control-full"
          placeholder={field.placeholder}
          disabled={disabled}
        />
      );
    case 'number':
      return (
        <InputNumber
          variant="underlined"
          className="sm-edit-control-full"
          placeholder={field.placeholder}
          disabled={disabled}
        />
      );
    case 'switch':
      return <Switch disabled={disabled} />;
    case 'textarea':
      return (
        <TextArea
          variant="underlined"
          placeholder={field.placeholder}
          disabled={disabled}
          rows={3}
        />
      );
    case 'select':
      return (
        <Select
          variant="underlined"
          className="sm-edit-control-full"
          placeholder={field.placeholder}
          disabled={disabled}
          options={field.options}
        />
      );
    case 'ref-selector':
      return (
        <RefSelector<Record<string, unknown>>
          placeholder={field.placeholder}
          disabled={disabled}
          {...field.refSelector}
        />
      );
    default:
      return null;
  }
}

export function EditFormFields({ fields, editable = true }: EditFormFieldsProps) {
  return (
    <div className="sm-edit-fields">
      {fields.map((field) => {
        const disabled = Boolean(field.disabled || !editable);
        const className = `sm-edit-field${field.fullWidth ? ' sm-edit-field--full' : ''}`;

        if (field.type === 'readonly') {
          return (
            <Form.Item
              key={field.dataIndex}
              name={field.dataIndex}
              label={field.label}
              className={className}
            >
              <ReadonlyText />
            </Form.Item>
          );
        }

        return (
          <Form.Item
            key={field.dataIndex}
            name={field.dataIndex}
            label={field.label}
            rules={field.rules}
            valuePropName={field.type === 'switch' ? 'checked' : undefined}
            getValueProps={field.type === 'date' ? getDatePickerValueProps : undefined}
            normalize={field.type === 'date' ? normalizeDatePickerValue : undefined}
            className={className}
          >
            {renderFormControl(field, disabled)}
          </Form.Item>
        );
      })}
    </div>
  );
}
