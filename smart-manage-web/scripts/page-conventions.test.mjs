import { resolve } from 'node:path';
import { describe, expect, it } from 'vitest';
import { inspectPageConventions, verifyPageConventions } from './page-conventions.mjs';

const imports = `import Dialog from '@/domain/common/page/edit/ModalEditPage';
import { Form as Fields } from 'antd';
import { FormFieldCell as Cell } from '@/domain/common/page/edit/FormFieldLayout';`;
describe('页面接入规范', () => {
  it('当前领域页面符合可机械判断的接入规则', () => {
    expect(verifyPageConventions(resolve(import.meta.dirname, '../src/domain'))).toEqual([]);
  });
  it('识别模板导入别名和新增编辑条件标题', () => {
    expect(
      inspectPageConventions(imports + `<Dialog title={fresh ? '新增资料' : '编辑资料'} />`),
    ).toHaveLength(1);
    expect(inspectPageConventions(imports + `<Dialog title="资料" />`)).toEqual([]);
  });
  it('模板标题按整个标题的边界检查操作前后缀', () => {
    expect(inspectPageConventions(imports + '<Dialog title={`脚本${name}编辑器`} />')).toEqual([]);
    for (const title of ['`编辑${name}`', '`${name}详情`', '`资料${name}详情${suffix}`']) {
      expect(inspectPageConventions(imports + `<Dialog title={${title}} />`)).toHaveLength(
        title === '`资料${name}详情${suffix}`' ? 0 : 1,
      );
    }
  });
  it('字段内容类必须在每个条件分支中存在', () => {
    for (const expression of [
      "valid ? 'sm-edit-field-content' : 'wrong'",
      "valid ? 'wrong' : 'sm-edit-field-content'",
      "valid ? 'sm-edit-field-content' : (other ? 'sm-edit-field-content' : unknown)",
    ]) {
      expect(
        inspectPageConventions(
          imports + `<Cell><Fields.Item label="名称" className={${expression}} /></Cell>`,
        ),
      ).toHaveLength(1);
    }
    expect(
      inspectPageConventions(
        imports +
          `<Cell><Fields.Item label="名称" className={valid ? 'sm-edit-field-content' : 'extra sm-edit-field-content'} /></Cell>`,
      ),
    ).toEqual([]);
  });
  it('普通标签字段要求直接字段容器及内容类', () => {
    expect(inspectPageConventions(imports + `<Fields.Item label="名称" />`)).toHaveLength(2);
    expect(
      inspectPageConventions(
        imports + `<Cell><Fields.Item label="名称" className="sm-edit-field-content" /></Cell>`,
      ),
    ).toEqual([]);
  });
  it('不把隐藏值和无标签的明细控件当成普通字段', () => {
    expect(
      inspectPageConventions(
        imports + `<Fields.Item name="id" hidden /><Fields.Item name="quantity" />`,
      ),
    ).toEqual([]);
    expect(
      inspectPageConventions(`import Other from './Other'; <Other title="编辑资料" />`),
    ).toEqual([]);
    expect(
      inspectPageConventions(
        imports +
          `import Editor from './ScriptEditor';
      <Fields.Item label="代码"><Editor /></Fields.Item>`,
      ),
    ).toEqual([]);
  });
});
