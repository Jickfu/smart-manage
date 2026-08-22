import { useState } from 'react';
import type { ReactNode } from 'react';
import { Collapse } from 'antd';

export interface EditSectionCollapseItem {
  key: string;
  label: ReactNode;
  children: ReactNode;
  extra?: ReactNode | ((expanded: boolean) => ReactNode);
  forceRender?: boolean;
}

interface EditSectionCollapseProps {
  items: EditSectionCollapseItem[];
  defaultActiveKeys?: string[];
  activeKeys?: string[];
  onActiveKeysChange?: (activeKeys: string[]) => void;
}

/** 编辑类页面的正文分区，统一标题与图标的折叠交互，并隔离标题栏操作区。 */
export function EditSectionCollapse({
  items,
  defaultActiveKeys = [],
  activeKeys,
  onActiveKeysChange,
}: EditSectionCollapseProps) {
  const [innerActiveKeys, setInnerActiveKeys] = useState(defaultActiveKeys);
  const currentActiveKeys = activeKeys ?? innerActiveKeys;

  const updateActiveKeys = (nextActiveKeys: string[]) => {
    if (activeKeys === undefined) setInnerActiveKeys(nextActiveKeys);
    onActiveKeysChange?.(nextActiveKeys);
  };

  const toggleItem = (key: string) => {
    updateActiveKeys(
      currentActiveKeys.includes(key)
        ? currentActiveKeys.filter((activeKey) => activeKey !== key)
        : [...currentActiveKeys, key],
    );
  };

  return (
    <Collapse
      className="sm-edit-collapse"
      collapsible="icon"
      activeKey={currentActiveKeys}
      onChange={(keys) => updateActiveKeys((Array.isArray(keys) ? keys : [keys]).map(String))}
      items={items.map((item) => {
        const expanded = currentActiveKeys.includes(item.key);
        return {
          key: item.key,
          label: (
            <button
              type="button"
              className="sm-edit-collapse-title"
              onClick={() => toggleItem(item.key)}
            >
              {item.label}
            </button>
          ),
          children: item.children,
          extra: typeof item.extra === 'function' ? item.extra(expanded) : item.extra,
          forceRender: item.forceRender,
        };
      })}
    />
  );
}
