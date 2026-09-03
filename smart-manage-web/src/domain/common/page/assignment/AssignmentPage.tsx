import type { ReactNode } from 'react';
import { Spin } from 'antd';
import { RequestErrorState } from '@/domain/common/component/RequestErrorState';
import type { AccessResource } from '../access/access';
import { PermissionActions } from '../access/PermissionActions';
import { useBeforeCloseGuard } from '../tab/useBeforeCloseGuard';
import '../pageLayout.css';
import './AssignmentPage.css';

interface AssignmentPageProps {
  loading: boolean;
  saving: boolean;
  error?: Error | null;
  children: ReactNode;
  onSave: () => void;
  onExit: () => void;
  onRetry: () => void;
  access: AccessResource<{ save: string }>;
  subject?: ReactNode;
  selectedCount?: number;
  totalCount?: number;
  dirty?: boolean;
  saveDisabled?: boolean;
  closeGuard?: { appNumber: string; tabKey: string };
}

/** 关系分配专用页面框架，操作区与内容区严格分离。 */
export function AssignmentPage({
  loading,
  saving,
  error,
  children,
  onSave,
  onExit,
  onRetry,
  access,
  subject,
  selectedCount,
  totalCount,
  dirty = false,
  saveDisabled = false,
  closeGuard,
}: AssignmentPageProps) {
  useBeforeCloseGuard(closeGuard?.appNumber, closeGuard?.tabKey, dirty);

  return (
    <section className="sm-common-page sm-edit-page">
      {error && <RequestErrorState error={error} onRetry={onRetry} />}
      <div className="sm-edit-header" hidden={Boolean(error)} inert={Boolean(error)}>
        <div className="sm-edit-header-actions">
          <PermissionActions
            prefix={access.prefix}
            actions={[
              {
                key: 'save',
                label: '保存',
                permission: access.permissions.save,
                type: 'primary',
                loading: saving,
                disabled: saveDisabled || Boolean(error),
                onClick: () => {
                  if (!error) onSave();
                },
              },
              { key: 'exit', label: '退出', onClick: onExit },
            ]}
          />
        </div>
        <div className="sm-assignment-header-context">
          {subject && <span className="sm-assignment-subject">{subject}</span>}
          {selectedCount !== undefined && totalCount !== undefined && (
            <span>
              已选 {selectedCount} / {totalCount}
            </span>
          )}
          {dirty && <span className="sm-assignment-dirty">有未保存修改</span>}
        </div>
      </div>
      <div
        className="sm-edit-body sm-assignment-body"
        hidden={Boolean(error)}
        inert={Boolean(error)}
      >
        <Spin spinning={loading}>{children}</Spin>
      </div>
    </section>
  );
}
