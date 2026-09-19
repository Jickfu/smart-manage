import { lazy } from 'react';
import type { ApplicationHomeRegistration } from '@/domain/common/registry/applicationHomeRegistry';

export default [
  { appNumber: 'base', component: lazy(() => import('./base/home/BaseHome')) },
  { appNumber: 'monitor', component: lazy(() => import('./monitor/home/MonitorHome')) },
  { appNumber: 'message', component: lazy(() => import('./message/home/MessageHome')) },
  { appNumber: 'scheduler', component: lazy(() => import('./scheduler/home/SchedulerHome')) },
] satisfies readonly ApplicationHomeRegistration[];
