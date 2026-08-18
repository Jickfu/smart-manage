import type { ReactNode } from 'react';
import { useMemo, useState } from 'react';
import type { TableProps } from 'antd';
import { Button, Result, Spin, Table } from 'antd';
import { FilterOutlined, SettingOutlined } from '@ant-design/icons';
import type {
  ColumnType,
  ColumnsType,
  FilterDropdownProps,
  SorterResult,
  TableRowSelection,
} from 'antd/es/table/interface';
import ListFilterBar from './ListFilterBar';
import ListTableShell from './ListTableShell';
import type { AccessResource, PermissionAction } from './access';
import { PermissionActions } from './PermissionActions';
import './ListPage.css';
import { usePageTabTitle } from './usePageTabTitle';
import ColumnSettingsModal from './ColumnSettingsModal';
import {
  applyColumnSettings,
  createColumnSettingsStorageKey,
  createDefaultColumnSettings,
  mergeColumnSettings,
  readStoredColumnSettings,
  removeStoredColumnSettings,
  writeStoredColumnSettings,
} from './columnSettings';
import type { ColumnSetting } from './columnSettings';
import { useUserStore } from '@/stores/user';
import ListColumnFilter from './ListColumnFilter';
import ListFilterSummary from './ListFilterSummary';
import ListExpandedFilters from './ListExpandedFilters';
import type { ListColumnFeatures, ListFilterCondition, ListSortCondition } from './listQuery';

interface StandardListPermissions {
  save: string;
  delete?: string;
  enable?: string;
  disable?: string;
}

const operatorLabels: Record<string, string> = {
  CONTAINS: '包含',
  NOT_CONTAINS: '不包含',
  EQ: '等于',
  NE: '不等于',
  STARTS_WITH: '开头是',
  ENDS_WITH: '结尾是',
  EMPTY: '为空',
  NOT_EMPTY: '不为空',
  GT: '大于',
  GE: '大于等于',
  LT: '小于',
  LE: '小于等于',
  TODAY: '今天',
  THIS_WEEK: '本周',
  THIS_MONTH: '本月',
  LAST_MONTH: '上月',
  PAST_MONTH: '过去一个月',
  PAST_THREE_MONTHS: '过去三个月',
  BETWEEN: '从…到…',
  IN: '是',
};

const filterSummaryLabel = (
  condition: ListFilterCondition,
  feature: ListColumnFeatures[string] | undefined,
) => {
  const values = condition.values ?? (condition.value == null ? [] : [condition.value]);
  const displayValues = values.map((value) => {
    const option = feature?.filter?.options?.find((item) => item.value === value);
    return typeof option?.label === 'string' ? option.label : String(value);
  });
  const suffix = displayValues.length ? ` ${displayValues.join('、')}` : '';
  return `${feature?.label ?? condition.field}：${operatorLabels[condition.operator] ?? condition.operator}${suffix}`;
};

interface ListPageProps<T> {
  title: string;
  /** 过滤区内容 */
  filterContent?: ReactNode;
  /** 过滤摘要文案 */
  filterSummary?: ReactNode;
  /** 工具栏额外操作 */
  toolbarActions?: PermissionAction[];
  /** 业务自定义工具区内容。 */
  toolbarExtra?: ReactNode;
  /** 固定在表头可视区域右侧、不随表头横向滚动的操作。 */
  tableHeaderExtra?: ReactNode;
  /** 传入稳定页面键后启用按当前用户隔离的通用列设置。 */
  columnSettingsKey?: string;
  /** 表头筛选和排序能力，键为列 key 或 dataIndex。 */
  columnFeatures?: ListColumnFeatures;
  /** 已应用的表头筛选条件。 */
  columnFilters?: ListFilterCondition[];
  /** 已应用的服务端排序。 */
  columnSort?: ListSortCondition;
  onColumnFiltersChange?: (filters: ListFilterCondition[]) => void;
  onColumnSortChange?: (sort?: ListSortCondition) => void;
  /** 当前领域的标准列表权限声明 */
  access?: AccessResource<StandardListPermissions>;
  /** 左侧树面板（左树右表布局） */
  treePanel?: ReactNode;
  /** 是否加载中 */
  loading?: boolean;
  /** 是否请求出错 */
  error?: Error | null;
  /** 手动重试 */
  onRetry?: () => void;
  total?: number;
  quickSearchPlaceholder?: string;
  pageNum?: number;
  pageSize?: number;
  onAddNew?: () => void;
  onDelete?: () => void;
  onEnable?: () => void;
  onDisable?: () => void;
  enabledCommandLoading?: boolean;
  onRefresh?: () => void;
  onQuickSearch?: (value: string) => void;
  onPageChange?: (pageNum: number, pageSize: number) => void;
  /** 是否显示通用序号列；树形表格通常关闭，避免层级内序号重复。 */
  showSequence?: boolean;
  /** 是否显示分页器；一次性加载完整层级的树形表格应关闭。 */
  showPagination?: boolean;
  /** Ant Design Table 原生展开配置。 */
  expandable?: TableProps<T>['expandable'];
  /** 是否启用白色/浅灰色交替行，默认关闭。 */
  striped?: boolean;
  /** 业务行样式，会与通用斑马纹类名合并。 */
  rowClassName?: TableProps<T>['rowClassName'];
  /** 数据语义变化时用于重建 Table 内部状态，例如重新应用默认展开行。 */
  tableStateKey?: React.Key;

  /** Table — 行 key */
  rowKey: string | ((record: T) => string);
  /** Table — 列定义（不含勾选列和序号列；至少保留一个未设置 width 的业务列吸收剩余空间） */
  columns: ColumnsType<T>;
  /** Table — 数据源 */
  dataSource: T[];
  /** 勾选模式：不传则不显示勾选列 */
  selectMode?: 'checkbox' | 'radio';
  /** 受控选中 key */
  selectedRowKeys?: React.Key[];
  /** 选中变更 */
  onSelectChange?: (keys: React.Key[]) => void;
}

/**
 * 通用列表页框架。
 *
 * 自动注入：
 * - 序号列（`#` 表头，跨页递增）
 * - 勾选列（checkbox / radio，由 selectMode 控制）
 * - Table size="small" 紧凑模式
 * - 加载中 / 错误 / 空数据四态
 */
function ListPage<T>({
  title,
  filterContent,
  filterSummary,
  toolbarActions,
  toolbarExtra,
  tableHeaderExtra,
  columnSettingsKey,
  columnFeatures,
  columnFilters = [],
  columnSort,
  onColumnFiltersChange,
  onColumnSortChange,
  access,
  treePanel,
  loading = false,
  error = null,
  onRetry,
  total,
  quickSearchPlaceholder,
  pageNum = 1,
  pageSize = 20,
  onAddNew,
  onDelete,
  onEnable,
  onDisable,
  enabledCommandLoading = false,
  onRefresh,
  onQuickSearch,
  onPageChange,
  showSequence = true,
  showPagination = true,
  expandable,
  striped = false,
  rowClassName,
  tableStateKey,
  rowKey,
  columns,
  dataSource,
  selectMode,
  selectedRowKeys,
  onSelectChange,
}: ListPageProps<T>) {
  usePageTabTitle(title);
  const userId = useUserStore((state) => state.userInfo?.id);
  const [columnSettingsOpen, setColumnSettingsOpen] = useState(false);
  const defaultColumnSettings = useMemo(() => createDefaultColumnSettings(columns), [columns]);
  const storageKey =
    columnSettingsKey && userId
      ? createColumnSettingsStorageKey(userId, columnSettingsKey)
      : undefined;
  const loadedColumnSettings = useMemo(
    () =>
      mergeColumnSettings(
        defaultColumnSettings,
        storageKey ? readStoredColumnSettings(storageKey) : undefined,
      ),
    [defaultColumnSettings, storageKey],
  );
  const [columnSettingsOverride, setColumnSettingsOverride] = useState<{
    storageKey?: string;
    settings: ColumnSetting[];
  }>();
  const columnSettings =
    columnSettingsOverride && columnSettingsOverride.storageKey === storageKey
      ? mergeColumnSettings(defaultColumnSettings, columnSettingsOverride.settings)
      : loadedColumnSettings;

  const configuredColumns = useMemo(
    () =>
      columns.map((column) => {
        if ('children' in column) return column;
        const typedColumn = column as ColumnType<T>;
        const dataIndex = typedColumn.dataIndex;
        const columnKey = String(
          typedColumn.key ?? (Array.isArray(dataIndex) ? dataIndex.join('.') : (dataIndex ?? '')),
        );
        const feature = columnFeatures?.[columnKey];
        if (!feature) return typedColumn;
        const filter = columnFilters.find((item) => item.field === columnKey);
        return {
          ...typedColumn,
          // antd 的服务端排序回调依赖 columnKey；业务列未显式声明 key 时使用稳定字段键补齐。
          key: typedColumn.key ?? columnKey,
          ...(feature.filter
            ? {
                filteredValue: filter ? [JSON.stringify(filter)] : null,
                filterIcon: (filtered: boolean) => (
                  <FilterOutlined className={filtered ? 'sm-list-filter-icon-active' : undefined} />
                ),
                filterDropdown: ({ confirm }: FilterDropdownProps) => (
                  <ListColumnFilter
                    key={`${columnKey}-${JSON.stringify(filter ?? null)}`}
                    field={columnKey}
                    type={feature.filter?.type ?? 'string'}
                    options={feature.filter?.options}
                    value={filter}
                    onConfirm={(condition) => {
                      const nextFilters = columnFilters.filter((item) => item.field !== columnKey);
                      if (condition) nextFilters.push(condition);
                      onColumnFiltersChange?.(nextFilters);
                      confirm({ closeDropdown: true });
                    }}
                  />
                ),
              }
            : {}),
          ...(feature.sorter
            ? {
                sorter: true,
                sortOrder:
                  columnSort?.field === columnKey
                    ? columnSort.order === 'ASC'
                      ? ('ascend' as const)
                      : ('descend' as const)
                    : null,
              }
            : {}),
        };
      }) as ColumnsType<T>,
    [columnFeatures, columnFilters, columnSort, columns, onColumnFiltersChange],
  );

  const displayedColumns = useMemo(
    () =>
      columnSettingsKey
        ? applyColumnSettings(configuredColumns, columnSettings)
        : configuredColumns,
    [columnSettings, columnSettingsKey, configuredColumns],
  );
  const rowSelection: TableRowSelection<T> | undefined = useMemo(
    () =>
      selectMode
        ? {
            type: selectMode,
            selectedRowKeys,
            onChange: (keys) => onSelectChange?.(keys),
            columnWidth: 36,
            fixed: true,
          }
        : undefined,
    [selectMode, selectedRowKeys, onSelectChange],
  );

  /** 点击行选中/取消选中 */
  const onRow = useMemo(
    () =>
      selectMode && onSelectChange
        ? (record: T) => ({
            onClick: () => {
              const key =
                typeof rowKey === 'function' ? rowKey(record) : String(record[rowKey as keyof T]);
              const prevKeys = selectedRowKeys ?? [];

              let nextKeys: React.Key[];
              if (selectMode === 'radio') {
                // 单选：点击同一行不做取消，点不同行切换
                nextKeys = prevKeys.includes(key) ? prevKeys : [key];
              } else {
                // 多选：点击切换该行的选中状态
                nextKeys = prevKeys.includes(key)
                  ? prevKeys.filter((k) => k !== key)
                  : [...prevKeys, key];
              }
              onSelectChange?.(nextKeys);
            },
          })
        : undefined,
    [selectMode, rowKey, onSelectChange, selectedRowKeys],
  );

  // 注入序号列 + 业务列（pageNum/pageSize 变化时更新序号公式）
  const fullColumns: ColumnsType<T> = useMemo(
    () =>
      showSequence
        ? [
            {
              title: '#',
              width: 44,
              className: 'sm-list-sequence-column',
              align: 'center' as const,
              fixed: 'left' as const,
              render: (_text: unknown, _record: T, index: number) =>
                (pageNum - 1) * pageSize + index + 1,
            },
            ...displayedColumns,
          ]
        : displayedColumns,
    [displayedColumns, pageNum, pageSize, showSequence],
  );

  const resolvedTableHeaderExtra = columnSettingsKey ? (
    <Button
      type="text"
      icon={<SettingOutlined />}
      title="列设置"
      aria-label="列设置"
      onClick={() => setColumnSettingsOpen(true)}
    />
  ) : (
    tableHeaderExtra
  );

  const resolvedFilterSummary = (
    <div className="sm-list-filter-summary-combined">
      {filterSummary}
      {columnFilters.length > 0 && (
        <ListFilterSummary
          items={columnFilters.map((filter) => ({
            key: `column-${filter.field}`,
            label: filterSummaryLabel(filter, columnFeatures?.[filter.field]),
            onRemove: () =>
              onColumnFiltersChange?.(columnFilters.filter((item) => item.field !== filter.field)),
          }))}
        />
      )}
    </div>
  );
  const resolvedFilterContent =
    filterContent ??
    (columnFeatures && onColumnFiltersChange ? (
      <ListExpandedFilters
        features={columnFeatures}
        filters={columnFilters}
        onChange={onColumnFiltersChange}
      />
    ) : undefined);

  const resolveRowClassName: TableProps<T>['rowClassName'] = (record, index, indent) => {
    const stripeClass = striped
      ? index % 2 === 0
        ? 'sm-list-row-base'
        : 'sm-list-row-alternate'
      : undefined;
    const businessClass =
      typeof rowClassName === 'function' ? rowClassName(record, index, indent) : rowClassName;
    return [stripeClass, businessClass].filter(Boolean).join(' ');
  };

  const handleTableChange: TableProps<T>['onChange'] = (_pagination, _filters, sorter, extra) => {
    if (extra.action !== 'sort') return;
    const activeSorter = (Array.isArray(sorter) ? sorter[0] : sorter) as SorterResult<T>;
    if (!activeSorter?.order || activeSorter.columnKey === undefined) {
      onColumnSortChange?.(undefined);
      return;
    }
    onColumnSortChange?.({
      field: String(activeSorter.columnKey),
      order: activeSorter.order === 'ascend' ? 'ASC' : 'DESC',
    });
  };

  // 错误态
  if (error) {
    return (
      <section className="sm-common-page sm-list-page">
        <div className="sm-list-top">
          <ListFilterBar
            title={title}
            filterContent={resolvedFilterContent}
            filterSummary={resolvedFilterSummary}
            quickSearchPlaceholder={quickSearchPlaceholder}
            onQuickSearch={onQuickSearch}
          />
        </div>
        <div className="sm-list-main">
          <Result
            status="error"
            title="加载失败"
            subTitle={error.message || '请检查网络连接后重试'}
            extra={
              onRetry && (
                <Button type="primary" onClick={onRetry}>
                  重试
                </Button>
              )
            }
          />
        </div>
      </section>
    );
  }

  return (
    <section className="sm-common-page sm-list-page">
      <div className="sm-list-top">
        <ListFilterBar
          title={title}
          filterContent={resolvedFilterContent}
          filterSummary={resolvedFilterSummary}
          quickSearchPlaceholder={quickSearchPlaceholder}
          onQuickSearch={onQuickSearch}
        />
        <div className="sm-list-toolbar">
          <PermissionActions
            prefix={access?.prefix}
            actions={[
              ...(onAddNew
                ? [
                    {
                      key: 'add',
                      label: '新增',
                      permission: access?.permissions.save,
                      type: 'primary' as const,
                      onClick: onAddNew,
                    },
                  ]
                : []),
              ...(onDelete
                ? [
                    {
                      key: 'delete',
                      label: '删除',
                      permission: access?.permissions.delete,
                      danger: true,
                      onClick: onDelete,
                    },
                  ]
                : []),
              ...(onEnable
                ? [
                    {
                      key: 'enable',
                      label: '启用',
                      permission: access?.permissions.enable,
                      disabled: (selectedRowKeys?.length ?? 0) === 0,
                      loading: enabledCommandLoading,
                      onClick: onEnable,
                    },
                  ]
                : []),
              ...(onDisable
                ? [
                    {
                      key: 'disable',
                      label: '禁用',
                      permission: access?.permissions.disable,
                      disabled: (selectedRowKeys?.length ?? 0) === 0,
                      loading: enabledCommandLoading,
                      onClick: onDisable,
                    },
                  ]
                : []),
              ...(onRefresh
                ? [{ key: 'refresh', label: '刷新', type: 'primary' as const, onClick: onRefresh }]
                : []),
              ...(toolbarActions ?? []),
            ]}
          />
          {toolbarExtra}
        </div>
      </div>

      <div className="sm-list-main">
        <Spin spinning={loading}>
          <ListTableShell
            table={
              <Table<T>
                key={tableStateKey}
                className="sm-list-table"
                rowKey={rowKey}
                rowSelection={rowSelection}
                onRow={onRow}
                columns={fullColumns}
                onChange={handleTableChange}
                dataSource={dataSource}
                expandable={expandable}
                rowClassName={resolveRowClassName}
                size="small"
                pagination={false}
                sticky
                scroll={{ x: 'max-content', y: 1 }}
              />
            }
            total={total}
            selectedCount={selectedRowKeys?.length ?? 0}
            pageNum={pageNum}
            pageSize={pageSize}
            onPageChange={onPageChange}
            showPagination={showPagination}
            treePanel={treePanel}
            tableHeaderExtra={resolvedTableHeaderExtra}
          />
        </Spin>
      </div>
      {columnSettingsKey && columnSettingsOpen && (
        <ColumnSettingsModal
          open={columnSettingsOpen}
          settings={columnSettings}
          defaults={defaultColumnSettings}
          onCancel={() => setColumnSettingsOpen(false)}
          onConfirm={(nextSettings) => {
            setColumnSettingsOverride({ storageKey, settings: nextSettings });
            if (storageKey) {
              if (JSON.stringify(nextSettings) === JSON.stringify(defaultColumnSettings)) {
                removeStoredColumnSettings(storageKey);
              } else {
                writeStoredColumnSettings(storageKey, nextSettings);
              }
            }
            setColumnSettingsOpen(false);
          }}
        />
      )}
    </section>
  );
}

export default ListPage;
