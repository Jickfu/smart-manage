import type { ReactNode } from 'react';
import { useState } from 'react';
import { Button, ConfigProvider, Input } from 'antd';
import { DownOutlined, UpOutlined } from '@ant-design/icons';

interface ListFilterBarProps {
  title: string;
  filterContent?: ReactNode;
  defaultExpanded?: boolean;
  filterSummary?: ReactNode;
  quickSearchPlaceholder?: string;
  onQuickSearch?: (value: string) => void;
}

const { Search } = Input;

const ListFilterBar = ({
  title,
  filterContent,
  defaultExpanded = false,
  filterSummary,
  quickSearchPlaceholder = '快速搜索',
  onQuickSearch,
}: ListFilterBarProps) => {
  const [expanded, setExpanded] = useState(defaultExpanded);

  return (
    <div className="sm-list-filter">
      <div className="sm-list-filter-main">
        <div className="sm-list-filter-title">{title}</div>
        <div className="sm-list-filter-summary">{!expanded && filterSummary}</div>
        <div className="sm-list-filter-search">
          <Search
            allowClear
            variant="underlined"
            placeholder={quickSearchPlaceholder}
            onSearch={(value) => onQuickSearch?.(value)}
          />
        </div>
        {filterContent && (
          <Button
            className="sm-list-filter-toggle"
            type="text"
            icon={expanded ? <UpOutlined /> : <DownOutlined />}
            aria-expanded={expanded}
            onClick={() => setExpanded((current) => !current)}
          >
            {expanded ? '收起过滤' : '展开过滤'}
          </Button>
        )}
      </div>
      {expanded && filterContent && (
        <ConfigProvider variant="outlined">
          <div className="sm-list-filter-panel">{filterContent}</div>
        </ConfigProvider>
      )}
    </div>
  );
};

export default ListFilterBar;
