import { test } from 'node:test';
import assert from 'node:assert/strict';
import { designerDefinition, feedbackPayload } from './protocol.mjs';

test('官方经典属性面板数组和节点权限串均能回显多个办理人规则', () => {
  const rules = ['sm:user:1', 'sm:role:2', 'sm:leader'];
  const expected = { storageIds: 'sm:user:1,sm:role:2,sm:leader' };
  assert.deepEqual(feedbackPayload({ storageIds: rules }), expected);
  assert.deepEqual(feedbackPayload({ storageIds: rules.join('@@') }), expected);
  assert.deepEqual(feedbackPayload({ storageIds: [] }), { storageIds: '' });
  assert.throws(() => feedbackPayload({ storageIds: [{}] }));
});

test('经典画布缺失或无效文字坐标按节点定位，不改动原定义和历史轨迹', () => {
  const source = {
    modelValue: 'CLASSICS',
    nodeList: [
      { nodeType: 0, coordinate: '200,200' },
      { nodeType: 1, coordinate: '400,200|NaN,NaN', skipList: [{ condition: 'days > 1' }] },
      { nodeType: 2, coordinate: '600,200|' },
      { nodeType: 3, coordinate: '400,400' },
    ],
    activityNodeList: ['review'],
  };
  const original = structuredClone(source);
  const result = designerDefinition(source);
  assert.deepEqual(result.nodeList.map(node => node.coordinate), [
    '200,200|200,240', '400,200|400,200', '600,200|600,240', '400,400|400,440',
  ]);
  assert.deepEqual(source, original);
  assert.deepEqual(result.activityNodeList, source.activityNodeList);
  assert.deepEqual(result.nodeList[1].skipList, source.nodeList[1].skipList);
});

test('有效自定义文字位置包括零坐标保持不变，重复处理结果稳定', () => {
  const source = { modelValue: 'CLASSICS', nodeList: [
    { nodeType: 0, coordinate: '200,200|0,0' },
    { nodeType: 1, coordinate: '400,200|-20,235.5' },
  ] };
  assert.deepEqual(designerDefinition(source), source);
  assert.deepEqual(designerDefinition(designerDefinition(source)), source);
});

test('不伪造无效节点位置，仿钉钉模式保留官方布局数据', () => {
  for (const coordinate of [undefined, 'NaN,200', ',200', '1,2,3']) {
    assert.throws(() => designerDefinition({ modelValue: 'CLASSICS', nodeList: [
      { nodeName: '审批', nodeType: 1, coordinate },
    ] }), /审批.*坐标无效/);
  }
  const mimic = { modelValue: 'MIMIC', nodeList: [{ nodeType: 1 }] };
  assert.deepEqual(designerDefinition(mimic), mimic);
});
