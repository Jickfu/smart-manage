import { createElement } from 'react';
import type { ReactNode } from 'react';
import { applicationHomes } from '@/domain/common/registry/applicationHomes.gen';

const homes = new Map<string, ReactNode>();
for (const registration of applicationHomes) {
  if (homes.has(registration.appNumber)) throw new Error(`重复应用首页：${registration.appNumber}`);
  homes.set(registration.appNumber, createElement(registration.component));
}
export const resolveApplicationHome = (appNumber: string) => homes.get(appNumber);
