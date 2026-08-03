# 基础数据管理

## 模块定位

基础数据是系统内核提供的可配置选项集，用于维护下拉框、筛选项和轻量枚举等跨模块引用数据。一个基础数据主记录代表一个选项集，`number` 是稳定的调用编码；明细 `number` 是提交给业务接口的选项值，`name` 是界面显示名称。

基础数据不承载客户、供应商、物料、仓库等具有独立身份、生命周期、权限或复杂业务规则的主数据。这类数据必须位于所属业务领域，不能以通用选项集代替领域模型。

## 聚合边界

- `BasicData` 是聚合根，负责编码、名称、备注、启停状态和乐观锁版本；
- `BasicDataEntry` 是聚合内明细，不提供独立写接口，生命周期完全从属于聚合根；
- 保存命令接收完整聚合，根版本控制主表与全部明细的并发修改；
- 明细按 ID 增量同步，保留已有明细身份和审计信息；请求中的明细 ID 必须属于当前聚合；
- 主记录编码全局唯一，明细编码在同一主记录下唯一；
- 删除主记录前显式删除明细，不依赖数据库级联删除。

## 消费端语义

- 消费端通过基础数据编码读取选项，不直接依赖 Mapper 或明细表；
- 只有主记录和明细均启用时，明细才会出现在选项结果中；
- 选项按 `sort`、`number`、`id` 稳定排序；
- 选项结果使用 JetCache 本地缓存，保存、删除和启停命令在数据库事务提交后失效对应编码的缓存；
- 当前项目声明为单节点运行。改为多节点部署前，必须将本地缓存失效策略重新纳入架构验收。

## 接口与权限

| 能力 | 接口 | 权限码 |
| --- | --- | --- |
| 分页列表 | `/sys/base/basic-data/listPage` | `sys:base:basic-data:listPage` |
| 详情 | `/sys/base/basic-data/detail` | `sys:base:basic-data:detail` |
| 新增默认值 | `/sys/base/basic-data/createNewData` | `sys:base:basic-data:save` |
| 保存聚合 | `/sys/base/basic-data/save` | `sys:base:basic-data:save` |
| 删除聚合 | `/sys/base/basic-data/delete` | `sys:base:basic-data:delete` |
| 批量启用 | `/sys/base/basic-data/enable` | `sys:base:basic-data:enable` |
| 批量禁用 | `/sys/base/basic-data/disable` | `sys:base:basic-data:disable` |
| 获取启用选项 | `/sys/base/basic-data/options` | 登录用户 |

## 前端页面

- `sys/base/basic-data` 注册为标准列表页，提供搜索、分页、批量启停和单条删除；
- `sys/base/basic-data/edit` 注册为标准编辑页，主信息与选项明细处于同一表单和脏数据保护范围；
- 新增页使用临时页签，首次保存后替换为真实 ID 页签；保存后保留当前编辑页并重新查询服务端状态。

## 数据库迁移

- 基线表结构由 `V1__baseline_schema.sql` 建立；
- `V22__harden_basic_data_aggregate.sql` 收紧明细排序约束并明确表语义；
- 已执行迁移不可修改，后续结构和必要初始化数据继续通过新增 Flyway 迁移维护。
