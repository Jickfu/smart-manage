# 前端架构

本文档是前端技术基线、路由、状态职责、页面类型和页签生命周期的权威来源。具体页面实现与交互约束归[前端页面指南](../development/frontend-page-guide.md)，注册协议细节归[页面注册约定](./page-registration-convention.md)。

运维中心首页是固定业务总览；运行监控按 Host/Instance 分离快照，历史图按百分比、B/s、req/s、ms 和数量分图展示，见[内建监控架构](./monitoring.md)。

## 技术基线与目录

- 使用 pnpm、React、TypeScript 和 Ant Design，不引入其他 UI 组件库。
- 使用 `@/` 别名引用 `src`。
- 业务页面按 `src/domain/{领域}/{应用}/{模块}` 组织。
- 页面组件目录使用 PascalCase，工具和 Hook 文件使用 camelCase。
- 前端 ID 使用 `string`，避免雪花 ID 精度丢失。

## 应用与路由

应用使用 React Router `MemoryRouter`。浏览器地址使用 `/index.html?app={appNumber}` 指定首次打开的应用，也可以使用 `/index.html?app={appNumber}&entry={menuNumber}` 在应用菜单加载完成后首次打开指定菜单入口。`entry` 使用稳定菜单编码，只能从服务端返回的当前用户有权菜单树中解析；不存在、已停用、不属于指定应用、不是页面菜单或当前用户无权访问时停留在应用首页。页面内导航不修改浏览器 URL，启动参数也不随页签切换持续同步。

URL 菜单入口必须复用侧边栏菜单的目标解析和工作台页签打开逻辑，不得根据菜单名称、路径、权限编码或前端组件路径另建推断规则。内部页面和工作台 iframe 外链可以自动打开；`NEW_TAB` 外链不得在初始化时自动弹出，由界面提示用户从菜单主动打开，避免浏览器弹窗策略导致不确定行为。

界面采用两层页签：

- Header 页签切换应用；
- 应用工作台页签切换业务单据或页面；
- 已打开页签通过显隐保留查询条件、表单状态和滚动位置；
- 页签必须有容量、关闭和资源释放策略。

## 状态职责

| 状态类型 | 负责组件 |
| --- | --- |
| 服务端查询、缓存、加载和命令状态 | TanStack Query |
| 用户、token、侧边栏和当前应用等跨页面客户端状态 | Zustand |
| 编辑字段、校验和脏数据 | Ant Design Form |

查询统一使用 `useQuery`，命令统一使用 `useMutation` 或项目领域命令封装。业务单据维护独立 Query Key Factory；命令成功后由领域 Mutation 统一处理缓存失效、详情回显和页签替换。

## 请求错误协议与反馈

`api/responseError.ts` 在请求边界校验响应，`ApiError` 分别保留 `source`、真实 `httpStatus`、后端 `apiCode`、`feedbackLevel`、用户说明和诊断 ID；不得把 HTTP 状态或 -1 塞进业务码。错误来源区分 API、HTTP、NETWORK、TIMEOUT、PROTOCOL 和 CLIENT；原始 Axios 取消对象不包装，解释策略识别为 CANCELED，且不显示错误。NETWORK 只表示没有获得可用响应，不推测 DNS、CORS 或断网的真实原因。

| 响应 | 结果 |
| --- | --- |
| 2xx + 有效 Result，code=0 | 成功 |
| 任意 HTTP 状态 + 有效失败 Result | API，保留实际 HTTP 状态和非零业务码 |
| 非 2xx + 非 Result 或 code=0 | HTTP 失败，绝不当作成功 |
| 2xx + HTML、null 或畸形 Result | PROTOCOL |

Result 核心字段必须是对象自身的安全整数 `code`、字符串 `msg` 和存在的 `data`（允许 null）；`traceId` 缺失不改变业务失败身份，存在时只能为 string 或 null。失败响应的 `feedbackLevel` 缺失、未知或 null 时按 ERROR，不回退旧数字码级别表。

Blob 响应只对 JSON/+json MIME 的非文件响应作至多 64 KiB 的文本解析，禁止嗅探任意文件正文。2xx 且带 attachment，或 inline 同时带 filename/filename* 的明确文件头保留原文件，支持合法 JSON 附件。裸 inline 不享有豁免；非 2xx 永不享有文件豁免。JSON Blob 中有效失败 Result 仍为 API；2xx 非法/超限 JSON 或“期待文件却收到成功 Result”为 PROTOCOL；非 2xx 解析失败/超限仍为 HTTP。

`api/errorPresentation.ts` 统一解释反馈强度和安全文案，不选择具体 UI。普通可信 API 失败消费后端反馈级别；HTTP 5xx 和认证/权限等安全失败不能被 WARNING 弱化。稳定业务码仍用于认证、CSRF 和并发冲突的明确行为，不根据文案猜测类型。401 由请求层清理会话并跳转登录，CSRF 由专属通知处理，普通反馈必须抑制二次提示。未知、本地、传输和协议异常不透传底层 message、stack、请求参数或响应正文；附加 data 不自动展示，诊断 ID 只接受受控字符。

### 查询错误的显示所有权

`QueryFeedbackProvider` 位于 Ant Design App 下，每个 Provider 自己创建 QueryClient、QueryCache 和故障状态；请求层只规范错误，不弹 UI。QueryCache 仅在最终失败（重试耗尽）后使用现有 `useOperationFeedback` 的 Message + Alert 展示可关闭、常驻、去重的系统反馈。不引入 Notification 或全局 MutationCache 兜底；Mutation 仍由既有领域命令负责。只有需要用户作出选择的场景使用 Modal。

查询通过 `meta.errorPresentation` 明确所有权：

| 声明 | 本地 UI 职责 |
| --- | --- |
| 未声明 | 系统兜底，不要求每个调用方重复 catch |
| `local-initial` | 无有效数据时显示区域错误与重试；有旧数据的普通刷新失败交给系统反馈 |
| `local` | 每次失败均由该区域完整展示，例如引用选择器、历史曲线、Cron 预览 |

`getBlockingQueryError` 是区域阻断判定的唯一入口。`data !== undefined && !isPlaceholderData` 表示有效旧数据，空数组与 null 均有效。HTTP 403/404 或业务码 100403/100404 无论有无缓存都阻断，不能把 404 解释为新增。权限查询采用更严格规则：任何失败都不使用缓存权限进行 `can()` 授权判断。取消、401、CSRF 不参与普通错误 UI；CSRF 专属提示使用稳定 key。

同一 query key 有多个 enabled observer 时，按每个 observer 自己的 meta 和当前结果判定是否实际拥有错误；只有全部 enabled observer 都本地拥有时才不再系统提示，只要有一个未接管就保留系统兜底。否则隐藏但仍挂载的局部 owner 可能吞掉可见 default 页面的错误。此时不能用最后一次写入的 query.meta 替代各 observer。没有 enabled observer 时才回退 query.meta。该规则不依赖工作台页签可见性。

每个 queryHash 只保留当前故障成员关系。系统提示按来源、HTTP 状态、业务码、最终反馈级别和安全文案生成指纹，traceId 不参与去重；只有全部成员真实成功、移除或 reset 后才解除该故障并关闭提示。开始 refetch、手动关闭或 Message maxCount 淘汰都不代表恢复，不得造成轮询重复弹出。Provider 真正卸载时清理缓存和提示，StrictMode 的 effect 重放不能清掉仍活跃的查询。

`RequestErrorState` 只呈现错误区域、受控诊断 ID 与重试，不查询、不布局整页、不持有业务状态。ListPage、EditPageShell、AssignmentPage 分别保持原页面结构，不为相同错误区域额外提取 PageShell，也不重命名或重排页面目录。编辑壳、弹框编辑和分配壳遇到阻断时保留原内容挂载，以 hidden/inert 撤销交互能力，保存/提交 handler 也必须检查阻断状态。普通后台失败不得覆盖 Form 的未保存输入；相同数据重试恢复后保留输入和脏状态。区域 Retry 只手动执行当前 isEnabled/业务上下文允许的查询，不能把 refetch 当作自动遵守 enabled 的 API；依赖 ID 尚未取得时先恢复父查询，再让子查询自然启动。

EditPage/ModalEditPage 的 initialValues 必须从 query.data 以 useMemo 稳定派生，或使用稳定常量，禁止内联构造对象。模板记录最后实际应用的引用：仅在不 loading、不 blocking 且引用变化时同步；不能在阻断期间提前记录已应用。真正成功取得新服务端数据/版本仍按既有语义同步 Form，不以 dirty 阻止，从而避免“旧表单内容 + 最新 version”绕过乐观锁。Modal 关闭 resetFields 后同步清除已应用引用，保证同一缓存记录重新打开仍正确回显。保存完成只维护 revision/dirty，不重复灌值。

页面阻断不等于撤销此前已经打开的应用级确认弹框；后端权限和版本检查仍是最终边界。确认回调的作用域撤销不属于当前查询反馈能力，不通过新增全局确认框架隐式改变既有流程。

EditPage 的校验/transformValues 异常尚未进入领域 Mutation，由编辑层显示一次安全反馈；进入 Mutation 后不能再由 EditPage 重复提示。保存已成功但刷新失败时，保留一条保存结果摘要和一条去重的具体查询故障，这是不同语义的有意双层反馈；不得使用全局 silent counter 抑制同时发生的其他查询失败。

## 页面三层边界

`domain/common/page` 是跨领域复用的页面框架，不是通用工具目录。目录按页面能力归组，组件、专属 Hook、辅助函数、样式和测试就近放置，不另建全局 `hooks`、`utils` 或聚合导出入口。能力分包使用小写名称，与具体页面组件目录的 PascalCase 约定区分。

编辑能力集中在 `page/edit/`：包括标准与弹框编辑、字段布局、分区、编辑附件适配及保存后页签同步。`FormFieldLayout` 是编辑表单布局协议，弹框等调用者也从这里使用；`defineRefSelector` 是编辑字段协议的泛型适配，不代表独立引用选择器框架。

列表能力集中在 `page/list/`：包括列表页面、表格与树面板、过滤与查询条件、列设置、选择状态及列表页签适配。组件、纯函数、Hook、测试和专属样式按同一列表能力就近组织，不再按技术形态分散到通用目录。

`page/EditPageShell.tsx` 保留在根级：虽然沿用既有命名，其契约只包含标题、加载、错误、操作区和正文，不包含表单或编辑状态，供编辑、监控、控制台和详情等页面复用。它是明确的跨页面族共享入口，不得反向依赖具体页面族；不能只按名称前缀决定目录归属。

`page/pageLayout.css` 是编辑页、分配页及相关页面共同使用的布局样式，包含既有 `sm-edit-*` 布局与折叠区规则，不属于某一个 Shell 组件。其余专属 CSS 与组件同目录。公共能力移动时直接更新引用，不保留旧路径兼容导出。

`page` 根级只保留 `types.ts`、`pageLayout.css`、`EditPageShell.tsx` 三个共享文件及以下六个能力目录：

| 目录 | 所有权 |
| --- | --- |
| `edit/` | 编辑页、弹框编辑、字段与编辑生命周期 |
| `list/` | 列表、过滤查询、列设置、树与选择状态 |
| `assignment/` | 关系分配页面、候选与已选区域及选择辅助函数 |
| `access/` | 页面权限协议、权限查询与权限操作按钮 |
| `command/` | 通用命令 Mutation 与启停命令封装，不承载业务命令参数 |
| `tab/` | 页签键、标题上下文及关闭保护；编辑保存后的页签适配仍归 `edit/` |

`iconResolver` 与 `IconSelector` 同属既有 `common/component/`，不再放入页面框架。具体领域的页面、Hook 和 Mutation 仍归业务模块，不因本次归组继续上提。

页面框架的直接依赖规则如下：

- `edit`、`list`、`assignment` 三个具体页面族之间不得直接互相依赖，可使用 `access`、`command`、`tab` 及根级共享文件。
- `access`、`command`、`tab`、根级共享 Shell 和 `types.ts` 不得反向依赖三个具体页面族。
- 同一能力内允许就近复用。业务页面和已有 `common/component` 可使用页面能力，例如 `RefSelectorPanels` 复用列表树；不计算 `edit → component → list` 这样的传递依赖，不借目录治理拆改现有组件。
- `page/**` 下禁止 `index.ts/tsx`；整个 `src` 不得通过 `export ... from` 或 `export * from` 重新导出页面框架目标建立替代入口。本文件声明的普通导出不受影响。

门禁使用 TypeScript AST 检查当前 TS/TSX 的静态导入（含类型与 CSS 副作用导入）、直接重新导出、`import('...').Type` 及字面量动态导入，统一规范化别名与相对路径，旧平铺路径即使文件不存在也会拒绝。不分析非字面量动态导入、CSS 内部 `@import` 或传递依赖；不保存迁移前的历史代码快照。验证方式统一见[质量验证](../development/verification.md)。

- 页面壳层只负责布局、加载、错误、权限和按钮区。
- 表单或列表基础组件负责字段、过滤器、表格和引用选择器。
- 领域页面负责状态流转、Mutation、明细聚合和业务命令。

公共页面不得继续吸收具体单据状态、命令参数或采购专属逻辑。没有第二个真实使用方时不提前建立抽象或低代码字段引擎。

数据交换界面由 `domain/common/dataExchange` 提供导入布局、文件下载和制品导出 Hook；领域适配组件负责业务模式文案、匹配规则、模板接口和导入结果处理。列表页面只负责打开领域适配组件及完成后的列表刷新，不直接维护文件上传和结果下载流程。后端字段校验、事务模式和引用解析不得复制到前端公共组件。

## 页面类型

- `ListPage`：查询、分页、过滤、行操作和打开单据。
- `EditPage`：新增、编辑和查看，由 `OperationType` 控制能力。正文卡片由领域页面通过 `sections` 完整声明，通用页只提供壳层、表单、折叠布局和顶部命令，不内置基本信息、明细、附件或其他固定业务分区。
- `CustomPage`：文件配置、报表、监控、控制台等非标准页面，注册类型使用 `CUSTOM`。

自定义页面仍须优先复用通用页面壳层。除控制台、大屏等特殊交互外，主要操作位于顶部操作区，正文以白色折叠面板组织，控件密度、间距和只读表现参照通用编辑页，避免形成孤立的页面视觉体系。

`OperationType` 使用 `ADDNEW`、`EDIT` 和 `VIEW`。列表页优先将 `number` 作为第一列可点击标识；无业务编码的日志或记录可以使用更合适的标识。

## 单据页签生命周期

- 列表页单实例。
- 新增页每次创建新的临时 UUID 页签。
- 编辑和查看按真实单据 ID 多实例，同一单据共用一个 tab key。
- `createNewData` 不返回 ID。
- 首次保存后重新调用 `detail(id)` 获取数据库真实状态，并将临时 key 替换为真实单据 key。
- 保存后不关闭编辑页。
- 新增和编辑页面都要注册脏数据关闭保护。

业务单据通常以 `SAVED`、`SUBMITTED`、`AUDITED`、`CLOSED` 状态驱动页面能力。保存只处理新增或暂存修改；提交接收完整聚合并推进状态；已提交等状态进入只读 `VIEW`。

## 页面注册

后端菜单只保存稳定业务键，前端通过白名单注册表映射到真实组件。详细规则见[前端页面注册约定](./page-registration-convention.md)。

每个页面注册项必须显式声明稳定 `featureKey`。同一功能可以包含 LIST、EDIT、CUSTOM 等多个页面；页面组件路径不能代替功能身份。功能、菜单和权限之间的完整约束见[功能、菜单与权限模型](./feature-and-permission.md)。

工作台内容页签标题同样属于页面注册边界：菜单只决定打开哪个 `componentKey`，不提供页签标题。LIST、EDIT、CUSTOM 的默认标题规则、通用页面壳同步方式以及业务页面动态覆盖能力统一遵守前端页面注册约定。

页面实现、样式和交互约束统一见[前端页面指南](../development/frontend-page-guide.md)，编译、测试和构建要求统一见[质量验证](../development/verification.md)。
