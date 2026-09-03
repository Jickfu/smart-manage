import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useMemo, useRef, useState } from 'react';
import { Alert, Button, Empty, Space, Splitter, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { PlayCircleOutlined, ClearOutlined } from '@ant-design/icons';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import type { PageComponentProps } from '@/domain/common/page/types';
import { EditPageShell } from '@/domain/common/page/EditPageShell';
import { usePermissionAccess } from '@/domain/common/page/access/usePermissionAccess';
import { sqlApi } from './api';
import { sqlAccess } from './permissions';
import SqlEditor from './SqlEditor';
import type { SqlEditorRef } from './SqlEditor';
import type { SqlExecutionResult } from './types';
import './sqlConsole.css';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';

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
  const feedback = useOperationFeedback();
  const confirmOperation = useOperationConfirm();
  const [sqlText, setSqlText] = useState(DEFAULT_SQL);
  const [result, setResult] = useState<SqlExecutionResult>();
  const editorRef = useRef<SqlEditorRef>(null);
  const { can } = usePermissionAccess(sqlAccess.prefix);
  const executeMutation = useCommandMutation({
    mutationFn: sqlApi.execute,
    onSuccess: (data) => setResult(data),
  });
  const execute = (candidate = sqlText.trim()) => {
    if (!candidate) {
      feedback.warning('请输入要执行的 SQL');
      return;
    }
    const run = () => executeMutation.mutate(candidate);
    if (!requiresConfirmation(candidate)) {
      run();
      return;
    }
    void confirmOperation({
      type: 'destructive',
      title: '确认执行写入或结构变更 SQL？',
      description: '该操作可能修改数据库。多条语句仅允许纯 INSERT，并会在同一事务中执行。',
      confirmText: '确认执行',
      onConfirm: run,
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
      title="SQL 控制台"
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
            danger
            icon={<ClearOutlined />}
            onClick={() =>
              void confirmOperation({
                type: 'destructive',
                title: '确认清空 SQL？',
                description: '清空后当前编辑内容和执行结果将无法恢复。',
                confirmText: '确认清空',
                onConfirm: () => {
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
