import { useState } from 'react';
import DataImportModal from '@/domain/common/dataExchange/DataImportModal';
import { downloadArtifacts, downloadBlob } from '@/domain/common/dataExchange/fileDownload';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { userApi } from './api';
import type { UserImportMode, UserImportTransactionMode } from './types';

interface UserImportModalProps {
  open: boolean;
  onCancel: () => void;
  onImported: () => Promise<void>;
}

const UserImportModal = ({ open, onCancel, onImported }: UserImportModalProps) => {
  const feedback = useOperationFeedback();
  const [file, setFile] = useState<File>();
  const [mode, setMode] = useState<UserImportMode>('UPSERT');
  const [failurePolicy, setFailurePolicy] = useState<UserImportTransactionMode>('ATOMIC');
  const [importing, setImporting] = useState(false);

  const close = () => {
    if (importing) return;
    setFile(undefined);
    onCancel();
  };

  return (
    <DataImportModal
      title="导入用户"
      open={open}
      importing={importing}
      file={file}
      mode={mode}
      failurePolicy={failurePolicy}
      modeOptions={[
        { value: 'CREATE_ONLY', label: '添加新用户', description: '账号已存在的数据不会导入。' },
        {
          value: 'UPDATE_ONLY',
          label: '更新已有用户',
          description: '只更新系统中已经存在的账号。',
        },
        { value: 'UPSERT', label: '添加并更新', description: '已有账号更新，不存在的账号新增。' },
      ]}
      failurePolicyOptions={[
        {
          value: 'ATOMIC',
          label: '全部修正后再导入',
          description: '只要有一行错误，本次不写入任何数据。',
        },
        {
          value: 'BATCH',
          label: '先导入正确批次',
          description: '正确批次先写入，失败内容生成错误报告。',
        },
      ]}
      matchRule={
        <>
          系统使用创建后不可修改的<strong>登录账号 username</strong>
          识别已有用户；工号不作为匹配依据。
        </>
      }
      notice={
        <>
          新增用户的随机初始密码会生成一次性结果文件，请下载后立即安全交付。更新用户时，模板中的任职会整体替换现有任职。
        </>
      }
      onModeChange={setMode}
      onFailurePolicyChange={setFailurePolicy}
      onFileChange={setFile}
      onDownloadTemplate={async () =>
        downloadBlob(await userApi.importTemplate(), '用户导入模板.xlsx')
      }
      onCancel={close}
      onSubmit={async () => {
        if (!file) return;
        setImporting(true);
        try {
          const result = await userApi.importUsers(file, mode, failurePolicy);
          await downloadArtifacts([...result.credentialFiles, result.errorFile]);
          if (result.warnings.length)
            feedback.warning(
              `导入完成：成功 ${result.success}，失败 ${result.failed}；${result.warnings.join('；')}`,
            );
          else if (result.failed)
            feedback.warning(`导入完成：成功 ${result.success}，失败 ${result.failed}`);
          else feedback.success(`成功导入 ${result.success} 个用户`);
          await onImported();
          setFile(undefined);
          onCancel();
        } finally {
          setImporting(false);
        }
      }}
    />
  );
};

export default UserImportModal;
