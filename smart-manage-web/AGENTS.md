# 前端开发规则

本文件适用于 `smart-manage-web`。同时继承根目录 `AGENTS.md`。

## 技术与目录

- 使用 pnpm、React、TypeScript 和 Ant Design，不引入其他 UI 组件库。
- 使用 `@/` 别名引用 `src`。
- 业务页面按 `src/domain/{领域}/{应用}/{单据}` 组织。
- 页面组件目录使用 PascalCase，工具和 Hook 文件使用 camelCase。
- 前端 ID 必须使用 `string`，避免雪花 ID 精度丢失。

## 页面与状态边界

- 页面壳层只负责布局、加载、错误、权限和按钮区。
- 表单或列表基础组件负责字段渲染、过滤器、表格和引用选择器。
- 领域页面负责状态流转、Mutation、明细聚合和业务命令。
- TanStack Query 管理服务端状态，Zustand 管理跨页面客户端状态，Ant Design Form 管理编辑状态。
- 服务端查询使用 `useQuery`；保存、提交、删除和高风险命令使用 `useMutation` 或项目领域命令封装。
- 每个业务单据维护 Query Key Factory，至少包含 `all`、`lists`、`list(params)`、`details` 和 `detail(id)`。
- 禁止在页面中重复维护通用提交 loading 和通用错误提示。

## 页面注册与单据生命周期

- 页面只能通过 `pageRegistration.ts(x)` 白名单注册，禁止根据后端字符串、文件名或目录任意动态加载。
- 页面键重复、空注册清单或生成结果不一致必须直接失败，不保留兼容逻辑。
- 列表页单实例；编辑页按真实 ID 多实例；新增页每次使用新的临时 UUID。
- 编辑和查看同一单据共用同一个 tab key。
- 新增保存成功后重新查询详情，并将临时 tab key 替换为真实单据 tab key。
- 保存成功后不关闭编辑页，继续留在当前单据页面。
- 保存和提交必须区分；已提交等只读状态进入 `VIEW`，禁止普通保存。
- 新增和编辑页都必须注册脏数据关闭保护。
- 即使没有明细，主从单据也必须使用 `entrys: []`。
- 业务详情、记录详情和只读查看必须使用工作台通用编辑/查看页签，禁止使用 `Drawer` 抽屉承载；抽屉只允许用于不形成独立页面的临时辅助操作。

## 样式与校验

- 禁止在 TSX 中编写 CSS 或内联 `style`。
- 自定义 CSS 类名以 `sm-` 开头，优先使用 flex 布局。
- 公共 `ListPage` 的列定义必须至少保留一个未设置 `width` 的业务列，用于吸收表格剩余空间，避免选择列和序号列被同比放大。
- 表单字段校验由 Ant Design Form 负责；没有明确的外部不可信数据校验需求时不引入重复校验模型。
- ESLint 报错时，未经用户允许不得用注释跳过，也不得修改 `eslint.config.js` 降低规则。
- 只有需要自动修复格式或用户明确要求时才执行 `pnpm lint:fix` 或 `pnpm format`。

## 验证

修改前端代码至少执行：

```bash
pnpm lint
pnpm format:check
pnpm test
pnpm build
```

涉及页面注册时还必须执行 `pnpm gen:registry`，并确认生成结果已提交且再次生成无差异。
