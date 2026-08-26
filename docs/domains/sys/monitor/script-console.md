# 脚本控制台

## 模块定位

- 脚本控制台是运维中心下的管理员应急运维能力，用于服务不停机时调用公开领域 Service、组合查询与处理逻辑、查看真实返回值；
- 本次实现包含“脚本控制台、脚本管理、执行历史”三个页面；
- 脚本使用 GraalJS Community 执行，Rhino 已移除；
- 本模块不承担可配置业务规则的发布与运行。执行器保留独立执行配置和绑定对象边界，未来业务脚本应在独立领域中设计发布、版本、灰度、回滚和专用绑定。

## 脚本能力

- 使用 `app.getService('beanName')` 获取允许访问的公开领域 Service；
- 可用普通 JavaScript 对象传入 Service Form 参数；网关按公开方法的参数类型转换后，在调用目标方法前统一执行 Jakarta Bean Validation，对象字段、嵌套 `@Valid` 和方法参数约束与 HTTP 入参共用同一套规则；
- Service 返回值转换为 JSON 兼容的基础值、对象或数组后交给脚本，可继续判断、遍历或作为脚本返回值；
- `console.log(...)` 和脚本 `return` 值都会展示在结果区域；
- 控制台快捷键为 `Ctrl + E`：有选区时仅执行选区，无选区时执行全部内容。
- 控制台提供常用脚本模板，载入模板前必须确认，模板只替换当前编辑器内容，不会修改已保存脚本；
- “使用帮助”通过工作台独立页签打开，不使用 Drawer；帮助页包含快速开始、模板示例和当前运行实例可调用的 Service API。

## Service API 帮助

- 脚本执行网关与 API 元数据使用同一个 `ScriptServiceCatalog`，不存在两套独立白名单；
- 不要求其他领域 Service 增加脚本专用注解，也不修改其他领域代码；
- 元数据自动展示符合现有规则的 Bean 名称、公开方法、参数类型、返回类型、Form 字段、Jakarta Validation 常用约束和调用示例；
- API 元数据与脚本执行使用相同的功能授权和管理员身份复核；
- 帮助页中的调用示例只用于说明参数形态，执行前必须按实际业务数据和方法语义核对。

示例：

```javascript
const orderService = app.getService('orderService');
const result = orderService.detail({ id: 10001 });
console.log(result);
return result;
```

## 安全边界

- Controller 校验细粒度权限，公开 `ScriptService` 再次确认当前账号为 `administrator`；
- 仅允许访问 `sm.domain.*` 下、类名以 `Service` 结尾的公开领域 Service；脚本模块自身不可被脚本调用；
- 不向脚本暴露 Mapper、TxService、DataSource、ApplicationContext、Environment 或任意 Spring Bean；
- 禁止 HostAccess、Java 类查找、反射、文件、网络、进程、本地能力和线程创建；
- 每次执行创建独立 Context；同一时间只允许一个脚本执行的约束通过 PostgreSQL 会话级 advisory lock 跨实例生效，不依赖单实例 `Semaphore`；连接异常退出时数据库自动释放锁；超时后主动取消 Context；
- 源码、输出和超时分别受系统参数限制；错误响应不包含服务端异常堆栈；
- 参数类型转换或 Bean Validation 失败时统一返回参数错误，且不得进入目标 Service 方法；
- 脚本正文、输出及错误只写入专用执行审计，不写入通用操作日志正文。

## 事务语义

- 默认使用 `ATOMIC` 原子事务模式，整段脚本位于一个 Spring 事务中；加入该事务的普通 `REQUIRED` 数据库操作在脚本报错或超时时全部回滚；
- `REQUIRES_NEW` 独立事务、异步任务、远程调用、消息发送和文件等外部副作用无法随外层事务回滚；
- `NON_ATOMIC` 为显式高风险模式，不提供整段脚本回滚保证，前端必须明确提示并二次确认；
- 原子执行失败时先完成业务事务回滚，再使用 `REQUIRES_NEW` 独立事务保存失败审计。

## 脚本管理与审计

- 保存脚本以 `number` 唯一标识，使用 `description` 记录用途说明，并使用 `version` 乐观锁防止覆盖更新；删除同样校验版本；
- 执行日志保存实际执行源码快照、关联脚本、事务模式、状态、耗时、事务结果、截断后的输出、错误、执行人、IP 和时间；
- 删除已保存脚本不会删除历史快照，日志表不设置脚本外键；
- 状态包括 `SUCCESS`、`ERROR`、`TIMEOUT`，事务结果包括 `COMMITTED`、`ROLLED_BACK`、`NOT_APPLICABLE`。
- 执行日志按[日志数据生命周期](../../../architecture/log-lifecycle.md)转入历史和淘汰。

## 系统参数

| 参数 | 默认值 | 合法范围 | 说明 |
| --- | ---: | ---: | --- |
| `SCRIPT_CONSOLE_TIMEOUT_SECONDS` | 30 | 1～300 秒 | 超时后取消脚本；原子模式回滚事务 |
| `SCRIPT_CONSOLE_MAX_SOURCE_LENGTH` | 100000 | 1000～1000000 字符 | 保存和执行源码上限 |
| `SCRIPT_CONSOLE_MAX_OUTPUT_LENGTH` | 100000 | 1000～1000000 字符 | 输出超过限制时截断 |

脚本执行、脚本维护和执行历史使用分离的功能授权；这些授权只应授予 `administrator` 角色，公开 Service 仍执行管理员身份复核。
