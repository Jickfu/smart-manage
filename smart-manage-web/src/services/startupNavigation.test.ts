import { describe, expect, it } from 'vitest';
import { parseStartupNavigation } from './startupNavigation';

describe('parseStartupNavigation', () => {
  it('parses an application and its startup menu entry', () => {
    expect(parseStartupNavigation('?app=sys_base&entry=user_list')).toEqual({
      appNumber: 'sys_base',
      entryNumber: 'user_list',
    });
  });

  it('trims values and ignores an empty entry', () => {
    expect(parseStartupNavigation('?app=%20sys_base%20&entry=%20')).toEqual({
      appNumber: 'sys_base',
      entryNumber: undefined,
    });
  });

  it('defaults to home when the application is absent', () => {
    expect(parseStartupNavigation('')).toEqual({ appNumber: 'home', entryNumber: undefined });
  });
});
