# AGENTS.md

## 基本约定

- 使用简体中文沟通。
- 如果不明确之处会影响架构方向、数据安全、外部状态或产生明显不同的实现结果，必须停下来问我；可以从代码和现有约定中确认的问题应先自行核实，低风险细节可以采用明确说明的合理假设。
- 代码改动应从全局架构考虑，不能偷懒而破坏项目架构设计。
- 实现前必须确认需求边界、现有设计和可验证的完成标准。改动应保持最小充分范围，不擅自扩展功能、重构无关代码或增加未经需求验证的抽象；发现无关问题时应记录或说明，不得顺手修改。
- 每项代码任务都应执行与改动风险相匹配的编译、自动化测试、静态检查或构建验证。
- 当前架构基础要持续演进为可实际使用、可长期维护、符合主流社区习惯的企业级项目。
- 当前阶段以架构内核和工程质量为重点，不以快速扩展业务模块、制作开源展示材料或完成示范单据为目标。
- 本项目最终将开源在GitHub上，要保证项目质量，避免往GitHub上“投屎”。
- 系统处于架构搭建阶段，所有需求修改不要兼容旧逻辑，不要做冗余兜底代码，应让问题暴露出来。
- 关键代码或特殊处理的代码必须给出中文注释。
- 如果任务复杂时，自动启动计划模式。
- 如果你尝试了各种方式都访问不了网页或者获取不了想要的内容的时候，使用 /playwright-cli 打开浏览器获取数据。

## 技术栈

| 层级                        | 技术                                                                   |
|---------------------------|----------------------------------------------------------------------|
| **后端** (smart-manage-api) | Spring Boot 4, Java 21, MyBatis-Plus, Druid, Sa-Token, PostgreSQL    |
| **前端** (smart-manage-web) | Vite, React 19, TypeScript, Ant Design, TanStack Query, Zustand |

## 后端约定 (smart-manage-api)

### 分层架构
- 根包：`sm`
- `sm.framework` — 第三方 jar 配置（CORS, Sa-Token, JSON, Redis, MyBatis-Plus, 请求加解密, TraceId）
- `sm.system` — 本系统公共类（自定义注解、全局异常处理、实体基类、拦截器、监听器、工具类）
- `sm.domain.{领域}` — 领域模块（如 `sm.domain.sys` 系统服务云）
- `sm.domain.{领域}.{应用}` — 应用模块（如 `sm.domain.sys.monitor` 系统监控）
- `sm.domain.{领域}.{应用}.{单据}` — 单据/子模块
- 公共能力放在 `sm.domain.{领域}.common`或`sm.domain.{领域}.{应用}.common`

### 核心设计模式
**请求流程：** CorsFilter → EncryptApiFilter（SM4 解密） → SaServletFilter（登录校验 + 权限校验） → TraceIdInterceptor → Controller → Service（BizLogAspect） → TxService → Mapper
**统一响应格式：** 所有接口返回 `Result<T>`，包含 `code` / `msg` / `data` / `traceId`。成功用 `Result.success(data)`；异常由 `GlobalExceptionHandler` 统一处理。
**分页：** 入参继承 `PageForm`（pageNum, pageSize），返回 `PageData<T>`（total, pageNum, pageSize, records）。Service 中使用 MyBatis-Plus 的 `Page` + `mapper.selectPage()` 或自定义 Mapper 分页查询。
**权限鉴权：** Controller 方法上用 `@SaCheckPermission("sys:base:user:listPage")` 声明权限码；`StpInterfaceImpl` 实现权限/角色加载接口，统一从数据库加载并按 token 缓存到 Redis。
**登录验证码：** 登录接口需要验证码。`/captcha` 生成图文验证码存 Redis（key 为 `captcha:{uuid}`），登录时 SM2 解密前端密码和验证码后校验。
**请求加密（可选）：** `@EncryptApi` / `@DecryptApi` 标注 Controller 或方法后，`EncryptApiFilter` 通过 SM4/CBC 加解密请求/响应体。
**操作日志：** `@BizLog("创建用户")` 只标注在公开 Service 的业务命令方法上，Controller 和包内 TxService 禁止标注，同一业务调用链只能记录一次。保存、提交、审核、删除、启停和高风险执行等命令需要记录；普通列表、详情、选择和默认值查询不记录。登录、退出使用独立认证日志。AOP 自动记录操作人、IP、请求参数、响应体、耗时，通过 `LogWriteService` 异步落库。
**实体自动填充：** `MyBatisPlusMetaObjectHandler` 自动设置 `createTime` / `createUser` / `updateTime` / `updateUser`。
**文件存储：** `FileStorageService` 抽象 Local / FTP 等存储实现。`sm.system.storage` 只能通过 `FileStorageConfigProvider` 获取存储配置，禁止依赖 `sm.domain.sys` 的实体或 Service；系统领域负责实现配置提供器和敏感字段解密。


### 关键约定

- 项目处于架构搭建阶段，不考虑向后兼容。核心架构边界、安全逻辑、状态流转、事务和并发控制必须编写自动化测试；普通 CRUD 和纯展示代码不强求覆盖率，不以追求测试数量为目标。
- 超级管理员用户（`administrator`）拥有 `*` 通配权限，跳过普通权限校验。脚本控制台、SQL 控制台和 Arthas 等高风险能力还必须在公开 Service 入口校验当前账号确实为 `administrator`，不能只依赖可配置的业务权限码。
- 主键使用雪花 ID（MyBatis-Plus 的 `IdType.ASSIGN_ID`），乐观锁字段 `version`（通过 `@Version` 注解 +
  `OptimisticLockerInnerInterceptor` 实现）
- 数据库结构定义和必要初始化数据以项目根目录 `db/migration` 下的 Flyway 版本脚本为唯一权威来源；Maven 构建时将其复制到后端类路径。排查运行问题和核实迁移执行结果时，以当前连接数据库的实际状态为事实依据；禁止绕过 Flyway 修改数据库结构后不补迁移脚本，已执行的迁移禁止修改，结构调整必须新增版本脚本。
- 接口访问级别统一按注解判定：`@SaIgnore` 为公开接口，`@SaCheckPermission` 为权限接口，其余接口由全局过滤器执行登录校验
- **禁止**在查询中使用裸表名/字段名字符串（如 `"t_sys_user"`、`"username"`），必须使用 MyBatis-Plus 的 `LambdaQueryWrapper` + 方法引用（如 `UserEntity::getUsername`）
- **XML Mapper 表别名**：所有 SQL 中 FROM 主表别名为 `a`，JOIN 表按出现顺序依次为 `b, c, d...`。不使用语义化别名（如 `app`、`user`），保持 SQL 紧凑统一。
- Service 的公开业务方法禁止用 `return null` 表达业务失败。资源不存在、状态非法、无权限、参数不合法等场景必须抛出明确异常，让 `GlobalExceptionHandler` 统一返回。内部辅助方法确实允许缺省值时，方法命名和注释必须明确可空语义。
- JSON 反序列化、ID 转换等基础设施禁止静默吞错。比如 Long 解析失败不能返回 `null`，应暴露为参数异常。
- **VO 映射边界**：Entity 到列表、选择、明细和基础详情 VO 的纯字段映射使用模块内 `*Converter`（MapStruct）；Converter 禁止依赖
  Mapper、Service、缓存、安全上下文或外部资源。需要查询、权限判断、默认值、状态规则、树结构或主从聚合的转换属于业务组装，保留在公开
  Service 并使用 `assemble*` 命名。MyBatis 联表直接投影 VO 的查询保持不变。
- 标准业务接口语义：`listPage` 返回分页；`detail` 找不到应抛异常；`createNewData` 只返回新增默认值且不返回 id；`save` 负责新增和暂存修改；`submit` 接收页面当前完整聚合数据，在同一事务内保存并推进单据状态，不要求用户先点击保存；`delete` 负责删除或作废，具体语义由单据类型明确。
- 修改后端代码后至少执行 `mvn test`；只有文档修改或确认不影响测试代码的简单改动才可以仅执行 `mvn compile`。如果涉及实体、Mapper 或配置变更，也需要确认 MyBatis-Plus 相关代码可以正常编译。
- **事务分离**：Service 禁止直接写 `@Transactional`。每个含写操作的 Service 必须搭配一个 `*TxService`，将 `@Transactional(rollbackFor = Exception.class)` 放在 TxService 类级别。Service 注入 TxService，写方法（`save`/`deleteById` 等）以委托方式调用 TxService。读写共用的私有辅助方法留在 Service；仅事务内使用的私有方法移入 TxService。TxService 内需要的前置读取（如唯一性校验、存在性检查）直接使用 Mapper，不走 Service 缓存方法。
  - 例：`RoleService`（只读 + 委托） → `RoleTxService`（类级别 @Transactional，包含 save/deleteById 全部逻辑）
- **单据聚合边界**：每个单据只有一个对外公开的 `*Service`。对应 `*TxService` 是单据包内的事务实现类，使用包级可见性，只允许同一单据的 Service 委托调用；Controller 和其他单据禁止直接依赖 TxService。业务代码优先按单据归属组织，不额外拆分 Application/Domain/Infrastructure 层。
- **可选业务模块边界**：非系统内核业务必须集中在独立领域目录，系统内核禁止反向依赖。模块的表、云、应用、菜单和权限初始化集中在独立 Flyway 版本中，并在 `docs/modules` 记录边界与移除方式；不得为了可选模块引入散落的功能开关或采购专属公共代码。
- 采购申请是首个标准业务单据和架构纵向验收模块，归属 `scm` 供应链云、`procurement` 采购管理应用，后端目录为 `sm.domain.scm.procurement.purchaserequisition`。在该模块完成保存、提交、乐观锁、主从事务和前端页签生命周期验收前，不批量扩展业务模块。

### 命名规范

- 禁止单字母变量名
- 具有独立业务身份的主数据和业务单据统一使用 `number` 作为业务编码字段（非 `code`）；Entry、关系实体、日志和运行记录不强制使用 `number`。
- **主从单据**：`detail` 和 `save` 均一次请求完成，明细统一用数组字段传递。主明细默认名为 `entrys`
  ，如有多个明细表可自定义名称（前后端一致即可），后端 `@Transactional` 更新数据。**即使无明细数据，`entrys`
  也必须返回空数组 `[]`**
- 主从单据的明细属于主单聚合，统一使用 `*Entry` 命名并放在主单目录内，只保留 Entity、Mapper、Form、VO，不提供独立 Controller、Service、TxService 或写接口。明细表统一使用 `parent_id` 关联主表；删除主单时在 TxService 中先显式删除明细，再删除主表，不使用数据库级联删除。
- `*Util` — 纯静态工具类，不依赖 Spring 容器（如 `ServletUtil`、`StringUtil`）
- `*Helper` — `@Component` 组件，依赖 Spring 注入或配置（如 `CacheHelper`、`Argon2Helper`）
- `*TxService` — 事务服务，`@Service` + 类级别 `@Transactional(rollbackFor = Exception.class)`，包含所有写操作逻辑。对应读 Service 注入并委托写方法给它

### SQL执行
- 数据库结构定义以 Flyway 迁移为唯一权威来源；查询数据库实际状态用于排查运行问题和核实迁移结果，不能替代迁移脚本。
- 如果需要执行 SQL，可以使用 psql 命令直接执行。必须使用当前环境已有的安全凭据方式，例如临时设置 `PGPASSWORD` 或使用受控连接配置；禁止把数据库密码写入代码、文档、提交记录或最终回复。psql 命令在
  `D:\Program Files\PostgreSQL\16\bin`
- 客户端和数据库写入默认使用 UTF-8。只有确认 Windows 终端查询结果乱码时，才可以临时调整查询输出的客户端编码；数据库写入不得依赖 GBK。
- **创建表时必须加上表备注和字段备注**，使用 PostgreSQL `COMMENT ON` 语法。字段注释规范：
  - `id` → `ID`
  - `number` → `编码`
  - `name` → `名称`
  - `create_time` → `创建时间`
  - `update_time` → `更新时间`
  - `create_user` → `创建人`
  - `update_user` → `修改人`
  - 其他字段根据业务含义填写中文备注

## 前端约定 (smart-manage-web)

- 以下约定必须与当前代码保持一致。如果代码实现与本文档冲突，应先核实实际设计并修正文档或实现，不得长期保留两套口径。

### 构建与运行

```bash
cd smart-manage-web

pnpm dev        # 开发（localhost:8000，代理 /smart-manage-api → localhost:8080）
pnpm build      # tsc 类型检查 + vite build
pnpm test       # Vitest 关键前端逻辑测试
pnpm preview    # 预览构建产物
pnpm lint       # ESLint 检查（max-warnings 0）
pnpm lint:fix   # ESLint 自动修复
pnpm format     # Prettier 格式化
pnpm format:check # Prettier 格式校验
```

### 分层结构

```
src/
├── api/            # 接口层（axios 实例 + 拦截器）
├── assets/         # 静态文件
├── layouts/        # 布局组件（MainLayout → Sider + Header + Content）
├── domain/         # 领域/应用/单据页面（如 domain/sys/base/user）
│   └── common/     # 前端领域公共能力（页面框架、组件注册表等）
├── pages/          # 非业务页面组件
│   └── errors/     # 错误页面（404 等）
├── router/         # 路由配置（MemoryRouter，路由表）
├── stores/         # Zustand 状态（user：用户/token，app：侧边栏/当前模块）
├── styles/         # 样式（global.css：全局重置）
├── types/          # TypeScript 类型（Result<T>、PageResult<T> 等后端响应类型）
├── utils/          # 工具函数
├── App.tsx         # 根组件（ConfigProvider + QueryClient + RouterProvider）
└── main.tsx        # 入口（挂载 React + 全局样式）
```

### 核心设计模式

**路由方案：** 使用 React Router `MemoryRouter`。浏览器 URL 固定为 `/index.html?app=home`，不随页面内导航变化。`app` 参数决定当前显示的模块，默认 `home`。各应用模块路由通过 `routes` 配置表维护。
**ERP 双页签：** 前端参考金蝶苍穹/星瀚设计理念，采用两层 tabs。Header tabs 用于切换应用（app）；应用工作台内部 tabs 用于切换业务单据/页面。已打开 tab 不卸载 DOM，只通过显隐控制，保留查询条件、表单状态、滚动位置等现场。
**登录页：** `public/login.html` 为独立 HTML 页面，零框架依赖，加载速度快。页面初始化时调用 `/sys/base/captcha` 获取图文验证码，登录时 SM2 加密密码和验证码（C1C3C2 模式，cipherMode=1），调用 `/sys/base/login`。成功后存储 token 到 localStorage 并跳转目标页。支持 `?redirect=` 参数控制登录后跳转地址。
**请求拦截：** axios 实例（`src/api/request.ts`）统一处理：

- 请求拦截器：注入 `smtoken` header 从 localStorage
- 响应拦截器：`code !== 0` 视为业务错误；`code === 100401` 跳转 `/login.html?redirect=...`
- 后端统一响应体对应 `types/api.ts` 中的 `Result<T>` / `PageResult<T>`

**状态管理：** `Zustand` 管理客户端状态（用户信息、token、侧边栏折叠、当前模块）；`TanStack Query` 管理服务端状态（请求缓存、loading/error 自动化）。

- 服务端查询统一使用 `useQuery`；保存、提交、删除和高风险执行等命令统一使用 `useMutation`，不得在页面内重复维护提交 loading 和通用错误提示。
- 每个业务单据维护独立 Query Key Factory，至少提供 `all`、`lists`、`list(params)`、`details`、`detail(id)`；保存和删除成功后由领域
  Mutation 统一失效相关缓存
- 通用编辑页通过 `useCommandMutation` 管理命令提交状态；具体单据负责成功后的详情回显、列表缓存失效和页签 key 替换。
- 表单字段校验由 Ant Design Form 负责。当前项目不引入 Zod，避免维护重复校验模型；以后只有出现明确的外部不可信数据运行时校验需求时再评估引入。

**主题定制：**
- `ConfigProvider`（Ant Design）— 运行时通过 `theme.token` 定制主题色、圆角等全局样式，不使用暗黑模式
- Ant Design 6 使用 CSS-in-JS（`@ant-design/cssinjs`），无需 Less 变量覆盖
- 入口 `main.tsx` 无需额外引入样式文件，antd 组件自动按需加载样式

**SM2 加密：** 前端仅涉及 SM2 加密（公钥加密密码和验证码），不引入 SM4。公钥硬编码在 `login.html` 中（与后端 application-dev.yml 的 `smart-manage.sm2.js.public-key` 一致）。加密库使用 npm 包 `sm-crypto`，复制 `dist/sm2.js` 到 `public/js/` 供 login.html 使用。React 应用内部需要 SM2 时可直接 `import` 该 npm 包。

### 页面架构

- **页面三层边界**：页面壳层只负责布局、加载、错误、权限和按钮区；表单/列表基础组件负责字段渲染、过滤器、表格和引用选择器；领域业务页面负责状态流转、Mutation、明细聚合和业务命令。禁止把具体单据状态或命令参数继续加入公共页面壳层。
- 前端业务页面目录与后端保持一致，按 `src/domain/{领域}/{应用}/{单据}` 分层，例如 `src/domain/sys/base/user`。
- 菜单表中的 `component` 使用稳定业务键，例如 `sys/base/user/list`、`sys/base/file-config/custom`。后端只存菜单元数据，前端通过组件注册表把 `component` 映射为真实组件。
- 组件注册表是前端白名单，由 `pnpm gen:registry` 发现并导入 `src/domain/**/pageRegistration.ts` 或 `.tsx` 注册清单，禁止根据页面文件名、目录结构或后端字符串推断并任意动态加载组件。每个业务模块或单据目录维护一个 `pageRegistration.ts`，默认导出 `definePageRegistrations([...])` 的结果；一个注册清单可以声明该模块的多个页面，但一个页面组件文件原则上只实现一个页面组件，业务键重复时必须在生成或注册阶段直接报错。
- 标准页面类型：
  - `ListPage`：列表页，负责查询、分页、过滤、行操作、打开单据页。
  - `EditPage`：单据新增、编辑、查看页，由 `OperationType` 控制页面能力。
  - `CustomPage`：非标准列表/编辑形态的业务页面兜底，例如文件存储配置、图表报表、监控页、SQL 控制台、复杂操作页。页面类型值使用 `CUSTOM`，避免与应用工作台概念混淆。
- `OperationType` 统一使用正确拼写，基础值为 `ADDNEW`、`EDIT`、`VIEW`。
- 列表页默认第一列为可点击列，优先使用 `number` 字段；日志等没有 `number` 的页面可使用 `id` 或更合适的业务标识。
- 列表页可按需启用左树右表形态：左侧树面板作为 `ListPage` 的 `treePanel` 传入，右侧继续使用过滤区、按钮区和表格区。左右分割可使用
  Ant Design 的 `Splitter` 组件。
- 列表页原则上不提供独立“编辑/查看”按钮。点击单据后，根据单据状态推导 `OperationType`：暂存状态进入 `EDIT`；已提交、审核通过等状态进入 `VIEW`。字段可见性、锁定性、顶部按钮显隐由单据状态和 `OperationType` 共同决定。
- 业务单据默认应具备单据状态字段，用于驱动编辑页能力控制。日志、监控记录、单条配置等非业务单据不强制加入单据状态，避免把所有表都套进单据模型。单据状态建议使用 `char(1)` 存储：`A` 暂存（`SAVED`）、`B` 已提交（`SUBMITTED`）、`C` 审核通过（`AUDITED`）、`D` 已关闭（`CLOSED`）。注意使用正确英文拼写 `SUBMITTED`，不要使用 `SUBMITED`。
- 前端 `EditPage` 需要区分“保存”和“提交”两个按钮。保存仅保存暂存数据，单据保持 `SAVED`，后续仍可修改；提交将单据状态改为 `SUBMITTED`，提交后不可再普通保存或编辑，后续可扩展消息通知、审批流程等业务动作。
- 后端 `save` 负责新增和修改暂存数据；前端可以只有一个保存按钮触发 `save`，但 Service 内部必须明确区分新增 insert 和修改 update 流程。新增由 `id == null` 判定，修改由 `id != null` 判定，不能相信前端传来的 id 或状态来决定数据库行为。
- 后端 `submit` 负责在同一事务中保存页面当前完整聚合并推进状态，不要求用户先执行 `save`。已有单据必须校验数据库当前状态和 `version`，通常只允许 `SAVED` 状态提交为 `SUBMITTED`；新增单据应直接创建为暂存状态后立即推进为已提交。
- 需要快速限制“只有新增或暂存单据才允许保存”的场景时，可设计注解式校验（如 `@BillSaveCheck`），但校验依据必须来自数据库当前状态；不是所有单据都强制使用该注解。
- tab key 规则：列表页单实例；编辑页按单据 id 多实例；新增页每次打开新的临时实例。编辑/查看同一单据必须共用同一个 tab key，不能出现同一单据同时打开编辑和查看两个 tab。
- 新增页使用前端生成的 uuid 作为临时 tab key。`createNewData` 不返回 id；保存成功后，后端返回真实 id，前端立即调用 `detail(id)` 回显完整数据，并把临时 tab key 替换为真实单据 tab key。保存成功后不关闭 `EditPage`，继续留在当前单据页面。
- 常规 `EditPage` 挂在业务单据 tab 中。字段很少的单据可采用居中 Modal 编辑形态：顶部左侧标题、右侧关闭，中间内容，底部取消和保存按钮。
- `ListPage`、`EditPage` 需要抽取通用界面。
- `t_sys_cloud`、`t_sys_app` 是当前标准列表/编辑页面的示例单据；其中 `t_sys_app` 用作左树右表示例。

### 技术规范

- 使用 pnpm 进行包管理
- 使用 `@/` 别名引用 `src`
- UI 组件仅使用 Ant Design，禁止引入其它 UI 库
- `id` 必须用字符串存储（后端 Long 雪花 ID 会丢失精度）
- `package.json` 声明 `"type": "module"`，ESLint 使用 ES module 格式
- 修改前端代码后至少执行 `pnpm lint`、`pnpm format:check`、`pnpm test`、`pnpm build`。只有需要自动修复格式或用户明确要求时，才执行
  `pnpm lint:fix` 或 `pnpm format`。
- eslint报错时，只有在用户允许的情况下才可以使用注释跳过校验和修改eslint.config.js文件

### 样式规范

- 禁止在 tsx 文件中写 CSS 样式，禁止在 style 中写内联 CSS
- 自定义 CSS 类名以 `sm-` 开头（如 `sm-layout`、`sm-header`、`sm-content`）
- 优先使用 flex 布局

### 命名规范

- 禁止单字母变量名
- 页面组件目录：PascalCase（如 `pages/UserList.tsx`）
- 工具/Hook 文件：camelCase（如 `hooks/usePage.ts`）

### playwright-cli 测试

- 使用 playwright-cli 时加上 `--headed` 和 `--persistent` 参数以便观察测试过程
- 因为登录界面需要验证码，如果你需要登录，可以停下来让我输入验证码登录成功后再继续
