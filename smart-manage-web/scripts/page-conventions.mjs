import { readFileSync, readdirSync } from 'node:fs';
import { join, resolve } from 'node:path';
import { pathToFileURL } from 'node:url';
import ts from 'typescript';

/** 检查能够从当前文件确定的模板接入，不推断普通 Select 是否具有业务引用语义。 */
export function inspectPageConventions(source, filename = 'fixture.tsx') {
  const ast = ts.createSourceFile(
    filename,
    source,
    ts.ScriptTarget.Latest,
    true,
    ts.ScriptKind.TSX,
  );
  const imports = new Map();
  for (const statement of ast.statements) {
    if (!ts.isImportDeclaration(statement) || !ts.isStringLiteral(statement.moduleSpecifier))
      continue;
    const module = statement.moduleSpecifier.text;
    const clause = statement.importClause;
    if (clause?.name && /(?:^|\/)ModalEditPage$/.test(module))
      imports.set(clause.name.text, 'ModalEditPage');
    if (clause?.name && /(?:^|\/)(ScriptEditor|SqlEditor)$/.test(module))
      imports.set(clause.name.text, 'CodeEditor');
    if (clause?.namedBindings && ts.isNamedImports(clause.namedBindings)) {
      for (const binding of clause.namedBindings.elements) {
        const original = (binding.propertyName ?? binding.name).text;
        if (module === 'antd' && original === 'Form') imports.set(binding.name.text, 'Form');
        if (/(?:^|\/)FormFieldLayout$/.test(module) && original === 'FormFieldCell') {
          imports.set(binding.name.text, 'FormFieldCell');
        }
      }
    }
  }
  const violations = [];
  const report = (node, message) => {
    const line = ast.getLineAndCharacterOfPosition(node.getStart(ast)).line + 1;
    violations.push(`${filename}:${line}: ${message}`);
  };
  const attribute = (opening, name) =>
    opening.attributes.properties.find(
      (property) => ts.isJsxAttribute(property) && property.name.getText(ast) === name,
    );
  function titleParts(node) {
    if (!node) return [];
    if (ts.isStringLiteral(node) || ts.isNoSubstitutionTemplateLiteral(node)) return [node.text];
    if (ts.isJsxExpression(node) || ts.isParenthesizedExpression(node))
      return titleParts(node.expression);
    if (ts.isConditionalExpression(node))
      return [...titleParts(node.whenTrue), ...titleParts(node.whenFalse)];
    if (ts.isTemplateExpression(node))
      // 保留未知插值的位置，内部片段不能被当成整个标题的前后缀。
      return [
        node.head.text + node.templateSpans.map((span) => `\u0000${span.literal.text}`).join(''),
      ];
    return [];
  }
  function hasRequiredContentClass(node) {
    if (!node) return false;
    if (ts.isJsxExpression(node) || ts.isParenthesizedExpression(node))
      return hasRequiredContentClass(node.expression);
    // 条件的每条可达分支都必须满足字段布局契约。
    if (ts.isConditionalExpression(node))
      return hasRequiredContentClass(node.whenTrue) && hasRequiredContentClass(node.whenFalse);
    return titleParts(node).some((value) => value.split(/\s+/).includes('sm-edit-field-content'));
  }
  function visit(node) {
    if (ts.isJsxOpeningElement(node) || ts.isJsxSelfClosingElement(node)) {
      const tag = node.tagName.getText(ast);
      if (imports.get(tag) === 'ModalEditPage') {
        const title = attribute(node, 'title');
        if (
          titleParts(title?.initializer).some((part) => /^(新增|编辑|查看|详情)|详情$/.test(part))
        ) {
          report(title, 'ModalEditPage 标题只使用基础名称，不添加操作前缀或详情后缀');
        }
      }
      if (
        ts.isPropertyAccessExpression(node.tagName) &&
        node.tagName.name.text === 'Item' &&
        imports.get(node.tagName.expression.getText(ast)) === 'Form' &&
        attribute(node, 'label')
      ) {
        const element = ts.isJsxOpeningElement(node) ? node.parent : node;
        // 代码编辑器是页面指南明确定义的专用布局，不套普通字段轨道。
        let codeEditor = false;
        function findEditor(child) {
          if (
            (ts.isJsxOpeningElement(child) || ts.isJsxSelfClosingElement(child)) &&
            imports.get(child.tagName.getText(ast)) === 'CodeEditor'
          )
            codeEditor = true;
          ts.forEachChild(child, findEditor);
        }
        ts.forEachChild(element, findEditor);
        if (codeEditor) return;
        const parent = element.parent;
        const cell =
          ts.isJsxElement(parent) &&
          imports.get(parent.openingElement.tagName.getText(ast)) === 'FormFieldCell';
        if (!cell) report(node, '带标签的普通 Form.Item 必须直接放在 FormFieldCell 中');
        const className = attribute(node, 'className');
        if (!hasRequiredContentClass(className?.initializer)) {
          report(node, '普通字段必须显式使用 sm-edit-field-content');
        }
      }
    }
    ts.forEachChild(node, visit);
  }
  visit(ast);
  return violations;
}

export function verifyPageConventions(root) {
  return readdirSync(root, { withFileTypes: true }).flatMap((entry) => {
    const filename = join(root, entry.name);
    if (entry.isDirectory()) return verifyPageConventions(filename);
    return filename.endsWith('.tsx') && !filename.endsWith('.test.tsx')
      ? inspectPageConventions(readFileSync(filename, 'utf8'), filename)
      : [];
  });
}

if (process.argv[1] && pathToFileURL(resolve(process.argv[1])).href === import.meta.url) {
  const violations = verifyPageConventions(resolve(process.argv[2] ?? 'src/domain'));
  if (violations.length) {
    console.error(violations.join('\n'));
    process.exitCode = 1;
  } else console.log('Page template conventions passed.');
}
