import { useState, useCallback, useMemo, useRef } from 'react';
import type { ReactNode } from 'react';
import { Button, Input, Table, Pagination, Spin, Empty, Splitter } from 'antd';
import { SearchOutlined, CloseOutlined } from '@ant-design/icons';
import AppModal from './AppModal';
import type { ColumnsType, TableRowSelection } from 'antd/es/table/interface';
import type { DataNode } from 'antd/es/tree';
import ListTree from '@/domain/common/page/ListTree';
import { useRefSelectorQuery } from './useRefSelectorQuery';
import type { RefSelectorFetchFn } from './useRefSelectorQuery';
import './RefSelector.css';

// ============================================================
// 类型定义
// ============================================================

/** RefSelector 选择模式 */
type RefSelectorMode = 'default' | 'multiple' | 'tree-table';

/** 表格列定义 */
interface RefSelectorColumn<T> {
  title: string;
  dataIndex: string;
  width?: number | string;
  render?: (text: unknown, record: T, index: number) => ReactNode;
}

/** 字段名映射：key 用于 rowKey，label 用于备用文本 */
interface RefSelectorFieldNames {
  key: string;
  label: string;
}

/** RefSelector Props */
interface RefSelectorProps<T extends Record<string, unknown>> {
  /** 受控值（Form.Item 注入），单选为 T，多选为 T[] */
  value?: T | T[] | null;
  onChange?: (value: T | T[] | null) => void;

  /** 显示渲染函数，用于触发器展示 */
  displayRender: (record: T) => ReactNode;
  /** 字段名映射 */
  fieldNames: RefSelectorFieldNames;
  placeholder?: string;
  disabled?: boolean;

  /** 选择器标识，用于 queryKey 隔离不同实例的缓存 */
  selectorKey: string | readonly unknown[];
  /** 表格数据获取函数 */
  fetchFn: RefSelectorFetchFn<T>;
  /** 表格列定义（不含序号列和选择列，组件自动注入） */
  columns: RefSelectorColumn<T>[];

  /** 选择模式，默认单选 */
  mode?: RefSelectorMode;
  /** Modal 标题 */
  modalTitle: string;
  /** 每页条数，默认 20 */
  pageSize?: number;

  /** 树表模式：树形数据 */
  treeData?: Record<string, unknown>[];
  /** 树表模式：树字段映射 */
  treeFieldNames?: { key: string; title: string; children: string };
}

// ============================================================
// 组件
// ============================================================

/**
 * RefSelector — 引用选择器（F7 选择器）。
 *
 * 用于替代 antd Select 在数据量大或需要展示多列信息时的场景。
 * 触发器仿 Input variant="underlined" 下划线样式，点击放大镜弹出选择 Modal。
 *
 * 支持三种模式：
 * - default：单选，radio 列，双击行自动确认关闭
 * - multiple：多选，checkbox 列，右侧已选面板
 * - tree-table：单选，左树右表 Splitter 布局
 */
function RefSelector<T extends Record<string, unknown>>({
  value,
  onChange,
  displayRender,
  fieldNames,
  placeholder,
  disabled = false,
  selectorKey,
  fetchFn,
  columns,
  mode,
  modalTitle,
  pageSize = 20,
  treeData,
  treeFieldNames,
}: RefSelectorProps<T>) {
  const [modalOpen, setModalOpen] = useState(false);
  /**
   * 多选选择池 — 用 Map<key, record> 维护，跨分页不丢失已选记录。
   * 更新时返回新 Map 触发渲染，selectedRowKeys / 已选面板均由此派生。
   */
  const [selectionMap, setSelectionMap] = useState<Map<string, T>>(new Map());

  const query = useRefSelectorQuery({
    fetchFn,
    selectorKey,
    initialPageSize: pageSize,
    enabled: modalOpen,
  });

  // ---- 派生 ----

  const isMultiple = mode === 'multiple';

  /** 记录上次 selectedRowKeys，用于 onChange 差分同步 selectionMap */
  const prevKeysRef = useRef<React.Key[]>([]);

  /** 根据 selectionMap 计算 rowKey 数组，控制 Table 勾选态 */
  const selectedRowKeys = useMemo(() => [...selectionMap.keys()], [selectionMap]);

  /**
   * 统一更新 selectionMap 并同步 prevKeysRef。
   * 所有修改 selectionMap 的路径（onChange/行点击/面板删除/清空/open）均通过此函数，
   * 确保 prevKeysRef 始终与 selectionMap 一致。
   */
  const updateSelectionMap = useCallback(
    (updater: Map<string, T> | ((prev: Map<string, T>) => Map<string, T>)) => {
      setSelectionMap((prev) => {
        const next = typeof updater === 'function' ? updater(prev) : updater;
        prevKeysRef.current = [...next.keys()];
        return next;
      });
    },
    [],
  );

  /** Table rowKey 函数 */
  const rowKey = useCallback((record: T) => String(record[fieldNames.key]), [fieldNames.key]);

  // ---- 事件处理 ----

  /** 打开 Modal：重置查询状态 + 同步外部 value → selectionMap */
  const handleOpen = useCallback(() => {
    query.reset();
    const next = new Map<string, T>();
    if (value != null) {
      if (isMultiple && Array.isArray(value)) {
        for (const record of value) {
          next.set(String(record[fieldNames.key]), record);
        }
      } else if (!isMultiple) {
        const record = value as T;
        next.set(String(record[fieldNames.key]), record);
      }
    }
    updateSelectionMap(next);
    setModalOpen(true);
  }, [query, value, isMultiple, fieldNames.key, updateSelectionMap]);

  /** 取消：丢弃选择，关闭 Modal */
  const handleCancel = useCallback(() => {
    setModalOpen(false);
  }, []);

  /** 确认：提交选择给 onChange */
  const handleConfirm = useCallback(() => {
    const list = [...selectionMap.values()];
    if (isMultiple) {
      onChange?.(list.length > 0 ? list : null);
    } else {
      onChange?.(list.length > 0 ? list[0]! : null);
    }
    setModalOpen(false);
  }, [isMultiple, onChange, selectionMap]);

  /** 双击行（单选模式）：选中 + 确认关闭 */
  const handleRowDoubleClick = useCallback(
    (record: T) => {
      if (isMultiple) return;
      onChange?.(record);
      setModalOpen(false);
    },
    [isMultiple, onChange],
  );

  /** 清空已选值 */
  const handleClear = useCallback(
    (e: React.MouseEvent) => {
      e.stopPropagation();
      if (disabled) return;
      onChange?.(null);
    },
    [disabled, onChange],
  );

  /** 是否有已选值 */
  const hasValue = value != null && (!Array.isArray(value) || value.length > 0);

  // ---- 渲染：触发器 ----

  function renderTriggerContent(): ReactNode {
    if (value == null) {
      return <span className="sm-ref-selector-placeholder">{placeholder || '请选择'}</span>;
    }
    if (Array.isArray(value)) {
      if (value.length === 0) {
        return <span className="sm-ref-selector-placeholder">{placeholder || '请选择'}</span>;
      }
      return <span>已选 {value.length} 项</span>;
    }
    return displayRender(value);
  }

  // ---- 渲染：Modal 标题扩展区 ----

  const modalHeaderExtra = (
    <Input.Search
      variant="underlined"
      className="sm-ref-selector-header-search"
      placeholder="快速搜索"
      onSearch={query.onSearch}
    />
  );

  // ---- 渲染：Modal 底部按钮 ----

  const modalFooter = (
    <>
      <Button onClick={handleCancel}>取消</Button>
      <Button type="primary" onClick={handleConfirm}>
        确定
      </Button>
    </>
  );

  // ---- 渲染：表格 ----

  /** rowSelection 配置（受控选中态）。
   *  多选用 onChange 统一入口差分同步 selectionMap（覆盖单击/全选/Shift 连选所有路径）；
   *  单选用 onChange 直接替换 selectionMap。 */
  const rowSelection: TableRowSelection<T> = useMemo(() => {
    const base = {
      columnWidth: 36,
      selectedRowKeys,
    };

    if (isMultiple) {
      return {
        ...base,
        type: 'checkbox' as const,
        // 保留跨页已选 key，避免 antd 过滤非当前页 key 导致差分误删
        preserveSelectedRowKeys: true,
        onChange: (newKeys: React.Key[], selectedRows: T[]) => {
          const prevKeys = prevKeysRef.current;

          updateSelectionMap((prev) => {
            const next = new Map(prev);

            // 删除反选的 key（prevKeys 有、newKeys 没有）
            for (const key of prevKeys) {
              if (!newKeys.includes(key)) {
                next.delete(String(key));
              }
            }

            // 添加新选的 key（newKeys 有、prevKeys 没有），从当前页 rows 取 record 快照
            for (const key of newKeys) {
              if (!prevKeys.includes(key)) {
                const record = selectedRows.find((r) => String(r[fieldNames.key]) === String(key));
                if (record) next.set(String(key), record);
              }
            }

            return next;
          });
        },
      };
    }

    return {
      ...base,
      type: 'radio' as const,
      onChange: (_keys: React.Key[], rows: T[]) => {
        updateSelectionMap(() => {
          const next = new Map<string, T>();
          if (rows.length > 0) next.set(String(rows[0]![fieldNames.key]), rows[0]!);
          return next;
        });
      },
    };
  }, [isMultiple, selectedRowKeys, fieldNames.key, updateSelectionMap]);

  /** 完整列定义：序号 + 用户列 */
  const fullColumns: ColumnsType<T> = useMemo(
    () => [
      {
        title: '#',
        width: 44,
        className: 'sm-ref-selector-sequence-column',
        align: 'center' as const,
        fixed: 'left' as const,
        render: (_text: unknown, _record: T, index: number) =>
          (query.pageNum - 1) * query.pageSize + index + 1,
      },
      ...columns.map((col): ColumnsType<T>[number] => ({
        title: col.title,
        dataIndex: col.dataIndex,
        width: col.width,
        ellipsis: true,
        render: col.render
          ? (text: unknown, record: T, index: number) => col.render!(text, record, index)
          : undefined,
      })),
    ],
    [columns, query.pageNum, query.pageSize],
  );

  /** 行事件：点击行即选中（单选/多选），单选模式双击确认关闭 */
  const onRow = useCallback(
    (record: T) => ({
      onClick: () => {
        const key = String(record[fieldNames.key]);
        if (isMultiple) {
          updateSelectionMap((prev) => {
            const next = new Map(prev);
            if (next.has(key)) next.delete(key);
            else next.set(key, record);
            return next;
          });
        } else {
          updateSelectionMap(new Map([[key, record]]));
        }
      },
      onDoubleClick: () => handleRowDoubleClick(record),
    }),
    [isMultiple, fieldNames.key, handleRowDoubleClick, updateSelectionMap],
  );

  /** 表格内容（meta 栏 + Table） */
  function renderTableContent(): ReactNode {
    return (
      <div className="sm-ref-selector-table-wrap">
        {/* 元信息栏：总条数 + 分页 */}
        <div className="sm-ref-selector-meta">
          <span>共 {query.total} 条</span>
          <Pagination
            size="small"
            showSizeChanger
            pageSizeOptions={['10', '20', '50', '100']}
            current={query.pageNum}
            pageSize={query.pageSize}
            total={query.total}
            showTotal={(t) => `共 ${t} 条`}
            onChange={(nextPage, nextSize) => query.onPageChange(nextPage, nextSize)}
          />
        </div>
        {/* 表格体 */}
        <div className="sm-ref-selector-table-body">
          <Table<T>
            className="sm-ref-selector-table"
            rowKey={rowKey}
            rowSelection={rowSelection}
            columns={fullColumns}
            dataSource={query.records}
            size="small"
            pagination={false}
            loading={query.fetching}
            onRow={onRow}
            sticky
            tableLayout="fixed"
            scroll={{ x: 'max-content', y: 1 }}
          />
        </div>
      </div>
    );
  }

  // ---- 渲染：多选右侧已选面板 ----

  function renderSelectedPanel(): ReactNode {
    const list = [...selectionMap.values()];
    return (
      <aside className="sm-ref-selector-selected-panel">
        <div className="sm-ref-selector-selected-header">
          <span>已选 {selectionMap.size} 项</span>
          {selectionMap.size > 0 && (
            <Button type="link" onClick={() => updateSelectionMap(new Map())}>
              清空
            </Button>
          )}
        </div>
        <div className="sm-ref-selector-selected-list">
          {list.map((record) => (
            <div key={String(record[fieldNames.key])} className="sm-ref-selector-selected-item">
              <span className="sm-ref-selector-selected-item-label">{displayRender(record)}</span>
              <span
                className="sm-ref-selector-selected-item-remove"
                onClick={() => {
                  updateSelectionMap((prev) => {
                    const next = new Map(prev);
                    next.delete(String(record[fieldNames.key]));
                    return next;
                  });
                }}
              >
                <CloseOutlined />
              </span>
            </div>
          ))}
          {selectionMap.size === 0 && (
            <Empty
              image={Empty.PRESENTED_IMAGE_SIMPLE}
              description="暂未选择"
              className="sm-ref-selector-empty"
            />
          )}
        </div>
      </aside>
    );
  }

  // ---- 渲染：树表模式的树面板 ----

  function renderTree(): ReactNode {
    if (!treeData || treeData.length === 0) return null;
    return (
      <ListTree
        treeData={treeData as unknown as DataNode[]}
        fieldNames={treeFieldNames}
        onSelect={(keys) => {
          query.onTreeSelect(keys.length > 0 ? String(keys[0]) : undefined);
        }}
        defaultExpandAll
        blockNode
      />
    );
  }

  // ---- 渲染：Modal Body ----

  function renderModalBody(): ReactNode {
    if (query.error) {
      return (
        <div className="sm-ref-selector-body sm-ref-selector-error-body">
          <Empty description={query.error.message || '加载失败'} />
        </div>
      );
    }

    if (mode === 'tree-table') {
      return (
        <Splitter className="sm-ref-selector-split">
          <Splitter.Panel defaultSize={200} min={160} max="40%">
            <div className="sm-ref-selector-tree-panel">{renderTree()}</div>
          </Splitter.Panel>
          <Splitter.Panel>
            <div className="sm-ref-selector-body">{renderTableContent()}</div>
          </Splitter.Panel>
        </Splitter>
      );
    }

    if (mode === 'multiple') {
      return (
        <div className="sm-ref-selector-body-multi">
          {renderTableContent()}
          {renderSelectedPanel()}
        </div>
      );
    }

    return <div className="sm-ref-selector-body">{renderTableContent()}</div>;
  }

  // ============================================================
  // 主渲染
  // ============================================================

  return (
    <>
      {/* 触发器 */}
      <div
        className="sm-ref-selector-trigger"
        onClick={disabled ? undefined : handleOpen}
        role="button"
        tabIndex={disabled ? -1 : 0}
        onKeyDown={(e) => {
          if (e.key === 'Enter' && !disabled) handleOpen();
        }}
      >
        <div className="sm-ref-selector-trigger-display">{renderTriggerContent()}</div>
        {/* 清空按钮 — 有值时显示 */}
        {hasValue && !disabled && (
          <Button
            type="link"
            icon={<CloseOutlined />}
            className="sm-ref-selector-trigger-clear"
            onClick={handleClear}
          />
        )}
        <Button
          type="text"
          icon={<SearchOutlined />}
          className="sm-ref-selector-trigger-btn"
          onClick={(e) => {
            e.stopPropagation();
            if (!disabled) handleOpen();
          }}
          disabled={disabled}
        />
      </div>

      {/* 选择 Modal */}
      <AppModal
        title={modalTitle}
        headerExtra={modalHeaderExtra}
        open={modalOpen}
        onCancel={handleCancel}
        bodyMode="fixed"
        className="sm-ref-selector-modal"
        width={mode === 'tree-table' ? 960 : 800}
        footer={modalFooter}
      >
        <Spin spinning={query.loading && query.records.length === 0}>{renderModalBody()}</Spin>
      </AppModal>
    </>
  );
}

export default RefSelector;
export type { RefSelectorProps, RefSelectorColumn, RefSelectorFieldNames, RefSelectorMode };
