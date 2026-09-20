// @vitest-environment node
import { describe, expect, it } from 'vitest';
import { assetDirectory, assetFileName, chunkFileName } from './build-output';
import { auditChunks } from './build-audit';

const page = '/project/src/domain/sys/base/user/UserListPage.tsx';
const home = 'C:\\project\\src\\domain\\demo\\procurement\\home\\ProcurementHome.tsx';
const react = '/project/node_modules/.pnpm/react@19/node_modules/react/index.js';

describe('构建输出归属', () => {
  it('兼容 Windows、POSIX 和相对源码路径，保留内容哈希', () => {
    expect(assetDirectory([page])).toBe('assets/domains/sys/base');
    expect(assetDirectory([home])).toBe('assets/domains/demo/procurement');
    expect(assetFileName({ originalFileNames: ['src/domain/sys/base/user/style.css'] })).toBe(
      'assets/domains/sys/base/[name]-[hash][extname]',
    );
    expect(chunkFileName({ isEntry: false, moduleIds: [page] })).toBe(
      'assets/domains/sys/base/[name]-[hash].js',
    );
  });
  it('跨应用代码落公共目录，框架辅助模块不影响 vendor 归属', () => {
    expect(assetDirectory([page, home])).toBe('assets/shared');
    expect(assetDirectory(['/project/src/domain/common/chart/SmChart.tsx', react])).toBe(
      'assets/shared',
    );
    expect(assetDirectory([react, '\0rolldown:runtime'])).toBe('assets/vendor');
    expect(assetDirectory([])).toBe('assets/shared');
    expect(chunkFileName({ isEntry: true, moduleIds: [page] })).toBe(
      'assets/shell/[name]-[hash].js',
    );
  });
});

function fixture() {
  return [
    {
      fileName: 'assets/shell/main.js',
      moduleIds: ['/project/src/main.tsx'],
      imports: ['assets/vendor/react.js'],
      dynamicImports: ['assets/domains/sys/base/UserListPage.js'],
      isEntry: true,
      isDynamicEntry: false,
      facadeModuleId: '/project/index.html',
      code: 'main',
    },
    {
      fileName: 'assets/vendor/react.js',
      moduleIds: [react],
      imports: [],
      dynamicImports: [],
      isEntry: false,
      isDynamicEntry: false,
      facadeModuleId: null,
      code: 'react',
    },
    {
      fileName: 'assets/domains/sys/base/UserListPage.js',
      moduleIds: [page],
      imports: ['assets/vendor/react.js'],
      dynamicImports: [],
      isEntry: false,
      isDynamicEntry: true,
      facadeModuleId: page,
      code: 'page',
    },
  ];
}

describe('最终构建加载边界', () => {
  it('只统计静态依赖闭包，允许循环引用且不重复计数', () => {
    const chunks = fixture();
    chunks[1].imports.push(chunks[0].fileName);
    const report = auditChunks(chunks, [page], ['sys']);
    expect(report.lazyEntries).toBe(1);
    expect(report.entries[0]).toMatchObject({ jsFiles: 2, jsBytes: 9 });
  });
  it('拒绝页面通过间接依赖提前加载或丢失异步入口', () => {
    const chunks = fixture();
    chunks[1].imports.push(chunks[2].fileName);
    expect(() => auditChunks(chunks, [page], ['sys'])).toThrow('未保持异步入口');
    expect(() => auditChunks(fixture(), ['/missing/Page.tsx'], ['sys'])).toThrow('未保持异步入口');
  });
  it('拒绝归属错误、丢失引用和藏在共享块中的未选领域', () => {
    const chunks = fixture();
    chunks[2].moduleIds.push(home);
    expect(() => auditChunks(chunks, [page], ['sys'])).toThrow('未选领域');
    chunks[0].dynamicImports.push('missing.js');
    expect(() => auditChunks(chunks, [page], ['sys', 'demo'])).toThrow('构建依赖不存在');
    const misplaced = fixture();
    misplaced[2].fileName = 'assets/shared/UserListPage.js';
    misplaced[0].dynamicImports = [misplaced[2].fileName];
    expect(() => auditChunks(misplaced, [page], ['sys'])).toThrow('输出目录错误');
  });
  it('图表和编辑器可以异步加载，但不能进入初始依赖链', () => {
    for (const dependency of [
      'echarts',
      'zrender',
      '@codemirror/view',
      '@lezer/common',
      'codemirror',
    ]) {
      const chunks = fixture();
      const heavyModule = `/project/node_modules/${dependency}/index.js`;
      chunks[2].moduleIds.push(heavyModule);
      expect(() => auditChunks(chunks, [page], ['sys'])).not.toThrow();
      chunks[1].moduleIds.push(heavyModule);
      expect(() => auditChunks(chunks, [page], ['sys'])).toThrow('重型图表或编辑器');
    }
  });
});
