import { useCallback, useMemo, useState } from 'react';
import type { Key } from 'react';

/** 选中键可跨页保留；记录对象仅从当前页派生，不把缺失记录误认为单选。 */
export function deriveListSelection<RecordType extends { id: string }>(
  records: readonly RecordType[],
  selectedRowKeys: readonly Key[],
) {
  const selectedKeys = new Set(selectedRowKeys);
  const selectedRecords = records.filter((record) => selectedKeys.has(record.id));
  return {
    selectedIds: selectedRowKeys.map(String),
    selectedRecords,
    singleSelectedRecord: selectedRowKeys.length === 1 ? selectedRecords[0] : undefined,
  };
}

/** 只管理选择，不因刷新、翻页或数据变化隐式清空；清空时机由领域页面决定。 */
export function useListSelection<RecordType extends { id: string }>(
  records: readonly RecordType[],
) {
  const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
  const selection = useMemo(
    () => deriveListSelection(records, selectedRowKeys),
    [records, selectedRowKeys],
  );
  const clearSelection = useCallback(() => setSelectedRowKeys([]), []);
  return { selectedRowKeys, setSelectedRowKeys, ...selection, clearSelection };
}
