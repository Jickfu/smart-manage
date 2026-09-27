// @vitest-environment jsdom
import { installJsdomBrowserStubs } from '@/test/jsdomBrowserStubs';
import { act, type ComponentProps } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import ListColumnFilter from './ListColumnFilter';

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

async function renderFilter(props: ComponentProps<typeof ListColumnFilter>) {
  await act(async () => root.render(<ListColumnFilter {...props} />));
}

async function clickOperator(label: string) {
  const button = [...container.querySelectorAll('button')].find(
    (candidate) => candidate.textContent?.replace('✓', '') === label,
  );
  expect(button).toBeTruthy();
  await act(async () => button!.click());
}

async function enterValue(value: string) {
  const input = container.querySelector<HTMLInputElement>('.sm-list-column-filter-value input')!;
  await act(async () => {
    Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')!.set!.call(input, value);
    input.dispatchEvent(new Event('input', { bubbles: true }));
  });
  return input;
}

describe('列表表头输入型筛选', () => {
  it.each([
    { type: 'string' as const, nextLabel: '等于', value: '销售' },
    { type: 'number' as const, nextLabel: '大于', value: '120.5' },
  ])('$type 切换操作符时保留输入值', async ({ type, nextLabel, value }) => {
    const onConfirm = vi.fn();
    await renderFilter({ field: 'field', type, onConfirm });
    const input = await enterValue(value);
    await clickOperator(nextLabel);
    expect(container.querySelector<HTMLInputElement>('.sm-list-column-filter-value input')).toBe(
      input,
    );
    expect(input.value).toBe(value);
    await act(async () =>
      [...container.querySelectorAll('button')]
        .find((candidate) => candidate.textContent?.replace(/\s/g, '') === '确定')!
        .click(),
    );
    expect(onConfirm).toHaveBeenCalledWith({
      field: 'field',
      type,
      operator: type === 'string' ? 'EQ' : 'GT',
      value,
    });
  });

  it('字符串切到无值操作符再切回时保留原输入', async () => {
    await renderFilter({ field: 'name', type: 'string', onConfirm: vi.fn() });
    await enterValue('采购');
    await clickOperator('为空');
    expect(container.querySelector('.sm-list-column-filter-value')).toBeNull();
    await clickOperator('不等于');
    expect(
      container.querySelector<HTMLInputElement>('.sm-list-column-filter-value input')?.value,
    ).toBe('采购');
  });

  it('日期切换到其他操作符再切回时保留当前日期草稿', async () => {
    await renderFilter({
      field: 'createdTime',
      type: 'date',
      value: {
        field: 'createdTime',
        type: 'date',
        operator: 'EQ',
        value: '2026-09-15',
      },
      onConfirm: vi.fn(),
    });
    expect(
      container.querySelector<HTMLInputElement>('.sm-list-column-filter-value input')?.value,
    ).toBe('2026-09-15');
    await clickOperator('今天');
    expect(container.querySelector('.sm-list-column-filter-value')).toBeNull();
    await clickOperator('等于');
    expect(
      container.querySelector<HTMLInputElement>('.sm-list-column-filter-value input')?.value,
    ).toBe('2026-09-15');
  });

  it('日期区间切换到等于再切回时保留区间草稿', async () => {
    await renderFilter({
      field: 'createdTime',
      type: 'date',
      value: {
        field: 'createdTime',
        type: 'date',
        operator: 'BETWEEN',
        values: ['2026-09-01', '2026-09-27'],
      },
      onConfirm: vi.fn(),
    });
    await clickOperator('从…到…');
    expect(
      [...container.querySelectorAll<HTMLInputElement>('.sm-list-column-filter-value input')].map(
        (input) => input.value,
      ),
    ).toEqual(['2026-09-01', '2026-09-27']);
    await clickOperator('等于');
    await clickOperator('从…到…');
    expect(
      [...container.querySelectorAll<HTMLInputElement>('.sm-list-column-filter-value input')].map(
        (input) => input.value,
      ),
    ).toEqual(['2026-09-01', '2026-09-27']);
  });
});
