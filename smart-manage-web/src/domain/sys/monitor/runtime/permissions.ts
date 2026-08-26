export const runtimeMonitorPermissions = {
  view: 'sys:monitor:runtime:view',
  manage: 'sys:monitor:runtime:manage',
} as const;
export const runtimeMonitorAccess = {
  prefix: 'sys:monitor:runtime',
  permissions: runtimeMonitorPermissions,
} as const;
