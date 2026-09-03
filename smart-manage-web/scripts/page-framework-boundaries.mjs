import { readdirSync, readFileSync } from 'node:fs';
import { isAbsolute, join, posix, relative, resolve, sep } from 'node:path';
import ts from 'typescript';

const pageRoot = 'domain/common/page';
const rootFiles = new Set(['EditPageShell.tsx', 'pageLayout.css', 'types.ts']);
const pageFamilies = new Set(['edit', 'list', 'assignment']);
const capabilities = new Set([...pageFamilies, 'access', 'command', 'tab']);

function pagePath(file) {
  if (file === pageRoot) return '';
  return file.startsWith(`${pageRoot}/`) ? file.slice(pageRoot.length + 1) : null;
}

function familyOf(file) {
  const relative = pagePath(file);
  return relative === null ? null : relative.split('/')[0];
}

// 先做词法规范化：已删除的旧入口也必须报架构错误，不能因解析失败而漏检。
function canonicalPath(sourceRoot, source, specifier) {
  const modulePath = specifier.split(/[?#]/)[0];
  let target;
  // 保留 src 以上的真实祖先，先离开再进入 src 的合法路径也要得到同一分类。
  // 必须用 join：resolve 会把 @// 的剩余前导斜杠当成新的绝对根。
  if (modulePath.startsWith('@/')) {
    target = join(sourceRoot, modulePath.slice(2));
  } else if (modulePath.startsWith('.')) {
    target = join(sourceRoot, posix.dirname(source), modulePath);
  } else {
    return null;
  }
  const relativeTarget = relative(sourceRoot, target);
  if (
    relativeTarget === '..' ||
    relativeTarget.startsWith(`..${sep}`) ||
    isAbsolute(relativeTarget)
  ) {
    return null;
  }
  return relativeTarget.split(sep).join('/');
}

function resolveTarget(canonical, files) {
  const candidates = [canonical, `${canonical}.ts`, `${canonical}.tsx`];
  // TypeScript 允许源码用 .js 引用实际的 .ts/.tsx 模块。
  if (canonical.endsWith('.js')) {
    candidates.push(canonical.slice(0, -3) + '.ts', canonical.slice(0, -3) + '.tsx');
  }
  candidates.push(`${canonical}/index.ts`, `${canonical}/index.tsx`);
  return candidates.find((candidate) => files.has(candidate)) ?? canonical;
}

function moduleReferences(sourceFile) {
  const references = [];
  function visit(node) {
    let literal;
    let reexport = false;
    if (ts.isImportDeclaration(node) || ts.isExportDeclaration(node)) {
      literal = node.moduleSpecifier;
      reexport = ts.isExportDeclaration(node);
    } else if (ts.isImportTypeNode(node) && ts.isLiteralTypeNode(node.argument)) {
      literal = node.argument.literal;
    } else if (ts.isCallExpression(node) && node.expression.kind === ts.SyntaxKind.ImportKeyword) {
      literal = node.arguments[0];
    }
    if (literal && (ts.isStringLiteral(literal) || ts.isNoSubstitutionTemplateLiteral(literal))) {
      references.push({
        specifier: literal.text,
        line: sourceFile.getLineAndCharacterOfPosition(literal.getStart(sourceFile)).line + 1,
        reexport,
      });
    }
    ts.forEachChild(node, visit);
  }
  visit(sourceFile);
  return references;
}

/** 输入路径统一相对 src，使用正斜杠；规则只检查当前结构，不依赖 Git 历史。 */
export function inspectPageFramework({ sourceRoot, files, directories = [] }) {
  if (typeof sourceRoot !== 'string' || !isAbsolute(sourceRoot)) {
    throw new Error('Page Framework 检查需要绝对 sourceRoot，不能丢失项目祖先坐标。');
  }
  const violations = [];
  function report(source, rule, reference = {}) {
    violations.push({ source, line: 1, specifier: '', target: source, ...reference, rule });
  }

  const allDirectories = new Set(directories);
  for (const file of files.keys()) {
    let parent = posix.dirname(file);
    while (parent !== '.') {
      allDirectories.add(parent);
      parent = posix.dirname(parent);
    }
    const relative = pagePath(file);
    if (relative === null) continue;
    if (!relative.includes('/') && !rootFiles.has(relative)) {
      report(file, 'ROOT_FILE: page 根级只能保留三个共享文件');
    }
    if (/^index\.tsx?$/.test(posix.basename(file))) {
      report(file, 'INDEX_ENTRY: page 内禁止 index.ts/tsx 聚合入口');
    }
  }
  for (const directory of allDirectories) {
    const relative = pagePath(directory);
    if (relative && !relative.includes('/') && !capabilities.has(relative)) {
      report(directory, 'CAPABILITY_DIRECTORY: page 只允许六个能力目录');
    }
  }
  for (const rootFile of rootFiles) {
    if (!files.has(`${pageRoot}/${rootFile}`)) {
      report(`${pageRoot}/${rootFile}`, 'MISSING_ROOT_FILE: 缺少共享框架文件');
    }
  }

  for (const [source, content] of files) {
    if (!/\.tsx?$/.test(source)) continue;
    const sourceFile = ts.createSourceFile(source, content, ts.ScriptTarget.Latest, true);
    const sourceFamily = familyOf(source);
    for (const reference of moduleReferences(sourceFile)) {
      const canonical = canonicalPath(sourceRoot, source, reference.specifier);
      if (canonical === null || pagePath(canonical) === null) continue;
      const target = resolveTarget(canonical, files);
      const detail = { line: reference.line, specifier: reference.specifier, target };
      const relative = pagePath(target);
      const targetFamily = familyOf(target);

      if (reference.reexport) {
        report(source, 'REEXPORT_ENTRY: 不得重新导出 Page Framework 制造替代入口', detail);
      }
      if (canonical === pageRoot || capabilities.has(pagePath(canonical))) {
        report(source, 'DIRECT_FILE_IMPORT: 必须直接引用能力文件，不能引用目录入口', detail);
      } else if (!relative.includes('/') && !rootFiles.has(relative)) {
        report(source, 'FLAT_ENTRY: 禁止旧平铺入口或额外根级入口', detail);
      } else if (relative.includes('/') && !capabilities.has(targetFamily)) {
        report(source, 'CAPABILITY_IMPORT: 引用了非法能力目录', detail);
      } else if (/^index\.tsx?$/.test(posix.basename(target))) {
        report(source, 'INDEX_ENTRY: 不得引用 page 聚合入口', detail);
      } else if (!files.has(target)) {
        report(source, 'MISSING_TARGET: Page Framework 引用目标不存在', detail);
      }

      if (sourceFamily !== null && pageFamilies.has(targetFamily)) {
        if (pageFamilies.has(sourceFamily)) {
          if (sourceFamily !== targetFamily) {
            report(source, 'CROSS_FAMILY: 具体页面族之间不得直接依赖', detail);
          }
        } else {
          report(source, 'REVERSE_DEPENDENCY: 底层能力和共享入口不得反向依赖具体页面族', detail);
        }
      }
    }
  }
  return violations;
}

/** 读取文件清单与 TS/TSX 内容；不解析 CSS 内部依赖，不计算传递依赖闭包。 */
export function readPageFrameworkSources(sourceRoot) {
  const absoluteSourceRoot = resolve(sourceRoot);
  const files = new Map();
  const directories = [];
  function visit(relative) {
    for (const entry of readdirSync(join(absoluteSourceRoot, relative), { withFileTypes: true })) {
      const file = posix.join(relative, entry.name);
      if (entry.isDirectory()) {
        directories.push(file);
        visit(file);
      } else if (entry.isFile()) {
        files.set(
          file,
          /\.tsx?$/.test(file) ? readFileSync(join(absoluteSourceRoot, file), 'utf8') : '',
        );
      }
    }
  }
  visit('');
  return { sourceRoot: absoluteSourceRoot, files, directories };
}

export function formatPageFrameworkViolations(violations) {
  return violations
    .map(
      ({ source, line, specifier, target, rule }) =>
        `${source}:${line} [${rule}] ${JSON.stringify(specifier)} -> ${target}`,
    )
    .join('\n');
}
