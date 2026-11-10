import { DatePicker } from 'antd';
import dayjsGenerateConfig from '@rc-component/picker/generate/dayjs';
import dayjs, { type Dayjs } from 'dayjs';
import { beijingNow, parseBeijingTime } from './beijingTime';

// 复用 Ant Design 的日期面板和日历算法，只替换当前时间及文本解析。
export const BeijingDatePicker = DatePicker.generatePicker<Dayjs>({
  ...dayjsGenerateConfig,
  getNow: beijingNow,
  getFixedDate: (value) => dayjs.utc(value),
  locale: {
    ...dayjsGenerateConfig.locale,
    parse: (_locale, value, formats) => {
      for (const format of formats) {
        const parsed = parseBeijingTime(value, format);
        if (parsed.isValid()) return parsed;
      }
      return null;
    },
  },
});
