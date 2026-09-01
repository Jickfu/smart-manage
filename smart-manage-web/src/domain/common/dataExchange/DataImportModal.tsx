import { InboxOutlined } from '@ant-design/icons';
import { Alert, Button, Radio, Typography, Upload } from 'antd';
import type { ReactNode } from 'react';
import type { UploadFile } from 'antd/es/upload/interface';
import AppModal from '@/domain/common/component/AppModal';
import './DataImportModal.css';

export interface DataImportOption<TValue extends string> {
  value: TValue;
  label: string;
  description: string;
}

interface DataImportModalProps<TMode extends string, TFailurePolicy extends string> {
  title: string;
  open: boolean;
  importing: boolean;
  file?: File;
  mode: TMode;
  failurePolicy: TFailurePolicy;
  modeOptions: Array<DataImportOption<TMode>>;
  failurePolicyOptions: Array<DataImportOption<TFailurePolicy>>;
  matchRule: ReactNode;
  notice: ReactNode;
  onModeChange: (value: TMode) => void;
  onFailurePolicyChange: (value: TFailurePolicy) => void;
  onFileChange: (file?: File) => void;
  onDownloadTemplate: () => Promise<void>;
  onSubmit: () => Promise<void>;
  onCancel: () => void;
}

const OptionCards = <TValue extends string>({
  value,
  options,
  onChange,
}: {
  value: TValue;
  options: Array<DataImportOption<TValue>>;
  onChange: (value: TValue) => void;
}) => (
  <Radio.Group
    value={value}
    orientation="vertical"
    className="sm-data-import-option-group"
    onChange={(event) => onChange(event.target.value as TValue)}
  >
    {options.map((option) => (
      <Radio key={option.value} value={option.value} className="sm-data-import-option">
        <span className="sm-data-import-option-label">{option.label}</span>
        <span className="sm-data-import-option-description">{option.description}</span>
      </Radio>
    ))}
  </Radio.Group>
);

const DataImportModal = <TMode extends string, TFailurePolicy extends string>({
  title,
  open,
  importing,
  file,
  mode,
  failurePolicy,
  modeOptions,
  failurePolicyOptions,
  matchRule,
  notice,
  onModeChange,
  onFailurePolicyChange,
  onFileChange,
  onDownloadTemplate,
  onSubmit,
  onCancel,
}: DataImportModalProps<TMode, TFailurePolicy>) => {
  const fileList: UploadFile[] = file ? [{ uid: file.name, name: file.name, status: 'done' }] : [];

  return (
    <AppModal
      title={title}
      open={open}
      width={920}
      bodyMode="natural"
      className="sm-data-import-modal"
      closeDisabled={importing}
      onCancel={onCancel}
      footer={
        <>
          <Button onClick={onCancel} disabled={importing}>
            取消
          </Button>
          <Button
            type="primary"
            loading={importing}
            disabled={!file}
            onClick={() => void onSubmit()}
          >
            开始导入
          </Button>
        </>
      }
    >
      <div className="sm-data-import-grid">
        <section className="sm-data-import-section">
          <Typography.Title level={5}>1. 选择要执行的操作</Typography.Title>
          <OptionCards value={mode} options={modeOptions} onChange={onModeChange} />
        </section>
        <section className="sm-data-import-section">
          <Typography.Title level={5}>2. 确认导入规则</Typography.Title>
          <div className="sm-data-import-rule">{matchRule}</div>
          <Button
            type="link"
            className="sm-data-import-template"
            onClick={() => void onDownloadTemplate()}
          >
            下载最新模板
          </Button>
          <Typography.Text type="secondary">发现数据错误时：</Typography.Text>
          <OptionCards
            value={failurePolicy}
            options={failurePolicyOptions}
            onChange={onFailurePolicyChange}
          />
        </section>
        <section className="sm-data-import-section">
          <Typography.Title level={5}>3. 上传填写好的文件</Typography.Title>
          <Upload.Dragger
            accept=".xlsx"
            maxCount={1}
            fileList={fileList}
            disabled={importing}
            beforeUpload={(nextFile) => {
              onFileChange(nextFile);
              return false;
            }}
            onRemove={() => {
              onFileChange(undefined);
              return true;
            }}
            onChange={({ fileList: nextFileList }) => {
              if (nextFileList.length === 0) onFileChange(undefined);
            }}
          >
            <InboxOutlined className="sm-data-import-upload-icon" />
            <p className="ant-upload-text">点击或拖拽上传</p>
            <p className="ant-upload-hint">仅支持 .xlsx，文件不超过 20 MB</p>
          </Upload.Dragger>
        </section>
      </div>
      <Alert type="info" showIcon description={notice} className="sm-data-import-notice" />
    </AppModal>
  );
};

export default DataImportModal;
