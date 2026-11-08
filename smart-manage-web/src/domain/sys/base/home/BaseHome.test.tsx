// @vitest-environment jsdom
import { act } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { afterEach, beforeEach, expect, it, vi } from 'vitest';
import { installJsdomBrowserStubs } from '@/test/jsdomBrowserStubs';
import BaseHome from './BaseHome';
import { userApi } from '../user/api';
import { orgApi } from '../org/api';
import { roleApi } from '../role/api';
const permissions = vi.hoisted(() => ({ allowed: true }));
vi.mock('@/domain/common/home/QuickLaunchCard', () => ({ default: () => null }));
vi.mock('@/domain/common/page/access/usePermissionAccess', () => ({
  usePermissionAccess: () => ({ can: () => permissions.allowed, loading: false }),
}));
vi.mock('../user/api', () => ({ userApi: { listPage: vi.fn() } }));
vi.mock('../org/api', () => ({ orgApi: { tree: vi.fn() } }));
vi.mock('../role/api', () => ({ roleApi: { listPage: vi.fn() } }));
let container: HTMLDivElement;
let root: Root;
let client: QueryClient;
beforeEach(() => {
  installJsdomBrowserStubs();
  permissions.allowed = true;
  vi.clearAllMocks();
  container = document.createElement('div');
  document.body.append(container);
  root = createRoot(container);
  client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  vi.mocked(userApi.listPage).mockResolvedValue({
    total: 23,
    records: [],
    pageNum: 1,
    pageSize: 1,
  });
  vi.mocked(roleApi.listPage).mockResolvedValue({ total: 7, records: [], pageNum: 1, pageSize: 1 });
  vi.mocked(orgApi.tree).mockResolvedValue([
    {
      id: '1',
      name: '总部',
      number: '1',
      orgType: 'GROUP',
      enabled: true,
      archived: false,
      children: [
        {
          id: '2',
          name: '部门',
          number: '2',
          orgType: 'DEPARTMENT',
          enabled: false,
          archived: false,
          children: [],
        },
      ],
    },
  ]);
});
afterEach(async () => {
  await act(async () => root.unmount());
  client.clear();
  container.remove();
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});
async function renderHome() {
  await act(async () =>
    root.render(
      <QueryClientProvider client={client}>
        <BaseHome />
      </QueryClientProvider>,
    ),
  );
}
it('显示真实统计、递归统计组织，并按启用状态查询用户', async () => {
  await renderHome();
  await vi.waitFor(() =>
    expect(container.querySelector('.sm-app-home-metric-value')?.textContent).toBe('23'),
  );
  expect([...container.querySelectorAll('strong')].map((element) => element.textContent)).toEqual([
    '23',
    '2',
    '7',
  ]);
  expect(container.textContent).not.toContain('示例');
  expect(JSON.parse(vi.mocked(userApi.listPage).mock.calls[0]![0].filters!)).toEqual([
    { field: 'enabled', type: 'boolean', operator: 'IN', values: [true] },
  ]);
  expect(orgApi.tree).toHaveBeenCalledWith(false);
  permissions.allowed = false;
  await renderHome();
  expect(container.textContent).not.toContain('23');
  expect(container.textContent).toContain('无查看权限');
});
it('没有权限不请求统计', async () => {
  permissions.allowed = false;
  await renderHome();
  expect(userApi.listPage).not.toHaveBeenCalled();
  expect(orgApi.tree).not.toHaveBeenCalled();
  expect(roleApi.listPage).not.toHaveBeenCalled();
});
it('查询失败显示重试，不伪装为零', async () => {
  vi.mocked(userApi.listPage).mockRejectedValue(new Error('不可用'));
  await renderHome();
  await vi.waitFor(() => expect(container.textContent).toContain('加载失败'));
  expect(container.querySelector('.sm-app-home-metric-value')?.textContent).not.toBe('0');
});

it('真实零值保持数字，失败重试恢复后移除状态区', async () => {
  vi.mocked(userApi.listPage)
    .mockRejectedValueOnce(new Error('不可用'))
    .mockResolvedValue({ total: 0, records: [], pageNum: 1, pageSize: 1 });
  await renderHome();
  await vi.waitFor(() => expect(container.querySelector('[role="alert"]')).not.toBeNull());
  expect(container.querySelector('strong [role="alert"]')).toBeNull();
  await act(async () =>
    container.querySelector<HTMLButtonElement>('[role="alert"] button')!.click(),
  );
  await vi.waitFor(() =>
    expect(container.querySelector('.sm-app-home-metric-value')?.textContent).toBe('0'),
  );
  expect(container.querySelector('[role="alert"]')).toBeNull();
});
