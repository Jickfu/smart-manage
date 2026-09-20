import { createRequire } from 'node:module';
import { dirname, join } from 'node:path';
import { readdir, readFile, writeFile } from 'node:fs/promises';

const require = createRequire(import.meta.url);
const ICON_NAME_PATTERN = /(?:Outlined|Filled|TwoTone)\.js$/;
const PRIMARY_COLOR_SLOT = '__SM_PRIMARY_COLOR__';
const SECONDARY_COLOR_SLOT = '__SM_SECONDARY_COLOR__';
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

function sanitizeNode(node, iconName) {
  if (
    node.tag === 'style' &&
    Object.keys(node.attrs ?? {}).length === 0 &&
    !node.children?.length
  ) {
    return null;
  }
  if (!ALLOWED_TAGS.has(node.tag)) {
    throw new Error(`${iconName} 包含未支持的 SVG 标签：${node.tag}`);
  }
  const children = (node.children ?? [])
    .map((child) => sanitizeNode(child, iconName))
    .filter(Boolean);
  return {
    tag: node.tag,
    ...(Object.keys(node.attrs ?? {}).length ? { attrs: node.attrs } : {}),
    ...(children.length ? { children } : {}),
  };
}

const iconsPackagePath = require.resolve('@ant-design/icons/package.json');
const iconsPackageDirectory = dirname(iconsPackagePath);
const iconsSvgPackagePath = require.resolve('@ant-design/icons-svg/package.json', {
  paths: [iconsPackageDirectory],
});
const iconsSvgPackageDirectory = dirname(iconsSvgPackagePath);
const asnDirectory = join(iconsSvgPackageDirectory, 'lib', 'asn');
const [iconsPackage, iconsSvgPackage, asnFiles] = await Promise.all([
  readFile(iconsPackagePath, 'utf8').then(JSON.parse),
  readFile(iconsSvgPackagePath, 'utf8').then(JSON.parse),
  readdir(asnDirectory),
]);

const icons = {};
for (const fileName of asnFiles.filter((name) => ICON_NAME_PATTERN.test(name)).sort()) {
  const iconName = fileName.slice(0, -3);
  const definition = require(join(asnDirectory, fileName)).default;
  const sourceNode =
    typeof definition.icon === 'function'
      ? definition.icon(PRIMARY_COLOR_SLOT, SECONDARY_COLOR_SLOT)
      : definition.icon;
  icons[iconName] = {
    name: definition.name,
    theme: definition.theme,
    icon: sanitizeNode(sourceNode, iconName),
  };
}

const catalog = {
  protocolVersion: 1,
  source: { icons: iconsPackage.version, iconsSvg: iconsSvgPackage.version },
  colorSlots: { primary: PRIMARY_COLOR_SLOT, secondary: SECONDARY_COLOR_SLOT },
  icons,
};
const targetUrl = new URL(
  '../src/domain/common/component/iconCatalog.generated.json',
  import.meta.url,
);
await writeFile(targetUrl, `${JSON.stringify(catalog, null, 2)}\n`, 'utf8');
console.log(`已生成 ${Object.keys(icons).length} 个 Ant Design 图标：${targetUrl.pathname}`);
