# 质量验证

本文档是编译、测试、静态检查、构建、生成器和迁移验证的唯一命令来源。架构和领域文档只说明需要保护的风险，不重复维护具体命令。

验证应与改动风险匹配。以下命令是代码修改的最低门槛，不能通过降低规则、忽略错误或手工修补生成文件来绕过。

## 按改动类型选择验证

多种改动取对应要求的并集；已完成且覆盖最终改动的检查无需无故重跑。环境缺失或检查跳过必须标记未验证，不能计为通过。

| 改动类型 | 必须执行的验证 |
| --- | --- |
| 纯文档、AGENTS、项目 skill | 检查相对链接、章节锚点、真实源码路径、格式和事实一致性；执行 `git diff --check` 及下述模块约定脚本；skill 修改还需核对元数据和入口引用。无需 Maven 编译、前后端构建或浏览器验收 |
| 后端代码 | [后端](#后端)最低门槛及相关风险测试；只有该节明确的简单改动例外可仅编译 |
| 前端代码 | [前端](#前端)全部静态检查、格式、测试与构建；页面交互或视觉改动增加[浏览器验收](#浏览器验收) |
| 页面注册源 | 前端门槛，加[首次生成审查与再次生成稳定性](#页面注册生成) |
| 数据库结构、初始化数据、迁移顺序或验证脚本 | [Flyway 空库验证](#flyway-空库验证)，包含真实 PostgreSQL 测试 |
| PostgreSQL 测试保护的权限、凭据、事务或并发行为 | 即使未修改迁移，也必须执行[真实 PostgreSQL 验证](#真实-postgresql-验证) |
| 新增或显著扩展模块、治理入口或门禁引用 | 模块约定脚本，并叠加实际代码和风险所需检查；调整门禁还须验证其相关行为 |

文档检查覆盖 README、CONTRIBUTING、AGENTS、docs、项目 skill 及脚本的反向引用。移动章节时检查锚点；区分真实路径与 `{领域}` 等示例占位符，不为消除死链创建空文件。格式检查可从仓库根目录执行：

```bash
git diff --check
```

## 模块约定

新增或显著扩展业务模块时，先执行仓库级确定性约束检查：

页脚语法树检查依赖 Node.js 22+ 及前端锁定的 TypeScript；首次运行前在 `smart-manage-web` 执行 `pnpm install --frozen-lockfile`。缺少运行时或依赖时检查失败，不跳过该门禁。

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\verify-module-conventions.ps1
```

该脚本检查文档与 skill 路由、页面注册字段、前端内联样式、重复默认字号和 Controller 权限注解常量等文件或前端源码约束。Java 类型、包、注解和依赖边界统一由后端 ArchUnit 测试验证，脚本不重复扫描 Java import。业务状态、数据安全和交互语义仍必须通过评审及风险驱动测试验证。

当前检查清单如下：

| 分类 | 检查内容 | 违规后的修复方向 |
| --- | --- | --- |
| 治理文件 | 模块开发指南、前端页面指南、模块模式目录、模块开发 skill 及其代理配置必须存在 | 恢复规定文件；规范调整时同步更新仓库治理入口 |
| 文档与 skill 路由 | 根目录及前后端 `AGENTS.md` 必须指向模块开发指南；前端 `AGENTS.md` 还必须明确指向 `../docs/development/frontend-page-guide.md`；模块开发 skill 必须明确引用本脚本的执行要求 | 补充明确的文档路由或校验步骤；静态引用检查不能证明脚本已实际执行 |
| 前端样式 | `smart-manage-web/src/domain` 下的 TSX 文件不得使用内联 `style` | 使用项目既有样式文件、组件能力或设计 token |
| 前端字号 | `smart-manage-web/src` 下的 CSS 不得重复声明 `font-size: 12px` 或 `font-size: var(--ant-font-size-sm)` | 由全局主题提供默认密度；标题、骨架、图标和特殊展示按明确语义使用 token 或保留必要字号 |
| 操作反馈 | 除统一封装自身外，前端不得直接调用 Ant Design `message` 的操作反馈方法 | 改用 `useOperationFeedback` |
| 操作确认 | 前端不得直接调用 `Modal.confirm` 或使用 `Popconfirm` | 改用 `useOperationConfirm` |
| 弹框页脚 | `AppModal.footer` 渲染树中的按钮不得被 `Space`、`Flex` 或 `div` 包裹，`PermissionActions` 必须在属性展开后显式传 `grouped={false}` | 使用 Fragment 或按钮数组，由 `AppModal` 统一控制 `12px` 间隔；特殊布局先评审公共契约 |
| 后端权限 | Controller 的 `@SaCheckPermission` 不得直接填写权限编码字符串 | 引用对应模块的权限常量 |
| 页面注册 | 至少存在一个 `pageRegistration.ts` 或 `pageRegistration.tsx`，每个注册项都必须声明 `componentKey`、`featureKey` 和 `pageType`，且 `featureKey` 不得为空 | 补全显式注册字段，使页面与稳定功能身份建立关联 |

页脚检查由 `smart-manage-web/scripts/modal-footer-conventions.mjs` 使用 TypeScript AST 扫描整个 `src` 下的 TSX，识别组件导入别名、内联 JSX、数组、条件分支及同文件变量的作用域绑定；不执行函数、不展开跨文件自定义组件或动态属性对象，也不验证最终计算样式。正反例与真实仓库扫描由同目录 `modal-footer-conventions.test.mjs` 随 `pnpm test` 执行。单独排查可在前端目录执行 `node scripts/modal-footer-conventions.mjs src`，新增脚本格式使用 `pnpm exec prettier --check "scripts/modal-footer-conventions*.mjs"` 检查。

脚本使用源码静态扫描完成这些适合机械判断的确定性检查，不等价于完整的架构或业务验证。以下内容不由该脚本负责：

- Java 类型、包、注解、可见性和依赖边界，由后端 ArchUnit 测试验证。
- Feature、权限、菜单、迁移和页面注册之间基于真实数据的完整一致性，由对应测试和 CI 校验验证。
- 业务状态流转、事务、并发控制、数据安全及交互语义，由代码评审和风险驱动测试验证。
- 编译、单元测试、静态检查、前端构建及 Flyway 空库迁移，仍需执行本文件后续章节列出的命令。

新增、删除或调整脚本检查项时，必须同步更新脚本顶部帮助、本清单及相关成功或失败提示。可使用以下命令直接查看脚本内置说明：

```powershell
pwsh.exe -NoProfile -Command "Get-Help .\scripts\verify-module-conventions.ps1 -Detailed"
```

## 后端

从仓库根目录验证当前全部领域和平台隔离装配；领域边界变更必须执行两种配置：

```bash
mvn -f smart-manage-server/pom.xml verify
mvn -f smart-manage-server/pom.xml -Pplatform-only verify
```

纯文档修改不要求执行 Maven。确认不影响测试代码的简单后端改动才可以仅执行 `mvn -f smart-manage-server/pom.xml compile`；影响安全、权限、事务或并发语义的改动不属于该例外。实体、Mapper、配置和迁移变更还必须确认相关代码能够正常编译。

## 前端

修改前端代码至少执行：

```bash
cd smart-manage-web
pnpm lint
pnpm format:check
pnpm test
pnpm build
```

`pnpm build` 自动执行 `scripts/build-audit.ts` 的产物检查：验证领域裁剪、页面/首页异步入口及输出目录、JS 依赖完整性，并防止图表与代码编辑器进入初始加载链。修改构建策略时分别以默认的当前全部领域和 `SMART_MANAGE_DOMAINS=sys` 构建，对比 `dist/.vite/build-report.json` 与 manifest 中典型页面的静态依赖闭包，不能只比较文件总数。CI 的当前装配与平台隔离两个前端配置均执行该检查；最终恢复默认选择并重新生成注册清单。

构建配置及脚本变更还需检查配置类型与脚本格式：

```bash
pnpm exec tsc -p tsconfig.node.json --noEmit
pnpm exec prettier --check vite.config.ts "scripts/build-*.ts"
```

图标资源生成入口为 `pnpm gen:icons`，输出 `src/domain/common/component/iconCatalog.generated.json`。修改图标依赖、生成器、资源协议或渲染器时，必须审查生成文件中的来源版本和名称全集，并连续生成两次确认 SHA-256 不变；生成文件只能由脚本维护。还需核对生成 JSON 的未压缩体积不超过 2 MiB、gzip 体积不超过 512 KiB，并通过图标目录、TwoTone 实例隔离、初始化零 SVG DOM、非法节点/外部引用拒绝及首次同步渲染测试。

`pnpm test` 包含页面框架的真实仓库扫描与正反例架构测试。门禁与独立命令共用 `scripts/page-framework-boundaries.mjs`，检查 `common/page` 的根文件/能力目录白名单、聚合与旧平铺入口以及页面族直接依赖；规则范围见[前端架构](../architecture/frontend.md)。测试不依赖 Git 历史或固定迁移基线，随现有前端 CI 执行。单独排查时运行：

```bash
pnpm verify:page-framework
```

新增门禁脚本和测试的格式检查：

```bash
pnpm exec prettier --check "scripts/*page-framework*.mjs"
```

表格文本入口的公共选择样式和悬停规则由 `src/styles/tableLink.test.ts` 随 `pnpm test` 校验，覆盖原生按钮、Ant Design 链接按钮、禁用状态和工具栏隔离。真实悬停、拖选复制和键盘激活通过浏览器验收；按钮语义及接入方式见[前端页面指南](./frontend-page-guide.md#列表和命令)。

### 页面注册生成

在 `smart-manage-web` 目录执行。实际入口为 `package.json` 的 `gen:registry`，脚本为 `scripts/gen-registry.mjs`，输出为 `src/domain/common/registry/registry.gen.ts` 和 `applicationHomes.gen.ts`；`predev`、`prebuild` 也调用该生成器。

本地修改注册源后首次生成允许产生预期差异，必须审查并保留生成结果：

```bash
pnpm gen:registry
git diff -- src/domain/common/registry/registry.gen.ts src/domain/common/registry/applicationHomes.gen.ts
```

再次生成应相对首次结果没有新增差异。Windows 可比较两次生成后的文件哈希；这不会要求本地文件与 HEAD 相同：

```powershell
$registryFiles = @('src/domain/common/registry/registry.gen.ts', 'src/domain/common/registry/applicationHomes.gen.ts')
$registryFirstHash = (Get-FileHash -LiteralPath $registryFiles -Algorithm SHA256).Hash -join ','
pnpm gen:registry
if ($LASTEXITCODE -ne 0) { throw 'Registry generation failed' }
$registrySecondHash = (Get-FileHash -LiteralPath $registryFiles -Algorithm SHA256).Hash -join ','
if ($registryFirstHash -ne $registrySecondHash) { throw 'Registry generation is not stable' }
```

提交生成文件采用默认的当前全部领域；平台隔离验证时显式设为 `SMART_MANAGE_DOMAINS=sys`，验证后清除该变量并重新生成默认文件。两种选择均比较两个生成文件的连续生成哈希。

CI 在干净检出上通过 `pnpm build` 的 `prebuild` 生成后，从仓库根目录检查已提交的生成文件是否同步：

```bash
git diff --exit-code -- smart-manage-web/src/domain/common/registry/registry.gen.ts smart-manage-web/src/domain/common/registry/applicationHomes.gen.ts
```

生成文件只能通过生成命令维护，不得恢复文件、覆盖用户修改或手工编辑生成结果来满足检查。

ESLint 报错不得用注释跳过，也不得修改 `eslint.config.js` 降低规则。只有需要自动修复格式或用户明确要求时，才执行 `pnpm lint:fix` 或 `pnpm format`。

## Flyway 空库验证

Windows 环境可以运行：

```powershell
pwsh.exe -NoProfile -File .\scripts\verify-baseline.ps1
pwsh.exe -NoProfile -File .\scripts\verify-baseline.ps1 -PlatformOnly
```

数据库验证脚本统一要求 PowerShell 7；Windows PowerShell 5.1 无法正确读取现有无 BOM UTF-8 脚本，可能出现乱码或解析失败。Windows 上应使用 `pwsh.exe`，不得使用 `powershell.exe`。

脚本默认从 PATH 查找 PostgreSQL Client 16，并在迁移前输出和校验 `psql` 版本；特殊本地安装可通过 `-PsqlPath` 显式指定可执行文件。脚本创建临时数据库，通过项目锁定版本的 Flyway 执行全部迁移，校验版本、命名、checksum 和 `flyway_schema_history`，并在验证后清理。数据库结构、初始化数据、迁移顺序或脚本发生变化时必须执行此项验证。

临时库名使用随机 UUID，只在本次 CREATE 明确成功后清理；创建失败或结果不确定时不得执行 DROP。迁移失败与清理失败同时发生时保留原始验证异常并单独警告清理失败；仅清理失败时也以失败退出。

## 真实 PostgreSQL 验证

`scripts/verify-baseline.ps1` 默认验证当前全部领域，`-PlatformOnly` 选择平台隔离装配（后端 `platform-only`、前端 `sys`）。脚本以临时数据库运行 reactor 的全部 `*PostgresTests`；app 中的 `AssemblyMigrationPostgresTests` 在真实装配 classpath 发现领域声明、执行对应链并再次迁移，确认平台历史未改写、领域迁移声明与实际装配一致、历史表实际创建；Demo 专属数据由 Demo 模块内的 `DemoMigrationPostgresTests` 验证。`DomainFlywayMigrationPostgresTests` 用独立 schema 验证同号、平台增量、数据保留、启动失败、事务回滚、checksum 和乱序拒绝。管理员并发初始化通过真实 Mapper 和 PostgreSQL 触发器确认只写入一次，重启不覆盖。

双链机制测试不替代具体发行版本的带数据升级测试。今后每个数据库变更须按[正式发布与升级规则](./database.md#正式发布与升级规则)，为受支持旧版本准备结构与代表性数据，运行新增迁移，断言数据转换和业务不变量，再次迁移确认稳定；不得修改旧脚本制造测试通过。

修改真实数据库测试保护的权限、凭据代际、事务、锁或并发行为时，即使没有迁移变更，也必须执行上节的 `scripts/verify-baseline.ps1`。脚本迁移临时数据库后，以实际测试数据库参数运行全部 `*PostgresTests`；配置入口以脚本为准，凭据不得写入文档或提交。

空库验证脚本在迁移后自动运行全部 `*PostgresTests`。凭据代际测试必须使用真实触发器、Mapper 和 Spring 事务，覆盖安全变化、普通资料变化、回滚、旧证明 CAS 及验证码消费后的并发变更；角色整体授权测试通过 `pg_blocking_pids` 观察真实锁等待，覆盖替换、清空、删除和失败回滚。普通 `mvn test` 跳过依赖 PostgreSQL 的测试，不能替代该门禁。

PostgreSQL 客户端、数据库服务或必要配置缺失时，此项记为未验证并说明原因；普通 `mvn test` 通过或测试被跳过不能算作此项通过。

## 浏览器验收

页面交互、布局或样式发生变化时，按受影响场景核对真实页面；纯文档修改不触发浏览器验收。记录操作与可见结果，涉及字号、滚动或布局时检查最终计算样式及同类页面，生命周期改动覆盖回显、只读和脏数据保护等相关路径。

使用当前环境可用的浏览器工具完成真实页面验收，必要时保持可见浏览器与持久会话，便于用户接续操作。登录需要验证码时由用户完成，不绕过登录保护；工具或运行环境不可用时如实标记未验证。

## CI 门禁

`.github/workflows/quality-gate.yml` 当前执行：

1. 模块约定脚本与空库清理安全替身测试；
2. 默认当前领域及 `platform-only` 的后端 `mvn verify`；
3. 前端依赖锁定安装；
4. `pnpm lint`；
5. `pnpm format:check`；
6. `pnpm test`；
7. `pnpm build`；
8. 默认页面与首页注册表无差异检查；
9. PostgreSQL 16 上平台及完整业务发行的 Flyway 空库迁移；
10. 使用迁移完成后的真实权限目录执行代码权限一致性校验。
11. 使用迁移完成后的真实功能目录校验全部页面注册的 `featureKey`，并校验菜单与入口权限属于同一功能。

CI 固定验证当前仓库的默认装配与平台隔离装配，不包含具体业务领域名称。前端默认自动发现当前目录，平台作业实际删除全部可选领域目录；后端分别使用默认 `current-domains` 与 `platform-only`，平台作业同样实际删除领域子目录。领域一致性门禁比较后端聚合声明与前端目录。新增或移除领域只需维护后端聚合声明和对应两端目录，不需要修改工作流；声明了不存在的领域仍应失败，不能静默跳过。无业务领域时两个入口都验证平台。裁剪步骤见[移除 Demo](./remove-demo.md)。

主分支保护属于 GitHub 仓库外部设置，需要由仓库管理员启用并要求质量门禁通过。

## 按风险增加验证

部署密钥生成工具修改时，在仓库根目录执行 `node --test scripts/generate-deployment-keys.test.mjs`，验证生成、权限和拒绝覆盖。公钥发布与登录页加密流程修改时，执行后端安全过滤器测试和前端真实登录脚本测试，并保持 CSP 摘要一致。

修改空库验证脚本的生命周期时，先运行 `pwsh -NoProfile -File scripts/verify-baseline.tests.ps1`，以函数替身验证随机库名、创建失败不清理、后续失败清理、清理失败不覆盖原始异常及成功路径；该测试不连接数据库，不能替代真实 Flyway 空库验证。

- 架构边界：架构测试或静态检查。
- Java 类型、包、注解、可见性和依赖边界：优先扩展 `ArchitectureContractTests`，不得新增 regex/import 源码扫描与其重复校验。
- 认证、权限和安全：优先用行为测试验证拒绝路径和副作用顺序，用 ArchUnit 验证注解、类型和依赖边界；源码扫描只保留无法结构化表达的少量机械约束。
- JetCache 远程缓存：缓存值包含项目自定义对象时，在所属模块使用当前实际 value encoder 编码一个非空、字段完整的真实返回值，以覆盖集合元素和嵌套对象的序列化能力；不因此强制统一缓存模型包、逐类登记白名单或新增全仓源码扫描。
- 状态、事务和乐观锁：覆盖成功、非法状态、过期版本和回滚。
- 文件存储：覆盖上传、授权下载、删除失败后的补偿。
- 前端生命周期：覆盖缓存失效、临时页签替换、只读状态和脏数据关闭。
- 请求错误：覆盖响应规范化、安全文案与反馈级别、Query 最终重试、共享 observer 所有权、跨查询去重及真实 reset/remove；使用 jsdom 中的真实 React/Ant Design Form 验证后台失败和资源拒绝不丢输入、阻断保存、transform 异常只提示一次。QueryFeedbackProvider 必须在 StrictMode 下带活跃查询验证 effect 重放与最终卸载；保存后刷新失败同时验证结果摘要和独立查询反馈。测试依赖仅用于开发，不进入生产包。
- 表单快照：验证失败/相同快照重试不重复灌值、成功新版本同步、Modal 同缓存记录关闭再开；`scripts/form-snapshot-contracts.test.mjs` 通过 AST 阻止模板调用方内联构造 initialValues（含条件表达式分支），并非通用深比较或变量数据流分析。查询恢复测试使用实际业务页面和 QueryClient 验证互斥查询、依赖查询不被 Retry 越过 enabled 手工触发。
- 生产部署：验证 CSP、反向代理、敏感配置和被关闭的高风险入口。

不设置机械覆盖率或测试数量目标。
