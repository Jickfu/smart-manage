// @vitest-environment jsdom
import { act } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { ConfigProvider } from 'antd';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { beforeEach, afterEach, expect, it, vi } from 'vitest';
import { installJsdomBrowserStubs } from '@/test/jsdomBrowserStubs';
import { OperationType, type PageComponentProps } from '@/domain/common/page/types';
import DefinitionListPage from './DefinitionListPage';
import DefinitionDesignerPage from './DefinitionDesignerPage';
import DefinitionVersionsPage from './DefinitionVersionsPage';

const mocks = vi.hoisted(() => ({
  permissions: vi.fn(),
  definitions: vi.fn(),
  versions: vi.fn(),
  post: vi.fn(),
  businessTypes: vi.fn(),
  design: vi.fn(),
  confirm: vi.fn(),
  workspace: {
    registerBeforeClose: vi.fn(),
    unregisterBeforeClose: vi.fn(),
    openBillTab: vi.fn(),
    addContentTab: vi.fn(),
    removeContentTab: vi.fn(),
  },
}));
vi.mock('@/api/user', () => ({ getCurrentPermissions: mocks.permissions }));
vi.mock('../api', () => ({
  workflowApi: {
    definitions: mocks.definitions,
    versions: mocks.versions,
    design: mocks.design,
    businessTypes: mocks.businessTypes,
  },
  flowPost: mocks.post,
}));
vi.mock('@/stores/workbench', () => ({ useWorkbenchStore: { getState: () => mocks.workspace } }));
vi.mock('@/domain/common/component/useOperationConfirm', () => ({
  useOperationConfirm: () => mocks.confirm,
}));
vi.mock('@/domain/common/component/useOperationFeedback', () => ({
  useOperationFeedback: () => ({ fromError: vi.fn(), success: vi.fn() }),
}));

let container: HTMLDivElement;
let root: Root;
let client: QueryClient;
const props: PageComponentProps = {
  appNumber: 'process',
  componentKey: 'workflow/process/definition',
  tabKey: 'definition-test',
  title: '流程定义',
  active: true,
};
beforeEach(() => {
  installJsdomBrowserStubs();
  vi.clearAllMocks();
  mocks.permissions.mockImplementation(async (prefix: string) =>
    prefix === 'workflow:process:definition'
      ? ['workflow:process:definition:save', 'workflow:process:definition:design']
      : [],
  );
  mocks.definitions.mockResolvedValue({
    total: 1,
    records: [
      {
        id: '10',
        number: 'leave',
        name: '请假',
        businessType: 'demo/office/leave',
        enabled: true,
        version: 0,
        definitions: [{ id: '100', code: 'leave', name: '请假', version: '1', published: false }],
      },
    ],
  });
  mocks.design.mockResolvedValue({
    definition: { flowName: '请假', version: '1', isPublish: 0 },
    digest: 'digest',
  });
  mocks.businessTypes.mockResolvedValue([
    {
      key: 'demo/office/leave',
      name: '请假申请',
      featureKey: 'demo/office/leave',
      domainId: '1',
      domainName: '演示',
      appId: '11',
      appName: '办公样板',
    },
  ]);
  container = document.createElement('div');
  document.body.append(container);
  root = createRoot(container);
  client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
});
afterEach(async () => {
  await act(async () => root.unmount());
  client.clear();
  container.remove();
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});
async function renderPage(page: React.ReactNode) {
  await act(async () => {
    root.render(
      <QueryClientProvider client={client}>
        <ConfigProvider theme={{ zeroRuntime: true }}>{page}</ConfigProvider>
      </QueryClientProvider>,
    );
  });
  await act(async () => {
    await new Promise((resolve) => setTimeout(resolve, 30));
  });
}
function button(label: string) {
  return [...container.querySelectorAll('button')].find(
    (element) => element.textContent?.replace(/\s/g, '') === label,
  );
}
async function selectRow() {
  const row = container.querySelector('tbody tr[data-row-key]')!;
  await act(async () => row.dispatchEvent(new MouseEvent('click', { bubbles: true })));
}
it('按真实权限前缀显示新增，设计命令打开版本页签而不是弹框', async () => {
  await renderPage(<DefinitionListPage {...props} />);
  expect(button('新增流程')).toBeTruthy();
  expect(button('设计')).toHaveProperty('disabled', true);
  const codeLink = container.querySelector<HTMLButtonElement>('tbody button')!;
  expect(codeLink.textContent).toBe('leave');
  expect(codeLink.classList.contains('ant-btn-link')).toBe(true);
  expect(container.querySelectorAll('tbody button')).toHaveLength(1);
  await act(async () => codeLink.click());
  expect(mocks.workspace.openBillTab).toHaveBeenCalledWith(
    'process',
    'workflow/process/definition/designer',
    '100',
    OperationType.EDIT,
  );
  await selectRow();
  await act(async () => button('设计')!.click());
  expect(mocks.workspace.openBillTab).toHaveBeenCalledWith(
    'process',
    'workflow/process/definition/designer',
    '100',
    OperationType.EDIT,
  );
  expect(document.querySelector('iframe')).toBeNull();
  expect(container.querySelector('[aria-label="操作流程版本"]')).toBeNull();
  if (button('版本管理')!.disabled) await selectRow();
  await act(async () => button('版本管理')!.click());
  expect(mocks.workspace.addContentTab).toHaveBeenCalledWith(
    'process',
    expect.objectContaining({
      key: 'bill:workflow/process/definition/versions:10',
      componentKey: 'workflow/process/definition/versions',
      pageType: 'LIST',
      context: { bindingId: '10' },
    }),
  );
});
it('流程定义列表按领域应用树和关键词执行服务端分页筛选并清空选择', async () => {
  await renderPage(<DefinitionListPage {...props} />);
  expect(container.querySelector('.sm-list-tree-layout')?.textContent).toContain('全部流程');
  expect(container.querySelector('.sm-list-tree-layout')?.textContent).toContain('办公样板');
  expect(mocks.definitions).toHaveBeenLastCalledWith({
    pageNum: 1,
    pageSize: 20,
    keyword: undefined,
  });
  await selectRow();
  expect(button('设计')).toHaveProperty('disabled', false);
  const application = [...container.querySelectorAll<HTMLElement>('.ant-tree-title')].find(
    (node) => node.textContent === '办公样板',
  )!;
  await act(async () => application.click());
  await act(async () => {
    await new Promise((resolve) => setTimeout(resolve, 40));
  });
  expect(mocks.definitions).toHaveBeenLastCalledWith({
    pageNum: 1,
    pageSize: 20,
    keyword: undefined,
    appId: '11',
  });
  expect(button('设计')).toHaveProperty('disabled', true);
  const search = container.querySelector<HTMLInputElement>(
    'input[placeholder="搜索流程编码/名称"]',
  )!;
  await act(async () => {
    Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')!.set!.call(
      search,
      ' leave ',
    );
    search.dispatchEvent(new Event('input', { bubbles: true }));
    search.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Enter', code: 'Enter', keyCode: 13, bubbles: true }),
    );
    search.dispatchEvent(
      new KeyboardEvent('keyup', { key: 'Enter', code: 'Enter', keyCode: 13, bubbles: true }),
    );
  });
  await act(async () => {
    await new Promise((resolve) => setTimeout(resolve, 40));
  });
  expect(mocks.definitions).toHaveBeenLastCalledWith({
    pageNum: 1,
    pageSize: 20,
    keyword: 'leave',
    appId: '11',
  });
});
it('大量版本独立分页且清除跨页选择，版本链接直接打开指定设计', async () => {
  const versions = Array.from({ length: 45 }, (_, index) => ({
    id: String(index + 1),
    version: String(index + 1),
    code: 'leave',
    name: '请假',
    published: index < 44,
  }));
  mocks.versions.mockResolvedValue({ id: '10', name: '请假', definitions: versions });
  mocks.permissions.mockResolvedValue([
    'workflow:process:definition:save',
    'workflow:process:definition:design',
    'workflow:process:definition:publish',
  ]);
  await renderPage(<DefinitionVersionsPage {...props} context={{ bindingId: '10' }} />);
  expect(container.querySelectorAll('tbody tr[data-row-key]')).toHaveLength(20);
  expect(container.querySelector('tbody button')?.textContent).toBe('V45');
  await selectRow();
  expect(button('发布')).toHaveProperty('disabled', false);
  await act(async () =>
    container.querySelector<HTMLButtonElement>('.ant-pagination-next button')!.click(),
  );
  expect(button('复制新版本')).toHaveProperty('disabled', true);
  expect(container.querySelector('tbody button')?.textContent).toBe('V25');
  await act(async () => container.querySelector<HTMLButtonElement>('tbody button')!.click());
  expect(mocks.workspace.openBillTab).toHaveBeenCalledWith(
    'process',
    'workflow/process/definition/designer',
    '25',
    OperationType.VIEW,
  );
  const search = container.querySelector<HTMLInputElement>(
    'input[placeholder="搜索版本号，如 V12"]',
  )!;
  await act(async () => {
    Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')!.set!.call(search, 'V45');
    search.dispatchEvent(new Event('input', { bubbles: true }));
  });
  await act(async () =>
    search.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Enter', code: 'Enter', keyCode: 13, bubbles: true }),
    ),
  );
  expect(container.querySelectorAll('tbody tr[data-row-key]')).toHaveLength(1);
  expect(container.querySelector('tbody button')?.textContent).toBe('V45');
});
it('新增复用标准弹框编辑布局，并保留未保存内容关闭保护', async () => {
  await renderPage(<DefinitionListPage {...props} />);
  await act(async () => button('新增流程')!.click());
  await act(async () => {
    await new Promise((resolve) => setTimeout(resolve, 30));
  });
  const modal = document.querySelector('.sm-modal-edit')!;
  expect(modal).toBeTruthy();
  expect(modal.querySelectorAll('.sm-edit-form')).toHaveLength(1);
  expect(modal.textContent).toContain('业务类型');
  expect(modal.textContent?.replace(/\s/g, '')).toContain('保存');
  const input = modal.querySelector<HTMLInputElement>('input[id$="number"]')!;
  await act(async () => {
    Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')!.set!.call(
      input,
      'leave_new',
    );
    input.dispatchEvent(new Event('input', { bubbles: true }));
  });
  mocks.confirm.mockResolvedValue(false);
  const guard = mocks.workspace.registerBeforeClose.mock.lastCall![2] as () => Promise<boolean>;
  expect(await guard()).toBe(false);
  expect(input.value).toBe('leave_new');
});
it('只读权限不显示创建命令，已发布版本进入只读设计器', async () => {
  mocks.permissions.mockResolvedValue(['workflow:process:definition:design']);
  await renderPage(<DefinitionListPage {...props} />);
  expect(button('新增流程')).toBeUndefined();
  await selectRow();
  await act(async () => button('设计')!.click());
  expect(mocks.workspace.openBillTab).toHaveBeenCalledWith(
    'process',
    'workflow/process/definition/designer',
    '100',
    OperationType.VIEW,
  );
  mocks.design.mockResolvedValue({
    definition: { flowName: '请假', version: '2', isPublish: 1 },
    digest: 'digest',
  });
  await renderPage(
    <DefinitionDesignerPage {...props} billId="100" operationType={OperationType.VIEW} />,
  );
  expect(container.querySelector('iframe')?.src).toContain('readOnly=true');
});
it('新增业务类型使用参照选择器展示编码名称，保存只提交稳定业务 key', async () => {
  mocks.post.mockResolvedValue('200');
  await renderPage(<DefinitionListPage {...props} />);
  await act(async () => button('新增流程')!.click());
  const modal = document.querySelector('.sm-modal-edit')!;
  expect(modal.querySelector('.sm-ref-selector-trigger')).toBeTruthy();
  expect(modal.querySelector('.ant-select')).toBeNull();
  for (const [field, value] of [
    ['number', 'leave_new'],
    ['name', '请假新流程'],
  ]) {
    const input = modal.querySelector<HTMLInputElement>(`input[id$="${field}"]`)!;
    await act(async () => {
      Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')!.set!.call(input, value);
      input.dispatchEvent(new Event('input', { bubbles: true }));
    });
  }
  await act(async () =>
    modal.querySelector<HTMLButtonElement>('.sm-ref-selector-trigger-btn')!.click(),
  );
  await act(async () => {
    await new Promise((resolve) => setTimeout(resolve, 40));
  });
  const selector = document.querySelector('.sm-ref-selector-modal')!;
  expect(selector.textContent).toContain('业务编码');
  expect(selector.querySelector('.sm-ref-selector-tree-panel')?.textContent).toContain(
    '全部审批业务',
  );
  expect(selector.querySelector('.sm-ref-selector-tree-panel')?.textContent).toContain('办公样板');
  expect(selector.textContent).toContain('demo/office/leave');
  const row = selector.querySelector('tbody tr[data-row-key]')!;
  await act(async () => row.dispatchEvent(new MouseEvent('dblclick', { bubbles: true })));
  expect(modal.querySelector('.sm-ref-selector-trigger-input')).toHaveProperty('value', '请假申请');
  const save = [...modal.querySelectorAll('button')].find(
    (item) => item.textContent?.replace(/\s/g, '') === '保存',
  )!;
  await act(async () => save.click());
  await act(async () => {
    await new Promise((resolve) => setTimeout(resolve, 40));
  });
  expect(mocks.post).toHaveBeenCalledWith('definition/create', {
    number: 'leave_new',
    name: '请假新流程',
    businessType: 'demo/office/leave',
  });
});
it('审批业务树按目录 ID 区分同名应用，范围与关键词共同筛选右表', async () => {
  mocks.businessTypes.mockResolvedValue([
    {
      key: 'approval-a',
      name: '请假',
      featureKey: 'real/leave',
      domainId: '1',
      domainName: '演示',
      appId: '11',
      appName: '办公',
    },
    {
      key: 'approval-a2',
      name: '出差',
      featureKey: 'real/travel',
      domainId: '1',
      domainName: '演示',
      appId: '11',
      appName: '办公',
    },
    {
      key: 'approval-b',
      name: '采购',
      featureKey: 'real/purchase',
      domainId: '1',
      domainName: '演示',
      appId: '12',
      appName: '采购应用',
    },
    {
      key: 'approval-c',
      name: '合同',
      featureKey: 'real/contract',
      domainId: '2',
      domainName: '扩展',
      appId: '21',
      appName: '办公',
    },
  ]);
  await renderPage(<DefinitionListPage {...props} />);
  await act(async () => button('新增流程')!.click());
  await act(async () =>
    document.querySelector<HTMLButtonElement>('.sm-ref-selector-trigger-btn')!.click(),
  );
  const settle = async () =>
    act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 40));
    });
  await settle();
  const selector = document.querySelector('.sm-ref-selector-modal')!;
  const rows = () => [...selector.querySelectorAll('tbody tr[data-row-key]')];
  const node = (title: string, index = 0) =>
    [...selector.querySelectorAll<HTMLElement>('.ant-tree-title')].filter(
      (item) => item.textContent === title,
    )[index]!;
  expect(selector.querySelectorAll('.ant-tree-title')).toHaveLength(6);
  expect(rows()).toHaveLength(4);
  await act(async () => node('演示').click());
  await settle();
  expect(rows()).toHaveLength(3);
  await act(async () => node('办公').click());
  await settle();
  expect(rows()).toHaveLength(2);
  const searchFor = async (value: string) => {
    const search = selector.querySelector<HTMLInputElement>('input[placeholder="快速搜索"]')!;
    await act(async () => {
      Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')!.set!.call(
        search,
        value,
      );
      search.dispatchEvent(new Event('input', { bubbles: true }));
    });
    await act(async () =>
      search.dispatchEvent(
        new KeyboardEvent('keydown', { key: 'Enter', code: 'Enter', keyCode: 13, bubbles: true }),
      ),
    );
    await act(async () =>
      search.dispatchEvent(
        new KeyboardEvent('keyup', { key: 'Enter', code: 'Enter', keyCode: 13, bubbles: true }),
      ),
    );
    await settle();
  };
  await searchFor('APPROVAL-A2');
  expect(rows()).toHaveLength(1);
  expect(rows()[0]!.textContent).toContain('出差');
  await searchFor('');
  expect(rows()).toHaveLength(2);
  await act(async () => node('办公', 1).click());
  await settle();
  expect(rows()).toHaveLength(1);
  expect(rows()[0]!.textContent).toContain('合同');
  await act(async () => node('全部审批业务').click());
  await settle();
  expect(rows()).toHaveLength(4);
});
it('目录查询失败可在参照内重试，恢复后同时显示树和业务', async () => {
  mocks.businessTypes.mockRejectedValue(new Error('目录暂不可用'));
  await renderPage(<DefinitionListPage {...props} />);
  await act(async () => button('新增流程')!.click());
  await act(async () =>
    document.querySelector<HTMLButtonElement>('.sm-ref-selector-trigger-btn')!.click(),
  );
  await act(async () => {
    await new Promise((resolve) => setTimeout(resolve, 40));
  });
  const selector = document.querySelector('.sm-ref-selector-modal')!;
  expect(selector.textContent?.replace(/\s/g, '')).toContain('重试');
  mocks.businessTypes.mockResolvedValue([
    {
      key: 'approval',
      name: '请假申请',
      featureKey: 'demo/office/leave',
      domainId: '1',
      domainName: '演示',
      appId: '11',
      appName: '办公样板',
    },
  ]);
  const retry = [...selector.querySelectorAll('button')].find(
    (item) => item.textContent?.replace(/\s/g, '') === '重试',
  )!;
  await act(async () => retry.click());
  await act(async () => {
    await new Promise((resolve) => setTimeout(resolve, 40));
  });
  expect(selector.querySelector('.sm-ref-selector-tree-panel')?.textContent).toContain('办公样板');
  expect(selector.querySelector('tbody tr[data-row-key]')?.textContent).toContain('请假申请');
});
it('切换页签保留 iframe，只有对应窗口的消息影响关闭确认，保存后解除脏状态', async () => {
  const pageProps = { ...props, billId: '100', operationType: OperationType.EDIT };
  await renderPage(<DefinitionDesignerPage {...pageProps} />);
  const frame = container.querySelector('iframe')!;
  expect(frame).toBeTruthy();
  const guard = mocks.workspace.registerBeforeClose.mock.lastCall![2] as () => Promise<boolean>;
  const message = {
    channel: 'smart-manage-workflow',
    definitionId: '100',
    type: 'dirty',
    detail: true,
  };
  await act(async () =>
    window.dispatchEvent(
      new MessageEvent('message', {
        origin: 'https://untrusted.example',
        source: frame.contentWindow,
        data: message,
      }),
    ),
  );
  expect(await guard()).toBe(true);
  await act(async () =>
    window.dispatchEvent(
      new MessageEvent('message', { origin: location.origin, source: window, data: message }),
    ),
  );
  expect(await guard()).toBe(true);
  await act(async () =>
    window.dispatchEvent(
      new MessageEvent('message', {
        origin: location.origin,
        source: frame.contentWindow,
        data: message,
      }),
    ),
  );
  mocks.confirm.mockResolvedValue(false);
  expect(await guard()).toBe(false);
  await renderPage(<DefinitionDesignerPage {...pageProps} active={false} />);
  expect(container.querySelector('iframe')).toBe(frame);
  await act(async () =>
    window.dispatchEvent(
      new MessageEvent('message', {
        origin: location.origin,
        source: frame.contentWindow,
        data: { ...message, type: 'saved' },
      }),
    ),
  );
  expect(await guard()).toBe(true);
});
