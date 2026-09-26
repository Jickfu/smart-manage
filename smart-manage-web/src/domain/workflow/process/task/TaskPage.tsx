import type { PageComponentProps } from '@/domain/common/page/types';
import TaskCenter from './TaskCenter';
export default function TaskPage(props: PageComponentProps) {
  return (
    <TaskCenter
      active={props.active}
      context={{ ...props.context, appNumber: props.appNumber, tabKey: props.tabKey }}
    />
  );
}
