export interface ScriptTemplate {
  key: string;
  name: string;
  description: string;
  content: string;
}

export const scriptTemplates: ScriptTemplate[] = [
  {
    key: 'call-no-argument',
    name: '调用无参数方法',
    description: '获取领域 Service，调用一个不需要参数的公开方法并返回结果。',
    content: `const service = app.getService('yourService');

const result = service.yourMethod();
console.log(result);
return result;`,
  },
  {
    key: 'query-detail',
    name: '查询单条数据',
    description: '调用接收 ID 的详情方法，查看生产环境中的真实返回值。',
    content: `const service = app.getService('yourService');

const result = service.detail(10001);
console.log(result);
return result;`,
  },
  {
    key: 'form-argument',
    name: '传入 Form 对象',
    description: '普通 JavaScript 对象会按 Service 方法声明的参数类型转换。',
    content: `const service = app.getService('yourService');

const form = {
  id: 10001,
  version: 0,
};

const result = service.yourMethod(form);
return result;`,
  },
  {
    key: 'atomic-loop',
    name: '原子事务批量处理',
    description: '循环调用多个方法；请在控制台选择“原子事务”，使普通数据库操作失败时整体回滚。',
    content: `const service = app.getService('yourService');
const ids = [10001, 10002, 10003];

for (const id of ids) {
  service.yourMethod({ id });
}

return { success: true, count: ids.length };`,
  },
  {
    key: 'inspect-result',
    name: '检查返回结果',
    description: '读取返回对象字段，根据真实数据进行判断并输出诊断信息。',
    content: `const service = app.getService('yourService');
const result = service.yourMethod({ id: 10001 });

if (!result) {
  return { found: false };
}

console.log({ id: result.id, status: result.status });
return { found: true, data: result };`,
  },
];
