// @vitest-environment jsdom
import { act, type ComponentProps } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { Form, Input } from 'antd';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import EditPage from './EditPage';
import ModalEditPage from './ModalEditPage';
import { OperationType } from '../types';
import { ApiError } from '@/api/ApiError';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';

const mocks = vi.hoisted(() => ({
  feedback: { fromError: vi.fn(), warning: vi.fn() },
  dirty: undefined as { current: boolean } | undefined,
}));
vi.mock('@/domain/common/component/useOperationFeedback', () => ({
  useOperationFeedback: () => mocks.feedback,
}));
vi.mock('../tab/useBeforeCloseGuard', () => ({
  useBeforeCloseGuard: (_app: unknown, _tab: unknown, dirty: { current: boolean }) => {
    mocks.dirty = dirty;
  },
}));

let container: HTMLDivElement;
let root: Root;
let queryClient: QueryClient;
const denied = new ApiError({ source: 'API', message: '无权访问', apiCode: 100403 });
const sections = [
  {
    key: 'basic',
    label: '基本信息',
    content: () => (
      <Form.Item name="name" label="名称">
        <Input aria-label="名称" />
      </Form.Item>
    ),
  },
];

beforeEach(() => {
  vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
  // jsdom 无伪元素布局；仅忽略滚动条测量使用的第二参数，保留真实元素样式计算。
  const getComputedStyle = window.getComputedStyle.bind(window);
  vi.spyOn(window, 'getComputedStyle').mockImplementation((element) => getComputedStyle(element));
  Object.defineProperty(window, 'matchMedia', {
    configurable: true,
    value: vi.fn().mockImplementation(() => ({
      matches: false,
      addListener: vi.fn(),
      removeListener: vi.fn(),
    })),
  });
  container = document.createElement('div');
  document.body.append(container);
  root = createRoot(container);
  queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  vi.clearAllMocks();
});
afterEach(async () => {
  await act(async () => root.unmount());
  queryClient.clear();
  container.remove();
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});

async function renderEdit(props: Partial<ComponentProps<typeof EditPage>> = {}) {
  await act(async () =>
    root.render(
      <QueryClientProvider client={queryClient}>
        <EditPage
          title="编辑"
          operationType={OperationType.EDIT}
          sections={sections}
          initialValues={{ name: '原值' }}
          {...props}
        />
      </QueryClientProvider>,
    ),
  );
}
async function enterName(value: string) {
  const input = document.querySelector<HTMLInputElement>('input[aria-label="名称"]')!;
  await act(async () => {
    Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')!.set!.call(input, value);
    input.dispatchEvent(new Event('input', { bubbles: true }));
  });
  return input;
}
async function clickButton(label: string) {
  const button = [...document.querySelectorAll('button')].find(
    (candidate) => candidate.textContent?.replace(/\s/g, '') === label,
  )!;
  expect(button).toBeTruthy();
  await act(async () => button.click());
}

describe('edit error ownership and state preservation', () => {
  it('keeps dirty input mounted through background failure, denial and retry', async () => {
    const onSave = vi.fn();
    await renderEdit({ onSave });
    const input = await enterName('未保存');
    expect(mocks.dirty?.current).toBe(true);
    await renderEdit({
      onSave,
      initialValues: { name: '原值' },
      error: getBlockingQueryError({
        data: { name: '原值' },
        error: new ApiError({ source: 'NETWORK', message: '' }),
      }),
    });
    expect(input.value).toBe('未保存');
    await renderEdit({ onSave, error: denied });
    expect(container.querySelector('input')).toBe(input);
    expect(input.closest('[hidden][inert]')).not.toBeNull();
    await clickButton('保存');
    expect(onSave).not.toHaveBeenCalled();
    expect(mocks.dirty?.current).toBe(true);
    await renderEdit({ onSave, initialValues: { name: '重试后的服务器值' } });
    expect(input.closest('[hidden]')).toBeNull();
    expect(input.value).toBe('未保存');
    expect(mocks.dirty?.current).toBe(true);
  });

  it('reports transformValues failure once before entering a mutation', async () => {
    const error = new Error('internal attachment state');
    const onSave = vi.fn();
    await renderEdit({
      onSave,
      transformValues: () => {
        throw error;
      },
    });
    await enterName('修改');
    await clickButton('保存');
    expect(onSave).not.toHaveBeenCalled();
    expect(mocks.feedback.fromError).toHaveBeenCalledExactlyOnceWith(
      error,
      '表单数据组装失败，请检查输入后重试',
    );
    expect(mocks.dirty?.current).toBe(true);
  });

  it('keeps cancellation and mutation failure dirty without duplicate feedback', async () => {
    await renderEdit({ onSave: async () => false });
    await enterName('修改');
    await clickButton('保存');
    expect(mocks.dirty?.current).toBe(true);
    await renderEdit({
      onSave: async () => {
        throw new Error('owned by mutation');
      },
    });
    await clickButton('保存');
    expect(mocks.dirty?.current).toBe(true);
    expect(mocks.feedback.fromError).not.toHaveBeenCalled();
  });

  it('marks a completed save clean without reverting input to stale initial values', async () => {
    const initialValues = { name: '原值' };
    const onSave = vi.fn(async () => undefined);
    await renderEdit({ initialValues, onSave });
    const input = await enterName('已保存的新值');
    await clickButton('保存');
    expect(onSave).toHaveBeenCalledWith({ name: '已保存的新值' });
    expect(input.value).toBe('已保存的新值');
    expect(mocks.dirty?.current).toBe(false);
  });

  it('rechecks resource access after asynchronous validation', async () => {
    let finishValidation!: () => void;
    const onSave = vi.fn();
    const validatedSections = [
      {
        key: 'basic',
        label: '基本信息',
        content: () => (
          <Form.Item
            name="name"
            rules={[
              {
                validator: () =>
                  new Promise<void>((resolve) => {
                    finishValidation = resolve;
                  }),
              },
            ]}
          >
            <Input />
          </Form.Item>
        ),
      },
    ];
    await renderEdit({ onSave, sections: validatedSections });
    await clickButton('保存');
    await renderEdit({ onSave, sections: validatedSections, error: denied });
    await act(async () => finishValidation());
    expect(onSave).not.toHaveBeenCalled();
  });

  it('preserves modal form input and disables save while denied', async () => {
    const onSave = vi.fn();
    const renderModal = async (error?: Error) =>
      act(async () =>
        root.render(
          <QueryClientProvider client={queryClient}>
            <ModalEditPage
              title="编辑"
              open
              onClose={() => undefined}
              fields={[{ type: 'text', label: '名称', dataIndex: 'name' }]}
              initialValues={{ name: '原值' }}
              onSave={onSave}
              error={error}
            />
          </QueryClientProvider>,
        ),
      );
    await renderModal();
    const input = document.querySelector<HTMLInputElement>('input')!;
    await act(async () => {
      Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')!.set!.call(
        input,
        '未保存',
      );
      input.dispatchEvent(new Event('input', { bubbles: true }));
    });
    await renderModal(denied);
    expect(document.querySelector('input')).toBe(input);
    expect(input.closest('[hidden][inert]')).not.toBeNull();
    await clickButton('保存');
    expect(onSave).not.toHaveBeenCalled();
    await renderModal();
    expect(input.value).toBe('未保存');
  });
});
