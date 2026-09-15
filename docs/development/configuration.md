# 环境与配置

## 项目配置命名空间

Spring Boot、Sa-Token、MyBatis-Plus、JetCache 等框架或第三方组件继续使用各自原生配置前缀。项目自定义配置统一放在 `smart-manage` 下，并以架构层级作为第一级命名空间：

```yaml
smart-manage:
  infrastructure: # 第三方技术、外部设施和技术适配
  system:         # 跨领域共享的系统内核与平台能力
  domain:         # 具有独立业务生命周期的领域配置
    sys:          # 系统管理业务领域
```

领域配置必须在 `domain` 下先声明具体领域，再按真实所有者细分应用或模块，例如监控配置使用 `smart-manage.domain.sys.monitor`。配置按能力所有权而不是读取类的位置归属，禁止在 `smart-manage` 下新增 `monitor`、`security` 等绕过架构层级的一级节点。该约束由 `SmartManageConfigurationNamespaceTests` 对所有内置环境配置执行自动化校验。

## 公共配置与环境配置

`application.yml` 只维护不随部署环境变化的应用身份、固定协议和架构约束，例如应用名、Profile 选择入口、JSON 规则、Flyway 安全策略、静态资源寻址、Quartz 存储结构、API context path、MyBatis-Plus 规则以及认证凭据读取方式。

端口、容量、超时、连接池、线程池、文件大小限制、缓存规模、日志留存、会话时长、密码计算成本、实例身份和监控频率等具有部署属性的配置必须放在 `application-{profile}.yml` 或外部配置中。不同环境当前取值相同也不能作为上移到公共配置的理由；内置环境文件同时承担开源部署模板和配置契约的职责，允许存在有意义的重复。

环境变量继续负责具体部署实例的覆盖。是否归入环境文件按配置语义判断，不按当前值是否重复判断。`SmartManageConfigurationNamespaceTests` 同时防止已识别的环境配置重新进入公共文件。

## 修改 API context path

后端 context path、浏览器 API 前缀和部署代理分别属于后端、前端与部署层。默认均使用 `/smart-manage-api`，当前开发代理和 Nginx 示例保留原请求路径，因此三者应保持一致；它们不是项目目录名、前端页面基路径或应用身份，修改时不需要重命名项目。

### 配置入口与修改步骤

以改为 `/custom/api` 为例：

| 层次 | 需要配置的位置 | 配置方式 |
| --- | --- | --- |
| 后端 | [`application.yml`](../../smart-manage-api/src/main/resources/application.yml) 的 `server.servlet.context-path` | 推荐使用外部 YAML 同名属性或环境变量 `SERVER_SERVLET_CONTEXT_PATH=/custom/api`；衍生项目改变默认值时才修改仓库配置 |
| 前端 | [`smart-manage-web/.env`](../../smart-manage-web/.env) 的 `VITE_API_BASE_PATH` | 本机可在 `smart-manage-web/.env.local` 写入 `VITE_API_BASE_PATH=/custom/api`；构建环境也可直接提供同名环境变量。两个 local env 文件模式已被 Git 忽略 |
| 生产代理 | 部署环境中的 Nginx 配置，参考 [`smart-manage.conf.example`](../../deploy/nginx/smart-manage.conf.example) | 将 `location` 改为 `/custom/api/`，保持 `proxy_pass` 不带 URI，以保留完整请求路径 |
| 生产内部地址 | `SMART_MANAGE_INTERNAL_BASE_URL`，入口见 [`application-prod.yml`](../../smart-manage-api/src/main/resources/application-prod.yml) | 显式提供包含新路径的完整内部 HTTPS 地址，例如 `https://node.example.com:9443/custom/api`；每个节点必须可被其他实例直接访问 |

前端变量是**开发启动／构建时配置**，不是浏览器运行时配置：修改后重启开发服务器，生产环境重新构建并部署整个 `dist`。仅在运行 Nginx 时设置变量不会修改已构建的前端。路径必须非空、以 `/` 开头且不带尾斜杠，路径段支持字母、数字、`_`、`-`，支持 `/custom/api` 这样的多级前缀；Vite 启动和构建会拒绝非法配置。

Axios 请求、界面配置图片、独立登录页与 Vite 开发代理均读取这一项前端变量，无需逐文件修改。登录页源码位于 [`smart-manage-web/login.html`](../../smart-manage-web/login.html)，通过 Vite 多 HTML 入口生成，发布路径仍是 `/login.html`，没有并入 React 路由。API 前缀替换发生在 meta 标签中，固定的内联脚本读取该值；改前缀不需要重新计算 CSP 摘要，修改脚本本身仍需同步摘要并通过测试。

[`application-dev.yml`](../../smart-manage-api/src/main/resources/application-dev.yml) 和 [`application-test.yml`](../../smart-manage-api/src/main/resources/application-test.yml) 的本机内部地址默认引用 `server.port` 和 `server.servlet.context-path`，无需重复修改。若已显式提供 `SMART_MANAGE_INTERNAL_BASE_URL`，仍以该完整地址为准，需要同步更新。生产环境继续强制显式提供内部地址，不从本机监听地址推导跨节点可达地址。

### 仍需单独维护的内容

- **部署代理和生产内部地址**：Nginx 不自动读取 Spring 或 Vite 配置，生产节点的主机名、协议和端口也不能从浏览器地址推导；保留上表两个部署入口。
- **开发后端端口**：若同时修改后端端口，更新 [`vite.config.ts`](../../smart-manage-web/vite.config.ts) 的 `apiProxyTarget`。它表示开发代理目标，不是 API 路径；`preview` 当前没有 API 代理，预览完整功能仍需部署层转发。
- **衍生项目的默认说明**：改变仓库默认值时，更新 [README 快速开始](../../README.md#快速开始)中的后端、Swagger UI、Scalar 和开发代理示例；单个环境覆盖无需改动上游默认示例。
- **外部调用方**：检查衍生项目新增的客户端、网关规则及保存的完整接口地址。这些由各调用方维护，不能通过本项目配置自动修改。

不需要修改后端 Mock 测试的路径样例；这些测试不加载部署前缀。安全测试应保留非空 context path 场景，URI 与 `setContextPath` 保持一致。前端图片补齐测试已读取配置，完整 URL 的透传样例也无需随部署改名。

不要直接删除浏览器 API 前缀或用 Vite 的 `base` 替代它：`base` 表示前端资源基路径，页面地址不能推导独立后端路径。后端可通过外部配置将 `server.servlet.context-path` 设为空字符串，但如果采用根路径后端，部署代理必须另行明确将非空的浏览器 API 前缀映射到后端根路径；当前代理模板不提供这种重写，不能仅清空一个配置就完成迁移。浏览器连接继续保持同源，不因引入变量而改为任意跨域 API 地址。

实际修改后的检查按[质量验证](./verification.md)执行，覆盖登录、会话请求、图片地址、代理转发和跨实例内部调用。可从仓库根目录运行 `rg -n --hidden -g '!.git/**' '/smart-manage-api'` 排查残余默认值，但不要全局替换源码目录链接或独立测试样例。

## 环境划分

| Profile | 用途 | 主要配置来源 |
| --- | --- | --- |
| `dev` | IDEA 和个人电脑开发运行 | 内置 `application-dev.yml` 加本机外部覆盖 |
| `test` | 共享测试服务器 | 内置 `application-test.yml` 加服务器外部覆盖 |
| `prod` | 生产部署 | Jar 同级 `config/application.yml` 和 `config/application-prod.yml` |

## 本机开发

项目默认激活 `dev`。仓库已跟踪 `smart-manage-api/src/main/resources/application-dev.yml`，它是公开开发模板；直接启动方式见 [README](../../README.md#快速开始)。`dev` 不继承 `test`。

真实连接信息通过环境变量或不纳入版本控制的外部配置覆盖，不在受跟踪模板中填写个人或生产凭据。使用外部配置时确认进程工作目录与配置位置；配置加载行为以实际启动参数为准。

Spring Data Redis 与 JetCache 使用两套配置入口，两处地址、端口、密码和数据库必须指向同一预期环境。公开示例凭据和加密密钥只允许用于本地开发和演示。

## 共享测试环境与可选环境变量

`application-test.yml` 用于共享测试环境。`${NAME:default}` 表示允许服务器外部配置或环境变量覆盖，
不是要求必须使用操作系统环境变量。保留这些占位符可供 CI、临时启动和不同测试服务器复用。

| 环境变量 | 用途 |
| --- | --- |
| `SMART_MANAGE_DB_URL` | PostgreSQL JDBC 地址 |
| `SMART_MANAGE_DB_USERNAME` | PostgreSQL 用户名 |
| `SMART_MANAGE_DB_PASSWORD` | PostgreSQL 密码 |
| `SMART_MANAGE_REDIS_HOST` | Redis 地址 |
| `SMART_MANAGE_REDIS_PORT` | Redis 端口 |
| `SMART_MANAGE_REDIS_PASSWORD` | Redis 密码 |
| `SMART_MANAGE_REDIS_DATABASE` | Redis 数据库编号 |
| `SMART_MANAGE_SM4_KEY` | 服务端敏感配置加密密钥，Base64 编码后必须解码为 16 字节 |
| `SMART_MANAGE_SM2_PRIVATE_KEY` | SM2 私钥 |
| `SMART_MANAGE_SM2_PUBLIC_KEY` | SM2 公钥 |
| `SMART_MANAGE_INSTANCE_ID` | 集群内唯一的应用实例 ID，例如 `instance1`、`instance2` |
| `SMART_MANAGE_HOST_ID` | 可选的运行主机稳定标识；同一 OS Host 上的多个实例必须一致，缺省使用 hostname |
| `SMART_MANAGE_MONITOR_RETENTION_DAYS` | 内建监控历史保留天数，默认 7 天 |
| `SMART_MANAGE_INTERNAL_BASE_URL` | 当前实例供其他应用实例定向调用的内部基础地址 |
| `SMART_MANAGE_TRUSTED_PROXY_CIDRS` | 允许提供转发头的受信代理网段 |

开发环境还支持 `SMART_MANAGE_DRUID_USERNAME` 和 `SMART_MANAGE_DRUID_PASSWORD` 配置 Druid 监控登录。

## 文件存储配置

文件存储配置只由数据库 `t_sys_file_config` 管理，通过管理端“存储配置”页面维护，不提供 YAML 或环境变量覆盖入口。Flyway 基线初始化 Local 存储，默认目录为 `./smfiles/`；配置记录缺失或 Local 目录为空时，文件存储操作明确报错，不使用代码兜底目录。配置记录缺失时，管理员仍可通过存储配置页面补建。

相对目录按 Java 进程的工作目录解析，不自动定位到 JAR 所在目录。使用 Local 时必须固定启动工作目录，保证存储目录可写；单实例生产还须将目录持久化并纳入备份。多实例必须在存储配置页面设置所有实例可访问的 S3 或 FTP 存储，具体要求见[部署与多实例架构](../architecture/deployment.md#文件与对象存储)。

## 生产环境

首次部署先按[生产密钥生成与部署](./production-keys.md)生成 SM2 密钥对和部署级 SM4 密钥。该指南包含 OpenAPI 凭据区别、配置接入、替换清单、备份及存量轮换边界。

推荐部署目录：

```text
smart-manage/
├─ smart-manage-api.jar
├─ config/
│  ├─ application.yml
│  └─ application-prod.yml
├─ logs/
└─ smfiles/
```

以仓库中的 `smart-manage-api/src/main/resources/application-prod.yml` 为配置项参考，在部署目录创建不纳入版本控制的
`config/application.yml` 或 `config/application-prod.yml` 并填写真实配置。启动进程的工作目录必须是 Jar 所在目录。
外部 YAML 优先于 Jar 内配置，因此生产部署可以使用外部 YAML、环境变量或外部密钥管理设施提供配置；
Jar 内部 `${...}` 占位符用于强制检查不可缺省的生产配置。

生产环境使用 `prod` Profile，必须显式配置：

- 数据库地址、用户名和密码；
- Redis 地址和密码；
- SM2 公私钥；
- SM4 敏感配置加密密钥；
- `SMART_MANAGE_CORS_ALLOWED_ORIGIN`；
- `SMART_MANAGE_INITIAL_ADMINISTRATOR_PASSWORD`，且不能为 `admin`。
- `SMART_MANAGE_INSTANCE_ID`，且每个应用实例必须唯一。
- 可选 `SMART_MANAGE_HOST_ID`；显式配置时只能包含字母、数字、点、下划线和连字符。同一主机上的实例必须配置为相同值。
- `SMART_MANAGE_INTERNAL_BASE_URL`，且必须是其他应用实例可直接访问的 HTTPS 内部地址。
- `SMART_MANAGE_TRUSTED_PROXY_CIDRS`，且只能包含实际 Nginx 或负载均衡器网段。

公共配置和生产配置均固定关闭 `sa-token.is-log`。Sa-Token 内置事件日志会输出完整会话 token，
因此 dev、test 和 prod 都不得重新开启；登录、退出和会话终止审计统一由项目受控的认证日志监听器记录。

连接池、端口、缓存和慢 SQL 等调优项及默认值以[生产配置模板](../../smart-manage-api/src/main/resources/application-prod.yml)为准，本文不重复维护默认值表。

敏感配置不得写入代码、文档、镜像、提交记录或日志。外部配置文件必须限制为仅服务运行账号和管理员可读，并纳入受控备份；环境变量或密钥管理设施仍可作为更高优先级的可选覆盖方式。

跨实例内部地址的限制与目标节点鉴权遵守[部署架构](../architecture/deployment.md#节点本地能力)。

服务端敏感配置的密钥校验、密文格式与轮换边界遵守[服务端配置加密](../architecture/security.md#服务端配置加密)。不得直接替换密钥导致历史密文无法解密。

配置项以仓库内 `application.yml`、`application-*.yml` 和实际外部覆盖为准；配置入口、环境职责或必要部署条件变化时同步本文，不为每个默认值变化复制一份配置清单。

管理端点暴露与内建健康采样遵守[浏览器与生产边界](../architecture/security.md#浏览器与生产边界)，不能把进程内健康能力当成外部 HTTP 探针。
