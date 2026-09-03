import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useEffect, useRef, useState } from 'react';
import { Form, Input, InputNumber, Select, Switch } from 'antd';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { activeUiConfigQueryKey } from '@/api/uiConfig';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { EditPageShell } from '@/domain/common/page/edit/EditPageShell';
import { EditSectionCollapse } from '@/domain/common/page/edit/EditSectionCollapse';
import { FormFieldCell, FormFieldGrid } from '@/domain/common/page/edit/FormFieldLayout';
import { PermissionActions } from '@/domain/common/page/PermissionActions';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useConfigDirtyGuard } from '@/domain/sys/base/configCommon/useConfigDirtyGuard';
import { ImageAttachmentField } from './ImageAttachmentField';
import { uiConfigApi } from './api';
import { uiConfigAccess } from './permissions';
import type { UiConfigDetail } from './types';
import './UiConfigPage.css';

type WatermarkField = 'NAME' | 'PHONE' | 'EMAIL' | 'NUMBER' | 'ROOT_ORG';

interface UiConfigFormValues {
  pageTitle: string;
  systemName: string;
  loginBannerAttachmentId: string | null;
  loginBanner: string | null;
  loginLogoAttachmentId: string | null;
  loginLogo: string | null;
  headerLogoAttachmentId: string | null;
  headerLogo: string | null;
  watermarkEnabled: boolean;
  watermarkContent: string;
  watermarkFields: WatermarkField[];
  watermarkGapX: number;
  watermarkGapY: number;
  watermarkFontSize: number;
}

const watermarkFieldOptions = [
  { label: '当前用户姓名', value: 'NAME' },
  { label: '手机号（脱敏）', value: 'PHONE' },
  { label: '邮箱（脱敏）', value: 'EMAIL' },
  { label: '工号', value: 'NUMBER' },
  { label: '最根级组织名称', value: 'ROOT_ORG' },
] satisfies Array<{ label: string; value: WatermarkField }>;

const UiConfigPage = ({ appNumber, tabKey }: PageComponentProps) => {
  const feedback = useOperationFeedback();
  const queryClient = useQueryClient();
  const [form] = Form.useForm<UiConfigFormValues>();
  const [dirty, setDirty] = useState(false);
  const sessionUploadedIds = useRef(new Set<string>());
  const uploadSessions = useRef<Record<string, string>>({});
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
    // setFieldsValue 对 undefined 字段不会可靠清除旧值，先重置以避免已删除图片再次回显。
    form.resetFields();
    form.setFieldsValue({
      pageTitle: query.data.pageTitle ?? '',
      systemName: query.data.systemName ?? '',
      loginBannerAttachmentId: query.data.loginBannerAttachmentId ?? null,
      loginBanner: query.data.loginBanner ?? null,
      loginLogoAttachmentId: query.data.loginLogoAttachmentId ?? null,
      loginLogo: query.data.loginLogo ?? null,
      headerLogoAttachmentId: query.data.headerLogoAttachmentId ?? null,
      headerLogo: query.data.headerLogo ?? null,
      watermarkEnabled: query.data.watermarkEnabled ?? false,
      watermarkContent: query.data.watermarkContent ?? '',
      watermarkFields: [
        query.data.watermarkShowName ? 'NAME' : null,
        query.data.watermarkShowPhone ? 'PHONE' : null,
        query.data.watermarkShowEmail ? 'EMAIL' : null,
        query.data.watermarkShowNumber ? 'NUMBER' : null,
        query.data.watermarkShowRootOrg ? 'ROOT_ORG' : null,
      ].filter((field): field is WatermarkField => field !== null),
      watermarkGapX: query.data.watermarkGapX ?? 100,
      watermarkGapY: query.data.watermarkGapY ?? 100,
      watermarkFontSize: query.data.watermarkFontSize ?? 16,
    });
  }, [form, query.data]);
  const saveMutation = useCommandMutation({
    mutationFn: async (values: UiConfigFormValues) => {
      const payload: UiConfigDetail = {
        id: query.data?.id,
        version: query.data?.version,
        pageTitle: values.pageTitle.trim(),
        systemName: values.systemName.trim(),
        loginBannerAttachmentId: values.loginBannerAttachmentId,
        loginLogoAttachmentId: values.loginLogoAttachmentId,
        headerLogoAttachmentId: values.headerLogoAttachmentId,
        watermarkEnabled: values.watermarkEnabled,
        watermarkContent: values.watermarkContent.trim() || null,
        watermarkShowName: values.watermarkFields.includes('NAME'),
        watermarkShowPhone: values.watermarkFields.includes('PHONE'),
        watermarkShowEmail: values.watermarkFields.includes('EMAIL'),
        watermarkShowNumber: values.watermarkFields.includes('NUMBER'),
        watermarkShowRootOrg: values.watermarkFields.includes('ROOT_ORG'),
        watermarkGapX: values.watermarkGapX,
        watermarkGapY: values.watermarkGapY,
        watermarkFontSize: values.watermarkFontSize,
        attachmentUploadSessions: uploadSessions.current,
      };
      await uiConfigApi.save(payload);
      sessionUploadedIds.current.clear();
      uploadSessions.current = {};
      await query.refetch();
      await queryClient.invalidateQueries({ queryKey: activeUiConfigQueryKey });
      setDirty(false);
      feedback.success('界面配置保存成功');
    },
  });
  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      saveMutation.mutate(values);
    } catch (error) {
      // 表单校验错误由字段自身展示，校验通过前不得进入后端请求的加载状态。
      if (!(error as { errorFields?: unknown[] }).errorFields) {
        feedback.fromError(error, '表单校验失败');
      }
    }
  };
  const imageField = (
    idName: keyof UiConfigFormValues,
    urlName: keyof UiConfigFormValues,
    label: string,
    extra: string,
    attachmentId?: string | null,
    imageUrl?: string | null,
  ) => (
    <FormFieldCell>
      <Form.Item className="sm-edit-field-content" label={label} extra={extra}>
        <ImageAttachmentField
          attachmentId={attachmentId}
          imageUrl={imageUrl}
          onChange={(nextAttachmentId, nextImageUrl, uploadSessionId) => {
            if (attachmentId && sessionUploadedIds.current.has(attachmentId)) {
              sessionUploadedIds.current.delete(attachmentId);
              void uiConfigApi
                .deleteAttachment(attachmentId, uploadSessions.current[attachmentId])
                .catch(() => feedback.warning('未使用的临时图片清理失败，将由临时文件任务处理'));
            }
            if (nextAttachmentId) {
              sessionUploadedIds.current.add(nextAttachmentId);
              if (uploadSessionId) uploadSessions.current[nextAttachmentId] = uploadSessionId;
            }
            // 删除必须写入显式 null；undefined 会被 JSON 省略，并导致受控 Upload 保留旧预览。
            form.setFieldValue(idName, nextAttachmentId ?? null);
            form.setFieldValue(urlName, nextImageUrl ?? null);
            setDirty(true);
          }}
        />
      </Form.Item>
    </FormFieldCell>
  );
  return (
    <EditPageShell
      title="界面配置"
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
              onClick: handleSave,
            },
          ]}
        />
      }
    >
      <Form
        form={form}
        layout="vertical"
        className="sm-edit-form sm-ui-config-form"
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
        <EditSectionCollapse
          defaultActiveKeys={['basic', 'images', 'watermark']}
          items={[
            {
              key: 'basic',
              label: '基本信息',
              children: (
                <FormFieldGrid>
                  <FormFieldCell>
                    <Form.Item
                      className="sm-edit-field-content"
                      name="pageTitle"
                      label="页面标题"
                      rules={[{ required: true, message: '页面标题不能为空' }]}
                    >
                      <Input variant="underlined" />
                    </Form.Item>
                  </FormFieldCell>
                  <FormFieldCell>
                    <Form.Item
                      className="sm-edit-field-content"
                      name="systemName"
                      label="系统名称"
                      rules={[{ required: true, message: '系统名称不能为空' }]}
                    >
                      <Input variant="underlined" />
                    </Form.Item>
                  </FormFieldCell>
                </FormFieldGrid>
              ),
            },
            {
              key: 'images',
              label: '图片配置',
              children: (
                <FormFieldGrid>
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
                </FormFieldGrid>
              ),
            },
            {
              key: 'watermark',
              label: '水印配置',
              children: (
                <FormFieldGrid>
                  <FormFieldCell>
                    <Form.Item
                      className="sm-edit-field-content"
                      name="watermarkEnabled"
                      label="启用水印"
                      valuePropName="checked"
                    >
                      <Switch checkedChildren="开启" unCheckedChildren="关闭" />
                    </Form.Item>
                  </FormFieldCell>
                  <FormFieldCell>
                    <Form.Item
                      className="sm-edit-field-content"
                      name="watermarkFontSize"
                      label="字体大小"
                      rules={[{ required: true, message: '水印字体大小不能为空' }]}
                    >
                      <InputNumber
                        className="sm-edit-control-full"
                        min={12}
                        max={32}
                        precision={0}
                        suffix="px"
                        variant="underlined"
                      />
                    </Form.Item>
                  </FormFieldCell>
                  <FormFieldCell columnSpan={2}>
                    <Form.Item
                      className="sm-edit-field-content"
                      name="watermarkContent"
                      label="固定内容"
                      rules={[{ max: 200, message: '水印固定内容不能超过200个字符' }]}
                    >
                      <Input.TextArea
                        className="sm-ui-config-watermark-content"
                        variant="underlined"
                        maxLength={200}
                        showCount
                        placeholder="可选；固定内容与用户信息分行显示"
                      />
                    </Form.Item>
                  </FormFieldCell>
                  <FormFieldCell columnSpan={2}>
                    <Form.Item
                      className="sm-edit-field-content"
                      name="watermarkFields"
                      label="用户信息"
                      extra="手机号和邮箱固定脱敏；未设置的用户信息会自动忽略"
                    >
                      <Select
                        mode="multiple"
                        allowClear
                        variant="underlined"
                        placeholder="请选择需要显示的用户信息"
                        options={watermarkFieldOptions}
                      />
                    </Form.Item>
                  </FormFieldCell>
                  <FormFieldCell>
                    <Form.Item
                      className="sm-edit-field-content"
                      name="watermarkGapX"
                      label="水平间距"
                      rules={[{ required: true, message: '水印水平间距不能为空' }]}
                    >
                      <InputNumber
                        className="sm-edit-control-full"
                        min={20}
                        max={500}
                        precision={0}
                        suffix="px"
                        variant="underlined"
                      />
                    </Form.Item>
                  </FormFieldCell>
                  <FormFieldCell>
                    <Form.Item
                      className="sm-edit-field-content"
                      name="watermarkGapY"
                      label="垂直间距"
                      rules={[{ required: true, message: '水印垂直间距不能为空' }]}
                    >
                      <InputNumber
                        className="sm-edit-control-full"
                        min={20}
                        max={500}
                        precision={0}
                        suffix="px"
                        variant="underlined"
                      />
                    </Form.Item>
                  </FormFieldCell>
                </FormFieldGrid>
              ),
            },
          ]}
        />
      </Form>
    </EditPageShell>
  );
};

export default UiConfigPage;
