// @vitest-environment jsdom
import { act, type ComponentProps } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { Button, ConfigProvider } from 'antd';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { afterEach, beforeEach, expect, it, vi } from 'vitest';
import RefSelector, { type RefSelectorFooterContext } from './RefSelector';

type RecordValue = { id: string; name: string };
const record = { id: '1', name: '候选一' };
let root: Root;
let container: HTMLDivElement;
let client: QueryClient;

beforeEach(() => {
  vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
  vi.stubGlobal(
    'ResizeObserver',
    class {
      observe() {}
      unobserve() {}
      disconnect() {}
    },
  );
  const getComputedStyle = window.getComputedStyle.bind(window);
  vi.spyOn(window, 'getComputedStyle').mockImplementation((element) => getComputedStyle(element));
  Object.defineProperty(window, 'matchMedia', {
    configurable: true,
    value: () => ({
      matches: false,
      addListener() {},
      removeListener() {},
    }),
  });
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
async function renderSelector(
  props: Partial<ComponentProps<typeof RefSelector<RecordValue>>> = {},
  disabled = false,
) {
  await act(async () =>
    root.render(
      <QueryClientProvider client={client}>
        <ConfigProvider componentDisabled={disabled}>
          <RefSelector<RecordValue>
            selectorKey="footer-test"
            fetchFn={async () => ({ records: [record], total: 1 })}
            displayRender={(item) => item.name}
            fieldNames={{ key: 'id', label: 'name' }}
            columns={[{ title: '名称', dataIndex: 'name' }]}
            modalTitle="选择记录"
            trigger={<Button>打开</Button>}
            {...props}
          />
        </ConfigProvider>
      </QueryClientProvider>,
    ),
  );
}
async function click(label: string) {
  const button = [...document.querySelectorAll('button')].find(
    (item) => item.textContent?.replace(/\s/g, '') === label,
  )!;
  expect(button).toBeTruthy();
  await act(async () => button.click());
}
async function open() {
  await click('打开');
  await act(async () => {
    await new Promise((resolve) => setTimeout(resolve, 0));
  });
}
function candidateRow() {
  return document.querySelector<HTMLTableRowElement>('tr[data-row-key="1"]')!;
}

it('keeps default confirmation and cancellation semantics', async () => {
  const onChange = vi.fn();
  await renderSelector({ onChange });
  await open();
  await act(async () => candidateRow().click());
  await click('取消');
  expect(onChange).not.toHaveBeenCalled();
  await open();
  await act(async () => candidateRow().click());
  await click('确定');
  expect(onChange).toHaveBeenCalledExactlyOnceWith(record);
});

it.each(['button', 'double-click'] as const)(
  'uses the same async confirmation for %s and retains selection on rejection',
  async (entry) => {
    const onChange = vi.fn();
    let context!: RefSelectorFooterContext<RecordValue>;
    let rejectConfirm!: (reason: Error) => void;
    const onConfirm = vi.fn(
      () =>
        new Promise<void>((_resolve, reject) => {
          rejectConfirm = reject;
        }),
    );
    const footer = (current: RefSelectorFooterContext<RecordValue>) => {
      context = current;
      return (
        <Button loading={current.confirming} onClick={() => void current.confirm()}>
          业务确认
        </Button>
      );
    };
    await renderSelector({ onChange, onConfirm, footer });
    await open();
    await act(async () => candidateRow().click());
    expect(context.selectedRecords).toEqual([record]);
    if (entry === 'button') await click('业务确认');
    else
      await act(async () =>
        candidateRow().dispatchEvent(new MouseEvent('dblclick', { bubbles: true })),
      );
    expect(context.confirming).toBe(true);
    expect(onConfirm).toHaveBeenCalledExactlyOnceWith([record]);
    await act(async () => {
      context.cancel();
      context.clearSelection();
      await context.confirm();
    });
    expect(context.selectedRecords).toEqual([record]);
    expect(onConfirm).toHaveBeenCalledTimes(1);
    await act(async () => rejectConfirm(new Error('业务失败')));
    expect(context.confirming).toBe(false);
    expect(onChange).not.toHaveBeenCalled();
    expect(context.selectedRecords).toEqual([record]);
    onConfirm.mockImplementation(async () => undefined);
    await click('业务确认');
    expect(onChange).toHaveBeenCalledExactlyOnceWith(record);
  },
);

it('supports multiple selection, veto, clear and save-time revocation of an open portal', async () => {
  let context!: RefSelectorFooterContext<RecordValue>;
  const onChange = vi.fn();
  const onConfirm = vi.fn().mockResolvedValue(false);
  const props = {
    mode: 'multiple' as const,
    onChange,
    onConfirm,
    footer: (current: RefSelectorFooterContext<RecordValue>) => {
      context = current;
      return null;
    },
  };
  await renderSelector(props);
  await open();
  await act(async () => candidateRow().click());
  await act(async () => {
    expect(await context.confirm()).toBe(false);
  });
  expect(onChange).not.toHaveBeenCalled();
  await act(async () => context.clearSelection());
  expect(context.selectedRecords).toEqual([]);
  await act(async () => candidateRow().click());
  await renderSelector(props, true);
  await act(async () => {
    expect(await context.confirm()).toBe(false);
  });
  expect(onConfirm).toHaveBeenCalledTimes(1);
  expect(onChange).not.toHaveBeenCalled();
});

it('does not submit a stale async result after disabling and re-enabling the selector', async () => {
  const onChange = vi.fn();
  let complete!: () => void;
  let context!: RefSelectorFooterContext<RecordValue>;
  const onConfirm = vi.fn(
    () =>
      new Promise<void>((resolve) => {
        complete = resolve;
      }),
  );
  const props = {
    onChange,
    onConfirm,
    footer: (current: RefSelectorFooterContext<RecordValue>) => {
      context = current;
      return null;
    },
  };
  await renderSelector(props);
  await open();
  await act(async () => candidateRow().click());
  let result!: Promise<boolean>;
  await act(async () => {
    result = context.confirm();
  });
  await renderSelector(props, true);
  await renderSelector(props, false);
  await act(async () => complete());
  expect(await result).toBe(false);
  expect(onChange).not.toHaveBeenCalled();
});

it('revokes an in-flight selection commit when the owning page unmounts', async () => {
  const onChange = vi.fn();
  let complete!: () => void;
  let context!: RefSelectorFooterContext<RecordValue>;
  await renderSelector({
    onChange,
    onConfirm: () =>
      new Promise<void>((resolve) => {
        complete = resolve;
      }),
    footer: (current) => {
      context = current;
      return null;
    },
  });
  await open();
  let result!: Promise<boolean>;
  await act(async () => {
    result = context.confirm();
  });
  await act(async () => root.render(<div />));
  await act(async () => complete());
  expect(await result).toBe(false);
  expect(onChange).not.toHaveBeenCalled();
});
