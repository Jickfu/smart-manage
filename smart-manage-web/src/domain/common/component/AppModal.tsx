import { useEffect, useRef } from 'react';
import type { PointerEvent as ReactPointerEvent, ReactNode } from 'react';
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
  /** 是否允许通过标题栏拖动弹框，默认关闭。 */
  draggable?: boolean;
  /** 是否允许从弹框右下角调整大小，默认关闭。 */
  resizable?: boolean;
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
  draggable = false,
  resizable = false,
  destroyOnHidden = true,
  keyboard = true,
  ...modalProps
}: AppModalProps) => {
  const interactionRef = useRef<HTMLDivElement>(null);
  const dragStateRef = useRef({ pointerX: 0, pointerY: 0, offsetX: 0, offsetY: 0 });
  const modalClassName = [
    'sm-app-modal',
    `sm-app-modal--body-${bodyMode}`,
    draggable && 'sm-app-modal--draggable',
    resizable && 'sm-app-modal--resizable',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  useEffect(() => {
    if (!modalProps.open) return;
    dragStateRef.current = { pointerX: 0, pointerY: 0, offsetX: 0, offsetY: 0 };
    interactionRef.current?.style.removeProperty('--sm-app-modal-translate-x');
    interactionRef.current?.style.removeProperty('--sm-app-modal-translate-y');
  }, [modalProps.open]);

  const handleDragStart = (event: ReactPointerEvent<HTMLDivElement>) => {
    if (
      !draggable ||
      event.button !== 0 ||
      (event.target as HTMLElement).closest('button, input')
    ) {
      return;
    }
    dragStateRef.current.pointerX = event.clientX;
    dragStateRef.current.pointerY = event.clientY;
    event.currentTarget.setPointerCapture(event.pointerId);
  };

  const handleDragMove = (event: ReactPointerEvent<HTMLDivElement>) => {
    if (!draggable || !event.currentTarget.hasPointerCapture(event.pointerId)) return;

    const nextOffsetX =
      dragStateRef.current.offsetX + event.clientX - dragStateRef.current.pointerX;
    const nextOffsetY =
      dragStateRef.current.offsetY + event.clientY - dragStateRef.current.pointerY;
    interactionRef.current?.style.setProperty('--sm-app-modal-translate-x', `${nextOffsetX}px`);
    interactionRef.current?.style.setProperty('--sm-app-modal-translate-y', `${nextOffsetY}px`);
  };

  const handleDragEnd = (event: ReactPointerEvent<HTMLDivElement>) => {
    if (!draggable || !event.currentTarget.hasPointerCapture(event.pointerId)) return;

    dragStateRef.current.offsetX += event.clientX - dragStateRef.current.pointerX;
    dragStateRef.current.offsetY += event.clientY - dragStateRef.current.pointerY;
    event.currentTarget.releasePointerCapture(event.pointerId);
  };

  return (
    <Modal
      {...modalProps}
      title={
        <div
          className="sm-app-modal-header-content"
          onPointerDown={handleDragStart}
          onPointerMove={handleDragMove}
          onPointerUp={handleDragEnd}
          onPointerCancel={handleDragEnd}
        >
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
      modalRender={
        draggable
          ? (node) => (
              <div ref={interactionRef} className="sm-app-modal-interaction">
                {node}
              </div>
            )
          : undefined
      }
      footer={footer == null ? null : <div className="sm-app-modal-footer-content">{footer}</div>}
    >
      {children}
    </Modal>
  );
};

export default AppModal;
export type { AppModalBodyMode, AppModalProps };
