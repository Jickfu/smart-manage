import { useMemo, useState } from 'react';
import type { ComponentType, KeyboardEvent } from 'react';
import { Button, Empty, Input, Pagination, Segmented, Spin } from 'antd';
import { CloseOutlined, SearchOutlined } from '@ant-design/icons';
import { loadAllIcons, resolveIcon } from '@/domain/common/component/iconResolver';
import AppModal from './AppModal';
import './IconSelector.css';

interface IconSelectorProps {
  value?: string;
  onChange?: (value?: string) => void;
  disabled?: boolean;
  placeholder?: string;
}

interface IconOption {
  name: string;
  component: ComponentType;
}

type IconStyle = 'all' | 'outlined' | 'filled' | 'twoTone';

const ICON_PAGE_SIZE = 24;

function getIconStyle(name: string): Exclude<IconStyle, 'all'> {
  if (name.endsWith('Filled')) return 'filled';
  if (name.endsWith('TwoTone')) return 'twoTone';
  return 'outlined';
}

/** Ant Design 图标选择器，值为稳定的组件名称，例如 HomeOutlined。 */
function IconSelector({
  value,
  onChange,
  disabled = false,
  placeholder = '请选择图标',
}: IconSelectorProps) {
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [loadError, setLoadError] = useState(false);
  const [options, setOptions] = useState<IconOption[]>([]);
  const [keyword, setKeyword] = useState('');
  const [iconStyle, setIconStyle] = useState<IconStyle>('all');
  const [pendingValue, setPendingValue] = useState<string>();
  const [pageNum, setPageNum] = useState(1);

  const filteredOptions = useMemo(() => {
    const normalizedKeyword = keyword.trim().toLowerCase();
    return options.filter(
      (option) =>
        (iconStyle === 'all' || getIconStyle(option.name) === iconStyle) &&
        (!normalizedKeyword || option.name.toLowerCase().includes(normalizedKeyword)),
    );
  }, [iconStyle, keyword, options]);

  const pagedOptions = useMemo(() => {
    const startIndex = (pageNum - 1) * ICON_PAGE_SIZE;
    return filteredOptions.slice(startIndex, startIndex + ICON_PAGE_SIZE);
  }, [filteredOptions, pageNum]);

  const loadOptions = () => {
    setLoading(true);
    setLoadError(false);
    void loadAllIcons()
      .then((icons) => {
        setOptions(
          Object.entries(icons)
            .map(([name, component]) => ({ name, component }))
            .sort((left, right) => left.name.localeCompare(right.name)),
        );
      })
      .catch(() => setLoadError(true))
      .finally(() => setLoading(false));
  };

  const handleOpen = () => {
    if (disabled) return;
    if (options.length === 0) loadOptions();
    setPendingValue(value);
    setIconStyle(value ? getIconStyle(value) : 'all');
    setKeyword('');
    setPageNum(1);
    setOpen(true);
  };

  const handleTriggerKeyDown = (event: KeyboardEvent<HTMLDivElement>) => {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      handleOpen();
    }
  };

  const handleCancel = () => setOpen(false);

  const handleConfirm = () => {
    onChange?.(pendingValue);
    setOpen(false);
  };

  const modalHeaderExtra = (
    <Input.Search
      variant="underlined"
      className="sm-icon-selector-header-search"
      value={keyword}
      allowClear
      placeholder="快速搜索"
      onChange={(event) => {
        setKeyword(event.target.value);
        setPageNum(1);
      }}
    />
  );

  const modalFooter = (
    <>
      <Button onClick={handleCancel}>取消</Button>
      <Button type="primary" disabled={!pendingValue} onClick={handleConfirm}>
        确定
      </Button>
    </>
  );

  return (
    <>
      <div
        className={`sm-icon-selector-trigger${disabled ? ' sm-icon-selector-trigger--disabled' : ''}`}
        role="button"
        tabIndex={disabled ? -1 : 0}
        onClick={handleOpen}
        onKeyDown={handleTriggerKeyDown}
      >
        <span className="sm-icon-selector-preview">{resolveIcon(value)}</span>
        <span className={value ? 'sm-icon-selector-value' : 'sm-icon-selector-placeholder'}>
          {value || placeholder}
        </span>
        {value && !disabled && (
          <Button
            type="text"
            icon={<CloseOutlined />}
            className="sm-icon-selector-clear"
            aria-label="清空图标"
            onClick={(event) => {
              event.stopPropagation();
              onChange?.(undefined);
            }}
          />
        )}
        <SearchOutlined className="sm-icon-selector-search-icon" />
      </div>

      <AppModal
        title="选择图标"
        headerExtra={modalHeaderExtra}
        open={open}
        width={760}
        bodyMode="fixed"
        footer={modalFooter}
        onCancel={handleCancel}
        className="sm-icon-selector-modal"
      >
        <div className="sm-icon-selector-body">
          <div className="sm-icon-selector-meta">
            <div className="sm-icon-selector-meta-left">
              <Segmented<IconStyle>
                size="small"
                value={iconStyle}
                options={[
                  { label: '全部', value: 'all' },
                  { label: '线框风格', value: 'outlined' },
                  { label: '实底风格', value: 'filled' },
                  { label: '双色风格', value: 'twoTone' },
                ]}
                onChange={(nextStyle) => {
                  setIconStyle(nextStyle);
                  setPageNum(1);
                }}
              />
              <span>共 {filteredOptions.length} 个图标</span>
            </div>
            <Pagination
              size="small"
              current={pageNum}
              pageSize={ICON_PAGE_SIZE}
              total={filteredOptions.length}
              showTotal={(total) => `共 ${total} 个`}
              showSizeChanger={false}
              onChange={setPageNum}
            />
          </div>
          <div className="sm-icon-selector-content">
            <Spin spinning={loading}>
              {pagedOptions.length > 0 ? (
                <div className="sm-icon-selector-grid">
                  {pagedOptions.map((option) => {
                    const IconComponent = option.component;
                    const selected = pendingValue === option.name;
                    return (
                      <button
                        key={option.name}
                        type="button"
                        title={option.name}
                        aria-pressed={selected}
                        className={`sm-icon-selector-option${selected ? ' sm-icon-selector-option--selected' : ''}`}
                        onClick={() => setPendingValue(option.name)}
                        onDoubleClick={() => {
                          onChange?.(option.name);
                          setOpen(false);
                        }}
                      >
                        <IconComponent />
                        <span>{option.name}</span>
                      </button>
                    );
                  })}
                </div>
              ) : (
                <Empty
                  description={
                    loading ? '正在加载图标' : loadError ? '图标加载失败' : '没有匹配的图标'
                  }
                />
              )}
            </Spin>
          </div>
        </div>
      </AppModal>
    </>
  );
}

export default IconSelector;
