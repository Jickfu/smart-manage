import { createElement } from 'react';
import type { ReactNode } from 'react';
import ProcurementHome from '@/domain/scm/procurement/home/ProcurementHome';
import BaseHome from '@/domain/sys/base/home/BaseHome';
import MonitorHome from '@/domain/sys/monitor/home/MonitorHome';
import SchedulerHome from '@/domain/sys/scheduler/home/SchedulerHome';

const homes: Readonly<Record<string, ReactNode>> = {
  procurement: createElement(ProcurementHome),
  base: createElement(BaseHome),
  monitor: createElement(MonitorHome),
  scheduler: createElement(SchedulerHome),
};

export const resolveApplicationHome = (appNumber: string) => homes[appNumber];
