import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.inboxBroadcast,
    featureKey: 'sys/message/inbox-broadcast',
    title: '消息发布',
    pageType: 'LIST',
    component: lazy(() => import('./InboxMessageListPage')),
  },
  {
    componentKey: componentKeys.inboxBroadcastEdit,
    featureKey: 'sys/message/inbox-broadcast',
    title: '消息发布',
    pageType: 'EDIT',
    component: lazy(() => import('./InboxMessageEditPage')),
  },
]);
