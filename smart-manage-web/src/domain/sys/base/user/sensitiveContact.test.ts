import { describe, expect, it } from 'vitest';
import { resolveSensitiveContactUpdate } from './sensitiveContact';

describe('resolveSensitiveContactUpdate', () => {
  it('omits masked values when the operator neither sees nor replaces the original', () => {
    expect(
      resolveSensitiveContactUpdate('138****1234', {
        isAddNew: false,
        canReadSensitive: false,
        changed: false,
      }),
    ).toBeUndefined();
  });

  it('submits an explicitly re-entered value', () => {
    expect(
      resolveSensitiveContactUpdate(' 13812341234 ', {
        isAddNew: false,
        canReadSensitive: false,
        changed: true,
      }),
    ).toBe('13812341234');
  });
});
