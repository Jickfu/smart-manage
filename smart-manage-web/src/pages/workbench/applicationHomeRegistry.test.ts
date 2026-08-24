import { describe, expect, it } from 'vitest';
import { resolveApplicationHome } from './applicationHomeRegistry';

describe('applicationHomeRegistry', () => {
  it('resolves registered application homes', () => {
    expect(resolveApplicationHome('procurement')).toBeTruthy();
    expect(resolveApplicationHome('scheduler')).toBeTruthy();
  });

  it('returns undefined for unknown applications so the router can render its empty fallback', () => {
    expect(resolveApplicationHome('unknown-app')).toBeUndefined();
  });
});
