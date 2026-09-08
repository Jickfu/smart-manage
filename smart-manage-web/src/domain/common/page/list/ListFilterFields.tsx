import type { ReactNode } from 'react';

/** 自动换行且最多三列的筛选字段容器；少量字段使用较宽列，所有字段保持固定左侧起点。 */
export function ListFilterFields({ children }: { children: ReactNode }) {
  return <div className="sm-list-filter-fields">{children}</div>;
}

export function ListFilterField({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="sm-list-filter-field">
      <span title={label}>{label}</span>
      {children}
    </label>
  );
}
