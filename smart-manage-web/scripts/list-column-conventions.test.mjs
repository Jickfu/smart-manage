import { resolve } from 'node:path';
import { describe, expect, it } from 'vitest';
import { inspectListColumns, verifyListColumns } from './list-column-conventions.mjs';

const filename = resolve(import.meta.dirname, '../src/example.tsx');
const imported = 'import Page from "@/domain/common/page/list/ListPage";';
const reasons = (source) =>
  inspectListColumns(source, filename).violations.map((item) => item.reason);

describe('列表列省略声明规范', () => {
  it('拒绝遗漏 ellipsis，支持模板别名和同文件列变量', () => {
    expect(
      reasons(
        `${imported} const columns = [{ title: '名称', dataIndex: 'name' }]; <Page columns={columns} />;`,
      ),
    ).toEqual(['列表叶子列必须显式声明 ellipsis']);
    expect(
      reasons(
        `${imported} const columns = [{ title: '名称', dataIndex: 'name', ellipsis: true }]; <Page columns={columns} />;`,
      ),
    ).toEqual([]);
  });

  it('业务例外必须显式关闭并说明中文原因', () => {
    expect(reasons(`${imported}<Page columns={[{ title: '岗位', ellipsis: false }]} />;`)).toEqual([
      'ellipsis: false 必须在属性前用中文注释说明业务原因',
    ]);
    expect(
      reasons(`${imported}<Page columns={[{ title: '岗位',
      // 多岗位必须逐项对应展示。
      ellipsis: false }]} />;`),
    ).toEqual([]);
  });

  it('检查 useMemo、分组叶子列、条件数组和标准配置对象', () => {
    expect(
      reasons(
        `${imported} const columns = useMemo(() => [{ title: '分组', children: [{ title: '名称', ellipsis: { showTitle: true } }] }], []); <Page columns={columns} />;`,
      ),
    ).toEqual([]);
    expect(
      reasons(
        `${imported} const columns = useMemo(() => { return condition ? [{ title: '名称', ellipsis: true }] : [{ title: '描述' }]; }, []); <Page columns={columns} />;`,
      ),
    ).toEqual(['列表叶子列必须显式声明 ellipsis']);
  });

  it('不能用未知变量、空值或不可解析的配置绕过校验', () => {
    expect(reasons(`${imported}<Page columns={externalColumns} />;`)).toEqual([
      '列表 columns 必须使用可核对的同文件列声明',
    ]);
    expect(
      reasons(`${imported}<Page columns={[{ title: '名称', ellipsis: undefined }]} />;`),
    ).toEqual(['ellipsis 必须显式使用 true、false 或标准配置对象']);
  });

  it('不限制非列表内容，也不提供运行时行高或列属性兜底', () => {
    expect(reasons(`const rows = [{ title: '名称' }]; <OtherTable columns={rows} />;`)).toEqual([]);
  });

  it('当前仓库标准列表必须全部符合省略与业务例外约定', () => {
    expect(verifyListColumns()).toEqual([]);
  });
});
