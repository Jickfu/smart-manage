# OpenAPI 开放平台

## 模块定位

OpenAPI 平台为外部系统提供独立于浏览器会话的服务到服务调用能力。一期包含第三方应用、凭据包、IP 访问策略、显式 API 版本目录、应用授权、报文签名与加密、调用日志、近 24 小时统计和 API 文档。

它不是动态接口生成平台。业务模块必须通过 `OpenApiOperationContributor` 在代码中显式注册操作，并通过 Flyway 发布对应版本元数据；禁止扫描 Controller 后自动写数据库。代码注册决定实际可执行路由，发布记录决定版本是否开放，两者不一致时默认拒绝调用。

API 文档列表点击 API 名称后，以独立只读页签中的单一文档画布连续展示接口基本信息、请求参数表、返回参数表、请求结构示例和返回结构示例，并在右侧提供章节锚点；基本信息使用带边框的键值详情表，常规字段双列并排，长内容独占一行，窄屏降为单列，以 `description` 作为唯一用途说明，不额外展示版本 `documentation` 自由文本。参数表由版本 JSON Schema 展开生成，嵌套属性显式标注层级，示例必须读取版本中显式发布的 JSON 数据，不能用 JSON Schema 冒充响应示例。不使用多个折叠编辑卡片。发布、下线仍是列表页命令，不在文档详情页内修改版本元数据。

列表可以多选 API 版本并导出一份 UTF-8 CommonMark 文档。后端必须按所选 ID 重新读取权威版本元数据，限制单次最多 100 个版本、文档最大 5 MB；前端不得使用当前页列表快照自行拼接文档。Markdown 通过模块内生成器构造 CommonMark AST，并使用同一组版本元数据按详情页顺序输出基本信息、请求参数表、返回参数表和请求/返回结构示例，不额外输出 `documentation`；JSON Schema 无法解析时必须阻断导出，禁止生成伪参数行。第三方库依赖不得扩散到 Controller 或业务查询逻辑。

## 管理端业务试调

- 业务试调是独立于 HMAC、nonce 和报文加密的管理端能力，第三方应用通过引用选择器从当前接口已授权应用中选择，请求 JSON 使用可编辑代码视图，响应数据使用只读代码视图；它只验证请求数据、发布状态、应用授权、代理身份及实际业务响应，不代表外部传输协议端到端验证成功。
- 入口必须同时具备 `sys:base:openapi-catalog:test` 权限，并由公开 Service 的类级 `@AdministratorOnly` 复核真实 `administrator` 身份；仅靠前端隐藏按钮或通配权限不能替代身份复核。
- API 提供模块必须在 `OpenApiOperation` 中显式注册试调处理器；未注册处理器的 API 默认禁止试调。请求示例由 API 版本迁移显式发布，不从 JSON Schema 猜测。
- 执行时必须选择已启用且已获授该 API 的第三方应用，并复用正式 OpenAPI 的已发布版本、代理用户、固定组织和应用授权校验。试调以该代理身份建立请求级上下文，不使用当前管理员的数据权限执行实际业务。
- 请求 JSON 最大 64 KiB且必须是对象。操作日志只记录操作人、接口、结果与耗时，不保存请求或响应正文；试调响应只返回当前页面，不持久化为调用正文。

## 第三方应用

- `number` 是调用方稳定编码，全局唯一；新增默认停用。
- 代理用户是可以正常登录的启用普通用户，但超级管理员不能作为代理用户。
- 固定组织必须启用、未封存，且代理用户必须任职于该组织。
- 外部请求只在当前线程建立代理身份，不创建登录会话，不继承代理用户的浏览器 Token，也不允许切换组织。
- 一期认证类型固定为 `HMAC_SHA256`；认证校验器保持独立组件，后续认证方式必须按真实需求扩展，不在一期建立空实现。
- 报文加密可以选择 `NONE`、`AES_256_GCM` 或 `SM4_GCM`。`NONE` 仍必须执行 HMAC-SHA256 签名、防重放、授权和代理身份校验，只是不使用加密信封；AES/SM4 模式的请求和响应都必须加密。
- IP 策略支持不限制、白名单和黑名单。地址来自受信代理边界解析结果，配置每行一个 IPv4/IPv6 地址或 CIDR；不接受主机名、zone ID、端口、通配符或地址范围。保存 CIDR 时规范化为网络地址。

## 凭据包

每个凭据包包含稳定 `keyId` 和三份相互独立的随机密钥：

1. HMAC-SHA256 签名密钥，32 字节；
2. 请求报文加密密钥，AES 为 32 字节、SM4 为 16 字节；`NONE` 不生成；
3. 响应报文加密密钥，长度同请求密钥；`NONE` 不生成。

签名密钥以及加密模式下的两份报文密钥只在创建成功后显示一次，以 Base64 交付；数据库只保存经部署级 `Sm4Cipher` 加密后的密文。列表和详情不得返回密钥、摘要或可逆材料。轮换通过创建新凭据、切换调用方、再停用旧凭据完成；历史调用日志只记录 `keyId`。

## 请求协议

一期时间窗为服务器时间前后 300 秒，nonce 通过 Redis 原子消费并保留 10 分钟。请求头如下：

| 请求头 | 含义 |
| --- | --- |
| `X-Sm-Key-Id` | 凭据包 Key ID |
| `X-Sm-Timestamp` | Unix 秒时间戳 |
| `X-Sm-Nonce` | 每次请求唯一的 8～100 位字母数字及 `._-` 字符串 |
| `X-Sm-Request-Id` | 调用方请求标识，规则同 nonce |
| `Content-Type` | 固定为 `application/json`，不接受参数或其他等价写法 |
| `Content-Digest` | `sha-256=:Base64(SHA-256(原始请求体)):` |
| `Signature-Input` | 固定 RFC 9421 签名参数；不接受旧版私有格式 |
| `Signature` | `sm1=:Base64(HMAC-SHA256(签名基串)):` |

固定 `Signature-Input` 为：

```text
sm1=("@method" "@path" "@query" "content-type" "content-digest" "x-sm-key-id" "x-sm-timestamp" "x-sm-nonce");created={timestamp};keyid="{keyId}";nonce="{nonce}";alg="hmac-sha256"
```

签名基串由 RFC 9421 实现按 Structured Fields 和组件规范化规则构造。HTTP 方法保留请求中的原始大小写，标准 `POST` 请求必须签为大写；路径不含域名和查询串：

```text
"@method": POST
"@path": /openapi/sys/base/basic-data/v1/items/query
"@query": ?
"content-type": application/json
"content-digest": {Content-Digest}
"x-sm-key-id": {keyId}
"x-sm-timestamp": {timestamp}
"x-sm-nonce": {nonce}
"@signature-params": {Signature-Input 去掉 sm1= 前缀}
```

`@query` 覆盖包含前导 `?` 的原始查询串，不进行解码、参数排序或重新编码；请求没有查询串时固定签为 `?`。签名覆盖传输中的原始加密信封字节，而不是解密后的业务 JSON。调用方必须在序列化信封后计算摘要和签名，发送前不得再次格式化 JSON。

服务端只接受一个标签为 `sm1` 的签名，并严格要求上述覆盖组件、顺序和 `created`、`keyid`、`nonce`、`alg` 参数及其顺序。`created`、`keyid`、`nonce` 必须分别与对应请求头一致；不提供旧版小写方法签名或缺少 nonce 元数据参数的兼容路径。

## 加密信封

请求体及成功认证后的响应数据使用同一信封：

```json
{
  "version": "1",
  "algorithm": "AES-256-GCM",
  "keyId": "sm_xxx",
  "iv": "Base64(12字节随机IV)",
  "ciphertext": "Base64(密文，不含认证标签)",
  "tag": "Base64(16字节认证标签)"
}
```

每次加密必须生成新的 12 字节 IV。GCM AAD 是以下字段按固定顺序拼接的 UTF-8 文本：

```text
version={version}
algorithm={algorithm}
keyId={keyId}
direction={request|response}
method={大写HTTP方法}
path={请求路径}
query={包含前导 ? 的原始查询串；无查询串时为 ?}
created={timestamp}
nonce={nonce}
requestId={requestId}
```

Controller 的正常 `Result<T>` 完整序列化后作为响应加密明文，外层再返回 `Result<OpenApiEncryptedPayload>`。因此业务成功和业务失败都位于加密响应中；在可信凭据尚未建立前的路由、认证、大小限制或协议错误使用不包含敏感细节的普通 `Result`。

## 处理顺序

```text
路由与大小限制
  -> Key ID / 应用 / 凭据 / IP / 时间窗
  -> 原始密文摘要与 HMAC
  -> Redis nonce 原子消费
  -> 发布状态与应用授权
  -> 请求 GCM 解密
  -> 代理用户与固定组织上下文
  -> 业务 Controller
  -> 完整 Result<T> GCM 加密
  -> 调用审计
```

调用日志不保存请求正文、响应正文或任何密钥，按 `request_time` 月度分区，在线保留 180 天、历史保留 730 天，并接入系统日志生命周期白名单。

## 一期 API

`POST /openapi/sys/base/basic-data/v1/items/query`

- `operationKey`: `sys.basicData.items.queryByCategory`
- API 编码：`sys.basic-data.items`
- 版本：`v1`
- 解密后的请求：`{"categoryNumber":"分类编码"}`
- 解密后的响应：分类编码、分类名称，以及 `number`、`name`、`parentNumber`、`numberPath`、`namePath`。
- 只返回分类及全部祖先均启用的叶子资料，不暴露内部数据库主键。

## 验收基线

- 浏览器非安全方法继续要求合法 Origin；`/openapi/**` 不要求 Origin，但没有完整签名、加密和授权时必须拒绝。
- AES-256-GCM 与 SM4-GCM 均能往返解密；密文、标签、路径或查询串 AAD 任一变化必须失败。
- 请求摘要、查询串、`Content-Type` 或签名变化必须失败；同一 `keyId + nonce` 只能成功消费一次。
- 非标准 Structured Fields、额外或缺失签名、覆盖组件/参数变化、旧版小写方法签名或缺少 nonce 元数据参数必须失败。
- 停用应用、停用/过期凭据、下线版本、未授权操作、不可用代理身份或不匹配 IP 均默认拒绝。
- 管理接口不返回任何历史密钥；一次性凭据关闭后不能再次读取。
- 首个基础资料 API 的有效叶子语义与内部业务选项接口一致。
