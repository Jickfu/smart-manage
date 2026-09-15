import { describe, expect, it, vi } from 'vitest';
import { resolvePageActions } from './pageActions';

describe('页面操作组合', () => {
  const refresh = { key: 'refresh', label: '刷新', onClick: vi.fn() };
  const remove = { key: 'delete', label: '删除', danger: true, onClick: vi.fn() };

  it('默认布局危险操作后置，不修改输入；空声明不自动补按钮', () => {
    const builtins = [remove, refresh];
    expect(resolvePageActions(undefined, builtins)).toEqual([refresh, remove]);
    expect(builtins).toEqual([remove, refresh]);
    expect(resolvePageActions([], builtins)).toEqual([]);
  });

  it('混排保留危险按钮位置和命令属性，缺少的内置操作跳过', () => {
    const actions = resolvePageActions(
      [remove, { builtin: 'save' }, { builtin: 'refresh' }],
      [refresh],
    );
    expect(actions.map((action) => action.label)).toEqual(['删除', '刷新']);
    expect(actions[0]?.danger).toBe(true);
    expect(actions[1]?.onClick).toBe(refresh.onClick);
  });

  it('业务 key 和内置 key 隔离，但同类重复声明报错', () => {
    expect(
      resolvePageActions([refresh, { builtin: 'refresh' }], [refresh]).map((action) => action.key),
    ).toEqual(['custom:refresh', 'builtin:refresh']);
    expect(() => resolvePageActions([refresh, refresh], [])).toThrow('重复');
    expect(() => resolvePageActions([{ builtin: 'refresh' }, { builtin: 'refresh' }], [])).toThrow(
      '重复',
    );
  });
});
