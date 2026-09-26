import { Empty, Spin } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { RequestErrorState } from '@/domain/common/component/RequestErrorState';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import type { DomainViewProps } from '@/domain/common/registry/domainExtensions';
import { workflowApi } from '../api';

export default function TaskPreview({ active }: DomainViewProps) {
  const query = useQuery({
    queryKey: ['workflow', 'tasks', 'PENDING', 1, 10],
    queryFn: () => workflowApi.tasks({ box: 'PENDING', pageNum: 1, pageSize: 10 }),
    enabled: active,
    meta: { errorPresentation: 'local-initial' },
  });
  const error = getBlockingQueryError(query);
  if (error)
    return (
      <RequestErrorState
        error={error}
        onRetry={() => {
          if (active) void query.refetch();
        }}
      />
    );
  return (
    <Spin spinning={query.isLoading}>
      {query.data?.total === 0 ? (
        <Empty description="暂无待办任务" />
      ) : (
        <>
          {query.data && <p>待办任务：{query.data.total}</p>}
          {query.data?.records.map((task) => (
            <div className="sm-inbox-preview-item" key={task.id}>
              <strong>{task.number}</strong>
              <p>{task.currentNode}</p>
            </div>
          ))}
        </>
      )}
    </Spin>
  );
}
