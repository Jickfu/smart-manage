import { useState, useCallback, useEffect, useLayoutEffect, useMemo, useRef } from 'react';
import type { ReactNode } from 'react';
import { Button, Input, Table, Pagination, Spin, Empty, Splitter } from 'antd';
import { SearchOutlined, CloseOutlined } from '@ant-design/icons';
import type { InputRef } from 'antd';
import AppModal from './AppModal';
import type { ColumnsType, TableRowSelection } from 'antd/es/table/interface';
import { useRefSelectorQuery } from './useRefSelectorQuery';
import type { RefSelectorFetchFn } from './useRefSelectorQuery';
import { formatRefSelectorDisplayText, isRefSelectorTextOverflowing } from './refSelectorDisplay';
import { useRefSelection } from './useRefSelection';
import { RefSelectorSelectedPanel, RefSelectorTreePanel } from './RefSelectorPanels';
import './RefSelector.css';

// ============================================================
// 类型定义
// ============================================================

/** RefSelector 选择模式 */
type RefSelectorMode = 'default' | 'multiple' | 'tree-table' | 'tree-table-multiple';

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
  displayRender: (record: T) => string;
  /** 字段名映射 */
  fieldNames: RefSelectorFieldNames;
  placeholder?: string;
  disabled?: boolean;
  /** 自定义触发器；适用于工具栏按钮等非表单字段入口。 */
  trigger?: ReactNode;

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
  /** 是否允许拖动选择弹框，默认关闭。 */
  modalDraggable?: boolean;
  /** 是否允许调整选择弹框大小，默认关闭。 */
  modalResizable?: boolean;

  /** 树表模式：树形数据 */
  treeData?: Record<string, unknown>[];
  defaultTreeKey?: string;
  /** 树表模式：树字段映射 */
  treeFieldNames?: { key: string; title: string; children: string };
  /** 树表模式：固定在左树下方的范围或状态控件。 */
  treeFooter?: ReactNode;
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
 * - tree-table-multiple：多选，左树、数据表和已选面板组合布局
 */
function RefSelector<T extends Record<string, unknown>>({
  value,
  onChange,
  displayRender,
  fieldNames,
  placeholder,
  disabled = false,
  trigger,
  selectorKey,
  fetchFn,
  columns,
  mode,
  modalTitle,
  pageSize = 20,
  modalDraggable = false,
  modalResizable = false,
  treeData,
  defaultTreeKey,
  treeFieldNames,
  treeFooter,
}: RefSelectorProps<T>) {
  const [modalOpen, setModalOpen] = useState(false);
  const [triggerFocused, setTriggerFocused] = useState(false);
  const [showSelectionTotal, setShowSelectionTotal] = useState(false);
  const triggerRef = useRef<HTMLDivElement>(null);
  const triggerInputRef = useRef<InputRef>(null);
  const selectionTotalRef = useRef<HTMLSpanElement>(null);
  const query = useRefSelectorQuery({
    fetchFn,
    selectorKey,
    initialPageSize: pageSize,
    enabled: modalOpen,
  });

  // ---- 派生 ----

  const isMultiple = mode === 'multiple' || mode === 'tree-table-multiple';
  const hasTree = mode === 'tree-table' || mode === 'tree-table-multiple';
  const refSelection = useRefSelection(value, isMultiple, fieldNames.key);

  useEffect(() => {
    if (modalOpen && hasTree && defaultTreeKey && !query.parentId) {
      query.onTreeSelect(defaultTreeKey);
    }
  }, [defaultTreeKey, hasTree, modalOpen, query]);

  /** Table rowKey 函数 */
  const rowKey = useCallback((record: T) => String(record[fieldNames.key]), [fieldNames.key]);

  // ---- 事件处理 ----

  /** 打开 Modal：重置查询状态 + 同步外部 value → selectionMap */
  const handleOpen = useCallback(() => {
    query.reset();
    refSelection.resetFromValue();
    setModalOpen(true);
  }, [query, refSelection]);

  /** 取消：丢弃选择，关闭 Modal */
  const handleCancel = useCallback(() => {
    setModalOpen(false);
  }, []);

  /** 确认：提交选择给 onChange */
  const handleConfirm = useCallback(() => {
    const list = [...refSelection.selection.values()];
    if (isMultiple) {
      onChange?.(list.length > 0 ? list : null);
    } else {
      onChange?.(list.length > 0 ? list[0]! : null);
    }
    setModalOpen(false);
  }, [isMultiple, onChange, refSelection.selection]);

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

  const displayText = useMemo(
    () => formatRefSelectorDisplayText(value, displayRender),
    [displayRender, value],
  );

  /**
   * 多选文本是否溢出按“未显示总数时”的可用宽度判断。
   * 总数已经显示时，需要把它当前占用的宽度加回去，避免容器变宽后遮罩无法消失。
   */
  const updateSelectionTotal = useCallback(() => {
    const input = triggerInputRef.current?.input;
    if (!isMultiple || !input || !displayText) {
      setShowSelectionTotal(false);
      return;
    }
    const availableWidth = input.clientWidth + (selectionTotalRef.current?.offsetWidth ?? 0);
    setShowSelectionTotal(isRefSelectorTextOverflowing(input.scrollWidth, availableWidth));
  }, [displayText, isMultiple]);

  useLayoutEffect(() => {
    updateSelectionTotal();
  }, [updateSelectionTotal]);

  useEffect(() => {
    const trigger = triggerRef.current;
    if (!trigger) return;
    const resizeObserver = new ResizeObserver(updateSelectionTotal);
    resizeObserver.observe(trigger);
    return () => resizeObserver.disconnect();
  }, [updateSelectionTotal]);

  // ---- 渲染：触发器 ----

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
      selectedRowKeys: refSelection.selectedKeys,
    };

    if (isMultiple) {
      return {
        ...base,
        type: 'checkbox' as const,
        // 保留跨页已选 key，避免 antd 过滤非当前页 key 导致差分误删
        preserveSelectedRowKeys: true,
        onChange: (newKeys: React.Key[], selectedRows: T[]) => {
          refSelection.mergeTableChange(newKeys, selectedRows);
        },
      };
    }

    return {
      ...base,
      type: 'radio' as const,
      onChange: (_keys: React.Key[], rows: T[]) => {
        refSelection.replaceSingle(rows[0]);
      },
    };
  }, [isMultiple, refSelection]);

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
        refSelection.toggle(record);
      },
      onDoubleClick: () => handleRowDoubleClick(record),
    }),
    [handleRowDoubleClick, refSelection],
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
    return (
      <RefSelectorSelectedPanel
        selection={refSelection.selection}
        keyField={fieldNames.key}
        displayRender={displayRender}
        onClear={refSelection.clear}
        onRemove={(key) =>
          refSelection.setSelection((previous) => {
            const next = new Map(previous);
            next.delete(key);
            return next;
          })
        }
      />
    );
  }

  // ---- 渲染：树表模式的树面板 ----

  function renderTree(): ReactNode {
    return (
      <RefSelectorTreePanel
        treeData={treeData}
        fieldNames={treeFieldNames}
        footer={treeFooter}
        selectedKey={query.parentId}
        onSelect={query.onTreeSelect}
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

    if (hasTree) {
      return (
        <Splitter className="sm-ref-selector-split">
          <Splitter.Panel defaultSize={200} min={160} max="40%">
            <div className="sm-ref-selector-tree-panel">{renderTree()}</div>
          </Splitter.Panel>
          <Splitter.Panel>
            {isMultiple ? (
              <div className="sm-ref-selector-body-multi">
                {renderTableContent()}
                {renderSelectedPanel()}
              </div>
            ) : (
              <div className="sm-ref-selector-body">{renderTableContent()}</div>
            )}
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
      {trigger ? (
        <span
          className="sm-ref-selector-custom-trigger"
          onClick={disabled ? undefined : handleOpen}
        >
          {trigger}
        </span>
      ) : (
        <div ref={triggerRef} className="sm-ref-selector-trigger">
          <Input
            ref={triggerInputRef}
            variant="borderless"
            className="sm-ref-selector-trigger-input"
            value={displayText}
            placeholder={placeholder || '请选择'}
            disabled={disabled}
            readOnly
            onFocus={() => setTriggerFocused(true)}
            onBlur={() => {
              // 输入框只用于查看；失焦后重新以受控引用值审查并恢复规范显示。
              setTriggerFocused(false);
              updateSelectionTotal();
            }}
          />
          {showSelectionTotal && !triggerFocused && isMultiple && Array.isArray(value) && (
            <span
              ref={selectionTotalRef}
              className="sm-ref-selector-trigger-total"
              onClick={() => triggerInputRef.current?.focus({ cursor: 'end' })}
            >
              共{value.length}项
            </span>
          )}
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
      )}

      {/* 选择 Modal */}
      <AppModal
        title={modalTitle}
        headerExtra={modalHeaderExtra}
        open={modalOpen}
        onCancel={handleCancel}
        bodyMode="fixed"
        className="sm-ref-selector-modal"
        width={hasTree ? (isMultiple ? 1120 : 960) : 800}
        draggable={modalDraggable}
        resizable={modalResizable}
        footer={modalFooter}
      >
        <Spin spinning={query.loading && query.records.length === 0}>{renderModalBody()}</Spin>
      </AppModal>
    </>
  );
}

export default RefSelector;
export type { RefSelectorProps, RefSelectorColumn, RefSelectorFieldNames, RefSelectorMode };
