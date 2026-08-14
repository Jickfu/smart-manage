interface SensitiveContactUpdateOptions {
  isAddNew: boolean;
  canReadSensitive: boolean;
  changed: boolean;
}

/** 未获明文权限且未主动重新填写时不提交字段，避免把遮罩值写回数据库。 */
export function resolveSensitiveContactUpdate(
  value: unknown,
  options: SensitiveContactUpdateOptions,
): string | undefined {
  if (!options.isAddNew && !options.canReadSensitive && !options.changed) return undefined;
  return String(value ?? '').trim() || undefined;
}
