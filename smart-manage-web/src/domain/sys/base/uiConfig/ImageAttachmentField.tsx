import { useMemo } from 'react';
import { App, Button, Upload } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { UploadFile, UploadProps } from 'antd';
import { uiConfigApi } from './api';

interface ImageAttachmentFieldProps {
  attachmentId?: string;
  imageUrl?: string;
  disabled?: boolean;
  onChange: (attachmentId?: string, imageUrl?: string, uploadSessionId?: string) => void;
}

export function ImageAttachmentField({
  attachmentId,
  imageUrl,
  disabled,
  onChange,
}: ImageAttachmentFieldProps) {
  const { message } = App.useApp();
  const fileList = useMemo<UploadFile[]>(
    () =>
      attachmentId || imageUrl
        ? [
            {
              uid: attachmentId ?? imageUrl!,
              name: '当前图片',
              status: 'done',
              url: imageUrl,
            },
          ]
        : [],
    [attachmentId, imageUrl],
  );
  const customRequest: UploadProps['customRequest'] = async ({ file, onSuccess, onError }) => {
    try {
      const attachment = await uiConfigApi.uploadImage(file as File);
      onChange(attachment.id, attachment.url, attachment.uploadSessionId);
      onSuccess?.(attachment);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '图片上传失败');
      onError?.(error as Error);
    }
  };
  return (
    <Upload
      accept="image/png,image/jpeg,image/webp,image/gif"
      listType="picture-card"
      maxCount={1}
      fileList={fileList}
      onChange={() => undefined}
      customRequest={customRequest}
      beforeUpload={(file) => {
        if (!file.type.startsWith('image/')) {
          message.error('只能上传图片文件');
          return Upload.LIST_IGNORE;
        }
        if (file.size > 5 * 1024 * 1024) {
          message.error('图片大小不能超过 5 MB');
          return Upload.LIST_IGNORE;
        }
        return true;
      }}
      disabled={disabled}
      onPreview={(file) => {
        if (file.url) window.open(file.url, '_blank', 'noopener,noreferrer');
      }}
      onRemove={async () => {
        onChange(undefined, undefined);
        return true;
      }}
    >
      {fileList.length === 0 && (
        <Button type="text" icon={<PlusOutlined />} disabled={disabled}>
          上传
        </Button>
      )}
    </Upload>
  );
}
