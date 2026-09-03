import axios from 'axios';
import { describe, expect, it } from 'vitest';
import { ApiError } from './ApiError';
import { getErrorPresentation } from './errorPresentation';

describe('error presentation policy', () => {
  it('uses the backend hint rather than a frontend business-code map', () => {
    expect(
      getErrorPresentation(
        new ApiError({
          source: 'API',
          apiCode: 987654,
          message: '可修正',
          feedbackLevel: 'WARNING',
          httpStatus: 200,
        }),
      ).type,
    ).toBe('warning');
    expect(
      getErrorPresentation(new ApiError({ source: 'API', apiCode: 100422, message: '异常' })).type,
    ).toBe('error');
  });
  it.each([500, 502, 401, 403])('cannot downgrade HTTP %i through an API hint', (httpStatus) => {
    expect(
      getErrorPresentation(
        new ApiError({
          source: 'API',
          apiCode: 100409,
          message: '冲突',
          feedbackLevel: 'WARNING',
          httpStatus,
        }),
      ).type,
    ).toBe('error');
  });
  it.each([100401, 100419])('suppresses security code %i owned elsewhere', (apiCode) => {
    expect(
      getErrorPresentation(new ApiError({ source: 'API', apiCode, message: '安全错误' }))
        .suppressed,
    ).toBe(true);
  });
  it('suppresses cancellation and avoids exposing client exception details', () => {
    expect(getErrorPresentation(new axios.CanceledError()).source).toBe('CANCELED');
    expect(getErrorPresentation(new axios.CanceledError()).suppressed).toBe(true);
    expect(getErrorPresentation(new Error('private detail'), '保存数据组装失败')).toMatchObject({
      source: 'CLIENT',
      message: '保存数据组装失败',
      type: 'error',
    });
  });
  it('only exposes a safe trace ID, never additional response data', () => {
    const result = getErrorPresentation(
      new ApiError({ source: 'API', message: '失败', traceId: '<private>', data: 'private body' }),
    );
    expect(result.traceId).toBe('');
    expect(result).not.toHaveProperty('data');
  });
});
