import { describe, expect, it } from 'vitest';
import { parseSchedulerScope } from './schedulerScope';
import { executionQueryKeys } from '../execution/queryKeys';
import { jobQueryKeys } from '../job/queryKeys';

describe('调度业务归属查询', () => {
  it('领域汇总和应用节点携带正确范围，并保留 bigint 精度', () => {
    expect(parseSchedulerScope('all')).toEqual({});
    expect(parseSchedulerScope('domain:9400000010')).toEqual({ domainId: '9400000010' });
    expect(parseSchedulerScope('app:9400000010:9400000000000000020')).toEqual({
      domainId: '9400000010',
      appId: '9400000000000000020',
    });
  });

  it('领域汇总和应用筛选不会复用列表缓存', () => {
    const oldScope = parseSchedulerScope('domain:10');
    const newScope = parseSchedulerScope('app:10:20');
    expect(executionQueryKeys.list(undefined, oldScope)).not.toEqual(
      executionQueryKeys.list(undefined, newScope),
    );
    expect(jobQueryKeys.list(undefined, oldScope)).not.toEqual(
      jobQueryKeys.list(undefined, newScope),
    );
  });
});
