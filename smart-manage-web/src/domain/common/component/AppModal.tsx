import type { ReactNode } from 'react';
import { Modal } from 'antd';
import type { ModalProps } from 'antd';
import './AppModal.css';

type AppModalBodyMode = 'scroll' | 'fixed' | 'natural';

interface AppModalProps extends Omit<
  ModalProps,
  'centered' | 'children' | 'className' | 'closeIcon' | 'footer' | 'mask' | 'onCancel' | 'title'
> {
  title: ReactNode;
  headerExtra?: ReactNode;
  children: ReactNode;
  footer?: ReactNode;
  onCancel: () => void;
  className?: string;
  bodyMode?: AppModalBodyMode;
  closeDisabled?: boolean;
}

/**
 * 业务弹框通用壳层：统一标题、扩展操作区、原生关闭按钮、内容区滚动和底部操作区。
 * 业务状态与操作仍由调用方管理，避免公共壳层依赖具体领域逻辑。
 */
const AppModal = ({
  title,
  headerExtra,
  children,
  footer,
  onCancel,
  className,
  bodyMode = 'scroll',
  closeDisabled = false,
  destroyOnHidden = true,
  keyboard = true,
  ...modalProps
}: AppModalProps) => {
  const modalClassName = ['sm-app-modal', `sm-app-modal--body-${bodyMode}`, className]
    .filter(Boolean)
    .join(' ');

  return (
    <Modal
      {...modalProps}
      title={
        <div className="sm-app-modal-header-content">
          <span className="sm-app-modal-title">{title}</span>
          {headerExtra && <div className="sm-app-modal-header-extra">{headerExtra}</div>}
        </div>
      }
      open={modalProps.open}
      onCancel={onCancel}
      centered
      mask={{ closable: false }}
      className={modalClassName}
      closable={{ disabled: closeDisabled }}
      keyboard={closeDisabled ? false : keyboard}
      destroyOnHidden={destroyOnHidden}
      footer={footer == null ? null : <div className="sm-app-modal-footer-content">{footer}</div>}
    >
      {children}
    </Modal>
  );
};

export default AppModal;
export type { AppModalBodyMode, AppModalProps };
