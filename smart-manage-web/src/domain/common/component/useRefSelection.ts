import { useCallback, useMemo, useRef, useState } from 'react';
import { createRefSelection, mergeRefTableSelection } from './refSelection';

export function useRefSelection<T extends Record<string, unknown>>(
  value: T | T[] | null | undefined,
  multiple: boolean,
  keyField: string,
) {
  const [selection, setSelectionState] = useState<Map<string, T>>(new Map());
  const previousKeysRef = useRef<React.Key[]>([]);

  const setSelection = useCallback(
    (updater: Map<string, T> | ((previous: Map<string, T>) => Map<string, T>)) => {
      setSelectionState((previous) => {
        const next = typeof updater === 'function' ? updater(previous) : updater;
        previousKeysRef.current = [...next.keys()];
        return next;
      });
    },
    [],
  );
  const resetFromValue = useCallback(
    () => setSelection(createRefSelection(value, multiple, keyField)),
    [keyField, multiple, setSelection, value],
  );
  const toggle = useCallback(
    (record: T) => {
      const key = String(record[keyField]);
      setSelection((previous) => {
        if (!multiple) return new Map([[key, record]]);
        const next = new Map(previous);
        if (next.has(key)) next.delete(key);
        else next.set(key, record);
        return next;
      });
    },
    [keyField, multiple, setSelection],
  );
  const replaceSingle = useCallback(
    (record?: T) =>
      setSelection(record ? new Map([[String(record[keyField]), record]]) : new Map()),
    [keyField, setSelection],
  );
  const mergeTableChange = useCallback(
    (keys: React.Key[], rows: T[]) =>
      setSelection((previous) =>
        mergeRefTableSelection(previous, previousKeysRef.current, keys, rows, keyField),
      ),
    [keyField, setSelection],
  );

  return {
    selection,
    selectedKeys: useMemo(() => [...selection.keys()], [selection]),
    setSelection,
    resetFromValue,
    toggle,
    replaceSingle,
    mergeTableChange,
    clear: useCallback(() => setSelection(new Map()), [setSelection]),
  };
}
