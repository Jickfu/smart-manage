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
  onChange: (attachments: BusinessAttachment[], changeType: 'upload' | 'delete') => void;
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
  const { message, modal } = App.useApp();
  const columns = useMemo<ColumnsType<BusinessAttachment>>(
    () => [
      {
        title: '文件名',
        dataIndex: 'originalName',
        render: (name: string, attachment) => (
          <Button
            type="link"
            icon={<DownloadOutlined />}
            onClick={async () => {
              try {
                const access = await businessAttachmentApi.downloadAccess(
                  attachment.id,
                  attachment.uploadSessionId,
                );
                if (access.directUrl) {
                  window.open(access.directUrl, '_blank', 'noopener,noreferrer');
                  return;
                }
                const blob = await businessAttachmentApi.download(
                  attachment.id,
                  attachment.uploadSessionId,
                );
                const objectUrl = URL.createObjectURL(blob);
                const anchor = document.createElement('a');
                anchor.href = objectUrl;
                anchor.download = attachment.originalName;
                anchor.click();
                URL.revokeObjectURL(objectUrl);
              } catch (error) {
                message.error(error instanceof Error ? error.message : '附件下载失败');
              }
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
                  onClick={() => {
                    modal.confirm({
                      title: '确认删除附件？',
                      content: '删除会立即生效，取消或关闭当前表单也无法恢复。',
                      okText: '删除',
                      okType: 'danger',
                      cancelText: '取消',
                      onOk: async () => {
                        try {
                          await businessAttachmentApi.delete(
                            attachment.id,
                            attachment.uploadSessionId,
                          );
                          onChange(
                            attachments.filter((item) => item.id !== attachment.id),
                            'delete',
                          );
                        } catch (error) {
                          message.error(error instanceof Error ? error.message : '附件删除失败');
                          throw error;
                        }
                      },
                    });
                  }}
                >
                  删除
                </Button>
              ),
            },
          ]
        : []),
    ],
    [attachments, editable, message, modal, onChange],
  );
  const customRequest: UploadProps['customRequest'] = async ({ file, onSuccess, onError }) => {
    try {
      const attachment = await businessAttachmentApi.upload(resourceType, file as File);
      onChange([...attachments, attachment], 'upload');
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
