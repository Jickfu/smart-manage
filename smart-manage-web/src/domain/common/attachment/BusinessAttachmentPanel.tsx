import { useMemo } from 'react';
import { App, Button, Table, Upload } from 'antd';
import { DownloadOutlined, UploadOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import type { UploadProps } from 'antd';
import { businessAttachmentApi } from './api';
import type { BusinessAttachment } from './types';
import './BusinessAttachmentPanel.css';

interface BusinessAttachmentPanelProps {
  resourceType: string;
  attachments: BusinessAttachment[];
  editable: boolean;
  onChange: (attachments: BusinessAttachment[]) => void;
}

function formatFileSize(bytes?: number) {
  if (bytes === undefined) return '-';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

export function BusinessAttachmentPanel({
  resourceType,
  attachments,
  editable,
  onChange,
}: BusinessAttachmentPanelProps) {
  const { message } = App.useApp();
  const columns = useMemo<ColumnsType<BusinessAttachment>>(
    () => [
      {
        title: '文件名',
        dataIndex: 'originalName',
        render: (name: string, attachment) => (
          <Button
            type="link"
            icon={<DownloadOutlined />}
            onClick={() => {
              if (attachment.url) window.open(attachment.url, '_blank', 'noopener,noreferrer');
            }}
          >
            {name}
          </Button>
        ),
      },
      { title: '大小', dataIndex: 'fileSize', width: 120, render: formatFileSize },
      {
        title: '类型',
        dataIndex: 'mimeType',
        width: 220,
        render: (value?: string) => value || '-',
      },
      {
        title: '上传时间',
        dataIndex: 'createTime',
        width: 180,
        render: (value?: string) => value || '-',
      },
      ...(editable
        ? [
            {
              title: '操作',
              key: 'action',
              width: 80,
              render: (_value: unknown, attachment: BusinessAttachment) => (
                <Button
                  type="link"
                  danger
                  onClick={async () => {
                    try {
                      await businessAttachmentApi.delete(attachment.id, attachment.uploadSessionId);
                      onChange(attachments.filter((item) => item.id !== attachment.id));
                    } catch (error) {
                      message.error(error instanceof Error ? error.message : '附件删除失败');
                    }
                  }}
                >
                  删除
                </Button>
              ),
            },
          ]
        : []),
    ],
    [attachments, editable, message, onChange],
  );
  const customRequest: UploadProps['customRequest'] = async ({ file, onSuccess, onError }) => {
    try {
      const attachment = await businessAttachmentApi.upload(resourceType, file as File);
      onChange([...attachments, attachment]);
      onSuccess?.(attachment);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '附件上传失败');
      onError?.(error as Error);
    }
  };

  return (
    <div className="sm-business-attachment-panel">
      {editable && (
        <div className="sm-business-attachment-toolbar">
          <Upload showUploadList={false} customRequest={customRequest}>
            <Button icon={<UploadOutlined />}>上传附件</Button>
          </Upload>
        </div>
      )}
      <Table
        rowKey="id"
        columns={columns}
        dataSource={attachments}
        pagination={false}
        size="small"
        scroll={{ x: 'max-content' }}
      />
    </div>
  );
}
