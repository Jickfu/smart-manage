# OpenAPI 开放平台

## 模块定位

OpenAPI 平台为外部系统提供独立于浏览器会话的服务到服务调用能力。一期包含第三方应用、凭据包、IP 访问策略、显式 API 版本目录、应用授权、报文签名与加密、调用日志、近 24 小时统计和 API 文档。

它不是动态接口生成平台。业务模块必须通过 `OpenApiOperationContributor` 在代码中显式注册操作，并通过 Flyway 发布对应版本元数据；禁止扫描 Controller 后自动写数据库。代码注册决定实际可执行路由，发布记录决定版本是否开放，两者不一致时默认拒绝调用。

## 第三方应用

- `number` 是调用方稳定编码，全局唯一；新增默认停用。
- 代理用户是可以正常登录的启用普通用户，但超级管理员不能作为代理用户。
- 固定组织必须启用、未封存，且代理用户必须任职于该组织。
- 外部请求只在当前线程建立代理身份，不创建登录会话，不继承代理用户的浏览器 Token，也不允许切换组织。
- 一期认证类型固定为 `HMAC_SHA256`；认证校验器保持独立组件，后续认证方式必须按真实需求扩展，不在一期建立空实现。
- 报文加密可以选择 `NONE`、`AES_256_GCM` 或 `SM4_GCM`。`NONE` 仍必须执行 HMAC-SHA256 签名、防重放、授权和代理身份校验，只是不使用加密信封；AES/SM4 模式的请求和响应都必须加密。
- IP 策略支持不限制、白名单和黑名单。地址来自受信代理边界解析结果，配置每行一个 IPv4/IPv6 地址或 CIDR。

## 凭据包

每个凭据包包含稳定 `keyId` 和三份相互独立的随机密钥：

1. HMAC-SHA256 签名密钥，32 字节；
2. 请求报文加密密钥，AES 为 32 字节、SM4 为 16 字节；`NONE` 不生成；
3. 响应报文加密密钥，长度同请求密钥；`NONE` 不生成。

签名密钥以及加密模式下的两份报文密钥只在创建成功后显示一次，以 Base64 交付；数据库只保存经部署级 `SM4Helper` 加密后的密文。列表和详情不得返回密钥、摘要或可逆材料。轮换通过创建新凭据、切换调用方、再停用旧凭据完成；历史调用日志只记录 `keyId`。

## 请求协议

一期时间窗为服务器时间前后 300 秒，nonce 通过 Redis 原子消费并保留 10 分钟。请求头如下：

| 请求头 | 含义 |
| --- | --- |
| `X-Sm-Key-Id` | 凭据包 Key ID |
| `X-Sm-Timestamp` | Unix 秒时间戳 |
| `X-Sm-Nonce` | 每次请求唯一的 8～100 位字母数字及 `._-` 字符串 |
| `X-Sm-Request-Id` | 调用方请求标识，规则同 nonce |
| `Content-Digest` | `sha-256=:Base64(SHA-256(原始请求体)):` |
| `Signature-Input` | 固定 RFC 9421 风格签名参数 |
| `Signature` | `sm1=:Base64(HMAC-SHA256(签名基串)):` |

固定 `Signature-Input` 为：

```text
sm1=("@method" "@path" "content-digest" "x-sm-key-id" "x-sm-timestamp" "x-sm-nonce");created={timestamp};keyid="{keyId}";alg="hmac-sha256"
```

签名基串按以下顺序拼接，HTTP 方法使用小写，路径不含域名和查询串：

```text
"@method": post
"@path": /openapi/sys/base/basic-data/v1/items/query
"content-digest": {Content-Digest}
"x-sm-key-id": {keyId}
"x-sm-timestamp": {timestamp}
"x-sm-nonce": {nonce}
"@signature-params": {Signature-Input 去掉 sm1= 前缀}
```

签名覆盖传输中的原始加密信封字节，而不是解密后的业务 JSON。调用方必须在序列化信封后计算摘要和签名，发送前不得再次格式化 JSON。

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
- AES-256-GCM 与 SM4-GCM 均能往返解密；密文、标签或 AAD 任一变化必须失败。
- 请求摘要或签名变化必须失败；同一 `keyId + nonce` 只能成功消费一次。
- 停用应用、停用/过期凭据、下线版本、未授权操作、不可用代理身份或不匹配 IP 均默认拒绝。
- 管理接口不返回任何历史密钥；一次性凭据关闭后不能再次读取。
- 首个基础资料 API 的有效叶子语义与内部业务选项接口一致。
