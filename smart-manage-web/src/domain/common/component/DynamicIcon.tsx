import { createElement, useId, type ReactElement, type ReactNode } from 'react';
import { generate } from '@ant-design/colors';
import QuestionCircleOutlined from '@ant-design/icons/QuestionCircleOutlined';
import { getIconColorSlots, getIconDefinition, type IconNode } from './iconCatalog';

type TwoToneColor = string | readonly [string, string];

interface DynamicIconProps {
  name: string;
  fallback?: ReactNode;
  twoToneColor?: TwoToneColor;
  title?: string;
}

const DEFAULT_PRIMARY_COLOR = '#333';
const DEFAULT_SECONDARY_COLOR = '#E6E6E6';

function normalizeAttributeName(name: string): string {
  if (name === 'class') return 'className';
  return name.replace(/-([a-z])/g, (_, character: string) => character.toUpperCase());
}

function resolveColors(twoToneColor?: TwoToneColor): readonly [string, string] {
  if (!twoToneColor) return [DEFAULT_PRIMARY_COLOR, DEFAULT_SECONDARY_COLOR];
  if (typeof twoToneColor !== 'string') return twoToneColor;
  return [twoToneColor, generate(twoToneColor)[0] ?? DEFAULT_SECONDARY_COLOR];
}

function renderNode(
  node: IconNode,
  key: string,
  idPrefix: string,
  primaryColor: string,
  secondaryColor: string,
): ReactElement {
  const colorSlots = getIconColorSlots();
  const properties = Object.fromEntries(
    Object.entries(node.attrs ?? {}).map(([name, originalValue]) => {
      let value = originalValue;
      if (value === colorSlots.primary) value = primaryColor;
      if (value === colorSlots.secondary) value = secondaryColor;
      if (name === 'id') value = `${idPrefix}-${value}`;
      value = value.replaceAll(/url\(#([^)]+)\)/g, `url(#${idPrefix}-$1)`);
      return [normalizeAttributeName(name), value];
    }),
  );
  return createElement(
    node.tag,
    { key, ...properties },
    node.children?.map((child, index) =>
      renderNode(child, `${key}-${index}`, idPrefix, primaryColor, secondaryColor),
    ),
  );
}

/** 配置图标只渲染当前实例，不创建逐图标模块、请求或订阅生命周期。 */
export default function DynamicIcon({ name, fallback, twoToneColor, title }: DynamicIconProps) {
  const definition = getIconDefinition(name);
  const reactId = useId().replaceAll(':', '');
  if (!definition) return fallback ?? <QuestionCircleOutlined title="未知图标" />;
  const [primaryColor, secondaryColor] = resolveColors(twoToneColor);
  const root = definition.icon;
  const accessibleProperties = title
    ? { role: 'img', 'aria-label': title }
    : { 'aria-hidden': true as const };
  return (
    <span
      className={`anticon anticon-${definition.name} sm-dynamic-icon`}
      {...accessibleProperties}
    >
      {createElement(
        'svg',
        {
          ...Object.fromEntries(
            Object.entries(root.attrs ?? {}).map(([attributeName, value]) => [
              normalizeAttributeName(attributeName),
              value,
            ]),
          ),
          width: '1em',
          height: '1em',
          fill: 'currentColor',
          focusable: 'false',
          'aria-hidden': 'true',
          'data-icon': definition.name,
        },
        root.children?.map((child, index) =>
          renderNode(child, `${name}-${index}`, `sm-icon-${reactId}`, primaryColor, secondaryColor),
        ),
      )}
    </span>
  );
}
