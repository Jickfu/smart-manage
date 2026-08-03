import { useMemo, useState } from 'react';
import type { Key } from 'react';
import {
  App,
  Alert,
  Button,
  Card,
  Descriptions,
  Drawer,
  Input,
  Space,
  Statistic,
  Table,
  Tag,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import SmChart from '@/domain/common/chart/SmChart';
import { PermissionActions } from '@/domain/common/page/PermissionActions';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { usePermissionAccess } from '@/domain/common/page/usePermissionAccess';
import type { PageComponentProps } from '@/domain/common/page/types';
import { redisApi } from './api';
import { redisAccess } from './permissions';
import { redisQueryKeys } from './queryKeys';
import type { RedisKey } from './types';
import './redisPage.css';

export default function RedisPage(_: PageComponentProps) {
  const { modal } = App.useApp();
  const { can } = usePermissionAccess(redisAccess.prefix);
  const queryClient = useQueryClient();
  const [patternInput, setPatternInput] = useState('*');
  const [pattern, setPattern] = useState('*');
  const [cursor, setCursor] = useState('0');
  const [cursorHistory, setCursorHistory] = useState<string[]>([]);
  const [selectedKeys, setSelectedKeys] = useState<Key[]>([]);
  const [valueKey, setValueKey] = useState<string>();
  const runtimeQuery = useQuery({ queryKey: redisQueryKeys.runtime(), queryFn: redisApi.runtime });
  const keysQuery = useQuery({
    queryKey: redisQueryKeys.keys(cursor, pattern),
    queryFn: () => redisApi.keys({ cursor, pattern, count: 30 }),
  });
  const valueQuery = useQuery({
    queryKey: redisQueryKeys.value(valueKey),
    queryFn: () => redisApi.value(valueKey!),
    enabled: Boolean(valueKey),
  });
  const deleteMutation = useCommandMutation({
    mutationFn: redisApi.delete,
    successMessage: (keys) => `已删除 ${keys.length} 个 Key`,
    onSuccess: async () => {
      setSelectedKeys([]);
      await queryClient.invalidateQueries({ queryKey: redisQueryKeys.all });
    },
  });
  const memoryOption = useMemo(
    () => ({
      series: [
        {
          type: 'gauge',
          min: 0,
          max: Math.max(
            runtimeQuery.data?.maxMemoryBytes || runtimeQuery.data?.usedMemoryBytes || 1,
            1,
          ),
          progress: { show: true },
          detail: { formatter: runtimeQuery.data?.usedMemoryDisplay ?? '-' },
          data: [{ value: runtimeQuery.data?.usedMemoryBytes ?? 0, name: 'Redis 内存' }],
        },
      ],
    }),
    [runtimeQuery.data],
  );
  const confirmDelete = (keys: string[]) =>
    modal.confirm({
      title: `删除 ${keys.length} 个 Redis Key？`,
      content: '删除后无法恢复，不会自动清理关联业务状态。',
      okText: '确认删除',
      okButtonProps: { danger: true },
      onOk: () => deleteMutation.mutateAsync(keys),
    });
  const columns: ColumnsType<RedisKey> = [
    {
      title: 'Key',
      dataIndex: 'key',
      render: (key, record) => (
        <Button
          type="link"
          disabled={!record.valueReadable || !can(redisAccess.permissions.value)}
          onClick={() => setValueKey(key)}
        >
          {key}
        </Button>
      ),
    },
    { title: '类型', dataIndex: 'type', width: 100, render: (value) => <Tag>{value}</Tag> },
    {
      title: 'TTL',
      dataIndex: 'ttl',
      width: 120,
      render: (value) => (value === -1 ? '永久' : value === -2 ? '不存在' : `${value} 秒`),
    },
    {
      title: '内存',
      dataIndex: 'memoryBytes',
      width: 120,
      render: (value) => (value == null ? '-' : `${value} B`),
    },
    {
      title: 'Value',
      dataIndex: 'valueReadable',
      width: 110,
      render: (value) => (value ? '可预览' : <Tag color="warning">敏感</Tag>),
    },
    {
      title: '操作',
      key: 'actions',
      width: 90,
      render: (_, record) => (
        <PermissionActions
          prefix={redisAccess.prefix}
          actions={[
            {
              key: 'delete',
              label: '删除',
              permission: redisAccess.permissions.delete,
              danger: true,
              onClick: () => confirmDelete([record.key]),
            },
          ]}
        />
      ),
    },
  ];
  const search = () => {
    setPattern(patternInput.trim() || '*');
    setCursor('0');
    setCursorHistory([]);
    setSelectedKeys([]);
  };
  const next = () => {
    const nextCursor = keysQuery.data?.nextCursor;
    if (!nextCursor || nextCursor === '0') return;
    setCursorHistory((history) => [...history, cursor]);
    setCursor(nextCursor);
    setSelectedKeys([]);
  };
  const previous = () => {
    const previousCursor = cursorHistory.at(-1);
    if (previousCursor === undefined) return;
    setCursorHistory((history) => history.slice(0, -1));
    setCursor(previousCursor);
    setSelectedKeys([]);
  };
  return (
    <div className="sm-redis-page">
      <Alert
        type="warning"
        showIcon
        title="Redis 管理属于高风险能力；敏感认证 Key 只展示元数据。"
      />
      {(runtimeQuery.error || keysQuery.error) && (
        <Alert
          type="error"
          showIcon
          title={(runtimeQuery.error ?? keysQuery.error)?.message ?? 'Redis 查询失败'}
        />
      )}
      <div className="sm-redis-overview">
        <Card title="Redis 实时状态">
          <Descriptions
            column={2}
            items={[
              { key: 'version', label: '版本', children: runtimeQuery.data?.version ?? '-' },
              { key: 'database', label: 'DB', children: runtimeQuery.data?.database ?? '-' },
              {
                key: 'clients',
                label: '客户端',
                children: runtimeQuery.data?.connectedClients ?? '-',
              },
              { key: 'keys', label: 'Key 数', children: runtimeQuery.data?.dbSize ?? '-' },
              {
                key: 'uptime',
                label: '运行时间',
                children: runtimeQuery.data
                  ? `${Math.floor(runtimeQuery.data.uptimeSeconds / 86400)} 天`
                  : '-',
              },
              {
                key: 'hitRate',
                label: '命中率',
                children:
                  runtimeQuery.data?.hitRate == null
                    ? '-'
                    : `${(runtimeQuery.data.hitRate * 100).toFixed(1)}%`,
              },
            ]}
          />
          <Statistic title="已用内存" value={runtimeQuery.data?.usedMemoryDisplay ?? '-'} />
        </Card>
        <Card title="内存快照">
          <SmChart option={memoryOption} ariaLabel="Redis 当前内存使用量" />
        </Card>
      </div>
      <Card
        title="Redis Key"
        extra={
          <Space>
            <Input.Search
              value={patternInput}
              placeholder="SCAN Pattern，例如 sys:*"
              enterButton="查询"
              onChange={(event) => setPatternInput(event.target.value)}
              onSearch={search}
            />
            <Button onClick={() => keysQuery.refetch()}>刷新</Button>
          </Space>
        }
      >
        <PermissionActions
          prefix={redisAccess.prefix}
          actions={[
            {
              key: 'batchDelete',
              label: `删除所选（${selectedKeys.length}）`,
              permission: redisAccess.permissions.delete,
              danger: true,
              disabled: selectedKeys.length === 0,
              loading: deleteMutation.isPending,
              onClick: () => confirmDelete(selectedKeys.map(String)),
            },
          ]}
        />
        <Table
          rowKey="key"
          loading={keysQuery.isLoading}
          columns={columns}
          dataSource={keysQuery.data?.records ?? []}
          pagination={false}
          rowSelection={{ selectedRowKeys: selectedKeys, onChange: setSelectedKeys }}
          scroll={{ x: 900 }}
        />
        <div className="sm-redis-pagination">
          <Button disabled={cursorHistory.length === 0} onClick={previous}>
            上一页
          </Button>
          <span>游标：{cursor}</span>
          <Button disabled={keysQuery.data?.finished} onClick={next}>
            下一页
          </Button>
        </div>
      </Card>
      <Drawer
        title={valueKey ? `Value：${valueKey}` : 'Value'}
        size="large"
        open={Boolean(valueKey)}
        onClose={() => setValueKey(undefined)}
      >
        {valueQuery.error && <Alert type="error" showIcon title={valueQuery.error.message} />}
        {valueQuery.data?.truncated && (
          <Alert type="warning" showIcon title="Value 超过安全预览上限，仅展示部分内容。" />
        )}
        <pre className="sm-redis-value">
          {valueQuery.isLoading ? '加载中…' : JSON.stringify(valueQuery.data?.items ?? [], null, 2)}
        </pre>
      </Drawer>
    </div>
  );
}
