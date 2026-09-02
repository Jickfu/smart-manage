import { describe, expect, it } from 'vitest';
import { createThemeConfig } from './theme';

describe('createThemeConfig', () => {
  it('使用紧凑正文和 14px 核心输入值字号', () => {
    const theme = createThemeConfig();

    expect(theme.token).toMatchObject({
      fontSize: 12,
      fontSizeSM: 12,
      fontSizeLG: 14,
    });
    expect(theme.components?.Button).toMatchObject({
      contentFontSize: 12,
      contentFontSizeSM: 12,
      contentFontSizeLG: 14,
    });
    expect(theme.components?.Input).toMatchObject({
      inputFontSize: 14,
      inputFontSizeSM: 12,
      inputFontSizeLG: 14,
    });
    expect(theme.components?.InputNumber).toMatchObject({
      inputFontSize: 14,
      inputFontSizeSM: 12,
      inputFontSizeLG: 14,
    });
    expect(theme.components?.Select).toMatchObject({ fontSize: 14, optionFontSize: 12 });
    expect(theme.components?.TreeSelect).toMatchObject({ fontSize: 14 });
    expect(theme.components?.Cascader).toMatchObject({ fontSize: 14 });
    expect(theme.components?.DatePicker).toMatchObject({ fontSize: 14 });
    expect(theme.components?.Mentions).toMatchObject({ fontSize: 14 });
    expect(theme.components?.ColorPicker).toMatchObject({ fontSize: 14 });
    expect(theme.components?.Tabs).toMatchObject({
      titleFontSize: 12,
      titleFontSizeSM: 12,
      titleFontSizeLG: 14,
    });
  });

  it('保留页面结构与弹窗标题的视觉层级', () => {
    const theme = createThemeConfig();

    expect(theme.components?.Card).toMatchObject({ headerFontSize: 14 });
    expect(theme.components?.Modal).toMatchObject({ titleFontSize: 16 });
  });
});
