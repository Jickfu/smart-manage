// @vitest-environment node
import { afterEach, describe, expect, it, vi } from 'vitest';
import { assetDirectory, assetFileName, chunkFileName } from './build-output';
import { auditChunks, buildAudit } from './build-audit';

const page = '/project/src/domain/sys/base/user/UserListPage.tsx';
const home = 'C:\\project\\src\\domain\\demo\\procurement\\home\\ProcurementHome.tsx';
const react = '/project/node_modules/.pnpm/react@19/node_modules/react/index.js';

afterEach(() => vi.unstubAllEnvs());

describe('构建输出归属', () => {
  it('同应用合并 CSS 保留应用归属，跨应用合并进入 shared', () => {
    const sources = ['src/domain/sys/base/user/style.css', 'src/domain/sys/base/role/style.css'];
    expect(assetFileName({ originalFileNames: sources })).toBe(
      'assets/domains/sys/base/[name]-[hash][extname]',
    );
    expect(
      assetFileName({ originalFileNames: [...sources, 'src/domain/sys/monitor/home/style.css'] }),
    ).toBe('assets/shared/[name]-[hash][extname]');
  });
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

interface ModuleImports {
  importedIds: string[];
  dynamicallyImportedIds: string[];
}

/** 直接调用真实插件 hook，只替换构建器上下文；不在测试中另写入口发现逻辑。 */
function runBuildHook(modules: Record<string, ModuleImports>, chunks = fixture()) {
  vi.stubEnv('SMART_MANAGE_DOMAINS', 'sys');
  const plugin = buildAudit();
  const hook = plugin.generateBundle;
  if (!hook || typeof hook === 'function') throw new Error('预期带顺序的 generateBundle hook');
  expect(plugin.apply).toBe('build');
  expect(hook.order).toBe('post');
  const emitFile = vi.fn();
  const context = {
    getModuleIds: () => Object.keys(modules).values(),
    getModuleInfo: (moduleId: string) => modules[moduleId],
    emitFile,
    info: vi.fn(),
  };
  const bundle = Object.fromEntries(
    chunks.map((chunk) => [chunk.fileName, { ...chunk, type: 'chunk' }]),
  );
  Reflect.apply(hook.handler, context, [{}, bundle, false]);
  expect(emitFile).toHaveBeenCalledTimes(1);
  return JSON.parse(emitFile.mock.calls[0][0].source);
}

describe('插件注册入口发现', () => {
  it.each([
    'pageRegistration.ts',
    'pageRegistration.tsx',
    'applicationHomes.ts',
    'applicationHomes.tsx',
  ])('从 %s 的直接动态导入发现入口并去重', (registration) => {
    const modules = {
      [`/project/src/domain/sys/${registration}`]: {
        importedIds: [react],
        dynamicallyImportedIds: [page, page],
      },
      '/project/src/unrelated.ts': {
        importedIds: [],
        dynamicallyImportedIds: ['/missing/Ignored.tsx'],
      },
    };
    expect(runBuildHook(modules).lazyEntries).toBe(1);
  });
  it.each(['UserListPage', 'BaseHome'])('发现误改为直接静态导入的 %s 并拒绝构建', (component) => {
    const target = `/project/src/domain/sys/base/${component}.tsx`;
    const chunks = fixture();
    chunks[2].facadeModuleId = target;
    chunks[2].moduleIds = [target];
    chunks[2].isDynamicEntry = false;
    const modules = {
      '/project/src/domain/sys/pageRegistration.ts': {
        importedIds: [target],
        dynamicallyImportedIds: [],
      },
    };
    expect(() => runBuildHook(modules, chunks)).toThrow(`页面或首页未保持异步入口：${target}`);
  });
  it('注册清单没有可识别入口时失败，不能生成空报告', () => {
    expect(() =>
      runBuildHook({
        '/project/src/domain/sys/pageRegistration.ts': {
          importedIds: [react],
          dynamicallyImportedIds: [],
        },
      }),
    ).toThrow('未发现页面和首页注册入口');
  });
  it('明确边界：不递归识别中间文件转导出，也不识别其他命名的静态组件', () => {
    const barrel = '/project/src/domain/sys/base/entries.ts';
    const renamed = '/project/src/domain/sys/base/UserScreen.tsx';
    const indirect = '/project/src/domain/sys/base/IndirectPage.tsx';
    // 保留一个正常入口，避免非零检查掩盖另外两种入口没有被收集的事实。
    const modules = {
      '/project/src/domain/sys/pageRegistration.ts': {
        importedIds: [barrel, renamed],
        dynamicallyImportedIds: [page],
      },
      [barrel]: { importedIds: [indirect], dynamicallyImportedIds: [] },
    };
    expect(runBuildHook(modules).lazyEntries).toBe(1);
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
