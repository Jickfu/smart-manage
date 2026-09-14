import { readFileSync, readdirSync } from 'node:fs';
import { join, resolve } from 'node:path';
import { pathToFileURL } from 'node:url';
import ts from 'typescript';

/** 仅检查页脚渲染树；不遍历按钮事件、正文或其他组件的 footer。 */
export function inspectModalFooters(source, fileName = 'fixture.tsx') {
  const sourceFile = ts.createSourceFile(
    fileName,
    source,
    ts.ScriptTarget.Latest,
    true,
    ts.ScriptKind.TSX,
  );
  const host = ts.createCompilerHost({});
  host.getSourceFile = (requested) => (requested === fileName ? sourceFile : undefined);
  const program = ts.createProgram([fileName], { noResolve: true, noLib: true }, host);
  const checker = program.getTypeChecker();
  const violations = [];
  const imports = new Map();
  for (const statement of sourceFile.statements) {
    if (!ts.isImportDeclaration(statement)) continue;
    const moduleName = statement.moduleSpecifier.text;
    const clause = statement.importClause;
    if (clause?.name && /(?:^|\/)AppModal$/.test(moduleName)) {
      imports.set(clause.name.text, 'AppModal');
    }
    if (clause?.namedBindings && ts.isNamedImports(clause.namedBindings)) {
      for (const binding of clause.namedBindings.elements) {
        const original = (binding.propertyName ?? binding.name).text;
        if (moduleName === 'antd' || /(?:^|\/)PermissionActions$/.test(moduleName)) {
          imports.set(binding.name.text, original);
        }
      }
    }
  }
  const report = (node, message) => {
    const { line } = sourceFile.getLineAndCharacterOfPosition(node.getStart(sourceFile));
    violations.push(`${fileName}:${line + 1}: ${message}`);
  };
  function inspect(node, wrapped = false, visited = new Set()) {
    if (!node || visited.has(node)) return;
    const nextVisited = new Set(visited).add(node);
    if (ts.isIdentifier(node)) {
      // 使用符号绑定追踪局部变量，避免同名变量跨作用域误报；不执行函数或跨文件组件。
      const declaration = checker.getSymbolAtLocation(node)?.valueDeclaration;
      if (declaration && ts.isVariableDeclaration(declaration)) {
        inspect(declaration.initializer, wrapped, nextVisited);
      }
    } else if (ts.isJsxElement(node) || ts.isJsxSelfClosingElement(node)) {
      const opening = ts.isJsxElement(node) ? node.openingElement : node;
      const tag = opening.tagName.getText(sourceFile);
      const component = imports.get(tag);
      if (component === 'PermissionActions') {
        const properties = opening.attributes.properties;
        const groupedIndex = properties.findLastIndex(
          (attribute) => ts.isJsxAttribute(attribute) && attribute.name.text === 'grouped',
        );
        const initializer = properties[groupedIndex]?.initializer;
        if (
          !initializer ||
          !ts.isJsxExpression(initializer) ||
          initializer.expression?.kind !== ts.SyntaxKind.FalseKeyword ||
          properties.slice(groupedIndex + 1).some(ts.isJsxSpreadAttribute)
        ) {
          report(
            opening,
            'AppModal footer PermissionActions must set grouped={false} after spreads',
          );
        }
      }
      if (wrapped && (component === 'Button' || component === 'PermissionActions')) {
        report(
          opening,
          'AppModal footer buttons must not be wrapped in Space, Flex or div; use Fragment',
        );
      }
      if (ts.isJsxElement(node) && component !== 'Button' && component !== 'PermissionActions') {
        const nextWrapped =
          wrapped || tag === 'div' || component === 'Space' || component === 'Flex';
        for (const child of node.children) inspect(child, nextWrapped, nextVisited);
      }
    } else if (ts.isJsxFragment(node)) {
      for (const child of node.children) inspect(child, wrapped, nextVisited);
    } else if (
      ts.isJsxExpression(node) ||
      ts.isParenthesizedExpression(node) ||
      ts.isAsExpression(node) ||
      ts.isSatisfiesExpression(node)
    ) {
      inspect(node.expression, wrapped, nextVisited);
    } else if (ts.isConditionalExpression(node)) {
      inspect(node.whenTrue, wrapped, nextVisited);
      inspect(node.whenFalse, wrapped, nextVisited);
    } else if (ts.isBinaryExpression(node)) {
      inspect(node.left, wrapped, nextVisited);
      inspect(node.right, wrapped, nextVisited);
    } else if (ts.isArrayLiteralExpression(node)) {
      for (const element of node.elements) inspect(element, wrapped, nextVisited);
    }
  }
  function visit(node) {
    if (
      (ts.isJsxOpeningElement(node) || ts.isJsxSelfClosingElement(node)) &&
      imports.get(node.tagName.getText(sourceFile)) === 'AppModal'
    ) {
      for (const attribute of node.attributes.properties) {
        if (ts.isJsxAttribute(attribute) && attribute.name.text === 'footer')
          inspect(attribute.initializer);
      }
    }
    ts.forEachChild(node, visit);
  }
  visit(sourceFile);
  return violations;
}

export function inspectModalFooterDirectory(directory) {
  return readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    const fileName = join(directory, entry.name);
    if (entry.isDirectory()) return inspectModalFooterDirectory(fileName);
    return entry.name.endsWith('.tsx')
      ? inspectModalFooters(readFileSync(fileName, 'utf8'), fileName)
      : [];
  });
}

if (process.argv[1] && import.meta.url === pathToFileURL(resolve(process.argv[1])).href) {
  const violations = inspectModalFooterDirectory(resolve(process.argv[2] ?? 'src'));
  if (violations.length) {
    console.error(violations.join('\n'));
    process.exitCode = 1;
  } else {
    console.log('AppModal footer conventions passed.');
  }
}
