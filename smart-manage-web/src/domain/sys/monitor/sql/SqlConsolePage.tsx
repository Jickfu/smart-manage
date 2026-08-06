import { useMemo, useRef, useState } from 'react';
import { App, Alert, Button, Empty, Space, Splitter, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { PlayCircleOutlined, ClearOutlined } from '@ant-design/icons';
import { useMutation } from '@tanstack/react-query';
import type { PageComponentProps } from '@/domain/common/page/types';
import { EditPageShell } from '@/domain/common/page/EditPageShell';
import { usePermissionAccess } from '@/domain/common/page/usePermissionAccess';
import { sqlApi } from './api';
import { sqlAccess } from './permissions';
import SqlEditor from './SqlEditor';
import type { SqlEditorRef } from './SqlEditor';
import type { SqlExecutionResult } from './types';
import './sqlConsole.css';

const DEFAULT_SQL = 'SELECT current_database() AS database_name, now() AS server_time;';

function requiresConfirmation(sqlText: string) {
  return !/^\s*(?:--[^\n]*\n|\/\*[\s\S]*?\*\/\s*)*(select|with)\b/i.test(sqlText);
}

function displayValue(value: unknown) {
  if (value === null) return <Tag>NULL</Tag>;
  if (typeof value === 'object') return JSON.stringify(value);
  return String(value);
}

export default function SqlConsolePage(_props: PageComponentProps) {
  const { modal, message } = App.useApp();
  const [sqlText, setSqlText] = useState(DEFAULT_SQL);
  const [result, setResult] = useState<SqlExecutionResult>();
  const editorRef = useRef<SqlEditorRef>(null);
  const { can } = usePermissionAccess(sqlAccess.prefix);
  const executeMutation = useMutation({
    mutationFn: sqlApi.execute,
    onSuccess: (data) => setResult(data),
    onError: (error: Error) => message.error(error.message),
  });
  const execute = (candidate = sqlText.trim()) => {
    if (!candidate) {
      message.warning('请输入要执行的 SQL');
      return;
    }
    const run = () => executeMutation.mutate(candidate);
    if (!requiresConfirmation(candidate)) {
      run();
      return;
    }
    modal.confirm({
      title: '确认执行写入或结构变更 SQL？',
      content: '该操作可能修改数据库。多条语句仅允许纯 INSERT，并会在同一事务中执行。',
      okText: '确认执行',
      okButtonProps: { danger: true },
      onOk: run,
    });
  };
  const tableColumns = useMemo<ColumnsType<unknown[]>>(
    () =>
      (result?.columns ?? []).map((column, columnIndex) => ({
        title: (
          <div
            className="sm-sql-column-title"
            title={[column.typeName, column.comment].filter(Boolean).join(' · ')}
          >
            <span>{column.label}</span>
            {column.comment && <small>{column.comment}</small>}
          </div>
        ),
        key: column.key,
        width: 180,
        ellipsis: true,
        render: (_value, record) => displayValue(record[columnIndex]),
      })),
    [result],
  );
  return (
    <EditPageShell
      loading={false}
      actions={
        <>
          {can(sqlAccess.permissions.execute) && (
            <Button
              type="primary"
              icon={<PlayCircleOutlined />}
              loading={executeMutation.isPending}
              onClick={() => execute(editorRef.current?.getExecutableSql() ?? '')}
            >
              执行
            </Button>
          )}
          <Button
            icon={<ClearOutlined />}
            onClick={() =>
              modal.confirm({
                title: '确认清空 SQL？',
                content: '清空后当前编辑内容和执行结果将无法恢复。',
                okText: '确认清空',
                okButtonProps: { danger: true },
                onOk: () => {
                  setSqlText('');
                  setResult(undefined);
                },
              })
            }
          >
            清空
          </Button>
          <Typography.Text type="secondary" className="sm-sql-console-hint">
            选中 SQL 后按 Ctrl + E 可执行选区
          </Typography.Text>
        </>
      }
    >
      <Splitter orientation="vertical" className="sm-sql-console-splitter">
        <Splitter.Panel defaultSize="45%" min={180}>
          <section className="sm-sql-console-panel">
            <SqlEditor ref={editorRef} value={sqlText} onChange={setSqlText} onExecute={execute} />
          </section>
        </Splitter.Panel>
        <Splitter.Panel min={180}>
          <section className="sm-sql-console-panel sm-sql-result-panel">
            <div className="sm-sql-result-summary">
              <Typography.Text strong>执行结果</Typography.Text>
              {result && (
                <Space wrap>
                  <Tag color={result.type === 'ERROR' ? 'error' : 'success'}>{result.type}</Tag>
                  <span>{result.rowCount} 行</span>
                  <span>{result.executeDuration} ms</span>
                  {result.statementCount > 1 && <span>{result.statementCount} 条语句</span>}
                </Space>
              )}
            </div>
            {result?.type === 'QUERY' ? (
              <>
                {result.truncated && (
                  <Alert type="warning" showIcon title={`${result.message}，结果已截断`} />
                )}
                <Table<unknown[]>
                  className="sm-sql-result-table"
                  size="small"
                  rowKey={(_record, index) => String(index)}
                  columns={tableColumns}
                  dataSource={result.rows ?? []}
                  pagination={false}
                  sticky
                  scroll={{ x: 'max-content', y: 1 }}
                />
              </>
            ) : result ? (
              <Alert
                type={result.type === 'ERROR' ? 'error' : 'success'}
                showIcon
                title={result.message || '执行完成'}
                description={
                  result.statementRowCounts && result.statementRowCounts.length > 1
                    ? `各语句影响行数：${result.statementRowCounts.join('、')}`
                    : undefined
                }
              />
            ) : (
              <Empty description="执行 SQL 后在此查看结果" />
            )}
          </section>
        </Splitter.Panel>
      </Splitter>
    </EditPageShell>
  );
}
