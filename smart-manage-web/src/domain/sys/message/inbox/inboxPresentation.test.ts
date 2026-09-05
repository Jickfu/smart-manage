import { describe, expect, it } from 'vitest';
import { inboxPollingInterval, inboxReceiptId, formatInboxTime } from './inboxPresentation';

describe('个人消息查询边界', () => {
  it('0及无效配置不创建定时器，正常秒数转换为毫秒', () => {
    for (const seconds of [undefined, 0, -1, 1, 9, 10.5, NaN, Infinity, 2147484])
      expect(inboxPollingInterval(seconds)).toBe(false);
    expect(inboxPollingInterval(10)).toBe(10000);
    expect(inboxPollingInterval(60)).toBe(60000);
    expect(inboxPollingInterval(2147483)).toBe(2147483000);
  });
  it('展示格式化不改变微秒收件键', () => {
    const receipt = { messageId: '9007199254740993', receivedTime: '2026-09-01 12:00:00.123456' };
    expect(formatInboxTime(receipt.receivedTime)).toBe('2026-09-01 12:00');
    expect(inboxReceiptId(receipt)).toBe('2026-09-01 12:00:00.123456:9007199254740993');
  });
});
