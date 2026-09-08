// @vitest-environment jsdom
import { act, useMemo, type ComponentProps } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { Button, Form, Input } from 'antd';
import { QueryClient, QueryClientProvider, useQuery } from '@tanstack/react-query';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import EditPage from './EditPage';
import ModalEditPage from './ModalEditPage';
import { EditFormFields } from './EditFormFields';
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
const defaultInitialValues = { name: '原值' };
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
  // jsdom 不提供尺寸观察器，提示浮层使用静态尺寸替身。
  vi.stubGlobal(
    'ResizeObserver',
    class {
      observe() {}
      unobserve() {}
      disconnect() {}
    },
  );
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
          initialValues={defaultInitialValues}
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
  it('focuses the current form error only after the validation freeze is released', async () => {
    const onSave = vi.fn();
    await renderEdit({
      onSave,
      initialValues: { name: '' },
      sections: [
        {
          key: 'required',
          label: '必填',
          content: () => (
            <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入名称' }]}>
              <Input aria-label="名称" />
            </Form.Item>
          ),
        },
      ],
    });
    await clickButton('保存');
    const input = container.querySelector('input')!;
    expect(input.closest('[inert]')).toBeNull();
    expect(document.activeElement).toBe(input);
    expect(onSave).not.toHaveBeenCalled();
  });

  it('isolates mounted field labels and keeps identity across tab promotion', async () => {
    const renderTabs = async (tabKey: string) =>
      act(async () =>
        root.render(
          <QueryClientProvider client={queryClient}>
            <div hidden>
              <EditPage title="旧页" sections={sections} operationType={OperationType.EDIT} />
            </div>
            <EditPage
              title="当前页"
              sections={sections}
              operationType={OperationType.EDIT}
              closeGuard={{ appNumber: 'sys', tabKey }}
            />
          </QueryClientProvider>,
        ),
      );
    await renderTabs('temporary');
    const inputs = [...container.querySelectorAll<HTMLInputElement>('input')];
    expect(new Set(inputs.map((input) => input.id)).size).toBe(2);
    const labels = [...container.querySelectorAll('label')];
    labels.forEach((label, index) => expect(label.control).toBe(inputs[index]));
    const currentId = inputs[1]!.id;
    await renderTabs('persisted');
    expect(container.querySelectorAll('input')[1]!.id).toBe(currentId);
  });

  it.each(['success', 'failure'] as const)(
    'freezes all section entry points while saving and restores after %s',
    async (outcome) => {
      let resolveSave!: () => void;
      let rejectSave!: (error: Error) => void;
      const onSave = vi.fn(
        () =>
          new Promise<void>((resolve, reject) => {
            resolveSave = resolve;
            rejectSave = reject;
          }),
      );
      const editableStates: boolean[] = [];
      const editSections = [
        {
          ...sections[0]!,
          content: (editable: boolean) => {
            editableStates.push(editable);
            return (
              <>
                {sections[0]!.content()}
                <input aria-label="自定义字段" />
                <Button disabled={!editable}>明细新增</Button>
              </>
            );
          },
          extra: (editable: boolean) => <Button disabled={!editable}>外部编辑</Button>,
        },
      ];
      await renderEdit({ onSave, sections: editSections });
      const input = await enterName('待保存');
      await clickButton('保存');
      expect(input.disabled).toBe(true);
      expect(input.closest('[inert]')).not.toBeNull();
      expect(editableStates.at(-1)).toBe(false);
      for (const label of ['明细新增', '外部编辑']) {
        const button = [...container.querySelectorAll('button')].find(
          (candidate) => candidate.textContent === label,
        )!;
        expect(button.disabled).toBe(true);
      }
      expect(mocks.dirty?.current).toBe(true);
      if (outcome === 'success') {
        await renderEdit({ onSave, sections: editSections, initialValues: { name: '服务端快照' } });
        await act(async () => resolveSave());
        expect(input.value).toBe('服务端快照');
        expect(mocks.dirty?.current).toBe(false);
      } else {
        await act(async () => rejectSave(new Error('保存失败')));
        expect(input.value).toBe('待保存');
        expect(mocks.dirty?.current).toBe(true);
      }
      expect(input.disabled).toBe(false);
      expect(input.closest('[inert]')).toBeNull();
    },
  );

  it('keeps failed rerenders unchanged but synchronizes a successful new server version', async () => {
    const queryKey = ['edit-snapshot'];
    let server = { name: '版本一', version: 1 };
    let failure: Error | undefined;
    const onSave = vi.fn();
    function QueryEditor({ renderCount }: { renderCount: number }) {
      const query = useQuery({
        queryKey,
        retry: false,
        queryFn: async () => {
          if (failure) throw failure;
          return server;
        },
      });
      const initialValues = useMemo(
        () => (query.data ? { name: query.data.name } : undefined),
        [query.data],
      );
      return (
        <EditPage
          title={`编辑${renderCount}`}
          operationType={OperationType.EDIT}
          sections={sections}
          initialValues={initialValues}
          loading={query.isLoading}
          error={getBlockingQueryError(query)}
          onSave={async (values) => {
            onSave({ ...values, version: query.data?.version });
          }}
        />
      );
    }
    const render = async (renderCount: number) =>
      act(async () =>
        root.render(
          <QueryClientProvider client={queryClient}>
            <QueryEditor renderCount={renderCount} />
          </QueryClientProvider>,
        ),
      );
    const settleQuery = async () =>
      act(async () => {
        await new Promise((resolve) => setTimeout(resolve, 0));
      });
    await render(1);
    await settleQuery();
    const input = await enterName('未保存');
    await render(2);
    expect(input.value).toBe('未保存');
    failure = new ApiError({ source: 'NETWORK', message: '' });
    await act(async () => {
      await queryClient.refetchQueries({ queryKey });
    });
    await settleQuery();
    expect(input.value).toBe('未保存');
    failure = denied;
    await act(async () => {
      await queryClient.refetchQueries({ queryKey });
    });
    await settleQuery();
    expect(input.closest('[hidden][inert]')).not.toBeNull();
    failure = undefined;
    // 服务端返回新对象但内容相同，TanStack structural sharing 保留快照引用。
    server = { ...server };
    await act(async () => {
      await queryClient.refetchQueries({ queryKey });
    });
    await settleQuery();
    expect(input.value).toBe('未保存');
    server = { name: '版本二', version: 2 };
    await act(async () => {
      await queryClient.refetchQueries({ queryKey });
    });
    await settleQuery();
    expect(input.value).toBe('版本二');
    await clickButton('保存');
    expect(onSave).toHaveBeenCalledExactlyOnceWith({ name: '版本二', version: 2 });
  });

  it('does not consume a new snapshot while blocked', async () => {
    await renderEdit();
    const input = await enterName('未保存');
    const nextValues = { name: '新快照' };
    await renderEdit({ initialValues: nextValues, error: denied });
    expect(input.value).toBe('未保存');
    await renderEdit({ initialValues: nextValues });
    expect(input.value).toBe('新快照');
  });

  it('rehydrates the same cached record after closing and reopening the modal', async () => {
    const renderModal = async (open: boolean) =>
      act(async () =>
        root.render(
          <QueryClientProvider client={queryClient}>
            <ModalEditPage
              title="编辑"
              open={open}
              onClose={() => undefined}
              fields={[{ type: 'text', label: '名称', dataIndex: 'name' }]}
              initialValues={defaultInitialValues}
              onSave={async () => undefined}
            />
          </QueryClientProvider>,
        ),
      );
    await renderModal(true);
    expect(document.querySelector<HTMLInputElement>('input')?.value).toBe('原值');
    await renderModal(false);
    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 500));
    });
    await renderModal(true);
    expect(document.querySelector<HTMLInputElement>('input')?.value).toBe('原值');
  });

  it('keeps dirty input mounted through background failure, denial and retry', async () => {
    const onSave = vi.fn();
    await renderEdit({ onSave });
    const input = await enterName('未保存');
    expect(mocks.dirty?.current).toBe(true);
    await renderEdit({
      onSave,
      initialValues: defaultInitialValues,
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
    await renderEdit({ onSave, initialValues: defaultInitialValues });
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
              initialValues={defaultInitialValues}
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

describe('通用字段说明', () => {
  it('在标签旁展示调用方问号提示，支持录入、只读和自定义字段且不增加 extra 区域', async () => {
    await act(async () => {
      root.render(
        <Form layout="vertical">
          <EditFormFields
            fields={[
              { label: '录入字段', dataIndex: 'input', type: 'text', tooltip: '调用方提供的说明' },
              { label: '只读字段', dataIndex: 'readonly', type: 'readonly', tooltip: '只读说明' },
              {
                label: '自定义字段',
                dataIndex: 'custom',
                type: 'custom',
                content: '内容',
                tooltip: '自定义说明',
              },
              { label: '普通字段', dataIndex: 'plain', type: 'text' },
            ]}
          />
        </Form>,
      );
    });
    const icons = container.querySelectorAll('.ant-form-item-label .anticon-question-circle');
    expect(icons).toHaveLength(3);
    expect(container.querySelector('.ant-form-item-extra')).toBeNull();
    expect(document.querySelector('[role="tooltip"]')).toBeNull();
    await act(async () => {
      icons[0]!.dispatchEvent(new MouseEvent('mouseover', { bubbles: true }));
      await new Promise((resolve) => setTimeout(resolve, 200));
    });
    expect(document.querySelector('[role="tooltip"]')?.textContent).toBe('调用方提供的说明');
  });
});
