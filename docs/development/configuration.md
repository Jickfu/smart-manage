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
| `SMART_MANAGE_SM2_PRIVATE_KEY` | SM2 私钥 |
| `SMART_MANAGE_SM2_PUBLIC_KEY` | SM2 公钥 |

开发环境还支持 `SMART_MANAGE_DRUID_USERNAME` 和 `SMART_MANAGE_DRUID_PASSWORD` 配置 Druid 监控登录。

## 生产环境

生产环境使用 `prod` Profile，必须显式配置：

- 数据库地址、用户名和密码；
- Redis 地址和密码；
- SM2 公私钥；
- 上传目录；
- `SMART_MANAGE_CORS_ALLOWED_ORIGIN`；
- `SMART_MANAGE_INITIAL_ADMINISTRATOR_PASSWORD`，且不能为 `admin`。

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

配置的最终权威来源是 `smart-manage-api/src/main/resources/application-*.yml`；新增或删除配置项时必须同步更新本文档。
