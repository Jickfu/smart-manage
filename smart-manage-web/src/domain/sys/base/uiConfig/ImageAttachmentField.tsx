import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useEffect, useMemo } from 'react';
import { Button, Upload } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { UploadFile, UploadProps } from 'antd';
import { resolveAssetUrl } from '@/utils/assetUrl';
import { uiConfigApi } from './api';

interface ImageAttachmentFieldProps {
  attachmentId?: string | null;
  imageUrl?: string | null;
  disabled?: boolean;
  onChange: (
    attachmentId: string | null,
    imageUrl: string | null,
    uploadSessionId?: string,
  ) => void;
}

export function ImageAttachmentField({
  attachmentId,
  imageUrl,
  disabled,
  onChange,
}: ImageAttachmentFieldProps) {
  const feedback = useOperationFeedback();
  const resolvedImageUrl = resolveAssetUrl(imageUrl);
  useEffect(
    () => () => {
      if (imageUrl?.startsWith('blob:')) URL.revokeObjectURL(imageUrl);
    },
    [imageUrl],
  );
  const fileList = useMemo<UploadFile[]>(
    () =>
      attachmentId || imageUrl
        ? [
            {
              uid: attachmentId ?? imageUrl!,
              name: '当前图片',
              status: 'done',
              url: resolvedImageUrl,
              thumbUrl: resolvedImageUrl,
            },
          ]
        : [],
    [attachmentId, imageUrl, resolvedImageUrl],
  );
  const customRequest: UploadProps['customRequest'] = async ({ file, onSuccess, onError }) => {
    try {
      const attachment = await uiConfigApi.uploadImage(file as File);
      const previewUrl = URL.createObjectURL(file as File);
      onChange(attachment.id, previewUrl, attachment.uploadSessionId);
      onSuccess?.(attachment);
    } catch (error) {
      feedback.fromError(error, '图片上传失败');
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
          feedback.warning('只能上传图片文件');
          return Upload.LIST_IGNORE;
        }
        if (file.size > 5 * 1024 * 1024) {
          feedback.warning('图片大小不能超过 5 MB');
          return Upload.LIST_IGNORE;
        }
        return true;
      }}
      disabled={disabled}
      onPreview={(file) => {
        if (file.url) window.open(file.url, '_blank', 'noopener,noreferrer');
      }}
      onRemove={async () => {
        onChange(null, null);
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
