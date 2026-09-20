import { beforeEach, expect, it } from 'vitest';
import generatedCatalog from './iconCatalog.generated.json';
import {
  getIconCatalogMetadata,
  getIconDefinition,
  initializeIconCatalog,
  resetIconCatalogForTests,
  selectableIconNames,
} from './iconCatalog';

beforeEach(() => resetIconCatalogForTests());

it('一次初始化完整名称索引，不创建逐图标加载状态', async () => {
  await initializeIconCatalog(generatedCatalog);

  expect(selectableIconNames).toHaveLength(831);
  expect(selectableIconNames).toContain('HomeOutlined');
  expect(selectableIconNames).toContain('RocketTwoTone');
  expect(getIconDefinition('RocketTwoTone')?.theme).toBe('twotone');
  expect(getIconCatalogMetadata()).toEqual({ icons: '6.2.5', iconsSvg: '4.4.2' });
});

it('拒绝未知 SVG 标签和外部引用', async () => {
  const invalidTag = structuredClone(generatedCatalog);
  const invalidTagNode = invalidTag.icons.HomeOutlined?.icon.children?.[0];
  if (!invalidTagNode) throw new Error('测试图标缺少图形节点');
  invalidTagNode.tag = 'script';
  expect(() => initializeIconCatalog(invalidTag)).toThrow('未支持的图形节点');

  resetIconCatalogForTests();
  const externalReference = structuredClone(generatedCatalog);
  const externalReferenceNode = externalReference.icons.HomeOutlined?.icon.children?.[0];
  if (!externalReferenceNode) throw new Error('测试图标缺少图形节点');
  const externalReferenceAttributes = externalReferenceNode.attrs as Record<string, string>;
  externalReferenceAttributes.fill = 'https://example.com/a.svg';
  expect(() => initializeIconCatalog(externalReference)).toThrow('外部图形引用');
});
