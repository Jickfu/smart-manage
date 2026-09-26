import { useQuery } from '@tanstack/react-query';
import { Button } from 'antd';
import { DomainView } from '@/domain/common/registry/DomainView';
import type { DomainViewProps } from '@/domain/common/registry/domainExtensions';
import { EditPageShell } from '@/domain/common/page/EditPageShell';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { workflowApi } from '../api';
import '../workflow.css';

export default function ApprovalView(props: DomainViewProps) {
  const query = useQuery({
    queryKey: ['workflow', 'instance', props.resourceId],
    queryFn: () => workflowApi.detail(props.resourceId!),
    enabled: props.active && Boolean(props.resourceId),
    meta: { errorPresentation: 'local-initial' },
  });
  const detail = query.data;
  const error = getBlockingQueryError(query);
  if (detail && !error)
    return (
      <DomainView
        viewKey={`approval:${detail.businessType}`}
        {...props}
        context={{ ...props.context, businessId: detail.businessId }}
      />
    );
  return (
    <EditPageShell
      title="审批详情"
      loading={query.isLoading}
      error={error}
      onRetry={() => {
        if (props.active) void query.refetch();
      }}
      actions={<Button onClick={props.onBack}>返回</Button>}
    >
      <span />
    </EditPageShell>
  );
}
