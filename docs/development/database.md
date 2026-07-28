# 数据库开发

## 权威来源

数据库结构和必要初始化数据只由根目录 `db/migration` 下的 Flyway 迁移定义。后端构建将迁移复制到类路径，应用启动时执行尚未应用的版本。

- 已执行迁移禁止删除或修改。
- 结构和初始化数据调整必须新增版本脚本。
- 查询实际数据库状态用于排障和核实迁移结果，不能替代迁移。
- Flyway 版本允许存在空缺。

## SQL 约定

- Java 查询条件使用 MyBatis-Plus `LambdaQueryWrapper` 和方法引用，禁止裸表名、字段名字符串。
- XML Mapper 的主表别名使用 `a`，JOIN 表按出现顺序使用 `b`、`c`、`d`。
- 主从表通过明细表 `parent_id` 关联，不使用数据库级联删除。
- 客户端和数据库写入使用 UTF-8，不依赖 GBK 写入。

## 表和字段备注

创建表时必须使用 PostgreSQL `COMMENT ON` 添加表备注和字段备注。

| 字段 | 统一备注 |
| --- | --- |
| `id` | ID |
| `number` | 编码 |
| `name` | 名称 |
| `create_time` | 创建时间 |
| `update_time` | 更新时间 |
| `create_user` | 创建人 |
| `update_user` | 修改人 |

其他字段根据业务含义填写简体中文备注。

## 本地排障

PostgreSQL 客户端路径取决于安装时的选择。Windows 常见安装位置包括：

```text
D:\Program Files\PostgreSQL\16\bin
C:\Program Files\PostgreSQL\16\bin
```

执行 SQL 时必须使用当前环境已有的安全凭据方式，例如临时 `PGPASSWORD` 或受控连接配置。禁止把密码写入脚本、代码、文档、提交记录或回复。

只有确认 Windows 终端输出乱码时，才临时调整查询输出的客户端编码；数据库写入始终使用 UTF-8。

## 验证

迁移变更必须执行[Flyway 空库验证](./verification.md#flyway-空库验证)，并确认后端测试或编译通过。
