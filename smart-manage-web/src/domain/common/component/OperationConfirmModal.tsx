import { Button } from 'antd';
import deleteIllustration from '@/assets/operation-confirm/delete.png';
import destructiveIllustration from '@/assets/operation-confirm/destructive.png';
import normalIllustration from '@/assets/operation-confirm/normal.png';
import warningIllustration from '@/assets/operation-confirm/warning.png';
import AppModal from './AppModal';
import type { OperationConfirmOptions, OperationConfirmType } from './operationConfirmTypes';
import { getOperationConfirmPolicy } from './operationConfirmPolicy';
import './OperationConfirmModal.css';

const illustrations: Record<OperationConfirmType, string> = {
  delete: deleteIllustration,
  destructive: destructiveIllustration,
  warning: warningIllustration,
  normal: normalIllustration,
};

interface OperationConfirmModalProps {
  open: boolean;
  options: OperationConfirmOptions | null;
  confirming: boolean;
  onCancel: () => void;
  onConfirm: () => void;
}

/** 统一操作确认弹框，只承载风险表达与确认状态，不包含领域命令逻辑。 */
export default function OperationConfirmModal({
  open,
  options,
  confirming,
  onCancel,
  onConfirm,
}: OperationConfirmModalProps) {
  if (!options) return null;

  const policy = getOperationConfirmPolicy(options.type);
  return (
    <AppModal
      open={open}
      title={
        <span className="sm-operation-confirm-title">
          <span
            className={`sm-operation-confirm-title-dot sm-operation-confirm-title-dot--${options.type}`}
            aria-hidden="true"
          />
          <span>{options.title ?? '操作确认'}</span>
        </span>
      }
      width={460}
      className="sm-operation-confirm"
      bodyMode="natural"
      draggable
      closeDisabled={confirming}
      keyboard={!confirming}
      onCancel={onCancel}
      footer={
        <>
          <Button disabled={confirming} onClick={onCancel}>
            {options.cancelText ?? '取消'}
          </Button>
          <Button type="primary" danger={policy.dangerous} loading={confirming} onClick={onConfirm}>
            {options.confirmText ?? '确定'}
          </Button>
        </>
      }
    >
      <div className="sm-operation-confirm-content">
        <img
          className="sm-operation-confirm-illustration"
          src={illustrations[options.type]}
          alt=""
        />
        <div className="sm-operation-confirm-description">{options.description}</div>
      </div>
    </AppModal>
  );
}
