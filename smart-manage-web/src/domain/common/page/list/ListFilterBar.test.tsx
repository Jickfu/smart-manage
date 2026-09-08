// @vitest-environment jsdom
import { act } from 'react';
import { createRoot } from 'react-dom/client';
import { expect, it, vi } from 'vitest';
import ListFilterBar from './ListFilterBar';

it.each([false, true])('首次展开配置 %s，后续重渲染保留用户选择和摘要', async (defaultExpanded) => {
  vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
  const container = document.createElement('div');
  const root = createRoot(container);
  const render = async (initial?: boolean) =>
    act(async () =>
      root.render(
        <ListFilterBar
          title="列表"
          defaultExpanded={initial}
          filterContent={<input />}
          filterSummary="状态：启用"
        />,
      ),
    );
  try {
    await render(defaultExpanded || undefined);
    expect(!!container.querySelector('.sm-list-filter-panel')).toBe(defaultExpanded);
    await act(async () =>
      (container.querySelector('.sm-list-filter-toggle') as HTMLButtonElement).click(),
    );
    await render(defaultExpanded);
    expect(!!container.querySelector('.sm-list-filter-panel')).toBe(!defaultExpanded);
    expect(container.querySelector('.sm-list-filter-summary')?.textContent).toBe(
      defaultExpanded ? '状态：启用' : '',
    );
  } finally {
    await act(async () => root.unmount());
    vi.unstubAllGlobals();
  }
});
