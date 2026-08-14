import { useEffect, useRef, useState } from 'react';
import { App, Button, Empty, Image, Input, Modal, Popover, Progress, Tooltip, Upload } from 'antd';
import {
  FileImageOutlined,
  FilePdfOutlined,
  PaperClipOutlined,
  UploadOutlined,
} from '@ant-design/icons';
import type { UploadProps } from 'antd';
import { businessAttachmentApi } from './api';
import type { BusinessAttachment } from './types';
import './BusinessAttachmentPanel.css';

interface BusinessAttachmentPanelProps {
  resourceType: string;
  attachments: BusinessAttachment[];
  editable: boolean;
  onChange: (
    attachments: BusinessAttachment[],
    changeType: 'upload' | 'delete' | 'metadata',
  ) => void;
}

interface PendingUpload {
  uid: string;
  name: string;
  size?: number;
  percent: number;
  controller: AbortController;
}

interface PreviewState {
  name: string;
  mimeType?: string;
  url: string;
}

function formatFileSize(bytes?: number) {
  if (bytes === undefined) return '-';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

function attachmentIcon(attachment: Pick<BusinessAttachment, 'mimeType'>) {
  if (attachment.mimeType?.startsWith('image/')) return <FileImageOutlined />;
  if (attachment.mimeType === 'application/pdf') return <FilePdfOutlined />;
  return <PaperClipOutlined />;
}

function isPreviewable(attachment: BusinessAttachment) {
  return [
    'image/jpeg',
    'image/png',
    'image/gif',
    'image/webp',
    'image/bmp',
    'application/pdf',
  ].includes(attachment.mimeType ?? '');
}

export function BusinessAttachmentPanel({
  resourceType,
  attachments,
  editable,
  onChange,
}: BusinessAttachmentPanelProps) {
  const { message, modal } = App.useApp();
  const attachmentsRef = useRef(attachments);
  const previewUrlRef = useRef<string | undefined>(undefined);
  const [pendingUploads, setPendingUploads] = useState<PendingUpload[]>([]);
  const [preview, setPreview] = useState<PreviewState>();

  useEffect(() => {
    attachmentsRef.current = attachments;
  }, [attachments]);

  useEffect(
    () => () => {
      if (previewUrlRef.current) URL.revokeObjectURL(previewUrlRef.current);
    },
    [],
  );

  const replaceAttachment = (updated: BusinessAttachment) => {
    const nextAttachments = attachmentsRef.current.map((attachment) =>
      attachment.id === updated.id ? updated : attachment,
    );
    attachmentsRef.current = nextAttachments;
    // 备注立即持久化，不改变单据表单的脏数据状态。
    onChange(nextAttachments, 'metadata');
  };

  const downloadAttachment = async (attachment: BusinessAttachment) => {
    try {
      const access = await businessAttachmentApi.downloadAccess(
        attachment.id,
        attachment.uploadSessionId,
      );
      if (access.directUrl) {
        window.open(access.directUrl, '_blank', 'noopener,noreferrer');
        return;
      }
      const blob = await businessAttachmentApi.download(attachment.id, attachment.uploadSessionId);
      const objectUrl = URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = objectUrl;
      anchor.download = attachment.originalName;
      anchor.click();
      URL.revokeObjectURL(objectUrl);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '附件下载失败');
    }
  };

  const previewAttachment = async (attachment: BusinessAttachment) => {
    try {
      const blob = await businessAttachmentApi.preview(attachment.id, attachment.uploadSessionId);
      setPreview((current) => {
        if (current) URL.revokeObjectURL(current.url);
        const url = URL.createObjectURL(blob);
        previewUrlRef.current = url;
        return {
          name: attachment.originalName,
          mimeType: attachment.mimeType,
          url,
        };
      });
    } catch (error) {
      message.error(error instanceof Error ? error.message : '附件预览失败');
    }
  };

  const deleteAttachment = (attachment: BusinessAttachment) => {
    modal.confirm({
      title: '确认删除附件？',
      content: '删除会立即生效，取消或关闭当前表单也无法恢复。',
      okText: '删除',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          await businessAttachmentApi.delete(attachment.id, attachment.uploadSessionId);
          const nextAttachments = attachmentsRef.current.filter(
            (item) => item.id !== attachment.id,
          );
          attachmentsRef.current = nextAttachments;
          onChange(nextAttachments, 'delete');
        } catch (error) {
          message.error(error instanceof Error ? error.message : '附件删除失败');
          throw error;
        }
      },
    });
  };

  const customRequest: UploadProps['customRequest'] = async ({
    file,
    onProgress,
    onSuccess,
    onError,
  }) => {
    const uploadFile = file as File & { uid: string };
    const controller = new AbortController();
    const pending: PendingUpload = {
      uid: uploadFile.uid,
      name: uploadFile.name,
      size: uploadFile.size,
      percent: 0,
      controller,
    };
    setPendingUploads((items) => [...items, pending]);
    try {
      const attachment = await businessAttachmentApi.upload(resourceType, uploadFile, {
        signal: controller.signal,
        onProgress: (percent) => {
          setPendingUploads((items) =>
            items.map((item) => (item.uid === uploadFile.uid ? { ...item, percent } : item)),
          );
          onProgress?.({ percent });
        },
      });
      const nextAttachments = [...attachmentsRef.current, attachment];
      attachmentsRef.current = nextAttachments;
      onChange(nextAttachments, 'upload');
      onSuccess?.(attachment);
    } catch (error) {
      if (!controller.signal.aborted) {
        message.error(error instanceof Error ? error.message : '附件上传失败');
        onError?.(error as Error);
      }
    } finally {
      setPendingUploads((items) => items.filter((item) => item.uid !== uploadFile.uid));
    }
  };

  const closePreview = () => {
    setPreview((current) => {
      if (current) URL.revokeObjectURL(current.url);
      previewUrlRef.current = undefined;
      return undefined;
    });
  };

  return (
    <div className="sm-business-attachment-panel">
      {editable && (
        <div className="sm-business-attachment-toolbar">
          <Upload multiple showUploadList={false} customRequest={customRequest}>
            <Button icon={<UploadOutlined />}>上传附件</Button>
          </Upload>
        </div>
      )}

      {attachments.length === 0 && pendingUploads.length === 0 ? (
        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无附件" />
      ) : (
        <div className="sm-business-attachment-list">
          {pendingUploads.map((pending) => (
            <div className="sm-business-attachment-item is-uploading" key={pending.uid}>
              <span className="sm-business-attachment-icon">
                <PaperClipOutlined />
              </span>
              <div className="sm-business-attachment-file">
                <span className="sm-business-attachment-name">{pending.name}</span>
                <span className="sm-business-attachment-size">
                  ({formatFileSize(pending.size)})
                </span>
                <Progress percent={pending.percent} size="small" showInfo={false} />
              </div>
              <Button type="link" onClick={() => pending.controller.abort()}>
                取消上传
              </Button>
            </div>
          ))}

          {attachments.map((attachment) => (
            <div className="sm-business-attachment-item" key={attachment.id}>
              <span className="sm-business-attachment-icon">{attachmentIcon(attachment)}</span>
              <div className="sm-business-attachment-file">
                <Tooltip title={attachment.originalName}>
                  <span className="sm-business-attachment-name">{attachment.originalName}</span>
                </Tooltip>
                <span className="sm-business-attachment-size">
                  ({formatFileSize(attachment.fileSize)})
                </span>
                <span className="sm-business-attachment-actions">
                  {isPreviewable(attachment) && (
                    <Button type="link" onClick={() => previewAttachment(attachment)}>
                      预览
                    </Button>
                  )}
                  <Button type="link" onClick={() => downloadAttachment(attachment)}>
                    下载
                  </Button>
                  {editable && (
                    <Button type="link" danger onClick={() => deleteAttachment(attachment)}>
                      删除
                    </Button>
                  )}
                </span>
              </div>
              <span className="sm-business-attachment-uploader">
                {attachment.uploaderName || '-'}
              </span>
              <span className="sm-business-attachment-time">{attachment.createTime || '-'}</span>
              <div className="sm-business-attachment-remark">
                {editable ? (
                  <AttachmentRemarkEditor attachment={attachment} onUpdated={replaceAttachment} />
                ) : (
                  <Tooltip title={attachment.remark}>
                    <span>{attachment.remark || '-'}</span>
                  </Tooltip>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal
        open={Boolean(preview)}
        title={preview?.name}
        footer={null}
        onCancel={closePreview}
        width="80vw"
      >
        {preview?.mimeType?.startsWith('image/') ? (
          <Image
            className="sm-business-attachment-preview-image"
            src={preview.url}
            alt={preview.name}
            preview={false}
          />
        ) : preview ? (
          <iframe
            className="sm-business-attachment-preview-pdf"
            src={preview.url}
            title={preview.name}
          />
        ) : null}
      </Modal>
    </div>
  );
}

function AttachmentRemarkEditor({
  attachment,
  onUpdated,
}: {
  attachment: BusinessAttachment;
  onUpdated: (attachment: BusinessAttachment) => void;
}) {
  const { message } = App.useApp();
  const [open, setOpen] = useState(false);
  const [remark, setRemark] = useState(attachment.remark ?? '');
  const [saving, setSaving] = useState(false);

  const saveRemark = async () => {
    try {
      setSaving(true);
      const updated = await businessAttachmentApi.updateRemark(
        attachment.id,
        attachment.businessAttachmentId,
        remark,
        attachment.uploadSessionId,
      );
      onUpdated(updated);
      setOpen(false);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '附件备注保存失败');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Popover
      open={open}
      trigger="click"
      onOpenChange={(nextOpen) => {
        setOpen(nextOpen);
        if (nextOpen) setRemark(attachment.remark ?? '');
      }}
      content={
        <div className="sm-business-attachment-remark-editor">
          <Input.TextArea
            value={remark}
            maxLength={500}
            showCount
            rows={3}
            onChange={(event) => setRemark(event.target.value)}
          />
          <div className="sm-business-attachment-remark-actions">
            <Button onClick={() => setOpen(false)}>取消</Button>
            <Button type="primary" loading={saving} onClick={saveRemark}>
              确定
            </Button>
          </div>
        </div>
      }
    >
      <Button type="link" className="sm-business-attachment-remark-trigger">
        {attachment.remark || '备注'}
      </Button>
    </Popover>
  );
}
