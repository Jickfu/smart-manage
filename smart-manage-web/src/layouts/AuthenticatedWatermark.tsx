import type { ReactNode } from 'react';
import { Watermark } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { activeUiConfigQueryKey, getActiveUiConfig } from '@/api/uiConfig';
import { useUserStore } from '@/stores/user';
import {
  buildWatermarkContent,
  DEFAULT_WATERMARK_FONT_SIZE,
  DEFAULT_WATERMARK_GAP,
  estimateWatermarkWidth,
} from './watermarkContent';
import './AuthenticatedWatermark.css';

export const AuthenticatedWatermark = ({ children }: { children: ReactNode }) => {
  const user = useUserStore((state) => state.userInfo);
  const configQuery = useQuery({
    queryKey: activeUiConfigQueryKey,
    queryFn: getActiveUiConfig,
  });
  const content = buildWatermarkContent(configQuery.data, user);
  const fontSize = configQuery.data?.watermarkFontSize ?? DEFAULT_WATERMARK_FONT_SIZE;

  if (content.length === 0) return children;

  return (
    <Watermark
      className="sm-authenticated-watermark"
      content={content}
      width={estimateWatermarkWidth(content, fontSize)}
      height={Math.max(64, content.length * (fontSize + 8) + 24)}
      font={{ fontSize }}
      gap={[
        configQuery.data?.watermarkGapX ?? DEFAULT_WATERMARK_GAP,
        configQuery.data?.watermarkGapY ?? DEFAULT_WATERMARK_GAP,
      ]}
    >
      {children}
    </Watermark>
  );
};
