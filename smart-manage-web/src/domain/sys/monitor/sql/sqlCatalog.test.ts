import { describe, expect, it } from 'vitest';
import { buildCatalogSelectSql, quotePostgresIdentifier } from './sqlCatalog';

describe('SQL catalog helpers', () => {
  it('quotes embedded double quotes in PostgreSQL identifiers', () => {
    expect(quotePostgresIdentifier('odd"name')).toBe('"odd""name"');
  });

  it('builds a bounded query with schema-qualified identifiers', () => {
    expect(buildCatalogSelectSql({ schemaName: 'public', objectName: 't_sys_user' })).toBe(
      'SELECT *\nFROM "public"."t_sys_user"\nLIMIT 100;',
    );
  });
});
