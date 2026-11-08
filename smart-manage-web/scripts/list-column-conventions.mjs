import { readdirSync, readFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import ts from 'typescript';

const sourceRoot = resolve(dirname(fileURLToPath(import.meta.url)), '../src');

/** 只解析列表列声明，不给运行中的表格增加默认属性或行高限制。 */
export function inspectListColumns(source, filename) {
  const ast = ts.createSourceFile(
    filename,
    source,
    ts.ScriptTarget.Latest,
    true,
    ts.ScriptKind.TSX,
  );
  const listNames = new Set();
  for (const statement of ast.statements) {
    if (!ts.isImportDeclaration(statement) || !ts.isStringLiteral(statement.moduleSpecifier))
      continue;
    const specifier = statement.moduleSpecifier.text.replace(/\.(tsx?|jsx?)$/, '');
    const target = specifier.startsWith('@/')
      ? resolve(sourceRoot, specifier.slice(2))
      : resolve(dirname(filename), specifier);
    if (
      target === resolve(sourceRoot, 'domain/common/page/list/ListPage') &&
      statement.importClause?.name
    ) {
      listNames.add(statement.importClause.name.text);
    }
  }
  const declarations = new Map();
  function collect(node) {
    if (ts.isVariableDeclaration(node) && ts.isIdentifier(node.name) && node.initializer) {
      declarations.set(node.name.text, node.initializer);
    }
    ts.forEachChild(node, collect);
  }
  collect(ast);
  const columns = new Set();
  const unresolved = [];
  function inspectExpression(expression, seen = new Set()) {
    if (!expression || seen.has(expression)) return;
    const nextSeen = new Set(seen).add(expression);
    if (ts.isIdentifier(expression)) {
      const declaration = declarations.get(expression.text);
      if (declaration) inspectExpression(declaration, nextSeen);
      else unresolved.push(expression);
    } else if (ts.isArrayLiteralExpression(expression)) {
      expression.elements.forEach((element) => inspectExpression(element, nextSeen));
    } else if (ts.isObjectLiteralExpression(expression)) {
      const children = expression.properties.find(
        (property) =>
          ts.isPropertyAssignment(property) && property.name.getText(ast) === 'children',
      );
      if (children) inspectExpression(children.initializer, nextSeen);
      else columns.add(expression);
    } else if (
      ts.isParenthesizedExpression(expression) ||
      ts.isAsExpression(expression) ||
      ts.isSatisfiesExpression(expression) ||
      ts.isSpreadElement(expression)
    ) {
      inspectExpression(expression.expression, nextSeen);
    } else if (ts.isConditionalExpression(expression)) {
      inspectExpression(expression.whenTrue, nextSeen);
      inspectExpression(expression.whenFalse, nextSeen);
    } else if (ts.isArrowFunction(expression) || ts.isFunctionExpression(expression)) {
      if (ts.isBlock(expression.body)) {
        expression.body.statements
          .filter(ts.isReturnStatement)
          .forEach((statement) => inspectExpression(statement.expression, nextSeen));
      } else inspectExpression(expression.body, nextSeen);
    } else if (
      ts.isCallExpression(expression) &&
      expression.expression.getText(ast) === 'useMemo'
    ) {
      inspectExpression(expression.arguments[0], nextSeen);
    } else unresolved.push(expression);
  }
  function visit(node) {
    if (
      (ts.isJsxOpeningElement(node) || ts.isJsxSelfClosingElement(node)) &&
      listNames.has(node.tagName.getText(ast))
    ) {
      const attribute = node.attributes.properties.find(
        (property) => ts.isJsxAttribute(property) && property.name.getText(ast) === 'columns',
      );
      if (attribute?.initializer && ts.isJsxExpression(attribute.initializer)) {
        inspectExpression(attribute.initializer.expression);
      }
    }
    ts.forEachChild(node, visit);
  }
  visit(ast);
  const violations = unresolved.map((node) => ({
    node,
    reason: '列表 columns 必须使用可核对的同文件列声明',
  }));
  for (const column of columns) {
    const ellipsis = column.properties.find(
      (property) => ts.isPropertyAssignment(property) && property.name.getText(ast) === 'ellipsis',
    );
    if (!ellipsis) violations.push({ node: column, reason: '列表叶子列必须显式声明 ellipsis' });
    else if (
      ![
        ts.SyntaxKind.TrueKeyword,
        ts.SyntaxKind.FalseKeyword,
        ts.SyntaxKind.ObjectLiteralExpression,
      ].includes(ellipsis.initializer.kind)
    ) {
      violations.push({
        node: ellipsis,
        reason: 'ellipsis 必须显式使用 true、false 或标准配置对象',
      });
    } else if (
      ellipsis.initializer.kind === ts.SyntaxKind.FalseKeyword &&
      !/[\u4e00-\u9fff]/.test(
        ellipsis.getFullText(ast).slice(0, ellipsis.getLeadingTriviaWidth(ast)),
      )
    ) {
      violations.push({
        node: ellipsis,
        reason: 'ellipsis: false 必须在属性前用中文注释说明业务原因',
      });
    }
  }
  return { ast, columns: [...columns], violations };
}

export function verifyListColumns(root = sourceRoot) {
  const violations = [];
  function scan(directory) {
    for (const entry of readdirSync(directory, { withFileTypes: true })) {
      const filename = join(directory, entry.name);
      if (entry.isDirectory()) scan(filename);
      else if (filename.endsWith('.tsx') && !filename.endsWith('.test.tsx')) {
        const result = inspectListColumns(readFileSync(filename, 'utf8'), filename);
        for (const violation of result.violations) {
          const line = result.ast.getLineAndCharacterOfPosition(violation.node.getStart()).line + 1;
          violations.push(`${filename}:${line}: ${violation.reason}`);
        }
      }
    }
  }
  scan(root);
  return violations;
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  const violations = verifyListColumns(process.argv[2] ? resolve(process.argv[2]) : sourceRoot);
  if (violations.length) {
    console.error(violations.join('\n'));
    process.exitCode = 1;
  } else console.log('List column ellipsis conventions passed.');
}
