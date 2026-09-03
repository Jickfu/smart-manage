import { Button } from 'antd';
import type { ButtonProps } from 'antd';

export type EditSectionActionButtonProps = Omit<ButtonProps, 'type'>;

/** 编辑页正文卡片标题栏操作，统一使用轻量的链接按钮样式。 */
export function EditSectionActionButton(props: EditSectionActionButtonProps) {
  return <Button {...props} type="link" />;
}
