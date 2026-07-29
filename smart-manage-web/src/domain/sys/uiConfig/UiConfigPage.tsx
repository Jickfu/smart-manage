import { useEffect, useRef, useState } from 'react';
import { App, Form, Input } from 'antd';
import { useMutation, useQuery } from '@tanstack/react-query';
import { EditPageShell } from '@/domain/common/page/EditPageShell';
import { PermissionActions } from '@/domain/common/page/PermissionActions';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useConfigDirtyGuard } from '@/domain/sys/configCommon/useConfigDirtyGuard';
import { ImageAttachmentField } from './ImageAttachmentField';
import { uiConfigApi } from './api';
import { uiConfigAccess } from './permissions';
import type { UiConfigDetail } from './types';
import './UiConfigPage.css';

interface UiConfigFormValues {
  pageTitle: string;
  systemName: string;
  loginBannerAttachmentId?: string;
  loginBanner?: string;
  loginLogoAttachmentId?: string;
  loginLogo?: string;
  headerLogoAttachmentId?: string;
  headerLogo?: string;
}

const UiConfigPage = ({ appNumber, tabKey }: PageComponentProps) => {
  const { message } = App.useApp();
  const [form] = Form.useForm<UiConfigFormValues>();
  const [dirty, setDirty] = useState(false);
  const sessionUploadedIds = useRef(new Set<string>());
  const query = useQuery({
    queryKey: ['sys', 'ui-config', 'singleton'],
    queryFn: uiConfigApi.singleton,
  });
  useConfigDirtyGuard(appNumber, tabKey, dirty);
  const loginBannerAttachmentId = Form.useWatch('loginBannerAttachmentId', form);
  const loginBanner = Form.useWatch('loginBanner', form);
  const loginLogoAttachmentId = Form.useWatch('loginLogoAttachmentId', form);
  const loginLogo = Form.useWatch('loginLogo', form);
  const headerLogoAttachmentId = Form.useWatch('headerLogoAttachmentId', form);
  const headerLogo = Form.useWatch('headerLogo', form);
  useEffect(() => {
    if (!query.data) return;
    form.setFieldsValue({
      pageTitle: query.data.pageTitle ?? '',
      systemName: query.data.systemName ?? '',
      loginBannerAttachmentId: query.data.loginBannerAttachmentId,
      loginBanner: query.data.loginBanner,
      loginLogoAttachmentId: query.data.loginLogoAttachmentId,
      loginLogo: query.data.loginLogo,
      headerLogoAttachmentId: query.data.headerLogoAttachmentId,
      headerLogo: query.data.headerLogo,
    });
  }, [form, query.data]);
  const saveMutation = useMutation({
    mutationFn: async () => {
      const values = await form.validateFields();
      const payload: UiConfigDetail = {
        id: query.data?.id,
        version: query.data?.version,
        pageTitle: values.pageTitle.trim(),
        systemName: values.systemName.trim(),
        loginBannerAttachmentId: values.loginBannerAttachmentId,
        loginLogoAttachmentId: values.loginLogoAttachmentId,
        headerLogoAttachmentId: values.headerLogoAttachmentId,
      };
      await uiConfigApi.save(payload);
      sessionUploadedIds.current.clear();
      await query.refetch();
      setDirty(false);
      message.success('界面配置保存成功，刷新登录页后可查看最新效果');
    },
    onError: (error) => message.error(error instanceof Error ? error.message : '保存失败'),
  });
  const imageField = (
    idName: keyof UiConfigFormValues,
    urlName: keyof UiConfigFormValues,
    label: string,
    extra: string,
    attachmentId?: string,
    imageUrl?: string,
  ) => (
    <Form.Item label={label} extra={extra}>
      <ImageAttachmentField
        attachmentId={attachmentId}
        imageUrl={imageUrl}
        onChange={(nextAttachmentId, nextImageUrl) => {
          if (attachmentId && sessionUploadedIds.current.has(attachmentId)) {
            sessionUploadedIds.current.delete(attachmentId);
            void uiConfigApi
              .deleteAttachment(attachmentId)
              .catch(() => message.warning('未使用的临时图片清理失败，将由临时文件任务处理'));
          }
          if (nextAttachmentId) {
            sessionUploadedIds.current.add(nextAttachmentId);
          }
          form.setFieldValue(idName, nextAttachmentId);
          form.setFieldValue(urlName, nextImageUrl);
          setDirty(true);
        }}
      />
    </Form.Item>
  );
  return (
    <EditPageShell
      loading={query.isLoading}
      error={query.error as Error | null}
      onRetry={() => query.refetch()}
      actions={
        <PermissionActions
          prefix={uiConfigAccess.prefix}
          actions={[
            {
              key: 'save',
              label: '保存',
              permission: uiConfigAccess.permissions.save,
              type: 'primary',
              loading: saveMutation.isPending,
              onClick: () => saveMutation.mutate(),
            },
          ]}
        />
      }
    >
      <Form
        form={form}
        layout="vertical"
        className="sm-ui-config-form"
        onValuesChange={() => setDirty(true)}
      >
        <Form.Item name="loginBannerAttachmentId" hidden>
          <Input />
        </Form.Item>
        <Form.Item name="loginBanner" hidden>
          <Input />
        </Form.Item>
        <Form.Item name="loginLogoAttachmentId" hidden>
          <Input />
        </Form.Item>
        <Form.Item name="loginLogo" hidden>
          <Input />
        </Form.Item>
        <Form.Item name="headerLogoAttachmentId" hidden>
          <Input />
        </Form.Item>
        <Form.Item name="headerLogo" hidden>
          <Input />
        </Form.Item>
        <div className="sm-ui-config-text-grid">
          <Form.Item
            name="pageTitle"
            label="页面标题"
            rules={[{ required: true, message: '页面标题不能为空' }]}
          >
            <Input variant="underlined" />
          </Form.Item>
          <Form.Item
            name="systemName"
            label="系统名称"
            rules={[{ required: true, message: '系统名称不能为空' }]}
          >
            <Input variant="underlined" />
          </Form.Item>
        </div>
        <div className="sm-ui-config-image-grid">
          {imageField(
            'loginBannerAttachmentId',
            'loginBanner',
            '登录页 Banner',
            '建议使用横向图片',
            loginBannerAttachmentId,
            loginBanner,
          )}
          {imageField(
            'loginLogoAttachmentId',
            'loginLogo',
            '登录页 Logo',
            '建议使用透明背景图片',
            loginLogoAttachmentId,
            loginLogo,
          )}
          {imageField(
            'headerLogoAttachmentId',
            'headerLogo',
            '顶部 Logo',
            '建议使用紧凑横向图片',
            headerLogoAttachmentId,
            headerLogo,
          )}
        </div>
      </Form>
    </EditPageShell>
  );
};

export default UiConfigPage;
