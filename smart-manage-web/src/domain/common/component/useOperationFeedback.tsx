import { useCallback, useMemo, type ReactNode } from 'react';
import { Alert, App } from 'antd';
import { getErrorPresentation } from '@/api/errorPresentation';
import {
  getOperationFeedbackClassName,
  type OperationFeedbackType,
} from './operationFeedbackPolicy';
import './OperationFeedback.css';

let feedbackSequence = 0;

export interface OperationFeedbackApi {
  success: (content: ReactNode, options?: OperationFeedbackOptions) => void;
  warning: (content: ReactNode, options?: OperationFeedbackOptions) => void;
  error: (content: ReactNode, options?: OperationFeedbackOptions) => void;
  info: (content: ReactNode, options?: OperationFeedbackOptions) => void;
  fromError: (error: unknown, fallbackMessage?: string, options?: OperationFeedbackOptions) => void;
}

export interface OperationFeedbackOptions {
  /** 默认自动关闭；设为 false 时常驻并显示右侧关闭按钮。 */
  autoClose?: boolean;
  /** 自动关闭秒数，默认沿用 Ant Design Message 的 3 秒。 */
  duration?: number;
  /** 自动关闭时也可按需显示关闭按钮。 */
  closable?: boolean;
}

/** 统一展示操作结果，并允许调用方仅在确有需要时选择常驻提示。 */
export function useOperationFeedback(): OperationFeedbackApi {
  const { message } = App.useApp();

  const open = useCallback(
    (type: OperationFeedbackType, content: ReactNode, options: OperationFeedbackOptions = {}) => {
      const key = `sm-operation-feedback-${++feedbackSequence}`;
      const autoClose = options.autoClose ?? true;
      const closable = options.closable ?? !autoClose;
      message.open({
        key,
        duration: autoClose ? (options.duration ?? 3) : 0,
        pauseOnHover: true,
        className: 'sm-operation-feedback-notice',
        classNames: { root: 'sm-operation-feedback-message-root' },
        content: (
          <Alert
            className={getOperationFeedbackClassName(type)}
            classNames={{
              section: 'sm-operation-feedback-section',
              title: 'sm-operation-feedback-title',
              close: 'sm-operation-feedback-close',
            }}
            type={type}
            variant="outlined"
            showIcon
            closable={
              closable
                ? {
                    'aria-label': '关闭提示',
                    onClose: () => message.destroy(key),
                  }
                : false
            }
            title={content}
          />
        ),
      });
    },
    [message],
  );

  return useMemo(
    () => ({
      success: (content: ReactNode, options?: OperationFeedbackOptions) =>
        open('success', content, options),
      warning: (content: ReactNode, options?: OperationFeedbackOptions) =>
        open('warning', content, options),
      error: (content: ReactNode, options?: OperationFeedbackOptions) =>
        open('error', content, options),
      info: (content: ReactNode, options?: OperationFeedbackOptions) =>
        open('info', content, options),
      fromError: (
        error: unknown,
        fallbackMessage = '操作失败',
        options?: OperationFeedbackOptions,
      ) => {
        const presentation = getErrorPresentation(error, fallbackMessage);
        if (!presentation.suppressed) open(presentation.type, presentation.message, options);
      },
    }),
    [open],
  );
}
