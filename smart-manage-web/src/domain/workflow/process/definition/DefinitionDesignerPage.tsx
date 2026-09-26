import { useRef } from 'react';
import { Button } from 'antd';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { EditPageShell } from '@/domain/common/page/EditPageShell';
import { useBeforeCloseGuard } from '@/domain/common/page/tab/useBeforeCloseGuard';
import { usePermissionAccess } from '@/domain/common/page/access/usePermissionAccess';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import type { PageComponentProps } from '@/domain/common/page/types';
import { OperationType } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { workflowApi } from '../api';
import { definitionAccess } from './permissions';
import DesignerFrame from './DesignerFrame';

/** 每个定义版本拥有独立 iframe；切换页签保留画布，关闭时统一检查未保存修改。 */
export default function DefinitionDesignerPage(props: PageComponentProps) {
  const dirty = useRef(false);
  const client = useQueryClient();
  const { can, loading: permissionsLoading } = usePermissionAccess(definitionAccess.prefix);
  useBeforeCloseGuard(props.appNumber, props.tabKey, dirty);
  const query = useQuery({
    queryKey: ['workflow', 'design', props.billId],
    queryFn: () => workflowApi.design(props.billId!),
    enabled: props.active && Boolean(props.billId),
    meta: { errorPresentation: 'local-initial' },
  });
  const definition = query.data?.definition;
  const readOnly =
    props.operationType === OperationType.VIEW ||
    Boolean(definition?.isPublish) ||
    !can(definitionAccess.permissions.save);
  const title = definition
    ? `${definition.flowName} · V${definition.version}${readOnly ? '（只读）' : ''}`
    : '流程设计';
  return (
    <div className="sm-workflow-designer-page">
      <EditPageShell
        title={title}
        loading={query.isLoading || permissionsLoading}
        error={getBlockingQueryError(query)}
        onRetry={() => {
          if (props.active) void query.refetch();
        }}
        actions={
          <>
            <Button
              onClick={() => {
                void useWorkbenchStore.getState().removeContentTab(props.appNumber, props.tabKey);
              }}
            >
              关闭
            </Button>
            <span className="sm-workflow-designer-caption">{title}</span>
          </>
        }
      >
        {definition && !permissionsLoading && (
          <DesignerFrame
            definitionId={props.billId}
            readOnly={readOnly}
            onDirty={(value) => {
              dirty.current = value;
            }}
            onSaved={() => {
              void client.invalidateQueries({ queryKey: ['workflow', 'definitions'] });
            }}
          />
        )}
      </EditPageShell>
    </div>
  );
}
