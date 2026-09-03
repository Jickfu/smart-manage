import type { ReactNode } from 'react';
import { EditFormFields } from './EditFormFields';
import type { EditField } from './EditPage';

export interface EditPageSection {
  key: string;
  label: ReactNode;
  content: (editable: boolean) => ReactNode;
  extra?: (editable: boolean) => ReactNode;
}

/** 标准字段卡片构造器；卡片身份和顺序仍由领域页面显式决定。 */
export const editFormSection = (
  key: string,
  label: ReactNode,
  fields: EditField[],
): EditPageSection => ({
  key,
  label,
  content: (editable) => <EditFormFields fields={fields} editable={editable} />,
});
