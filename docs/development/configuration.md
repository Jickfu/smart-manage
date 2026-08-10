# 环境与配置

## 开发环境

默认开发配置连接本机 PostgreSQL 和 Redis：

```text
JDBC：jdbc:postgresql://localhost:5432/smart_manage
数据库用户：postgres
数据库密码：postgres
Redis：localhost:6379，密码 redis123，数据库 1
```

这些默认值、Redis 密码及开发环境 SM2 密钥均为公开配置，只允许用于本地开发和演示。

## 常用环境变量

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

敏感配置不得写入代码、文档、镜像、提交记录或日志。实际部署应使用受控的环境变量或密钥管理设施。

多实例运行监控和线程诊断通过 Redis 注册表发现实例。`SMART_MANAGE_INTERNAL_BASE_URL` 不得填写公网入口、负载均衡地址或浏览器可控地址；目标节点会重新校验共享登录态和权限。

`SMART_MANAGE_SM4_KEY` 用于加密文件存储密码等服务端敏感配置。生产环境缺失、Base64 格式错误或解码后不是 16 字节时，应用必须拒绝启动。轮换该密钥前必须先完成既有密文的重新加密，不能直接替换环境变量。

当前密文格式固定为带 `sm4-gcm:v1:` 版本前缀的 SM4/GCM 认证密文，不兼容旧的无版本 SM4/CBC 密文。项目尚无真实生产密文时应重新保存相关凭据；如果未来存在生产迁移需求，必须先设计离线迁移和回滚方案，不得在运行时代码中长期保留 CBC 兼容分支。

从旧版本升级且系统参数中已经存在 `SM4_KEY` 时，必须在执行 `V23__remove_sm4_key_system_parameter.sql` 前，将原值通过安全渠道配置为 `SMART_MANAGE_SM4_KEY`。迁移只删除数据库中的密钥，不会也不能自动把密钥写入部署环境。

配置的最终权威来源是 `smart-manage-api/src/main/resources/application-*.yml`；新增或删除配置项时必须同步更新本文档。
