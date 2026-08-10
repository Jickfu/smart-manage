import { describe, expect, it } from 'vitest';
import { isSelectableIconName, loadAllIcons, resolveIcon } from './iconResolver';

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

  it('候选白名单覆盖数据库当前使用的图标', async () => {
    const icons = await loadAllIcons();
    const persistedIconNames = [
      'ApartmentOutlined',
      'AppstoreOutlined',
      'ClockCircleOutlined',
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

    expect(persistedIconNames.every((name) => icons[name])).toBe(true);
  });
});
