import { useState } from 'react';
import type { ReactNode } from 'react';
import { Alert } from 'antd';
import './PageNotice.css';

export interface PageNoticeConfig {
  /** 同一提示保持稳定；业务对象或提示语义变化时更换，重新展示提示。 */
  key: string;
  title: ReactNode;
  description?: ReactNode;
  type?: 'info' | 'warning' | 'error' | 'success';
  /** 仅可忽略的辅助说明允许关闭，默认不允许。 */
  closable?: boolean;
  onClose?: () => void;
}

function NoticeContent({ notice }: { notice: PageNoticeConfig }) {
  const [closed, setClosed] = useState(false);
  if (closed) return null;
  return (
    <Alert
      className="sm-page-notice"
      classNames={{ section: 'sm-page-notice-content' }}
      banner
      showIcon
      type={notice.type ?? 'info'}
      title={notice.title}
      description={notice.description}
      closable={
        notice.closable
          ? {
              'aria-label': '关闭提示',
              onClose: () => {
                setClosed(true);
                notice.onClose?.();
              },
            }
          : false
      }
    />
  );
}

/** 提示只占所属区域的独立一行；关闭状态限于本次挂载，不写入用户偏好。 */
export function PageNotice({ notice }: { notice?: PageNoticeConfig }) {
  return notice ? <NoticeContent key={notice.key} notice={notice} /> : null;
}
