export const openApiQueryKeys = {
  all: ['sys', 'base', 'openapi'] as const,
  applications: () => [...openApiQueryKeys.all, 'applications'] as const,
  applicationList: (scope: unknown) => [...openApiQueryKeys.applications(), 'list', scope] as const,
  applicationDetail: (id?: string) => [...openApiQueryKeys.applications(), 'detail', id] as const,
  catalog: () => [...openApiQueryKeys.all, 'catalog'] as const,
  catalogList: (scope: unknown) => [...openApiQueryKeys.catalog(), 'list', scope] as const,
  catalogDetail: (id?: string) => [...openApiQueryKeys.catalog(), 'detail', id] as const,
  invocations: () => [...openApiQueryKeys.all, 'invocations'] as const,
  invocationList: (scope: unknown) => [...openApiQueryKeys.invocations(), 'list', scope] as const,
  stats: () => [...openApiQueryKeys.invocations(), 'stats'] as const,
};
