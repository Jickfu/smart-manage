import { useMemo, useState } from 'react';
import { Alert, Button, Empty, Input, Space, Spin, Splitter, Table, Tree, Typography } from 'antd';
import type { DataNode } from 'antd/es/tree';
import type { ColumnsType } from 'antd/es/table';
import { useQuery } from '@tanstack/react-query';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { RequestErrorState } from '@/domain/common/component/RequestErrorState';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { sqlApi } from './api';
import { sqlQueryKeys } from './queryKeys';
import { buildCatalogSelectSql, qualifiedObjectName } from './sqlCatalog';
import type { SqlCatalogColumn, SqlCatalogObject } from './types';

const objectTypeLabels: Record<SqlCatalogObject['objectType'], string> = {
  TABLE: '表',
  PARTITIONED_TABLE: '分区表',
  VIEW: '视图',
  MATERIALIZED_VIEW: '物化视图',
  FOREIGN_TABLE: '外部表',
};

const columnColumns: ColumnsType<SqlCatalogColumn> = [
  { ellipsis: true, title: '#', dataIndex: 'position', width: 46 },
  {
    ellipsis: true,
    title: '字段',
    dataIndex: 'columnName',
    width: 150,
    render: (value, record) => `${record.primaryKey ? '🔑 ' : ''}${value}`,
  },
  { ellipsis: true, title: '类型', dataIndex: 'dataType', width: 150 },
  {
    ellipsis: true,
    title: '可空',
    dataIndex: 'nullable',
    width: 64,
    render: (value) => (value ? '是' : '否'),
  },
  { ellipsis: true, title: '默认值', dataIndex: 'defaultValue', width: 180 },
  { ellipsis: true, title: '说明', dataIndex: 'comment', width: 180 },
];

function objectKey(object: Pick<SqlCatalogObject, 'schemaName' | 'objectName'>) {
  return `${encodeURIComponent(object.schemaName)}/${encodeURIComponent(object.objectName)}`;
}

interface SqlCatalogBrowserProps {
  onAppendSql: (sql: string) => void;
}

export default function SqlCatalogBrowser({ onAppendSql }: SqlCatalogBrowserProps) {
  const feedback = useOperationFeedback();
  const [searchText, setSearchText] = useState('');
  const [keyword, setKeyword] = useState('');
  const [selectedKey, setSelectedKey] = useState<string>();
  const catalogQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: sqlQueryKeys.catalog(keyword),
    queryFn: () => sqlApi.catalog(keyword || undefined),
  });
  const objects = useMemo(() => catalogQuery.data?.objects ?? [], [catalogQuery.data?.objects]);
  const objectByKey = useMemo(
    () => new Map(objects.map((object) => [objectKey(object), object])),
    [objects],
  );
  const selectedObject = selectedKey ? objectByKey.get(selectedKey) : undefined;
  const detailQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: sqlQueryKeys.catalogDetail(selectedObject?.schemaName, selectedObject?.objectName),
    queryFn: () => sqlApi.catalogDetail(selectedObject!),
    enabled: Boolean(selectedObject),
  });
  const treeData = useMemo<DataNode[]>(() => {
    const objectsBySchema = new Map<string, SqlCatalogObject[]>();
    objects.forEach((object) => {
      const schemaObjects = objectsBySchema.get(object.schemaName) ?? [];
      schemaObjects.push(object);
      objectsBySchema.set(object.schemaName, schemaObjects);
    });
    return Array.from(objectsBySchema, ([schemaName, schemaObjects]) => ({
      key: `schema:${schemaName}`,
      title: schemaName,
      selectable: false,
      children: schemaObjects.map((object) => ({
        key: objectKey(object),
        title: (
          <span className="sm-sql-catalog-tree-title" title={object.comment || object.objectName}>
            <span>{object.objectName}</span>
            <small>{object.comment || objectTypeLabels[object.objectType]}</small>
          </span>
        ),
        isLeaf: true,
      })),
    }));
  }, [objects]);

  const copyObjectName = async () => {
    if (!selectedObject) return;
    try {
      await navigator.clipboard.writeText(qualifiedObjectName(selectedObject));
      feedback.success('对象名已复制');
    } catch (error) {
      feedback.fromError(error, '复制对象名失败');
    }
  };

  return (
    <section className="sm-sql-catalog">
      <div className="sm-sql-catalog-search">
        <Input.Search
          value={searchText}
          allowClear
          placeholder="搜索表、字段或说明"
          onChange={(event) => setSearchText(event.target.value)}
          onSearch={(value) => setKeyword(value.trim())}
        />
        <Button onClick={() => catalogQuery.refetch()}>刷新</Button>
      </div>
      {catalogQuery.data?.truncated && (
        <Alert banner showIcon type="info" title="结果较多，请输入关键字缩小范围" />
      )}
      <Splitter orientation="vertical" className="sm-sql-catalog-splitter">
        <Splitter.Panel defaultSize="45%" min={120}>
          <div className="sm-sql-catalog-tree-panel">
            {getBlockingQueryError(catalogQuery) ? (
              <RequestErrorState
                error={getBlockingQueryError(catalogQuery) as Error}
                onRetry={() => catalogQuery.refetch()}
              />
            ) : (
              <Spin spinning={catalogQuery.isLoading}>
                {treeData.length ? (
                  <Tree
                    virtual={false}
                    blockNode
                    defaultExpandedKeys={['schema:public']}
                    selectedKeys={selectedKey ? [selectedKey] : []}
                    treeData={treeData}
                    onSelect={(keys) => setSelectedKey(keys[0] ? String(keys[0]) : undefined)}
                  />
                ) : (
                  <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="没有匹配的数据对象" />
                )}
              </Spin>
            )}
          </div>
        </Splitter.Panel>
        <Splitter.Panel min={160}>
          <div className="sm-sql-catalog-detail">
            {selectedObject ? (
              <>
                <div className="sm-sql-catalog-detail-header">
                  <div className="sm-sql-catalog-detail-title">
                    <Typography.Text
                      strong
                      ellipsis={{ tooltip: qualifiedObjectName(selectedObject) }}
                    >
                      {selectedObject.objectName}
                    </Typography.Text>
                    <Typography.Text type="secondary">
                      {objectTypeLabels[selectedObject.objectType]}
                      {selectedObject.comment ? ` · ${selectedObject.comment}` : ''}
                    </Typography.Text>
                  </div>
                  <Space size={6}>
                    <Button onClick={() => void copyObjectName()}>复制对象名</Button>
                    <Button
                      type="primary"
                      onClick={() => onAppendSql(buildCatalogSelectSql(selectedObject))}
                    >
                      追加查询
                    </Button>
                  </Space>
                </div>
                {getBlockingQueryError(detailQuery) ? (
                  <RequestErrorState
                    error={getBlockingQueryError(detailQuery) as Error}
                    onRetry={() => detailQuery.refetch()}
                  />
                ) : (
                  <Table<SqlCatalogColumn>
                    className="sm-sql-catalog-column-table sm-table-fill"
                    size="small"
                    loading={detailQuery.isLoading}
                    rowKey="position"
                    columns={columnColumns}
                    dataSource={detailQuery.data?.columns ?? []}
                    pagination={false}
                    tableLayout="fixed"
                    scroll={{ x: 770, y: 1 }}
                  />
                )}
              </>
            ) : (
              <Empty description="选择数据对象后查看字段" />
            )}
          </div>
        </Splitter.Panel>
      </Splitter>
    </section>
  );
}
