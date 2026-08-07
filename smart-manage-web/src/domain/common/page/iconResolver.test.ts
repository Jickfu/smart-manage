import { describe, expect, it } from 'vitest';
import { isSelectableIconName, resolveIcon } from './iconResolver';

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
});
