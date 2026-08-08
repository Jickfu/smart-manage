import { describe, expect, it } from 'vitest';
import { resolveAssetUrl } from './assetUrl';

describe('resolveAssetUrl', () => {
  it('为后端本地上传路径补齐应用上下文', () => {
    expect(resolveAssetUrl('/upload/asset/logo.png')).toBe(
      '/smart-manage-api/upload/asset/logo.png',
    );
    expect(resolveAssetUrl('upload/asset/logo.png')).toBe(
      '/smart-manage-api/upload/asset/logo.png',
    );
  });

  it('保留外部地址和已完整的应用地址', () => {
    expect(resolveAssetUrl('https://cdn.example.com/logo.png')).toBe(
      'https://cdn.example.com/logo.png',
    );
    expect(resolveAssetUrl('/smart-manage-api/upload/logo.png')).toBe(
      '/smart-manage-api/upload/logo.png',
    );
  });

  it('为界面配置公开图片接口补齐应用上下文', () => {
    expect(resolveAssetUrl('/sys/base/ui-config/image/header-logo?v=123')).toBe(
      '/smart-manage-api/sys/base/ui-config/image/header-logo?v=123',
    );
  });

  it('未配置时使用默认资源', () => {
    expect(resolveAssetUrl(undefined, '/logo.svg')).toBe('/logo.svg');
  });
});
