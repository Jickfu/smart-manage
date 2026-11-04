export const singletonQueryKeys = {
  all: ['scheduler-singleton'] as const,
  lists: () => [...singletonQueryKeys.all, 'list'] as const,
  detail: (number?: string) => [...singletonQueryKeys.all, 'detail', number] as const,
  events: (number?: string) => [...singletonQueryKeys.all, 'events', number] as const,
};
