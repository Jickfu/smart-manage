/** 平台保留的标准单据状态；业务状态可在 E-Z 范围内按聚合扩展。 */
export enum StandardBillStatus {
  SAVED = 'A',
  SUBMITTED = 'B',
  AUDITED = 'C',
  CLOSED = 'D',
}

export interface BillStatusDefinition {
  value: string;
  label: string;
  color: string;
}

export interface BillStatusCatalog {
  readonly definitions: readonly BillStatusDefinition[];
  readonly options: readonly { value: string; label: string }[];
  get(value: string): BillStatusDefinition | undefined;
  require(value: string): BillStatusDefinition;
}

export const standardBillStatusDefinitions: readonly BillStatusDefinition[] = [
  { value: StandardBillStatus.SAVED, label: '暂存', color: 'default' },
  { value: StandardBillStatus.SUBMITTED, label: '已提交', color: 'blue' },
  { value: StandardBillStatus.AUDITED, label: '已审核', color: 'green' },
  { value: StandardBillStatus.CLOSED, label: '已关闭', color: 'default' },
];

/** 创建聚合自己的状态目录；扩展状态不得覆盖平台 A-D。 */
export function createBillStatusCatalog(
  extensions: readonly BillStatusDefinition[] = [],
): BillStatusCatalog {
  const definitions = [...standardBillStatusDefinitions, ...extensions];
  const definitionsByValue = new Map<string, BillStatusDefinition>();
  for (const definition of definitions) {
    if (!/^[A-Z]$/.test(definition.value)) {
      throw new Error(`单据状态编码必须是一个大写 ASCII 字母：${definition.value}`);
    }
    if (!definition.label.trim()) {
      throw new Error(`单据状态名称不能为空：${definition.value}`);
    }
    if (definitionsByValue.has(definition.value)) {
      throw new Error(`单据状态编码重复：${definition.value}`);
    }
    definitionsByValue.set(definition.value, { ...definition, label: definition.label.trim() });
  }
  const immutableDefinitions = [...definitionsByValue.values()];
  return {
    definitions: immutableDefinitions,
    options: immutableDefinitions.map(({ value, label }) => ({ value, label })),
    get: (value) => definitionsByValue.get(value),
    require: (value) => {
      const definition = definitionsByValue.get(value);
      if (!definition) throw new Error(`未知单据状态：${value}`);
      return definition;
    },
  };
}

export const standardBillStatusCatalog = createBillStatusCatalog();

export function isStandardBillEditable(status: string | undefined): boolean {
  return status === StandardBillStatus.SAVED;
}
