import { describe, expect, it } from 'vitest';
import type { ActiveUiConfig } from '@/api/uiConfig';
import type { UserInfoVO } from '@/types/api';
import {
  buildWatermarkContent,
  estimateWatermarkWidth,
  maskWatermarkEmail,
  maskWatermarkPhone,
} from './watermarkContent';

const user = {
  name: '张三',
  phone: '13812345678',
  email: 'zhangsan@example.com',
  number: 'E001',
  rootOrgName: '集团总部',
} as UserInfoVO;

describe('登录后水印内容', () => {
  it('组合固定内容、所选用户字段并脱敏联系方式', () => {
    const config: ActiveUiConfig = {
      watermarkEnabled: true,
      watermarkContent: '内部资料',
      watermarkShowName: true,
      watermarkShowPhone: true,
      watermarkShowEmail: true,
      watermarkShowNumber: true,
      watermarkShowRootOrg: true,
    };

    expect(buildWatermarkContent(config, user)).toEqual([
      '内部资料',
      '张三',
      '138****5678',
      'zh***@example.com',
      'E001',
      '集团总部',
    ]);
  });

  it('关闭配置或没有有效内容时不生成水印', () => {
    expect(buildWatermarkContent({ watermarkEnabled: false }, user)).toEqual([]);
    expect(buildWatermarkContent({ watermarkEnabled: true }, user)).toEqual([]);
  });

  it('联系方式脱敏不返回原值', () => {
    expect(maskWatermarkPhone('12345')).toBe('1***5');
    expect(maskWatermarkEmail('a@example.com')).toBe('a***@example.com');
    expect(maskWatermarkEmail('invalid')).toBe('***');
  });

  it('为长水印内容扩展画布且保持安全上限', () => {
    expect(estimateWatermarkWidth(['短文本'], 16)).toBe(120);
    expect(estimateWatermarkWidth(['很长的水印内容'.repeat(20)], 16)).toBe(720);
  });
});
