/**
 * 页面注册清单导入文件，由 pnpm gen:registry 自动生成，禁止手动修改。
 */

import type { ApplicationHomeRegistration } from './applicationHomeRegistry';
import domainHomes1 from '../../sys/applicationHomes';
import domainHomes2 from '../../demo/applicationHomes';
export const applicationHomes: readonly ApplicationHomeRegistration[] = [
  ...domainHomes1,
  ...domainHomes2,
];
