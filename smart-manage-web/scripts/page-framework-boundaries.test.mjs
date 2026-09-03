import { resolve } from 'node:path';
import { describe, expect, it } from 'vitest';
import {
  formatPageFrameworkViolations,
  inspectPageFramework,
  readPageFrameworkSources,
} from './page-framework-boundaries.mjs';

const pageRoot = 'domain/common/page';
function fixture(entries = {}) {
  return {
    sourceRoot: resolve('/workspace/smart-manage-web/src'),
    files: new Map(
      Object.entries({
        [`${pageRoot}/EditPageShell.tsx`]: 'export function EditPageShell() {}',
        [`${pageRoot}/types.ts`]: 'export type PageType = string;',
        [`${pageRoot}/pageLayout.css`]: '',
        ...entries,
      }),
    ),
  };
}

function rules(snapshot) {
  return inspectPageFramework(snapshot).map((violation) => violation.rule.split(':')[0]);
}

describe('Page Framework 当前架构门禁', () => {
  it('缺少绝对源根时拒绝检查，不悄悄使用错误坐标', () => {
    expect(() => inspectPageFramework({ ...fixture(), sourceRoot: 'src' })).toThrow(
      '绝对 sourceRoot',
    );
    expect(() => inspectPageFramework({ ...fixture(), sourceRoot: undefined })).toThrow(
      '绝对 sourceRoot',
    );
  });

  it('当前真实 src 满足目录和直接依赖边界，并随 pnpm test 进入 CI', () => {
    const violations = inspectPageFramework(
      readPageFrameworkSources(resolve(import.meta.dirname, '../src')),
    );
    expect(formatPageFrameworkViolations(violations)).toBe('');
  });

  it('允许同族、具体页面族到共享能力，以及现有 component 间接复用', () => {
    expect(
      rules(
        fixture({
          [`${pageRoot}/edit/EditPage.tsx`]: `
        import './fields';
        import '../access/access';
        import '../command/useCommandMutation';
        import '../tab/tabKeys';
        import '../types';
        import '../EditPageShell';
        import '../pageLayout.css';
        import '../../component/RefSelector';
        export function EditPage() {}
      `,
          [`${pageRoot}/edit/fields.ts`]: 'export type Field = string;',
          [`${pageRoot}/access/access.ts`]: '',
          [`${pageRoot}/command/useCommandMutation.ts`]: '',
          [`${pageRoot}/tab/tabKeys.ts`]: '',
          [`${pageRoot}/list/ListTree.tsx`]: '',
          'domain/common/component/RefSelector.tsx': `import '../page/list/ListTree';`,
          'domain/example/Page.tsx': `import '@/domain/common/page/edit/EditPage';`,
        }),
      ),
    ).toEqual([]);
  });

  it.each(['edit', 'list', 'assignment'])('允许 %s 依赖其自身和共享文件', (family) => {
    expect(
      rules(
        fixture({
          [`${pageRoot}/${family}/Page.tsx`]: `import './helper'; import '../types'; import '../pageLayout.css';`,
          [`${pageRoot}/${family}/helper.ts`]: '',
        }),
      ),
    ).toEqual([]);
  });

  it('拒绝根级额外文件、额外能力目录（包括空目录）及缺失共享文件', () => {
    const snapshot = fixture({
      [`${pageRoot}/helper.ts`]: '',
      [`${pageRoot}/utils/helper.ts`]: '',
    });
    snapshot.directories = [`${pageRoot}/hooks`];
    snapshot.files.delete(`${pageRoot}/types.ts`);
    expect(rules(snapshot)).toEqual(
      expect.arrayContaining(['ROOT_FILE', 'CAPABILITY_DIRECTORY', 'MISSING_ROOT_FILE']),
    );
    expect(inspectPageFramework(snapshot).some((entry) => entry.source.endsWith('/hooks'))).toBe(
      true,
    );
  });

  it.each(['index.ts', 'index.tsx'])('拒绝所有层级的 %s 聚合入口', (name) => {
    expect(rules(fixture({ [`${pageRoot}/edit/nested/${name}`]: '' }))).toContain('INDEX_ENTRY');
  });

  it.each([
    '@/domain/common/page/useCommandMutation',
    '../common/page/edit/../useCommandMutation.ts',
    '@/domain/common/page/access.ts',
  ])('即使文件不存在也拒绝旧平铺入口 %s', (specifier) => {
    expect(
      rules(
        fixture({
          'domain/example/Page.tsx': `import { command } from '${specifier}';`,
        }),
      ),
    ).toContain('FLAT_ENTRY');
  });

  it.each(['@/domain/common/page', '@/domain/common/page/edit', '@/domain/common/page/edit/index'])(
    '拒绝目录或 index 入口 %s',
    (specifier) => {
      const violations = inspectPageFramework(
        fixture({
          [`${pageRoot}/edit/index.ts`]: '',
          'Page.tsx': `import '${specifier}';`,
        }),
      );
      expect(violations).toContainEqual(
        expect.objectContaining({
          source: 'Page.tsx',
          specifier,
          rule: expect.stringMatching(/^(DIRECT_FILE_IMPORT|INDEX_ENTRY):/),
        }),
      );
    },
  );

  it.each([
    ['edit', 'list'],
    ['edit', 'assignment'],
    ['list', 'edit'],
    ['list', 'assignment'],
    ['assignment', 'edit'],
    ['assignment', 'list'],
  ])('拒绝 %s 直接依赖 %s', (sourceFamily, targetFamily) => {
    expect(
      rules(
        fixture({
          [`${pageRoot}/${sourceFamily}/Page.tsx`]: `import '../${targetFamily}/Page';`,
          [`${pageRoot}/${targetFamily}/Page.tsx`]: '',
        }),
      ),
    ).toContain('CROSS_FAMILY');
  });

  it.each([
    'access/access.ts',
    'command/command.ts',
    'tab/tab.ts',
    'EditPageShell.tsx',
    'types.ts',
  ])('拒绝底层入口 %s 反向依赖页面族', (source) => {
    for (const family of ['edit', 'list', 'assignment']) {
      expect(
        rules(
          fixture({
            [`${pageRoot}/${source}`]: `import '@/domain/common/page/${family}/Page';`,
            [`${pageRoot}/${family}/Page.tsx`]: '',
          }),
        ),
      ).toContain('REVERSE_DEPENDENCY');
    }
  });

  it.each([
    `import type { Page } from '../list/Page';`,
    `type Page = import('../list/Page').Page;`,
    `const page = import('../list/Page');`,
    'const page = import(`../list/Page`);',
    `import '../list/ListPage.css';`,
    `export type { Page } from '../list/Page';`,
  ])('各种直接依赖语法均不能绕过边界：%s', (content) => {
    expect(
      rules(
        fixture({
          [`${pageRoot}/edit/Page.tsx`]: content,
          [`${pageRoot}/list/Page.tsx`]: '',
          [`${pageRoot}/list/ListPage.css`]: '',
        }),
      ),
    ).toContain('CROSS_FAMILY');
  });

  it.each([
    `export * from '@/domain/common/page/edit/Page';`,
    `export { Page } from './domain/common/page/edit/Page';`,
    `export type { Page } from '@/domain/common/page/edit/Page';`,
  ])('拒绝在 page 外建立替代导出入口：%s', (content) => {
    expect(
      rules(
        fixture({
          'replacement.ts': content,
          [`${pageRoot}/edit/Page.tsx`]: '',
        }),
      ),
    ).toContain('REEXPORT_ENTRY');
  });

  it('不禁止与 Page Framework 无关的业务模块导出', () => {
    expect(
      rules(
        fixture({
          'domain/example/refSelector/index.ts': `export { selector } from './selector';`,
          'domain/example/refSelector/selector.ts': '',
        }),
      ),
    ).toEqual([]);
  });

  it('别名、相对路径、显式扩展名和查询后缀得到同一物理分类', () => {
    for (const specifier of [
      '@/domain/common/page/list/./Page',
      '../list/../list/Page.tsx',
      '../list/Page.js',
      '../list/Page?raw',
    ]) {
      const violations = inspectPageFramework(
        fixture({
          [`${pageRoot}/edit/Page.tsx`]: `\nimport '${specifier}';`,
          [`${pageRoot}/list/Page.tsx`]: '',
        }),
      );
      expect(violations).toContainEqual(
        expect.objectContaining({
          source: `${pageRoot}/edit/Page.tsx`,
          line: 2,
          specifier,
          target: `${pageRoot}/list/Page.tsx`,
          rule: expect.stringMatching(/^CROSS_FAMILY:/),
        }),
      );
      expect(formatPageFrameworkViolations(violations)).toContain(`${pageRoot}/edit/Page.tsx:2`);
    }
  });

  it('拒绝非法能力和缺失的能力目标', () => {
    expect(
      rules(
        fixture({
          'Page.tsx': `import '@/domain/common/page/utils/helper'; import '@/domain/common/page/edit/missing';`,
        }),
      ),
    ).toEqual(expect.arrayContaining(['CAPABILITY_IMPORT', 'MISSING_TARGET']));
  });

  it.each([
    '@//domain/common/page/list/Page',
    '@/../src/domain/common/page/list/Page',
    '../../../../../src/domain/common/page/list/Page',
    '@/../../smart-manage-web/src/domain/common/page/list/Page',
    '../../../../../../smart-manage-web/src/domain/common/page/list/Page',
  ])('完整源根坐标阻止离开后重入绕过：%s', (specifier) => {
    const violations = inspectPageFramework(
      fixture({
        [`${pageRoot}/edit/Test.ts`]: `import '${specifier}';`,
        [`${pageRoot}/list/Page.ts`]: '',
      }),
    );
    expect(violations).toContainEqual(
      expect.objectContaining({
        source: `${pageRoot}/edit/Test.ts`,
        specifier,
        target: `${pageRoot}/list/Page.ts`,
        rule: expect.stringMatching(/^CROSS_FAMILY:/),
      }),
    );
  });

  it.each([
    '@//domain/common/page/list/Page',
    '@/../../smart-manage-web/src/domain/common/page/list/Page',
    '../../../src/domain/common/page/list/Page',
  ])('page 外重入路径也不能重新导出框架：%s', (specifier) => {
    expect(
      rules(
        fixture({
          'domain/example/replacement.ts': `export { Page } from '${specifier}';`,
          [`${pageRoot}/list/Page.ts`]: '',
        }),
      ),
    ).toContain('REEXPORT_ENTRY');
  });

  it.each([
    '@/../outside/domain/common/page/list/Page',
    '../../../../../outside/domain/common/page/list/Page',
    '@/../src-other/domain/common/page/list/Page',
  ])('真正位于 src 外的路径不误判为页面族依赖：%s', (specifier) => {
    expect(
      rules(
        fixture({
          [`${pageRoot}/edit/Test.ts`]: `import '${specifier}';`,
          [`${pageRoot}/list/Page.ts`]: '',
        }),
      ),
    ).toEqual([]);
  });

  it('明确不分析非字面量 dynamic import、CSS 内部 @import 或普通字符串', () => {
    expect(
      rules(
        fixture({
          [`${pageRoot}/edit/Page.tsx`]: `
        const path = '../list/Page';
        const page = import(path);
        // import '../list/Page';
      `,
          [`${pageRoot}/edit/Page.css`]: '@import "../list/ListPage.css";',
        }),
      ),
    ).toEqual([]);
  });
});
