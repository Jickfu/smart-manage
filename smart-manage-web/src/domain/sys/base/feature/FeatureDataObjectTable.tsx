import { Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { FeatureDataObject } from './types';
import './FeatureEditPage.css';

const roleLabels: Record<FeatureDataObject['objectRole'], string> = {
  PRIMARY: '主表',
  DETAIL: '明细表',
  RELATION: '关联表',
  HISTORY: '历史表',
  LOG: '日志表',
};

const typeLabels: Record<FeatureDataObject['objectType'], string> = {
  TABLE: '表',
  PARTITIONED_TABLE: '分区表',
  VIEW: '视图',
  MATERIALIZED_VIEW: '物化视图',
  FOREIGN_TABLE: '外部表',
};

const columns: ColumnsType<FeatureDataObject> = [
  {
    ellipsis: true,
    title: '角色',
    dataIndex: 'objectRole',
    width: 80,
    render: (value: FeatureDataObject['objectRole']) => roleLabels[value],
  },
  { ellipsis: true, title: '名称', dataIndex: 'displayName', width: 130 },
  { ellipsis: true, title: 'Schema', dataIndex: 'schemaName', width: 110 },
  { ellipsis: true, title: '数据对象', dataIndex: 'objectName' },
  {
    ellipsis: true,
    title: '类型',
    dataIndex: 'objectType',
    width: 90,
    render: (value: FeatureDataObject['objectType']) => typeLabels[value],
  },
];

export function FeatureDataObjectTable({ entries }: { entries: FeatureDataObject[] }) {
  return (
    <Table<FeatureDataObject>
      className="sm-feature-data-object-table"
      size="small"
      rowKey="id"
      columns={columns}
      dataSource={entries}
      pagination={false}
      tableLayout="fixed"
      locale={{ emptyText: '该功能没有关联数据对象' }}
    />
  );
}
