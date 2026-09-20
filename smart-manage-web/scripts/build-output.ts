/** 输出目录只描述代码归属，不改变动态 import 的加载边界。 */
export function assetDirectory(moduleIds: readonly string[]): string {
  const normalized = moduleIds.map((moduleId) => moduleId.replaceAll('\\', '/'));
  // 构建器虚拟辅助模块不改变第三方代码的归属。
  const sources = normalized.filter(
    (moduleId) => !moduleId.includes('/node_modules/') && /(?:^|\/)src\//.test(moduleId),
  );
  const applications = new Set<string>();
  for (const moduleId of sources) {
    const match = moduleId.match(/(?:^|\/)src\/domain\/([^/]+)\/([^/]+)\//);
    if (match && match[1] !== 'common' && match[2] !== 'common') {
      applications.add(`${match[1]}/${match[2]}`);
    }
  }
  // 混合多个应用的共享块不冒充某个应用的私有资源。
  if (applications.size === 1) return `assets/domains/${[...applications][0]}`;
  if (sources.length === 0 && normalized.some((moduleId) => moduleId.includes('/node_modules/')))
    return 'assets/vendor';
  return 'assets/shared';
}

export function chunkFileName(chunk: { isEntry: boolean; moduleIds: string[] }): string {
  const directory = chunk.isEntry ? 'assets/shell' : assetDirectory(chunk.moduleIds);
  return `${directory}/[name]-[hash].js`;
}

export function assetFileName(asset: { originalFileNames: string[] }): string {
  return `${assetDirectory(asset.originalFileNames)}/[name]-[hash][extname]`;
}
