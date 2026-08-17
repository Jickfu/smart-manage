# 基础数据管理

## 模块定位

基础数据是系统内核提供的可配置基础资料平台，适用于下拉框、树形选择、筛选项及行业、地区、设备类型等轻量级跨模块引用数据。

基础数据不承载客户、供应商、物料、仓库等具有独立身份、生命周期、权限或复杂业务规则的主数据；这些数据必须位于所属业务领域。

## 数据层级

```text
云
└── 基础资料分类
    └── 基础资料节点
        └── 下级基础资料节点
```

- 云是分类的展示和归属维度；分类只能直接归属于一个云，分类本身不嵌套。
- 分类 `number` 是供业务代码稳定引用的全局唯一标识。
- 分类为资料节点选择编号模式和 `CATEGORY` 作用域的公共编号规则；分类自身的稳定编码不自动生成。
- 分类和资料节点的长期说明统一使用 `description`。
- 资料节点通过 `parent_id` 在同一分类内形成任意级树；平级资料是所有节点 `parent_id` 为空的特例。
- 分类与资料节点分别按命令维护，不再一次性加载和保存整个分类的全部节点。

## 资料节点派生字段

- `level`、`number_path`、`name_path` 和 `is_leaf` 只由后端生成和维护，前端不得提交。
- 长编码和长名称使用 `/` 连接祖先路径，因此编码和名称禁止包含 `/`。
- `is_leaf` 持久化，并与新增、移动、删除命令处于同一数据库事务。
- 新增首个子节点时，上级改为非叶子；移动或删除最后一个子节点时，原上级恢复为叶子。
- 业务选项接口只返回分类及所有祖先均启用的叶子节点。非叶子节点只承担分组和导航语义，不是有效业务选项。

## 业务约束

- 上级资料必须与当前资料属于同一分类。
- 禁止选择自身或自己的后代作为上级，避免形成环。
- 同一分类下资料编码唯一。
- 资料不能跨分类移动；需要跨分类时应在目标分类新建并显式处理原资料。
- 非叶子节点不能删除；分类下存在资料时不能删除分类。
- 系统预置分类和资料不能删除。
- 分类和资料节点都使用乐观锁版本号；路径变更在同一事务内更新全部后代。

## 节点编号

基础资料分类默认使用 `AUTO_DEFAULT`，并支持三种节点编号模式：

- `MANUAL`：新增时必须人工填写编码；
- `AUTO_LOCKED`：新增保存事务内自动生成，生成后禁止修改；
- `AUTO_DEFAULT`：新增时留空则自动生成，也允许人工填写；已有节点允许修改编码。

自动生成使用编号引用 `sys/base/basic-data-item.number` 调用公共 `NumberGeneratorAccessor`，以分类 ID 作为
流水作用域。规则格式可以加入受控变量 `category.number` 显示分类编码；分类编码变化只影响之后生成的号码，
计数器仍使用稳定分类 ID。人工填写不消耗流水。
修改已有编码时，后端继续在同一事务中更新全部后代 `number_path`。分类切换模式或规则不会重编已有节点。

## 页面交互

- `sys/base/basic-data` 使用左树右表：左树显示云和基础资料分类，右表显示具体资料。
- 左树上方提供分类新增、编辑和删除；新增、编辑分类使用通用弹框编辑页。
- 只有选择分类后才能新增基础资料；新增页将所选分类作为只读字段固定展示。
- 基础资料新增、编辑和查看使用工作台通用编辑页，上级资料使用树形选择。
- 右表遵循通用列表页的查询区、按钮区、分页、勾选和批量启停规范。

## 接口

| 能力 | 接口 | 权限码 |
| --- | --- | --- |
| 云与分类树 | `/sys/base/basic-data/categoryTree` | `sys:base:basic-data:listPage` |
| 分类详情 | `/sys/base/basic-data/categoryDetail` | `sys:base:basic-data:detail` |
| 保存分类 | `/sys/base/basic-data/saveCategory` | `sys:base:basic-data:save` |
| 删除分类 | `/sys/base/basic-data/deleteCategory` | `sys:base:basic-data:delete` |
| 资料分页 | `/sys/base/basic-data/listPage` | `sys:base:basic-data:listPage` |
| 资料详情 | `/sys/base/basic-data/detail` | `sys:base:basic-data:detail` |
| 保存资料 | `/sys/base/basic-data/save` | `sys:base:basic-data:save` |
| 删除资料 | `/sys/base/basic-data/delete` | `sys:base:basic-data:delete` |
| 批量启停 | `/sys/base/basic-data/enable`、`disable` | 对应启停权限 |
| 上级资料选项 | `/sys/base/basic-data/parentOptions` | `sys:base:basic-data:detail` |
| 有效叶子选项 | `/sys/base/basic-data/options` | 登录用户 |
| 分类编号规则选项 | `/sys/base/basic-data/numberRuleOptions` | `sys:base:basic-data:detail` |

## 缓存

业务消费端按分类编码读取有效叶子资料。缓存必须使用 Redis 远程缓存，或具备基于 Redis 广播/版本键的跨节点失效机制；分类或节点保存、删除、启停后，在数据库事务提交后使所有实例读取到新结果。不得依赖请求再次命中原节点。

## 数据库迁移

- 基线表由 `V1__baseline_schema.sql` 建立。
- `V12__add_numbering_rules.sql` 为分类增加节点编号模式和规则引用，已有分类默认使用 `AUTO_DEFAULT`。
- `V13__refine_numbering_rules.sql` 增加编号引用和结构化格式段，并将基础资料规则关联到 Feature。
- `V14__add_basic_data_item_version.sql` 补齐资料节点的乐观锁版本号字段。
- 已执行迁移不得修改，后续结构和初始化数据继续通过新增 Flyway 迁移维护。
