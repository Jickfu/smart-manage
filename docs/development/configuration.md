# 环境与配置

## 环境划分

| Profile | 用途 | 主要配置来源 |
| --- | --- | --- |
| `local` | IDEA 和个人电脑本地运行 | 后端资源目录私有 `application-local.yml` |
| `dev` | 共享开发/测试服务器 | 内置 `application-dev.yml` 加服务器外部覆盖 |
| `prod` | 生产部署 | Jar 同级 `config/application.yml` 和 `config/application-prod.yml` |

## 本机环境

本机完整配置连接个人使用的 PostgreSQL 和 Redis：

```text
JDBC：jdbc:postgresql://localhost:5432/smart_manage
数据库用户：postgres
数据库密码：postgres
Redis：localhost:6379，密码 redis123，数据库 1
```

示例中的数据库凭据和 SM2/SM4 密钥均为公开配置，只允许用于本地开发和演示。

### 本机开发配置文件

不希望设置操作系统环境变量时，复制
`smart-manage-api/src/main/resources/application-dev.yml` 为同级的 `application-local.yml`，
再填写本机配置。项目默认激活 `local`，`local` 不继承 `dev`，因此本地配置文件必须包含运行所需的完整配置。
该文件位于 classpath，不受 IDEA 工作目录影响。
实际 `application-local.yml` 已被 Git 忽略，
禁止移除忽略规则或提交其中的真实凭据。

Spring Data Redis 与 JetCache 使用两套配置入口，本机文件中的两处 Redis 地址、端口、密码和数据库必须保持一致。

## 共享测试环境与可选环境变量

`application-dev.yml` 用于共享测试环境。`${NAME:default}` 表示允许服务器外部配置或环境变量覆盖，
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
| `SMART_MANAGE_UPLOAD_DIR` | 上传文件目录 |
| `SMART_MANAGE_SM4_KEY` | 服务端敏感配置加密密钥，Base64 编码后必须解码为 16 字节 |
| `SMART_MANAGE_SM2_PRIVATE_KEY` | SM2 私钥 |
| `SMART_MANAGE_SM2_PUBLIC_KEY` | SM2 公钥 |
| `SMART_MANAGE_INSTANCE_ID` | 集群内唯一的应用实例 ID，例如 `instance1`、`instance2` |
| `SMART_MANAGE_INTERNAL_BASE_URL` | 当前实例供其他应用实例定向调用的内部基础地址 |

开发环境还支持 `SMART_MANAGE_DRUID_USERNAME` 和 `SMART_MANAGE_DRUID_PASSWORD` 配置 Druid 监控登录。

## 生产环境

推荐部署目录：

```text
smart-manage/
├─ smart-manage-api.jar
├─ config/
│  ├─ application.yml
│  └─ application-prod.yml
├─ logs/
└─ upload/
```

将仓库中的 `config/application.example.yml` 复制为部署目录的 `config/application.yml`，
将 `config/application-prod.example.yml` 复制为 `config/application-prod.yml` 并填写真实配置。
启动进程的工作目录必须是 Jar 所在目录。外部 YAML 优先于 Jar 内配置，因此生产部署不需要设置操作系统环境变量；
Jar 内部 `${...}` 仍作为其他部署方式和配置遗漏检查的支持入口。

生产环境使用 `prod` Profile，必须显式配置：

- 数据库地址、用户名和密码；
- Redis 地址和密码；
- SM2 公私钥；
- SM4 敏感配置加密密钥；
- 上传目录；
- `SMART_MANAGE_CORS_ALLOWED_ORIGIN`；
- `SMART_MANAGE_INITIAL_ADMINISTRATOR_PASSWORD`，且不能为 `admin`。
- `SMART_MANAGE_INSTANCE_ID`，且每个应用实例必须唯一。
- `SMART_MANAGE_INTERNAL_BASE_URL`，且必须是其他应用实例可直接访问的 HTTPS 内部地址。

可选的生产调优变量：

| 环境变量 | 默认值 |
| --- | --- |
| `SMART_MANAGE_SERVER_PORT` | `8080` |
| `SMART_MANAGE_DB_POOL_INITIAL_SIZE` | `5` |
| `SMART_MANAGE_DB_POOL_MIN_IDLE` | `10` |
| `SMART_MANAGE_DB_POOL_MAX_ACTIVE` | `100` |
| `SMART_MANAGE_REDIS_PORT` | `6379` |
| `SMART_MANAGE_REDIS_DATABASE` | `1` |

敏感配置不得写入代码、文档、镜像、提交记录或日志。外部配置文件必须限制为仅服务运行账号和管理员可读，并纳入受控备份；环境变量或密钥管理设施仍可作为更高优先级的可选覆盖方式。

多实例运行监控和线程诊断通过 Redis 注册表发现实例。`SMART_MANAGE_INTERNAL_BASE_URL` 不得填写公网入口、负载均衡地址或浏览器可控地址；目标节点会重新校验共享登录态和权限。

`SMART_MANAGE_SM4_KEY` 用于加密文件存储密码等服务端敏感配置。生产环境缺失、Base64 格式错误或解码后不是 16 字节时，应用必须拒绝启动。轮换该密钥前必须先完成既有密文的重新加密，不能直接替换环境变量。

当前密文格式固定为带 `sm4-gcm:v1:` 版本前缀的 SM4/GCM 认证密文，不兼容旧的无版本 SM4/CBC 密文。项目尚无真实生产密文时应重新保存相关凭据；如果未来存在生产迁移需求，必须先设计离线迁移和回滚方案，不得在运行时代码中长期保留 CBC 兼容分支。

从旧版本升级且系统参数中已经存在 `SM4_KEY` 时，必须在执行 `V23__remove_sm4_key_system_parameter.sql` 前，将原值通过安全渠道配置为 `SMART_MANAGE_SM4_KEY`。迁移只删除数据库中的密钥，不会也不能自动把密钥写入部署环境。

配置的最终权威来源是 `smart-manage-api/src/main/resources/application-*.yml`；新增或删除配置项时必须同步更新本文档。
