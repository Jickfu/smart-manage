import type { SqlCatalogObject } from './types';

/** PostgreSQL 标识符始终使用双引号，避免对象名与关键字冲突。 */
export function quotePostgresIdentifier(identifier: string) {
  return `"${identifier.replaceAll('"', '""')}"`;
}

export function qualifiedObjectName(object: Pick<SqlCatalogObject, 'schemaName' | 'objectName'>) {
  return `${quotePostgresIdentifier(object.schemaName)}.${quotePostgresIdentifier(object.objectName)}`;
}

export function buildCatalogSelectSql(object: Pick<SqlCatalogObject, 'schemaName' | 'objectName'>) {
  return `SELECT *\nFROM ${qualifiedObjectName(object)}\nLIMIT 100;`;
}
