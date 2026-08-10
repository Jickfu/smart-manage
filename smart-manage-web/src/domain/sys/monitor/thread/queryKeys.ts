export const threadDiagnosticQueryKeys = {
  all: ['sys', 'monitor', 'thread'] as const,
  instances: () => ['sys', 'monitor', 'instances'] as const,
  list: (instanceId?: string) => ['sys', 'monitor', 'thread', 'list', instanceId] as const,
  detail: (instanceId: string | undefined, threadId?: number) =>
    ['sys', 'monitor', 'thread', 'detail', instanceId, threadId] as const,
};
