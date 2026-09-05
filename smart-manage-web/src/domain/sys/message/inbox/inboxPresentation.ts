import dayjs from 'dayjs';
import type { InboxLevel, InboxReceiptKey } from './types';

export const inboxLevelLabels: Record<InboxLevel, string> = {
  NORMAL: '普通',
  IMPORTANT: '重要',
  URGENT: '紧急',
};

/** 时间只在展示层格式化，微秒收件键必须原样提交。 */
export const formatInboxTime = (value: string) =>
  dayjs(value.slice(0, 19)).format('YYYY-MM-DD HH:mm');
export const inboxReceiptId = (receipt: InboxReceiptKey) =>
  `${receipt.receivedTime}:${receipt.messageId}`;

/** 禁止定时器溢出或无效配置形成高频请求；0只停止定时轮询。 */
export function inboxPollingInterval(seconds?: number): number | false {
  return Number.isInteger(seconds) && seconds! >= 10 && seconds! <= 2147483
    ? seconds! * 1000
    : false;
}
