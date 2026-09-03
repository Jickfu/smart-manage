import { resolve } from 'node:path';
import {
  formatPageFrameworkViolations,
  inspectPageFramework,
  readPageFrameworkSources,
} from './page-framework-boundaries.mjs';

const sourceRoot = resolve(import.meta.dirname, '../src');
const violations = inspectPageFramework(readPageFrameworkSources(sourceRoot));
if (violations.length > 0) {
  console.error(formatPageFrameworkViolations(violations));
  process.exitCode = 1;
} else {
  console.log('Page Framework 目录与直接依赖边界校验通过。');
}
