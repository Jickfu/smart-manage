import iconCatalogUrl from './iconCatalog.generated.json?url';

export type IconTheme = 'outlined' | 'filled' | 'twotone';

export interface IconNode {
  tag: string;
  attrs?: Readonly<Record<string, string>>;
  children?: readonly IconNode[];
}

export interface IconDefinition {
  name: string;
  theme: IconTheme;
  icon: IconNode;
}

interface IconCatalogResource {
  protocolVersion: number;
  source: { icons: string; iconsSvg: string };
  colorSlots: { primary: string; secondary: string };
  icons: Record<string, IconDefinition>;
}

const ICON_NAME_PATTERN = /^[A-Z][A-Za-z0-9]*(?:Outlined|Filled|TwoTone)$/;
const ALLOWED_TAGS = new Set([
  'svg',
  'path',
  'defs',
  'g',
  'filter',
  'feOffset',
  'feGaussianBlur',
  'feColorMatrix',
  'feMerge',
  'feMergeNode',
]);
const ALLOWED_ATTRIBUTES = new Set([
  'viewBox',
  'focusable',
  'd',
  'fill',
  'fill-rule',
  'fill-opacity',
  'filterUnits',
  'height',
  'id',
  'width',
  'x',
  'y',
  'dy',
  'in',
  'result',
  'stdDeviation',
  'values',
  'filter',
  'transform',
]);

let catalog: IconCatalogResource | undefined;
let initialization: Promise<void> | undefined;
export const selectableIconNames: string[] = [];

function assertIconNode(value: unknown, iconName: string): asserts value is IconNode {
  if (!value || typeof value !== 'object') throw new Error(`${iconName} 的图形节点无效`);
  const node = value as Partial<IconNode>;
  if (typeof node.tag !== 'string' || !ALLOWED_TAGS.has(node.tag)) {
    throw new Error(`${iconName} 包含未支持的图形节点`);
  }
  if (node.attrs !== undefined) {
    if (!node.attrs || typeof node.attrs !== 'object' || Array.isArray(node.attrs)) {
      throw new Error(`${iconName} 的图形属性无效`);
    }
    for (const [name, attributeValue] of Object.entries(node.attrs)) {
      if (!ALLOWED_ATTRIBUTES.has(name) || typeof attributeValue !== 'string') {
        throw new Error(`${iconName} 包含未支持的图形属性`);
      }
      if (/^(?:javascript:|data:|https?:)/i.test(attributeValue.trim())) {
        throw new Error(`${iconName} 包含外部图形引用`);
      }
    }
  }
  if (node.children !== undefined) {
    if (!Array.isArray(node.children)) throw new Error(`${iconName} 的子节点无效`);
    node.children.forEach((child) => assertIconNode(child, iconName));
  }
}

function parseCatalog(value: unknown): IconCatalogResource {
  if (!value || typeof value !== 'object') throw new Error('图标资源不是有效对象');
  const resource = value as Partial<IconCatalogResource>;
  if (
    resource.protocolVersion !== 1 ||
    !resource.source ||
    typeof resource.source.icons !== 'string' ||
    typeof resource.source.iconsSvg !== 'string' ||
    !resource.colorSlots ||
    typeof resource.colorSlots.primary !== 'string' ||
    typeof resource.colorSlots.secondary !== 'string' ||
    !resource.icons ||
    typeof resource.icons !== 'object' ||
    Array.isArray(resource.icons)
  ) {
    throw new Error('图标资源协议不兼容');
  }
  for (const [iconName, definition] of Object.entries(resource.icons)) {
    if (!ICON_NAME_PATTERN.test(iconName) || !definition || typeof definition !== 'object') {
      throw new Error('图标名称或定义无效');
    }
    if (
      typeof definition.name !== 'string' ||
      !['outlined', 'filled', 'twotone'].includes(definition.theme)
    ) {
      throw new Error(`${iconName} 的元数据无效`);
    }
    assertIconNode(definition.icon, iconName);
  }
  return resource as IconCatalogResource;
}

/** 应用正常展示前一次性读取并校验完整图标数据；失败可由调用方重新触发。 */
export function initializeIconCatalog(resource?: unknown): Promise<void> {
  if (catalog) return Promise.resolve();
  if (resource !== undefined) {
    catalog = parseCatalog(resource);
    selectableIconNames.splice(0, selectableIconNames.length, ...Object.keys(catalog.icons).sort());
    return Promise.resolve();
  }
  if (initialization) return initialization;
  initialization = fetch(iconCatalogUrl, { credentials: 'same-origin' })
    .then(async (response) => {
      if (!response.ok) throw new Error(`图标资源请求失败（HTTP ${response.status}）`);
      catalog = parseCatalog(await response.json());
      selectableIconNames.splice(
        0,
        selectableIconNames.length,
        ...Object.keys(catalog.icons).sort(),
      );
    })
    .catch((error) => {
      initialization = undefined;
      throw error;
    });
  return initialization;
}

function requireCatalog(): IconCatalogResource {
  if (!catalog) throw new Error('图标资源尚未完成初始化');
  return catalog;
}

export function getIconDefinition(name: string): IconDefinition | undefined {
  return requireCatalog().icons[name];
}

export function getIconColorSlots(): Readonly<{ primary: string; secondary: string }> {
  return requireCatalog().colorSlots;
}

export function isSelectableIconName(name: string): boolean {
  return Object.hasOwn(requireCatalog().icons, name);
}

export function getIconCatalogMetadata(): Readonly<IconCatalogResource['source']> {
  return requireCatalog().source;
}

export function resetIconCatalogForTests(): void {
  catalog = undefined;
  initialization = undefined;
  selectableIconNames.splice(0);
}
