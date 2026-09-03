import { describe, expect, it } from 'vitest';
import { keepClickedNodeSelected } from './listTreeSelection';

describe('keepClickedNodeSelected', () => {
  it('首次选择节点时保留 Ant Design 返回的选中节点', () => {
    expect(keepClickedNodeSelected(['domain:1'], 'domain:1')).toEqual(['domain:1']);
  });

  it('重复点击已选节点时仍保持该节点选中', () => {
    expect(keepClickedNodeSelected([], 'domain:1')).toEqual(['domain:1']);
  });
});
