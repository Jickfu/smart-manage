// @vitest-environment jsdom
import { act } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { afterEach, beforeEach, expect, it, vi } from 'vitest';
import { installJsdomBrowserStubs } from '@/test/jsdomBrowserStubs';
import { PageNotice } from './PageNotice';
import { EditPageShell } from '../page/EditPageShell';
import { EditSectionCollapse } from '../page/edit/EditSectionCollapse';
import ListTableShell from '../page/list/ListTableShell';

let container: HTMLDivElement;
let root: Root;
beforeEach(() => {
  installJsdomBrowserStubs();
  container = document.createElement('div');
  document.body.append(container);
  root = createRoot(container);
});
afterEach(async () => {
  await act(async () => root.unmount());
  container.remove();
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});
it('默认不可关闭；显式关闭移除整行，同 key 不重现，新 key 重新显示', async () => {
  const onClose = vi.fn();
  await act(async () => root.render(<PageNotice notice={{ key: 'first', title: '说明' }} />));
  expect(container.querySelector('button')).toBeNull();
  await act(async () =>
    root.render(<PageNotice notice={{ key: 'first', title: '说明', closable: true, onClose }} />),
  );
  await act(async () => container.querySelector<HTMLButtonElement>('button')!.click());
  expect(onClose).toHaveBeenCalledTimes(1);
  expect(container.innerHTML).toBe('');
  await act(async () => root.render(<PageNotice notice={{ key: 'first', title: '说明' }} />));
  expect(container.innerHTML).toBe('');
  await act(async () => root.render(<PageNotice notice={{ key: 'second', title: '新说明' }} />));
  expect(container.textContent).toContain('新说明');
});
it('编辑提示位于按钮栏之前，卡片折叠仍保留分区提示', async () => {
  await act(async () =>
    root.render(
      <EditPageShell
        title="编辑"
        loading={false}
        actions={<button>保存</button>}
        notice={{ key: 'page', title: '整页说明' }}
      >
        <EditSectionCollapse
          defaultActiveKeys={['basic']}
          items={[
            {
              key: 'basic',
              label: '基本信息',
              notice: { key: 'section', title: '分区说明' },
              children: <input aria-label="内容" />,
            },
          ]}
        />
      </EditPageShell>,
    ),
  );
  expect(container.querySelector('.sm-edit-header')?.previousElementSibling?.textContent).toContain(
    '整页说明',
  );
  await act(async () =>
    container.querySelector<HTMLButtonElement>('.sm-edit-collapse-title')!.click(),
  );
  expect(container.querySelector('.sm-edit-section > .sm-page-notice')?.textContent).toContain(
    '分区说明',
  );
  expect(container.querySelector('.ant-collapse-panel-active')).toBeNull();
});
it('左树右表提示仅处于右表顶部，不进入树或统计按钮行', async () => {
  await act(async () =>
    root.render(
      <ListTableShell
        treePanel={<div>组织树</div>}
        table={<div>表格</div>}
        notice={{ key: 'table', title: '当前表格说明' }}
      />,
    ),
  );
  expect(
    container.querySelector('.sm-list-table-content')?.firstElementChild?.textContent,
  ).toContain('当前表格说明');
  expect(container.querySelector('.sm-list-tree-panel')?.textContent).toBe('组织树');
  expect(container.querySelector('.sm-list-table-meta')?.textContent).not.toContain('当前表格说明');
});
