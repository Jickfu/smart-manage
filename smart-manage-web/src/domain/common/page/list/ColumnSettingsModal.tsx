import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useState } from 'react';
import { Button, Checkbox, InputNumber, Select, Space, Table } from 'antd';
import AppModal from '@/domain/common/component/AppModal';
import type { ColumnsType } from 'antd/es/table/interface';
import type { ColumnSetting } from './columnSettings';
import {
  MAX_COLUMN_WIDTH,
  MIN_COLUMN_WIDTH,
  moveColumnSetting,
  normalizeFixedColumnOrder,
  validateColumnSettings,
} from './columnSettings';
import './ColumnSettingsModal.css';

interface ColumnSettingsModalProps {
  open: boolean;
  settings: ColumnSetting[];
  defaults: ColumnSetting[];
  onCancel: () => void;
  onConfirm: (settings: ColumnSetting[]) => void;
}

const DEFAULT_FIXED_WIDTH = 120;
const COMPACT_SELECT_CLASS_NAMES = { popup: { root: 'sm-column-settings-select-popup' } };

const ColumnSettingsModal = ({
  open,
  settings,
  defaults,
  onCancel,
  onConfirm,
}: ColumnSettingsModalProps) => {
  const feedback = useOperationFeedback();
  const [draft, setDraft] = useState(settings);
  const [selectedKey, setSelectedKey] = useState<string | undefined>(settings[0]?.key);

  const updateSetting = (key: string, update: Partial<ColumnSetting>) => {
    setDraft((current) =>
      normalizeFixedColumnOrder(
        current.map((setting) => (setting.key === key ? { ...setting, ...update } : setting)),
      ),
    );
  };

  const columns: ColumnsType<ColumnSetting> = [
    {
      key: 'sequence',
      title: '#',
      width: 48,
      align: 'center',
      render: (_value, _record, index) => index + 1,
    },
    { key: 'label', title: '列名', dataIndex: 'label' },
    {
      key: 'align',
      title: '对齐方式',
      width: 120,
      render: (_value, record) => (
        <Select
          size="small"
          classNames={COMPACT_SELECT_CLASS_NAMES}
          value={record.align}
          options={[
            { label: '默认', value: 'default' },
            { label: '居左', value: 'left' },
            { label: '居中', value: 'center' },
            { label: '居右', value: 'right' },
          ]}
          onChange={(align) => updateSetting(record.key, { align })}
        />
      ),
    },
    {
      key: 'hidden',
      title: '隐藏列',
      width: 84,
      align: 'center',
      render: (_value, record) => (
        <Checkbox
          checked={record.hidden}
          aria-label={`隐藏${record.label}`}
          onChange={(event) => updateSetting(record.key, { hidden: event.target.checked })}
        />
      ),
    },
    {
      key: 'fixed',
      title: '列冻结',
      width: 110,
      render: (_value, record) => (
        <Select
          size="small"
          classNames={COMPACT_SELECT_CLASS_NAMES}
          value={record.fixed}
          options={[
            { label: '不冻结', value: 'none' },
            { label: '冻结左侧', value: 'left' },
            { label: '冻结右侧', value: 'right' },
          ]}
          onChange={(fixed) =>
            updateSetting(record.key, {
              fixed,
              ...(fixed === 'none'
                ? {}
                : { widthMode: 'fixed', width: record.width ?? DEFAULT_FIXED_WIDTH }),
            })
          }
        />
      ),
    },
    {
      key: 'widthMode',
      title: '列宽模式',
      width: 100,
      render: (_value, record) => (
        <Select
          size="small"
          classNames={COMPACT_SELECT_CLASS_NAMES}
          value={record.widthMode}
          options={[
            { label: '自动', value: 'auto' },
            { label: '固定', value: 'fixed' },
          ]}
          onChange={(widthMode) =>
            updateSetting(record.key, {
              widthMode,
              ...(widthMode === 'auto'
                ? { fixed: 'none' }
                : { width: record.width ?? DEFAULT_FIXED_WIDTH }),
            })
          }
        />
      ),
    },
    {
      key: 'width',
      title: '固定宽度',
      width: 120,
      render: (_value, record) => (
        <InputNumber
          size="small"
          min={MIN_COLUMN_WIDTH}
          max={MAX_COLUMN_WIDTH}
          precision={0}
          suffix="px"
          disabled={record.widthMode === 'auto'}
          value={record.width}
          onChange={(width) =>
            updateSetting(record.key, { width: typeof width === 'number' ? width : undefined })
          }
        />
      ),
    },
  ];

  const handleConfirm = () => {
    const normalized = normalizeFixedColumnOrder(draft);
    const validationError = validateColumnSettings(normalized);
    if (validationError) {
      void feedback.warning(validationError);
      return;
    }
    onConfirm(normalized);
  };

  return (
    <AppModal
      title="列设置"
      open={open}
      width={900}
      bodyMode="fixed"
      className="sm-column-settings-modal"
      onCancel={onCancel}
      footer={
        <>
          <Button onClick={onCancel}>取消</Button>
          <Button type="primary" onClick={handleConfirm}>
            确定
          </Button>
        </>
      }
    >
      <div className="sm-column-settings-body">
        <div className="sm-column-settings-toolbar">
          <Space wrap>
            <Button onClick={() => setDraft(defaults)}>恢复出厂设置</Button>
            <Button
              onClick={() =>
                setDraft((current) => current.map((setting) => ({ ...setting, hidden: false })))
              }
            >
              全部显示
            </Button>
            <Button
              onClick={() =>
                setDraft((current) =>
                  current.map((setting) => ({ ...setting, fixed: 'none' as const })),
                )
              }
            >
              全部解冻
            </Button>
            <Button
              disabled={!selectedKey}
              onClick={() => selectedKey && setDraft(moveColumnSetting(draft, selectedKey, 'top'))}
            >
              置顶
            </Button>
            <Button
              disabled={!selectedKey}
              onClick={() =>
                selectedKey && setDraft(moveColumnSetting(draft, selectedKey, 'bottom'))
              }
            >
              置底
            </Button>
            <Button
              disabled={!selectedKey}
              onClick={() => selectedKey && setDraft(moveColumnSetting(draft, selectedKey, 'up'))}
            >
              上移
            </Button>
            <Button
              disabled={!selectedKey}
              onClick={() => selectedKey && setDraft(moveColumnSetting(draft, selectedKey, 'down'))}
            >
              下移
            </Button>
          </Space>
        </div>
        <div className="sm-column-settings-table">
          <Table<ColumnSetting>
            size="small"
            rowKey="key"
            pagination={false}
            sticky
            scroll={{ y: 400 }}
            columns={columns}
            dataSource={draft}
            rowSelection={{
              type: 'radio',
              columnWidth: 36,
              selectedRowKeys: selectedKey ? [selectedKey] : [],
              onChange: (keys) => setSelectedKey(keys[0] ? String(keys[0]) : undefined),
            }}
            onRow={(record) => ({ onClick: () => setSelectedKey(record.key) })}
          />
        </div>
      </div>
    </AppModal>
  );
};

export default ColumnSettingsModal;
