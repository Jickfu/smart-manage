import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
export default definePageRegistrations([
  {
    componentKey: 'sys/message/email-account',
    featureKey: 'sys/message/email-account',
    title: '发信账号',
    pageType: 'LIST',
    component: lazy(() => import('./EmailAccountListPage')),
  },
  {
    componentKey: 'sys/message/email-account/edit',
    featureKey: 'sys/message/email-account',
    title: '发信账号',
    pageType: 'EDIT',
    component: lazy(() => import('./EmailAccountEditPage')),
  },
  {
    componentKey: 'sys/message/email-compose',
    featureKey: 'sys/message/email-compose',
    title: '发送邮件',
    pageType: 'CUSTOM',
    component: lazy(() => import('./EmailComposePage')),
  },
  {
    componentKey: 'sys/message/email-record',
    featureKey: 'sys/message/email-record',
    title: '发送记录',
    pageType: 'CUSTOM',
    component: lazy(() => import('./EmailRecordPage')),
  },
  {
    componentKey: 'sys/message/email-record/detail',
    featureKey: 'sys/message/email-record',
    title: '邮件发送详情',
    pageType: 'CUSTOM',
    component: lazy(() => import('./EmailRecordDetailPage')),
  },
]);
