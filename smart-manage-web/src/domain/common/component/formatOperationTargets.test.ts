import { expect, it } from 'vitest';
import { formatOperationTargets } from './formatOperationTargets';

it('批量摘要说明完整命令数量及未展开数量，不丢失当前页外的对象身份', () => {
  const labels = ['角色 A', 'ID outside-page', '角色 C', '角色 D', '角色 E', '角色 F'];
  const summary = formatOperationTargets(labels);
  expect(summary).toContain('共 6 项');
  expect(summary).toContain('ID outside-page');
  expect(summary).toContain('另有 1 项未展开');
  expect(summary).not.toContain('角色 F');
  expect(labels).toHaveLength(6);
});

it('对象中的换行与引号使用字面形式，不伪装成另一行操作说明', () => {
  expect(formatOperationTargets(['名称"\n删除成功'])).toBe('共 1 项："名称\\\"\\n删除成功"。');
});
