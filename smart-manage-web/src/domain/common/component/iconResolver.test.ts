import { readdirSync } from 'node:fs';
import { describe, expect, it } from 'vitest';
import { isSelectableIconName, selectableIconNames, resolveIcon } from './iconResolver';

describe('isSelectableIconName', () => {
  it('只接受 Ant Design 图标组件命名', () => {
    expect(isSelectableIconName('HomeOutlined')).toBe(true);
    expect(isSelectableIconName('HomeFilled')).toBe(true);
    expect(isSelectableIconName('HomeTwoTone')).toBe(true);
    expect(isSelectableIconName('createFromIconfontCN')).toBe(false);
    expect(isSelectableIconName('IconProvider')).toBe(false);
  });
});

describe('resolveIcon', () => {
  it('图标名称为空时返回调用方提供的默认图标', () => {
    const fallback = 'fallback';

    expect(resolveIcon(undefined, fallback)).toBe(fallback);
  });

  it('未知图标名称不会触发全量动态图标加载', () => {
    const fallback = 'fallback';

    expect(resolveIcon('UnknownOutlined', fallback)).toBe(fallback);
  });

  it('完整候选目录覆盖数据库当前使用的图标', () => {
    const icons = selectableIconNames;
    const persistedIconNames = [
      'ApartmentOutlined',
      'AppstoreOutlined',
      'ClockCircleOutlined',
      'ClusterOutlined',
      'CodeOutlined',
      'ConsoleSqlOutlined',
      'DashboardOutlined',
      'DatabaseOutlined',
      'FileAddOutlined',
      'FileOutlined',
      'FileTextOutlined',
      'HistoryOutlined',
      'IdcardOutlined',
      'LinkOutlined',
      'MenuOutlined',
      'PaperClipOutlined',
      'SearchOutlined',
      'SettingOutlined',
      'ShoppingCartOutlined',
      'ShoppingOutlined',
      'SyncOutlined',
      'ToolOutlined',
      'UserOutlined',
    ];

    expect(persistedIconNames.every((name) => icons.includes(name))).toBe(true);
  });

  it('完整候选目录覆盖三种图标风格', () => {
    const iconNames = selectableIconNames;

    expect(iconNames.some((name) => name.endsWith('Outlined'))).toBe(true);
    expect(iconNames.some((name) => name.endsWith('Filled'))).toBe(true);
    expect(iconNames.some((name) => name.endsWith('TwoTone'))).toBe(true);
  });
});

it('候选目录与安装包全部图标一致，不暴露包工具函数', () => {
  const installedNames = readdirSync(
    new URL('../../../../node_modules/@ant-design/icons/es/icons/', import.meta.url),
  )
    .filter((name) => /(?:Outlined|Filled|TwoTone)\.js$/.test(name))
    .map((name) => name.slice(0, -3))
    .sort();
  expect(installedNames.length).toBeGreaterThan(800);
  expect(selectableIconNames).toEqual(installedNames);
  expect(isSelectableIconName('UnknownOutlined')).toBe(false);
});
