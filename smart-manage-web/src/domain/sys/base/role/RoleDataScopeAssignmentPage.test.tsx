// @vitest-environment jsdom
import { act } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import RoleDataScopeAssignmentPage from './RoleDataScopeAssignmentPage';
import { AssignmentPage } from '@/domain/common/page/assignment/AssignmentPage';
import { roleApi } from './api';
import { roleQueryKeys } from './queryKeys';
import { orgApi } from '../org/api';
import type { RoleDataScopeWorkspace } from './types';
import { ApiError } from '@/api/ApiError';

const mocks = vi.hoisted(() => ({
  feedback: { warning: vi.fn(), success: vi.fn(), fromError: vi.fn() },
  confirm: vi.fn(),
  dirty: false,
}));
vi.mock('./api', () => ({ roleApi: { dataScopeWorkspace: vi.fn(), assignDataScopes: vi.fn() } }));
vi.mock('../org/api', () => ({ orgApi: { options: vi.fn() } }));
vi.mock('@/domain/common/page/access/usePermissionAccess', () => ({
  usePermissionAccess: () => ({ can: () => true, loading: false }),
}));
vi.mock('@/domain/common/component/useOperationFeedback', () => ({
  useOperationFeedback: () => mocks.feedback,
}));
vi.mock('@/domain/common/component/useOperationConfirm', () => ({
  useOperationConfirm: () => mocks.confirm,
}));
vi.mock('@/domain/common/page/tab/useBeforeCloseGuard', () => ({
  useBeforeCloseGuard: (_app: string, _tab: string, dirty: boolean) => {
    mocks.dirty = dirty;
  },
}));
let container: HTMLDivElement;
let root: Root;
let queryClient: QueryClient;
let workspace: RoleDataScopeWorkspace;
const workspaceKey = [...roleQueryKeys.detail('role-test'), 'data-scopes'];
const organizations = [{ id: 'org-one', number: 'ONE', name: '组织一', namePath: '集团/组织一' }];

beforeEach(() => {
  vi.clearAllMocks();
  vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
  vi.stubGlobal(
    'ResizeObserver',
    class {
      observe() {}
      unobserve() {}
      disconnect() {}
    },
  );
  const computedStyle = window.getComputedStyle.bind(window);
  vi.spyOn(window, 'getComputedStyle').mockImplementation((element) => computedStyle(element));
  Object.defineProperty(window, 'matchMedia', {
    configurable: true,
    value: () => ({
      matches: false,
      addListener() {},
      removeListener() {},
      addEventListener() {},
      removeEventListener() {},
    }),
  });
  container = document.createElement('div');
  document.body.append(container);
  root = createRoot(container);
  queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false, staleTime: Infinity }, mutations: { retry: false } },
  });
  workspace = {
    roleId: 'role-test',
    roleNumber: 'admin',
    roleName: '测试角色',
    version: 3,
    defaultDataScope: 'ALL',
    resources: { purchase: ['SAVE', 'VIEW'], invoice: ['READ'] },
    rules: [],
  };
  vi.mocked(orgApi.options).mockResolvedValue(organizations);
  vi.mocked(roleApi.dataScopeWorkspace).mockImplementation(async () => workspace);
  vi.mocked(roleApi.assignDataScopes).mockResolvedValue('');
  mocks.confirm.mockResolvedValue(false);
  mocks.dirty = false;
});
afterEach(async () => {
  await act(async () => root.unmount());
  queryClient.clear();
  container.remove();
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});
async function renderPage() {
  queryClient.setQueryData(workspaceKey, workspace);
  queryClient.setQueryData(['sys', 'base', 'org', 'options'], organizations);
  await act(async () =>
    root.render(
      <QueryClientProvider client={queryClient}>
        <RoleDataScopeAssignmentPage
          appNumber="sys"
          componentKey="role-data-scope"
          tabKey="role-test"
          title="数据范围"
          billId="role-test"
          active
        />
      </QueryClientProvider>,
    ),
  );
}
function button(label: string, parent: ParentNode = container) {
  const result = [...parent.querySelectorAll<HTMLButtonElement>('button')].find(
    (element) => element.textContent?.replace(/\s/g, '') === label,
  );
  expect(result).toBeTruthy();
  return result!;
}
async function click(element: HTMLElement) {
  await act(async () => element.click());
}
function rows() {
  return [...container.querySelectorAll<HTMLTableRowElement>('tbody tr[data-row-key]')];
}
async function select(label: string, optionText: string) {
  const input = container.querySelector<HTMLInputElement>(`input[aria-label="${label}"]`)!;
  expect(input).toBeTruthy();
  await act(async () => {
    input.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }));
    input.click();
  });
  const option = [...document.querySelectorAll<HTMLElement>('.ant-select-item-option')].find(
    (element) => element.textContent === optionText,
  );
  expect(option, `找不到选项 ${optionText}`).toBeTruthy();
  await click(option!);
}

// 真实 Select、Table、Modal 的多步交互包含布局计算，为本组保留充分执行时间。
describe('角色数据范围编辑交互', { timeout: 15000 }, () => {
  it('admin 可编辑，恢复原值后 clean，顶部右侧不渲染但关闭保护仍获知 dirty', async () => {
    await renderPage();
    expect(container.querySelector('.sm-assignment-header-context')).toBeNull();
    expect(button('保存').disabled).toBe(true);
    expect(mocks.dirty).toBe(false);
    await select('默认数据范围', '本组织');
    expect(mocks.dirty).toBe(true);
    expect(button('保存').disabled).toBe(false);
    await select('默认数据范围', '全部数据');
    expect(mocks.dirty).toBe(false);
    expect(button('保存').disabled).toBe(true);
  });

  it('重复草稿有独立稳定身份，编辑后单选与增量多选删除都只影响草稿', async () => {
    await renderPage();
    await click(button('新增'));
    await click(button('新增'));
    await click(button('新增'));
    const keys = rows().map((row) => row.dataset.rowKey);
    expect(new Set(keys).size).toBe(3);
    await select('第1行操作', 'SAVE');
    expect(rows().map((row) => row.dataset.rowKey)).toEqual(keys);
    expect(rows()[0]!.classList.contains('ant-table-row-selected')).toBe(true);
    await click(button('删除'));
    expect(rows().map((row) => row.dataset.rowKey)).toEqual(keys.slice(1));
    for (const row of rows())
      await click(row.querySelector<HTMLInputElement>('input[type="checkbox"]')!);
    await click(button('删除'));
    expect(rows()).toHaveLength(0);
    expect(mocks.dirty).toBe(false);
    expect(roleApi.assignDataScopes).not.toHaveBeenCalled();
  });

  it('切换资源清操作并保留组织；保存请求没有本地行标识', async () => {
    workspace.rules = [
      {
        resourceType: 'purchase',
        action: 'SAVE',
        scopeType: 'CUSTOM_ORGS',
        orgIds: ['missing-org', 'org-one'],
      },
    ];
    await renderPage();
    const key = rows()[0]!.dataset.rowKey;
    await select('第1行资源', 'invoice');
    expect(rows()[0]!.dataset.rowKey).toBe(key);
    expect(
      container
        .querySelector<HTMLInputElement>('input[aria-label="第1行操作"]')!
        .closest('.ant-select')!.textContent,
    ).not.toContain('SAVE');
    await select('第1行数据范围', '本人相关');
    await select('第1行数据范围', '自定义组织');
    expect(
      container.querySelector<HTMLInputElement>('.sm-ref-selector-trigger-input')!.value,
    ).toContain('missing-org');
    mocks.confirm.mockImplementation(
      async ({ onConfirm }: { onConfirm: () => Promise<unknown> }) => {
        await onConfirm();
        return true;
      },
    );
    await click(button('保存'));
    expect(roleApi.assignDataScopes).toHaveBeenCalledWith({
      roleId: 'role-test',
      version: 3,
      defaultDataScope: 'ALL',
      rules: [
        {
          resourceType: 'invoice',
          action: undefined,
          scopeType: 'CUSTOM_ORGS',
          orgIds: ['missing-org', 'org-one'],
        },
      ],
    });
  });

  it('重复规则及空自定义组织在确认和 mutation 前阻断', async () => {
    await renderPage();
    await click(button('新增'));
    await click(button('新增'));
    await click(button('保存'));
    expect(mocks.feedback.warning).toHaveBeenCalledWith('同一资源操作只能配置一条数据范围规则');
    expect(mocks.confirm).not.toHaveBeenCalled();
    await click(rows()[1]!.querySelector<HTMLInputElement>('input[type="checkbox"]')!);
    await click(button('删除'));
    await select('第1行数据范围', '自定义组织');
    await click(button('保存'));
    expect(mocks.feedback.warning).toHaveBeenCalledWith('请选择至少一个组织');
    expect(roleApi.assignDataScopes).not.toHaveBeenCalled();
  });

  it('未知已选组织在打开并再次确认选择器后仍保留，候选刷新只更新名称', async () => {
    workspace.rules = [
      { resourceType: 'purchase', scopeType: 'CUSTOM_ORGS', orgIds: ['missing-org', 'org-one'] },
    ];
    await renderPage();
    await click(
      container.querySelector<HTMLButtonElement>('.sm-ref-selector-trigger button:last-child')!,
    );
    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 30));
    });
    expect(document.body.textContent).toContain('未解析组织（missing-org）');
    await click(button('确定', document));
    expect(mocks.dirty).toBe(false);
    expect(
      container.querySelector<HTMLInputElement>('.sm-ref-selector-trigger-input')!.value,
    ).toContain('missing-org');
    await act(async () => {
      queryClient.setQueryData(
        ['sys', 'base', 'org', 'options'],
        [
          ...organizations,
          { id: 'missing-org', number: 'TWO', name: '组织二', namePath: '集团/组织二' },
        ],
      );
      await new Promise((resolve) => setTimeout(resolve, 20));
    });
    expect(
      container.querySelector<HTMLInputElement>('.sm-ref-selector-trigger-input')!.value,
    ).toContain('集团/组织二');
    expect(mocks.dirty).toBe(false);
  });

  it('查询变为阻断错误后隐藏交互，旧保存按钮不能提交，草稿仍保留', async () => {
    await renderPage();
    await select('默认数据范围', '本组织');
    const save = button('保存');
    vi.mocked(roleApi.dataScopeWorkspace).mockRejectedValue(
      new ApiError({ source: 'API', apiCode: 100403, message: '无权访问' }),
    );
    await act(async () => {
      await queryClient.refetchQueries({ queryKey: workspaceKey });
      await new Promise((resolve) => setTimeout(resolve, 20));
    });
    expect(container.querySelector<HTMLElement>('.sm-edit-header')!.hidden).toBe(true);
    await click(save);
    expect(roleApi.assignDataScopes).not.toHaveBeenCalled();
    expect(mocks.dirty).toBe(true);
  });

  it('公共分配壳默认仍显示上下文，false 只影响呈现', async () => {
    const renderShell = (showHeaderContext?: boolean) => (
      <QueryClientProvider client={queryClient}>
        <AssignmentPage
          loading={false}
          saving={false}
          access={{ prefix: 'role', permissions: { save: 'save' } }}
          subject="角色摘要"
          selectedCount={1}
          totalCount={2}
          dirty
          showHeaderContext={showHeaderContext}
          onSave={() => {}}
          onExit={() => {}}
          onRetry={() => {}}
        >
          {null}
        </AssignmentPage>
      </QueryClientProvider>
    );
    await act(async () => root.render(renderShell()));
    expect(container.querySelector('.sm-assignment-header-context')!.textContent).toContain(
      '角色摘要',
    );
    await act(async () => root.render(renderShell(false)));
    expect(container.querySelector('.sm-assignment-header-context')).toBeNull();
    expect(mocks.dirty).toBe(true);
  });
});
