import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useMemo, useRef, useState } from 'react';
import { Alert, Button, Empty, Select, Space, Splitter, Tag, Typography } from 'antd';
import { ClearOutlined, PlayCircleOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { EditPageShell } from '@/domain/common/page/EditPageShell';
import { usePermissionAccess } from '@/domain/common/page/access/usePermissionAccess';
import { componentKeys } from '@/domain/common/registry/componentKeys';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { scriptApi } from './api';
import { scriptAccess } from './permissions';
import { scriptQueryKeys } from './queryKeys';
import ScriptEditor from './ScriptEditor';
import type { ScriptEditorRef } from './ScriptEditor';
import type { ScriptExecutionResult, ScriptTransactionMode } from './types';
import { scriptTemplates } from './scriptTemplates';
import './scriptConsole.css';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';

const DEFAULT_SCRIPT = `console.log('Hello Smart Manage');
return { success: true };`;

const statusColor = { SUCCESS: 'success', ERROR: 'error', TIMEOUT: 'warning' } as const;

const HELP_COMPONENT_KEY = componentKeys.scriptHelp;

export default function ScriptConsolePage(props: PageComponentProps) {
  const feedback = useOperationFeedback();
  const confirmOperation = useOperationConfirm();
  const openCustomTab = useWorkbenchStore((state) => state.openCustomTab);
  const editorRef = useRef<ScriptEditorRef>(null);
  const { can } = usePermissionAccess(scriptAccess.prefix);
  const [content, setContent] = useState(DEFAULT_SCRIPT);
  const [scriptId, setScriptId] = useState<string>();
  const [transactionMode, setTransactionMode] = useState<ScriptTransactionMode>('ATOMIC');
  const [result, setResult] = useState<ScriptExecutionResult>();
  const scriptListQuery = useQuery({
    queryKey: scriptQueryKeys.list({ pageNum: 1, pageSize: 100 }),
    queryFn: () => scriptApi.listPage({ pageNum: 1, pageSize: 100 }),
  });
  const executeMutation = useCommandMutation({
    mutationFn: scriptApi.execute,
    onSuccess: setResult,
  });
  const savedOptions = useMemo(
    () =>
      scriptListQuery.data?.records.map((script) => ({
        value: script.id,
        label: `${script.number} · ${script.name}`,
      })) ?? [],
    [scriptListQuery.data],
  );

  const execute = (candidate: string) => {
    if (!candidate.trim()) {
      feedback.warning('请输入要执行的脚本');
      return;
    }
    void confirmOperation({
      type: 'destructive',
      title:
        transactionMode === 'ATOMIC' ? '确认以原子事务执行脚本？' : '确认以非事务模式执行脚本？',
      description:
        transactionMode === 'ATOMIC'
          ? '加入当前 Spring 事务的数据库操作将在失败时回滚；独立事务、异步任务和外部副作用无法回滚。'
          : '脚本失败时，已经完成的数据库操作或外部副作用可能不会回滚。',
      confirmText: '确认执行',
      onConfirm: () =>
        executeMutation.mutateAsync({ scriptId, content: candidate.trim(), transactionMode }),
    });
  };

  return (
    <EditPageShell
      title="脚本控制台"
      loading={false}
      actions={
        <>
          <Select
            className="sm-script-console-mode"
            value={transactionMode}
            options={[
              { value: 'ATOMIC', label: '原子事务' },
              { value: 'NON_ATOMIC', label: '非事务（高风险）' },
            ]}
            onChange={setTransactionMode}
          />
          <Select
            className="sm-script-console-template"
            placeholder="载入示例模板"
            value={undefined}
            options={scriptTemplates.map((template) => ({
              value: template.key,
              label: template.name,
            }))}
            onChange={(templateKey) => {
              const template = scriptTemplates.find((item) => item.key === templateKey);
              if (!template) return;
              void confirmOperation({
                type: 'warning',
                title: `载入“${template.name}”模板？`,
                description: '当前编辑器内容将被模板替换，已保存的脚本不会受到影响。',
                confirmText: '确认载入',
                onConfirm: () => {
                  setContent(template.content);
                  setScriptId(undefined);
                  setResult(undefined);
                },
              });
            }}
          />
          <Select
            allowClear
            showSearch={{ optionFilterProp: 'label' }}
            className="sm-script-console-saved"
            placeholder="加载已保存脚本"
            loading={scriptListQuery.isLoading}
            value={scriptId}
            options={savedOptions}
            onClear={() => setScriptId(undefined)}
            onChange={async (id) => {
              const detail = await scriptApi.detail(id);
              setScriptId(id);
              setContent(detail.content);
              setResult(undefined);
            }}
          />
          {can(scriptAccess.permissions.execute) && (
            <Button
              type="primary"
              icon={<PlayCircleOutlined />}
              loading={executeMutation.isPending}
              onClick={() => execute(editorRef.current?.getExecutableScript() ?? '')}
            >
              执行
            </Button>
          )}
          <Button
            type="primary"
            icon={<QuestionCircleOutlined />}
            onClick={() => {
              openCustomTab(props.appNumber, HELP_COMPONENT_KEY);
            }}
          >
            使用帮助
          </Button>
          <Button
            danger
            icon={<ClearOutlined />}
            onClick={() =>
              void confirmOperation({
                type: 'destructive',
                title: '确认清空脚本？',
                description: '清空后当前编辑内容和执行结果将无法恢复。',
                confirmText: '确认清空',
                onConfirm: () => {
                  setContent('');
                  setScriptId(undefined);
                  setResult(undefined);
                },
              })
            }
          >
            清空
          </Button>
          <Typography.Text type="secondary" className="sm-script-console-hint">
            选中脚本后按 Ctrl + E 可执行选区
          </Typography.Text>
        </>
      }
    >
      <Splitter orientation="horizontal" className="sm-script-console-splitter">
        <Splitter.Panel defaultSize="55%" min={320}>
          <section className="sm-script-console-panel">
            <ScriptEditor
              ref={editorRef}
              value={content}
              onChange={setContent}
              onExecute={execute}
            />
          </section>
        </Splitter.Panel>
        <Splitter.Panel min={280}>
          <section className="sm-script-console-panel sm-script-result-panel">
            <div className="sm-script-result-summary">
              <Typography.Text strong>执行结果</Typography.Text>
              {result && (
                <Space wrap>
                  <Tag color={statusColor[result.status]}>{result.status}</Tag>
                  <Tag>{result.transactionResult}</Tag>
                  <span>{result.executeDuration} ms</span>
                </Space>
              )}
            </div>
            {result ? (
              <div className="sm-script-result-content">
                {result.errorMessage && <Alert type="error" showIcon title={result.errorMessage} />}
                {result.truncated && (
                  <Alert type="warning" showIcon title="输出超过系统限制，已截断" />
                )}
                <pre>{result.output || '脚本执行完成，无输出。'}</pre>
              </div>
            ) : (
              <Empty description="执行脚本后在此查看结果" />
            )}
          </section>
        </Splitter.Panel>
      </Splitter>
    </EditPageShell>
  );
}
