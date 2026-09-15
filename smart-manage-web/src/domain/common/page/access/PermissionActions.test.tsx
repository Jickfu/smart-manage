// @vitest-environment jsdom
import { act } from 'react';
import { createRoot } from 'react-dom/client';
import { expect, it, vi } from 'vitest';
import { PermissionActions } from './PermissionActions';

vi.mock('./usePermissionAccess', () => ({
  usePermissionAccess: () => ({ can: (permission?: string) => permission !== 'denied' }),
}));

it('权限过滤保持危险按钮与普通按钮的声明顺序和交互属性', async () => {
  const container = document.createElement('div');
  document.body.append(container);
  const root = createRoot(container);
  const onDelete = vi.fn();
  try {
    await act(async () =>
      root.render(
        <PermissionActions
          actions={[
            { key: 'delete', label: '删除', danger: true, onClick: onDelete },
            { key: 'hidden', label: '不可见', permission: 'denied', onClick: vi.fn() },
            { key: 'refresh', label: '刷新', disabled: true, onClick: vi.fn() },
          ]}
        />,
      ),
    );
    const buttons = [...container.querySelectorAll('button')];
    expect(buttons.map((button) => button.textContent?.replace(/\s/g, ''))).toEqual([
      '删除',
      '刷新',
    ]);
    expect(buttons[0]?.className).toContain('danger');
    expect(buttons[1]?.disabled).toBe(true);
    await act(async () => buttons[0]?.click());
    expect(onDelete).toHaveBeenCalledOnce();
  } finally {
    await act(async () => root.unmount());
    container.remove();
  }
});
