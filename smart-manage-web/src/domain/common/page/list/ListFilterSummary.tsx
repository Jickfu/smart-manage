import { Tag } from 'antd';

export interface ListFilterSummaryItem {
  key: string;
  label: string;
  removable?: boolean;
  onRemove?: () => void;
}

interface ListFilterSummaryProps {
  items: ListFilterSummaryItem[];
}

const ListFilterSummary = ({ items }: ListFilterSummaryProps) => (
  <div className="sm-list-filter-summary-items">
    {items.map((item) => (
      <Tag
        key={item.key}
        className="sm-list-filter-summary-tag"
        closable={item.removable !== false}
        onClose={(event) => {
          event.preventDefault();
          item.onRemove?.();
        }}
      >
        {item.label}
      </Tag>
    ))}
  </div>
);

export default ListFilterSummary;
