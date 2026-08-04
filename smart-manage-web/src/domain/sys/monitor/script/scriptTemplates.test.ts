import { describe, expect, it } from 'vitest';
import { scriptTemplates } from './scriptTemplates';

describe('scriptTemplates', () => {
  it('模板键唯一且包含可执行入口', () => {
    expect(new Set(scriptTemplates.map((template) => template.key)).size).toBe(
      scriptTemplates.length,
    );
    expect(scriptTemplates.length).toBeGreaterThanOrEqual(5);
    scriptTemplates.forEach((template) => {
      expect(template.name.trim()).not.toBe('');
      expect(template.content).toContain("app.getService('");
      expect(template.content).toContain('return');
    });
  });
});
