import { readdirSync, readFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import ts from 'typescript';
import { expect, it } from 'vitest';

const sourceRoot = resolve(import.meta.dirname, '../src');
const templates = new Set([
  resolve(sourceRoot, 'domain/common/page/edit/EditPage'),
  resolve(sourceRoot, 'domain/common/page/edit/ModalEditPage'),
]);

function containsInlineObject(node) {
  return ts.isObjectLiteralExpression(node) || Boolean(ts.forEachChild(node, containsInlineObject));
}

function inlineSnapshots(source, filename) {
  const ast = ts.createSourceFile(
    filename,
    source,
    ts.ScriptTarget.Latest,
    true,
    ts.ScriptKind.TSX,
  );
  const names = new Set();
  for (const statement of ast.statements) {
    if (!ts.isImportDeclaration(statement) || !ts.isStringLiteral(statement.moduleSpecifier))
      continue;
    const specifier = statement.moduleSpecifier.text.replace(/\.(tsx?|jsx?)$/, '');
    const target = specifier.startsWith('@/')
      ? resolve(sourceRoot, specifier.slice(2))
      : resolve(dirname(filename), specifier);
    if (templates.has(target) && statement.importClause?.name)
      names.add(statement.importClause.name.text);
  }
  const violations = [];
  function visit(node) {
    if (
      (ts.isJsxOpeningElement(node) || ts.isJsxSelfClosingElement(node)) &&
      names.has(node.tagName.getText(ast))
    ) {
      for (const attribute of node.attributes.properties) {
        if (!ts.isJsxAttribute(attribute) || attribute.name.getText(ast) !== 'initialValues')
          continue;
        const expression = attribute.initializer;
        if (
          expression &&
          ts.isJsxExpression(expression) &&
          expression.expression &&
          containsInlineObject(expression.expression)
        ) {
          violations.push(
            `${filename}:${ast.getLineAndCharacterOfPosition(attribute.getStart()).line + 1}`,
          );
        }
      }
    }
    ts.forEachChild(node, visit);
  }
  visit(ast);
  return violations;
}

it('rejects inline form snapshots and accepts stable references, including aliased template imports', () => {
  const filename = resolve(sourceRoot, 'example.tsx');
  const imported = 'import Template from "@/domain/common/page/edit/EditPage";';
  expect(
    inlineSnapshots(`${imported}<Template initialValues={{ name: "" }} />`, filename),
  ).toHaveLength(1);
  expect(
    inlineSnapshots(
      `${imported}<Template initialValues={data ? { ...data } : undefined} />`,
      filename,
    ),
  ).toHaveLength(1);
  expect(
    inlineSnapshots(`${imported}<Template initialValues={initialValues} />`, filename),
  ).toEqual([]);
});

it('keeps production template callers on stable snapshot references', () => {
  const violations = [];
  function scan(directory) {
    for (const entry of readdirSync(directory, { withFileTypes: true })) {
      const filename = join(directory, entry.name);
      if (entry.isDirectory()) scan(filename);
      else if (filename.endsWith('.tsx') && !filename.endsWith('.test.tsx'))
        violations.push(...inlineSnapshots(readFileSync(filename, 'utf8'), filename));
    }
  }
  scan(sourceRoot);
  expect(violations).toEqual([]);
});
