# 前端页面指南

本文维护桌面管理端 `smart-manage-web` 的页面实现、布局、交互和公共组件使用约定，不作为移动端布局规范。普通页面修改只读相关章节；新增或显著扩展模块再执行[模块开发指南](./module-development-guide.md)。依赖方向、权限和生命周期归[前端架构](../architecture/frontend.md)，注册协议归[页面注册约定](../architecture/page-registration-convention.md)，验证触发条件与命令归[质量验证](./verification.md)。

已采纳的视觉选择与适配边界归[前端设计规范](../design/README.md)；本指南维护具体页面与组件接入。设计正文和本指南按职责互链，不分别维护相同视觉参数。

公共组件的完整接口以当前源码为准，本文保留选择依据、特殊行为和必要示例；真实调用方见[模块样板目录](./module-pattern-catalog.md)。

## 页面形态选择

- 业务聚合、需要独立详情或持续编辑上下文的主数据和配置，使用 `LIST + EDIT` 工作台页签。
- 少量字段、无主从明细、无状态流转且不需要独立详情入口的轻量维护，可以使用列表加 `ModalEditPage`，例如弱口令词条；采用时在领域文档说明范围。字段少本身不足以把业务单据改为弹窗。
- 标准编辑和弹窗编辑分别复用 `EditPage`、`ModalEditPage`，均须落实权限、加载与错误阻断和版本检查。独立页签遵守脏数据关闭保护；弹窗的取消与未保存处理由页面明确，不能假定模板自动提供关闭确认。
- `CUSTOM` 仅用于通用列表与编辑模型无法表达的布局或交互，不用于绕过页面框架。
- 页签标题与稳定身份遵守[页面注册约定](../architecture/page-registration-convention.md#页签标题)。`ModalEditPage` 标题只显示基础名称，不添加“新增”“编辑”“查看”“详情”等操作文字；弹窗本身不因此新增工作台注册项。

页面框架导入本身不代表接入合规：仍须核对字段布局、引用选择、权限和版本、加载阻断、未保存处理及保存后同步。标题与普通字段的可确定声明由页面接入门禁检查；动态标题、跨组件字段和实体选择的业务含义仍需评审，不能把一次静态检查当作浏览器验收。

## 应用首页

- 新应用只有在存在明确的应用级快捷入口、业务摘要、待办、趋势或运行状态时才实现专属首页；首页组件放在对应领域应用的 `home/` 目录，并通过 `pages/workbench/applicationHomeRegistry.ts` 注册。没有专属内容时使用工作台默认首页，不虚构业务能力。
- 卡片行统一使用 `domain/common/home/HomeCardGrid`。该容器固定为四列轨道，卡片通过 `sm-home-card-span-1`、`sm-home-card-span-2`、`sm-home-card-span-3` 或 `sm-home-card-span-4` 表达等分、`1:3`、`2:2`、`3:1` 和整行布局；领域页面不得复制网格 CSS。
- 屏幕变窄时必须保持同一行的列数和跨列比例，由 `HomeCardGrid` 提供行内横向滚动，不得通过媒体查询把卡片自动折到下一行。首页整体内容超出视口时仍由首页容器纵向滚动；卡片内部存在固定高度列表时由卡片内部滚动。
- 文字指标卡的标题、主值和说明默认水平、垂直居中；同一卡片包含多个并列指标时使用稳定分割线区分。图表、表格和拓扑卡按内容特征布局，不强制套用文字指标卡结构。
- 系统管理首页复用现有模块查询：启用用户按启用状态统计（不表示一定满足登录条件），组织统计未封存树节点（含各层级），角色统计已定义角色总数（不表示已分配授权次数）。各指标分别遵守对应列表权限；权限未就绪不请求，无权限不展示缓存数值，失败显示错误与重试，不将失败当成零。查询键复用所属模块，随既有维护命令失效。
- 首页优先展示权限和数据范围内的真实业务数据；暂时没有安全、稳定的聚合接口时可以使用示例卡片，但每张示例卡必须明确显示“示例”，不得与真实数据混淆。
- `QuickLaunchCard` 是可选的当前用户偏好能力，不是应用首页必备区块。只有应用菜单较多且用户确有高频快捷发起需求时才接入；不需要时直接省略，不渲染占位。系统首页是否使用该能力由系统首页需求决定。
- 没有额外操作、筛选或状态信息时，不在应用首页顶部重复展示应用名称或说明性头部。

## 列表和命令

`ListPage` 是展示与命令入口，`useListPageQuery` 管理查询。接入时将 Hook 的 `records` 传给 `dataSource`，`onSearch` 传给 `onQuickSearch`，分页参数与回调显式对应，并展开 `columnQueryProps` 接通服务端筛选和排序。Hook 不返回可直接展开到页面的完整 props，也不负责树范围、选择清理或领域命令。树等额外条件应进入实际查询键和请求参数，不能只改变树的选中外观。

加载、阻断错误及重试由页面按实际查询接入；已有数据的刷新失败与首次加载失败按[请求错误协议](../architecture/frontend.md#请求错误协议与反馈)区分，优先复用 `getBlockingQueryError`，不直接把每次 `query.error` 都当作整页阻断。

- 默认使用复选框；单记录命令在恰好选中一条时启用。只有业务语义天然互斥且无法用命令启用条件表达时才使用单选。
- 默认不增加“操作”列。编辑或查看优先使用可点击的 `number` 等稳定业务标识，其他命令集中在页面操作区。
- 表格中打开工作台页签或编辑弹框的编码、姓名等文本入口使用按钮语义：采用 Ant Design `<Button type="link">`（或 `variant="link"`），简单原生按钮使用 `<button type="button" className="sm-table-link">`；真实 URL 导航使用带 `href` 的链接。
- 文本入口默认无下划线，启用状态下鼠标悬停显示下划线，并允许拖选、复制文字；保留按钮原生的焦点、Enter/Space 激活和禁用语义。交互样式统一由 `src/styles/global.css` 维护，业务页面复用公共规则。
- 可由 `columnFeatures` 表达的字段筛选、枚举筛选和排序不得通过 `filterContent` 重复实现。通过 `ListPage.expandedFilterFields` 按顺序声明需要在展开区显示的高频字段，复用同一列筛选状态；未声明时不自动铺开所有列。编码、名称优先使用右上角快速搜索，描述、技术标识和其他低频字段保留表头筛选。
- `ListPage.defaultFilterExpanded` 控制首次打开是否展开，默认 `false`；用户切换展开状态后不因重新渲染重置，收起时继续显示已应用条件摘要。
- 筛选摘要区 `sm-list-filter-summary` 仅用于展示当前已应用的筛选条件，`ListPage.filterSummary` 也必须遵守这一边界；禁止放置页面使用说明、业务提示、警告或其他非筛选信息，确保所有列表页语义一致。提示信息应按作用范围放在独立的页面提示区或相关内容附近，不得借用筛选摘要区。
- 筛选摘要项的关闭按钮表示移除对应条件，仅可清空的条件提供关闭入口。业务要求必须保留的筛选条件（如单据列表必须选择一个组织）仍须展示摘要，但不显示关闭按钮；使用 `ListFilterSummary` 时设置 `removable: false`。此类条件可以按业务规则切换取值，但筛选控件的清空、重置等入口也不得将其移除。
- 特有查询参数通过 `filterContent` 接入时使用 `ListFilterFields` 与 `ListFilterField`，不得自定义固定宽度或复制布局。过滤区采用浅灰背景、四周细边框和白色完整边框控件；一、两项使用较宽列，所有字段从轨道左侧开始，首项左侧距离不随条件数量改变，最多三列，按面板可用宽度自动降至两列、一列。查询条件之外的统计信息放在工具栏。
- 高频条件选中或清空后直接应用；审计日志保留结果、事件（登录日志）和时间范围，Trace ID 使用表头筛选。
- 至少保留一个未设置 `width` 的业务列吸收剩余空间。
- 普通只读列表的文本列必须显式使用 Ant Design `ellipsis: true`（或标准 `ellipsis` 配置对象），超宽文本保持单行省略，避免数据长短造成行高不一致。此规则适用于现有列表、新业务单据列表、CUSTOM 页面列表和只读弹框列表；不增加固定行高、强制裁剪或公共表格隐式默认值。
- `ListPage` 的每个叶子列必须显式声明 `ellipsis`，分组表头本身无需声明。确需分行的业务内容或非文本控件可使用 `ellipsis: false`，但必须在该属性前给出简体中文业务原因注释；例如用户多岗位需要逐项对应展示。可编辑明细和展开详情按自身业务布局处理，不为统一外观破坏必要内容。
- 关键金额和数量遵守[完整值要求](../design/foundations.md#关键金额与数量)，通过列宽、布局及现有有注释的业务例外处理，不全局关闭省略或放宽门禁；存量集中适配另行实施。
- 自定义 `render` 仍须核对最终文本截断、列宽、点击与复制行为；`ellipsis` 不能替代业务内容设计，也不能保证任意嵌套组件自动省略。长文本完整内容通过标准提示或原有详情入口查看，不因省略丢失访问路径。标准列表的声明约束由模块约定脚本及测试校验，特殊渲染和业务例外仍需浏览器验收。
- 左树必须具有符合业务语义的根节点并默认展开；组织树等已有稳定语义的结构必须与现有页面保持一致，不得擅自增加“全部”等虚构层级。
- `ListPage.treePanel` 中的筛选树使用 `ListTreePanel` 包裹 `ListTree`；即使没有搜索框或底部筛选，也不能省略面板容器。头部和底部控件分别通过 `header`、`footer` 传入，树主体由公共组件统一提供内边距与横纵滚动。
- 普通目录树采用 `virtual={false}`、`showLine={false}` 和 `blockNode`，长名称保持单行并通过面板横向滚动。确需虚拟化的大树必须另行验证可视高度、长名称和滚动行为，不能直接沿用 Ant Design 默认配置。
- 左树验收须与同类页面（如应用管理）对比最终计算样式，核对内边距、节点缩进、选中背景以及内容溢出时的滚动；只确认外层面板宽度或树能显示，不算完成布局验收。

## 编辑页

`EditPage` 必填 `title`、`sections` 和 `operationType`，普通字段通过 `editFormSection` 组合成分区；`ModalEditPage` 则使用 `fields`、`open`、`onClose` 和 `onSave`。两种模板的 props 不能互换，完整类型以各自源码为准。`initialValues` 是稳定的服务端快照，不能在渲染时内联重建等价对象；`editable`、权限、版本及外部脏状态仍由领域显式接入，不因用了模板就自动具备。

页签晋升、保存回显、只读与脏数据保护遵守[单据页签生命周期](../architecture/frontend.md#单据页签生命周期)；以下只说明页面接入方式。

- 业务详情、记录详情和只读查看使用工作台通用编辑/查看页签；Drawer 只用于不形成独立页面的临时辅助操作。
- 独立页签中的标准字段、引用选择器和普通命令表单使用 `EditPage`；轻量弹窗维护遵守上述页面形态选择。
- `EditPage` 的全部正文卡片由调用方通过 `sections` 决定身份、顺序和内容，公共组件不得固定基本信息、明细、附件或其他业务分区。标准字段卡片复用 `editFormSection`；附件等通用能力提炼为组件或 Hook 后由领域页面显式组合，并通过通用的值转换和脏状态接口接入保存生命周期。
- `EditPage` 的明细区域或其他使用 `EditSectionCollapse` 的数据卡片需要提供新增、删除、排序等操作时，必须通过对应分区的 `extra` 将操作统一放置在标题栏右侧。表格通过选择列确定操作目标，禁止新增“操作”列，也不得将操作按钮散落在折叠面板内容区。只有存在明确的特殊交互要求时才允许偏离，并应说明原因。
- 基本身份字段放在“基本信息”；协议、连接、运行参数等领域配置放在独立面板，不把所有字段堆进一个分组。
- 启停、封存等独立状态不进入普通保存或提交类型。新增业务单据尚未持久化，不显示单据状态；首次保存或直接提交后的真实状态由后端返回。已有单据状态只读展示，状态选择必须使用服务端校验的独立命令，详见[单据状态架构](../architecture/bill-status.md)。
- 通用说明性字段统一为 `description`，标签为“描述”。
- 标准新增/编辑页面使用 `useEditTabLifecycle` 处理新增识别、临时页签晋升和退出；领域页面继续拥有保存参数映射与 Query Key。
- 保存 Mutation 的 `mutationFn` 只执行远端命令。远端成功后通过 `runEditSavePostCommit` 独立执行页签同步与缓存刷新，并按结果反馈；本地同步失败不得误报为保存失败，也不得阻止另一同步阶段尝试执行。

### 页面与分区提示

列表、编辑及 CUSTOM 页面按作用范围选择公共提示插槽，不允许业务页面自由决定整行提示的位置。提示采用 `PageNoticeConfig` 声明，由公共 `PageNotice` 使用 Ant Design Alert 的公开接口呈现；不虚构“提示信息”折叠卡片，不挤入按钮、标题或筛选摘要行。

| 作用范围 | 接入位置 | 布局语义 |
| --- | --- | --- |
| 列表整页 | `ListPage.notice` | `sm-list-top` 上方独占一行，覆盖搜索、命令及树表的共同说明 |
| 列表表格 | `ListPage.tableNotice`（底层 `ListTableShell.notice`） | `sm-list-table-content` 内第一行、统计分页栏之前；左树右表时仅占右栏，随右栏宽度换行 |
| 编辑整页 | `EditPage.notice` / `EditPageShell.notice` | `sm-edit-header` 上方独占一行；无操作栏时仍位于正文之前 |
| 编辑分区 | `EditPageSection.notice` / `EditSectionCollapseItem.notice` | 对应卡片上方，与卡片组成一个视觉单元；折叠卡片不隐藏提示 |

同一提示只选择一个范围，不在整页和分区重复展示。影响整页的限制不能放进右侧表格或某张卡片，局部说明不提升为整页横幅。仅解释展开内容的辅助说明可以放在分区正文顶部；字段用途仍使用字段 Tooltip，校验错误由 Form 就近呈现。操作反馈与请求错误继续遵守原有协议，不用说明插槽代替加载失败、权限阻断或保存反馈；页面阻断时隐藏业务提示。

`toolbarExtra`、`actions`、`headerActions` 和分区 `extra` 用于操作、操作上下文控件或简短统计，禁止放置业务说明、警告和长段文字；`filterSummary` 仅用于当前筛选条件。CUSTOM 页面也应复用 `PageNotice` 并遵守上述独立行与作用范围。

提示声明包含稳定 `key`、`title`，可选 `description`、`type`、`closable` 和 `onClose`。默认 `type="info"`、显示图标、不可关闭；需要注意的约束用 `warning`，错误状态用 `error`，确认成功的状态用 `success`。默认使用 banner 表现，内容自然换行、允许长连续字符串断行，不固定高度、不单行省略、不横向滚动播放、不撑宽按钮区。间距遵守[设计基础](../design/foundations.md#空间与边界)，提示关闭后不保留占位。

重要限制在条件解除前必须可见，不能允许关闭。可忽略的辅助说明可以显式设置 `closable: true`，通过 `onClose` 接收关闭通知；关闭仅影响当前挂载的提示，切换保活页签不会重现，刷新或重新打开页面会恢复。不默认写入浏览器存储或用户偏好。业务对象或提示语义变化时更换 `key`，避免先前关闭状态隐藏新的提示；条件解除时传 `undefined`。提示不替代权限、按钮禁用、字段校验或服务端约束。

```tsx
<ListPage
  {...listProps}
  notice={{ key: 'page-help', title: '整页辅助说明', closable: true }}
  tableNotice={{ key: selectedCategory, type: 'warning', title: '当前分类的限制说明' }}
/>

<EditPage
  {...editProps}
  notice={{ key: 'document-rule', title: '整张单据的规则说明' }}
  sections={[
    {
      ...editFormSection('basic', '基本信息', fields),
      notice: { key: 'basic-rule', title: '仅适用于基本信息的说明' },
    },
  ]}
/>
```

示例变量由调用方提供。新增或修改提示须验证普通列表、左树右表、编辑页与分区折叠等实际涉及场景，包括长文本窄屏换行、关闭后无占位、提示身份变化重新显示及按钮区不受挤压；公共组件行为由测试保护，动态内容与实际布局仍须浏览器验收。

### 表格空状态

占满容器剩余高度的 Table 必须增加 `sm-table-fill` 语义类，由公共样式统一移除空状态占位行的悬空下边框；普通内容高度 Table 不使用该类。

### 可编辑明细表格

表单内可编辑明细表格统一复用 `EditableDetailTable`，完整接口见 [EditableDetailTable.tsx](../../smart-manage-web/src/domain/common/component/EditableDetailTable.tsx)。

- 默认显示 `#` 序号列，并将 `36px` 复选框列和紧随其后的 `44px` 序号列固定在左侧；只有表格已提供具有明确业务含义的顺序列时才关闭默认序号列。
- 点击复选框执行增量多选，点击行内其他区域收敛为当前行单选。
- 编辑元素使用容器白色背景，普通行控件保持无边框，悬停或选中行显示与单元格保持约 `3px` 间距的控件边框，焦点和错误状态分别使用主色和错误色，`Switch` 不增加外围编辑框。
- `RefSelector` 等默认使用下边框的字段进入明细表后，由通用明细组件消除重复底边框，只保留当前交互状态所需且不被内部输入元素遮挡的一层完整控件边框。
- 必填标识放在列头；校验失败时由页面提示并聚焦首个错误单元格，错误文案仅在该单元格聚焦时浮动展示，禁止使用会挤占表格高度的底部常驻错误区。
- 业务列、动态控件和领域校验仍由领域组件维护。

## 表单字段布局

普通表单字段统一使用 `FormFieldGrid` 与 `FormFieldCell`；标准 `EditPage` 通过 `EditFormFields` 间接复用，手写字段保持以下结构：

```tsx
<FormFieldGrid maxColumns={2}>
  <FormFieldCell columnSpan={2}>
    <Form.Item className="sm-edit-field-content">...</Form.Item>
  </FormFieldCell>
</FormFieldGrid>
```

- 页面默认四列上限，普通弹框使用 `maxColumns={2}`，窄参数弹框使用 `maxColumns={1}`；两列和单列容器整体居中，内部字段仍从左向右排列。
- `FormFieldCell` 默认占一列，使用 `columnSpan={2 | 3}` 或 `fullWidth` 跨列；`Form.Item` 必须是其直接子元素并使用 `sm-edit-field-content`。
- 非校验类字段说明通过字段名右侧的圆圈问号悬浮显示：通用 `EditField.tooltip` 接受调用方提供的内容或 Ant Design Tooltip 配置，普通编辑页、弹窗编辑页及只读字段统一支持；手写字段使用 `Form.Item.tooltip`。不要用字段下方的 `extra` 承载这类说明而改变表单行高。校验错误仍由 Form 在字段下方展示。
- 字段宽度、间距、断点和跨列计算属于公共布局契约，领域页面不得直接拼装或覆盖内部 `sm-edit-*` 类；新布局需求应先确认边界再扩展公共组件。
- 头像、分区标题、提示和按钮区由外层布局组织；查询筛选器、隐藏字段、可编辑表格单元格、代码编辑器及专用控制台不套用字段容器。

## 实体引用选择

### 适用范围

- 从已有实体集合中选择一条或多条记录，并用于表单引用、主从明细、关联关系、授权关系或分配结果时，统一使用 `RefSelector`。
- 该规则不受页面形态和入口位置影响，适用于 `EditPage`、列表页、CUSTOM 页面、明细卡片和操作按钮弹框。
- 优先由实体所属领域提供 `use*RefSelector`，集中定义候选查询、展示字段、选择模式和业务范围。

### 标准交互

- 搜索框位于弹框标题栏右侧，候选数据使用服务端分页和关键字查询。
- 单选使用单选列，多选使用复选列；点击整行执行选择或取消，多选支持跨页保留和已选结果管理。
- 选择列、序号列和内容型业务列使用稳定宽度，并至少保留一个未设置宽度的业务列吸收剩余空间。
- 表格统一使用固定布局、文本省略和完整 flex 高度链。

### 查询与缓存

- 影响候选范围的组织、排除集合和选择用途等上下文必须进入查询参数及稳定 `selectorKey`，避免候选数据串用。
- 已关联或不可选记录应由服务端排除；需要向用户解释不可选原因时，由选择器明确禁用并展示原因。

### 结果边界

- `footer(context)` 可替换整个底部操作区，返回 null 隐藏；省略时仍为取消／确定。上下文提供只读 `selectedRecords` 数组快照、`confirming`、`confirm()`、`cancel()` 和 `clearSelection()`；清空只影响弹框暂存选择。
- 默认确定、调用方的 `context.confirm()` 和单选双击统一先执行 `onConfirm(records)`。回调成功后才触发 `onChange` 并关闭；返回 false 或抛错保留弹框及选择，业务错误反馈由回调负责。异步等待时冻结候选区、禁止重复确认和关闭；回调期间选择器被禁用时撤销本次选择提交。自定义业务确认按钮必须调用 `context.confirm()` 并消费 `confirming`，不能另行绕过共同流程。无需业务前置动作时省略 `onConfirm`。

- 选择器确认后向领域页面返回实体对象，由领域页面合并表单或暂存关系；最终保存只提交实体 ID。
- 取消或关闭选择器不得修改页面暂存结果。
- 只有 `RefSelector` 无法表达真实业务交互时才扩展或自定义选择能力，并在实现基线中记录差异及原因。

## 弹框、表格和视觉

基础色彩、字体、空间与边界统一见[设计基础](../design/foundations.md)；工具栏命令视觉、顺序与表格四档行背景见[桌面设计](../design/desktop.md)。这些规则已迁入设计正文，本节只保留接入契约。实现时先遵守[适配边界](../design/adaptation-boundaries.md)，不为视觉差异修改依赖内部结构。

- 业务表单、命令参数和复杂结果展示弹框默认使用 `AppModal`。
- 删除、清空、覆盖、提交等需要用户二次确认的操作统一通过 `useOperationConfirm` 调用，并显式选择 `delete`、`destructive`、`warning` 或 `normal` 风险类型。确认弹框由公共组件统一提供可拖动标题栏、风险色圆点、业务插画、`14px` 提示文案和底部按钮区；点击遮罩不得关闭。危险操作不得使用 Enter 确认，业务页面禁止直接使用 `Modal.confirm` 或 `Popconfirm`。
- 保存、提交、审核、删除、复制等操作结果统一通过 `useOperationFeedback` 展示，顶部反馈正文统一使用 `14px`。反馈分为 `success`、`warning`、`error`、`info`：成功使用绿色，可由用户修正的输入或业务状态问题使用黄色，网络、安全、系统及未知失败使用红色，中性说明使用蓝色。反馈默认自动关闭；只有确需用户持续查看时由调用方设置 `autoClose: false`，此时右侧显示关闭按钮。顶部最多保留三条；业务页面禁止直接调用 Ant Design `message.*`。
- 错误级别解释、安全失败优先级与反馈所有权遵守[请求错误协议与反馈](../architecture/frontend.md#请求错误协议与反馈)，页面不维护第二套错误码映射。
- 业务弹框中的标准控件以通用编辑页为视觉基准，默认使用下边框控件和稳定列宽；特殊宽度必须由真实布局需要驱动。
- 表单字段校验由 Ant Design Form 负责；没有明确的外部不可信数据校验需求时，不引入重复校验模型。
- CUSTOM 页面中的数据表默认关闭表格内置底部分页，将分页放在所属卡片或区块右上角元信息区。
- 固定高度表格建立从容器到 `.ant-table-body` 的完整 flex 高度链，使横向滚动条固定在内容区底部；禁止用固定 `scroll.y` 撑出伪布局。
- TSX 中禁止 CSS 和内联 `style`；自定义类名以 `sm-` 开头。

### 页面操作的有序声明

- `ListPage.toolbarActions` 和 `EditPage.headerActions` 是完整有序声明，不再表示追加业务按钮。省略或传 `undefined` 使用底座默认布局；传 `[]` 不展示操作按钮；显式数组中未声明的内置按钮不会自动追加。
- 数组可混合业务 `PermissionAction` 与 `{ builtin: '...' }` 引用。列表内置名称为 `add`、`delete`、`enable`、`disable`、`refresh`；编辑内置名称为 `save`、`submit`、`exit`。回调及 `access` 仍通过页面原有属性传入，引用只控制位置，不能覆盖内置权限、校验、加载状态和保存流程。
- 未提供对应回调、只读状态等导致内置按钮不可用时跳过该引用；权限过滤后其余按钮保持声明顺序。业务按钮条件显示仍使用条件数组展开。
- `PermissionActions` 只过滤权限并按输入顺序渲染，`danger` 不触发自动排序。默认页面布局仍把危险命令放在末尾；显式布局应遵守上文命令分组规范，业务流程例外在调用处说明。
- 同一数组内业务 key 必须唯一，同一内置引用不能重复；重复声明直接报配置错误。业务 key 与内置名称使用不同命名空间，可以同名。
- 迁移旧调用时，在原业务按钮数组中显式补齐所需内置引用；直接使用 `PermissionActions` 的调用方也应自行声明危险命令的位置。

以下片段假定 `listProps`、`editProps` 已完成查询、权限和生命周期接入，业务 action 和回调由调用方提供；不构成可直接运行的完整页面。展开基础属性在前，显式操作声明在后，避免同名属性覆盖示例意图。

```tsx contract=page-actions
<>
<ListPage
  {...listProps}
  onRefresh={handleRefresh}
  toolbarActions={[registerAction, renewAction, { builtin: 'refresh' }, deleteAction]}
/>

<EditPage
  {...editProps}
  onSave={handleSave}
  onExit={handleExit}
  headerActions={[{ builtin: 'save' }, previewAction, { builtin: 'exit' }]}
/>
</>
```

### 异步确认与结果反馈

风险确认由调用方提供本次操作的安全对象标识、影响范围和后果，文案与提交参数使用同一份对象快照。批量操作明确总数；摘要未展开全部对象时明确剩余数量，跨页对象无已加载名称时可直接显示其 ID，不用当前页记录数代替命令范围。只做文案格式化可复用 `formatOperationTargets`，确认组件不推断领域字段。不得为说明对象展示真实密码、令牌、密钥、完整缓存值或原始敏感请求正文，隐私边界遵守[数据脱敏](../architecture/data-masking.md)。

`useOperationConfirm` 需要应用级 `OperationConfirmProvider`；反馈使用现有 Ant Design App 上下文，不在每个业务页面重复挂载 Provider。`confirmOperation(options)` 返回 `Promise<boolean>`，成功执行确认回调后为 `true`，取消或已有确认流程占用时为 `false`。回调拒绝时保留弹框、恢复可重试状态，不会立即把外层 Promise 结算为失败；回调返回 `false` 也不表示取消，它仍是一次正常完成。

命令必须在 `onConfirm` 内返回或等待完整 Promise；不要使用 `onConfirm: () => mutation.mutate(...)`，也不要先等待弹框关闭再执行命令，否则等待期间的防关闭、重试和重复提交控制失去命令生命周期。错误提示由领域 Mutation 按既有错误所有权处理，确认组件不自动展示错误；不要在 Mutation、调用方 catch 和反馈 Hook 中重复提示。

下例的 `deleteRecords` 代表已接入领域 Mutation 的异步命令：失败提示由它负责，成功提示仅在这里负责。实际页面若已在 Mutation 中提示成功，应省略这里的重复提示。

```tsx contract=operation-confirm
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';

export function useDeleteWithConfirmation(deleteRecords: (ids: string[]) => Promise<void>) {
  const confirmOperation = useOperationConfirm();
  const feedback = useOperationFeedback();
  return (selectedIds: string[]) => {
    const commandIds = [...selectedIds];
    return confirmOperation({
      type: 'delete',
      description: `确认删除选中的 ${commandIds.length} 条记录？`,
      onConfirm: async () => {
        await deleteRecords(commandIds);
        feedback.success('删除成功');
      },
    });
  };
}
```

片段只说明确认时序，不提供通用删除能力；空选择、命令权限、对象范围和刷新后的选择清理按实际页面处理。调用 Hook 必须遵守 React Hook 规则。`feedback.success/warning/error/info/fromError` 不返回消息 key，需要主动关闭或更新同一条提示时传入稳定 `options.key`，再调用 `feedback.close(key)`；错误分类继续复用 `fromError`，不自行映射错误码。

### 弹框页脚按钮

普通弹框底部按钮统一由 `AppModal` 页脚居中排列，间距为 `12px`，与 `RefSelector` 默认页脚一致。此契约同时适用于编辑、命令参数、操作结果及二开业务弹框。

- `footer` 使用单个按钮、Fragment 或按钮数组，使按钮成为公共页脚容器的直接子元素；不要额外使用 `Space`、`Flex` 或布局 `div` 包裹按钮，也不要在业务侧重复设置 `12px`。
- `PermissionActions` 必须显式设置 `grouped={false}`，且放在属性展开之后；`ModalEditPage` 已采用此方式。默认分组会生成内层 `Space`，使外层的 `gap` 无法控制按钮间距。
- 条件显示的按钮同样遵守此规则。正文中的 `Space` 不受页脚约束影响；不显示页脚时传 `null`。
- 确需左右分组、提示文字等特殊页脚布局时，先明确业务需要并评审布局契约，再扩展公共能力及对应检查；不得通过自定义组件隐藏包装、覆盖嵌套 CSS 或局部间距补丁绕过约定。

```tsx
<AppModal
  title="操作结果"
  open={open}
  onCancel={onClose}
  footer={
    <>
      <Button onClick={onClose}>关闭</Button>
      {canCopy && <Button onClick={onCopy}>复制结果</Button>}
    </>
  }
>
  {content}
</AppModal>
```

权限按钮页脚使用 `footer={<PermissionActions actions={actions} grouped={false} />}`。
常见违规由[质量验证](./verification.md#模块约定)中的页脚语法树检查拦截；跨文件自定义组件、函数返回值及动态属性展开仍需评审确认。
