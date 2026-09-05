import {
  ColorPicker,
  DatePicker,
  Form,
  Input,
  InputNumber,
  Select,
  Switch,
  TreeSelect,
} from 'antd';
import RefSelector from '@/domain/common/component/RefSelector';
import IconSelector from '@/domain/common/component/IconSelector';
import type { EditField } from './EditPage';
import { FormFieldCell, FormFieldGrid } from './FormFieldLayout';
import {
  getDatePickerValueProps,
  getDateTimePickerValueProps,
  normalizeDatePickerValue,
  normalizeDateTimePickerValue,
} from './dateFormValue';

const { TextArea } = Input;

interface EditFormFieldsProps {
  fields: EditField[];
  editable?: boolean;
  maxColumns?: 1 | 2 | 4;
}

interface EditFieldItemProps {
  field: EditField;
  editable: boolean;
}

interface StringColorPickerProps {
  value?: string;
  onChange?: (value?: string) => void;
  disabled?: boolean;
}

/** 将 ColorPicker 的 Color 对象边界转换为表单和接口使用的十六进制字符串。 */
function StringColorPicker({ value, onChange, disabled }: StringColorPickerProps) {
  return (
    <ColorPicker
      value={value || undefined}
      format="hex"
      disabled={disabled}
      disabledAlpha
      allowClear
      showText={(color) => color.toHexString()}
      onChange={(color) => onChange?.(color.toHexString())}
      onClear={() => onChange?.(undefined)}
    />
  );
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
    case 'datetime':
      return (
        <DatePicker
          variant="underlined"
          className="sm-edit-control-full"
          placeholder={field.placeholder}
          disabled={disabled}
          showTime
          format="YYYY-MM-DD HH:mm:ss"
        />
      );
    case 'color':
      return <StringColorPicker disabled={disabled} />;
    case 'icon-selector':
      return <IconSelector placeholder={field.placeholder} disabled={disabled} />;
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
          rows={field.rows ?? 3}
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
    case 'tree-select':
      return (
        <TreeSelect
          variant="underlined"
          className="sm-edit-control-full"
          placeholder={field.placeholder}
          disabled={disabled}
          allowClear
          showSearch={{
            filterTreeNode: (input, node) =>
              String(node.title ?? '')
                .toLowerCase()
                .includes(input.toLowerCase()),
          }}
          treeDefaultExpandAll
          treeData={field.treeData}
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
    case 'custom':
      return <div className="sm-edit-readonly">{field.content}</div>;
    default:
      return null;
  }
}

/**
 * 统一字段单元：外层负责列占位，内层 Form.Item 负责标签、控件和校验信息。
 * 间距包含在字段占位区域内，避免由字段容器的 gap 或相邻行承担。
 */
function EditFieldItem({ field, editable }: EditFieldItemProps) {
  const disabled = Boolean(field.disabled || !editable);

  if (field.type === 'custom') {
    return (
      <FormFieldCell columnSpan={field.columnSpan} fullWidth={field.fullWidth}>
        <Form.Item label={field.label} className="sm-edit-field-content">
          <div className="sm-edit-readonly">{field.content}</div>
        </Form.Item>
      </FormFieldCell>
    );
  }

  if (field.type === 'readonly') {
    return (
      <FormFieldCell columnSpan={field.columnSpan} fullWidth={field.fullWidth}>
        <Form.Item name={field.dataIndex} label={field.label} className="sm-edit-field-content">
          <ReadonlyText />
        </Form.Item>
      </FormFieldCell>
    );
  }

  return (
    <FormFieldCell columnSpan={field.columnSpan} fullWidth={field.fullWidth}>
      <Form.Item
        name={field.dataIndex}
        label={field.label}
        rules={field.rules}
        valuePropName={field.type === 'switch' ? 'checked' : undefined}
        getValueProps={
          field.type === 'date'
            ? getDatePickerValueProps
            : field.type === 'datetime'
              ? getDateTimePickerValueProps
              : undefined
        }
        normalize={
          field.type === 'date'
            ? normalizeDatePickerValue
            : field.type === 'datetime'
              ? normalizeDateTimePickerValue
              : undefined
        }
        className="sm-edit-field-content"
      >
        {renderFormControl(field, disabled)}
      </Form.Item>
    </FormFieldCell>
  );
}

export function EditFormFields({ fields, editable = true, maxColumns }: EditFormFieldsProps) {
  return (
    <FormFieldGrid maxColumns={maxColumns}>
      {fields.map((field) => (
        <EditFieldItem key={field.dataIndex} field={field} editable={editable} />
      ))}
    </FormFieldGrid>
  );
}
