import dayjs from 'dayjs';
import customParseFormat from 'dayjs/plugin/customParseFormat';
import utc from 'dayjs/plugin/utc';

dayjs.extend(customParseFormat);
dayjs.extend(utc);

export const DATE_FORMAT = 'YYYY-MM-DD';
export const DATE_TIME_FORMAT = 'YYYY-MM-DD HH:mm:ss';

const beijingClock = new Intl.DateTimeFormat('en-GB', {
  timeZone: 'Asia/Shanghai',
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
  hourCycle: 'h23',
});

// UTC 模式只承载无偏移的年月日和时分秒，避免浏览器时区及夏令时改变输入。
export function parseBeijingTime(value: string, format = DATE_TIME_FORMAT) {
  return dayjs.utc(value, format, true);
}

export function beijingNow() {
  const parts = Object.fromEntries(
    beijingClock.formatToParts(new Date()).map(({ type, value }) => [type, value]),
  );
  return parseBeijingTime(
    `${parts.year}-${parts.month}-${parts.day} ${parts.hour}:${parts.minute}:${parts.second}`,
  );
}
