import { lazy } from 'react';
import type { ApplicationHomeRegistration } from '@/domain/common/registry/applicationHomeRegistry';

export default [
  { appNumber: 'procurement', component: lazy(() => import('./procurement/home/ProcurementHome')) },
] satisfies readonly ApplicationHomeRegistration[];
