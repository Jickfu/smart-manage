import { gzipSync } from 'node:zlib';
import type { Plugin } from 'vite';
import { assetDirectory } from './build-output';
import { selectedDomains } from './selected-domains.mjs';

interface BuildChunk {
  fileName: string;
  moduleIds: string[];
  imports: string[];
  dynamicImports: string[];
  isEntry: boolean;
  isDynamicEntry: boolean;
  facadeModuleId: string | null;
  code: string;
}

/** 从最终 JS 依赖图核验加载边界；不把目录归类等同于按需加载。 */
export function auditChunks(chunks: BuildChunk[], pages: string[], domains: string[]) {
  const byFile = new Map(chunks.map((chunk) => [chunk.fileName, chunk]));
  const initial = new Set<string>();
  function visit(fileName: string, visited: Set<string>) {
    if (visited.has(fileName)) return;
    const chunk = byFile.get(fileName);
    if (!chunk) throw new Error(`构建依赖不存在：${fileName}`);
    visited.add(fileName);
    for (const dependency of chunk.imports) visit(dependency, visited);
  }
  const entries = chunks
    .filter((chunk) => chunk.isEntry && chunk.code.length > 0)
    .map((entry) => {
      const visited = new Set<string>();
      visit(entry.fileName, visited);
      for (const fileName of visited) initial.add(fileName);
      const dependencies = [...visited].map((fileName) => byFile.get(fileName)!);
      return {
        file: entry.fileName,
        jsFiles: visited.size,
        jsBytes: dependencies.reduce((total, chunk) => total + Buffer.byteLength(chunk.code), 0),
        gzipBytes: dependencies.reduce((total, chunk) => total + gzipSync(chunk.code).length, 0),
      };
    });
  for (const chunk of chunks) {
    for (const dependency of [...chunk.imports, ...chunk.dynamicImports]) {
      if (!byFile.has(dependency)) throw new Error(`构建依赖不存在：${dependency}`);
    }
    for (const moduleId of chunk.moduleIds) {
      const normalized = moduleId.replaceAll('\\', '/');
      const domain = normalized.match(/\/src\/domain\/([^/]+)\//)?.[1];
      if (domain && domain !== 'common' && !domains.includes(domain))
        throw new Error(`未选领域进入构建：${domain}`);
      if (
        initial.has(chunk.fileName) &&
        /\/node_modules\/(?:echarts|zrender|@codemirror|@lezer|codemirror)\//.test(normalized)
      )
        throw new Error(`重型图表或编辑器进入初始依赖：${moduleId}`);
    }
  }
  for (const page of pages) {
    const chunk = chunks.find((candidate) => candidate.facadeModuleId === page);
    if (!chunk?.isDynamicEntry || initial.has(chunk.fileName))
      throw new Error(`页面或首页未保持异步入口：${page}`);
    if (!chunk.fileName.startsWith(`${assetDirectory([page])}/`))
      throw new Error(`页面或首页输出目录错误：${page}`);
  }
  return { domains, lazyEntries: pages.length, entries };
}

export function buildAudit(): Plugin {
  return {
    name: 'smart-manage-build-audit',
    apply: 'build',
    generateBundle: {
      // 等待 Vite 完成预加载链接注入及空入口移除，报告最终写出的代码体积。
      order: 'post',
      handler(_options, bundle) {
        const pages = new Set<string>();
        for (const moduleId of this.getModuleIds()) {
          if (!/(?:pageRegistration|applicationHomes)\.tsx?$/.test(moduleId)) continue;
          const info = this.getModuleInfo(moduleId);
          if (!info) continue;
          for (const imported of info.dynamicallyImportedIds) pages.add(imported);
          // 同时检查误改为静态导入的页面，避免仅枚举动态入口漏掉回归。
          for (const imported of info.importedIds)
            if (/(?:Page|Home)\.tsx?$/.test(imported)) pages.add(imported);
        }
        if (pages.size === 0) throw new Error('未发现页面和首页注册入口');
        const chunks = Object.values(bundle).filter((output) => output.type === 'chunk');
        const domains = selectedDomains();
        const report = auditChunks(chunks, [...pages], domains);
        this.emitFile({
          type: 'asset',
          fileName: '.vite/build-report.json',
          source: JSON.stringify(report, null, 2),
        });
        this.info(`构建加载边界通过：${pages.size} 个异步页面/首页，领域 ${domains.join(',')}`);
      },
    },
  };
}
