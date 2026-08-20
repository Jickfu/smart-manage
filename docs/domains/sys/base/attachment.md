# 附件与对象存储

## 模块定位

附件模块负责文件上传生命周期、对象存储元数据、业务归属和统一访问入口，不复制业务模块的数据权限模型。单实例生产部署可以使用具备持久化与备份能力的 Local；多实例必须使用 S3 或 FTP 共享存储，并优先选择 S3/MinIO。

## 文件名和对象键

- 客户端原始文件名只保存为展示元数据，不能参与对象键拼接，也不能作为物理存储文件名。
- 服务端为每个对象生成至少 128 bit 随机标识，推荐使用去连字符的小写 UUID v4；随机值冲突时必须重新生成，数据库对 `object_key` 建唯一约束。
- 对象键保留经过服务端检测、白名单校验和小写规范化的扩展名，便于受控运维场景直接识别文件类型；禁止直接使用客户端文件名或未经校验的原始扩展名。对象键扩展名不作为授权依据，实际响应类型仍以服务端检测并保存的 MIME 为准。
- S3 对象键不使用开头 `/`，使用以下稳定格式：

```text
biz/{domainNumber}/{appNumber}/{resourceNumber}/{yyyy}/{MM}/{shard}/{uuid}.{ext}
asset/{domainNumber}/{appNumber}/{resourceNumber}/{yyyy}/{MM}/{shard}/{uuid}.{ext}
```

例如：

```text
biz/scm/procurement/purchase-requisition/2026/08/a7/a7f3c9d4b35f4d6699bcdf51ca7b28cd.pdf
asset/sys/base/ui-config/2026/08/42/42c1fbe574f947bea0b3425776279d1a.png
```

`shard` 取 UUID 前两位，主要用于 Local/FTP 兼容和人工排障；现代 S3/MinIO 不依赖目录分片解决性能问题。年/月前缀用于生命周期策略和运维筛选，业务统计以数据库查询为准，不扫描 Bucket 计算。

对象键创建后保持不变。应用改名、业务状态变化、TEMP 转 ACTIVE 或业务单据归档都不能触发对象重命名或 S3 copy/delete。

## 业务资源注册

`bizType` 是服务端注册的稳定业务资源类型，不是前端自定义目录。编码使用 `{domain}.{application}.{resource}`：

```text
scm.procurement.purchase-requisition
sys.base.ui-config
sys.report.report-template
```

系统只建设轻量的 `BusinessResourceRegistry`，不建设动态字段、动态表单或动态数据表等动态模型平台。业务资源注册表为附件、评论和审计等通用能力提供稳定资源身份和统一授权入口；具体业务模型、状态和权限仍由所属业务模块显式编码。

每个注册项至少定义稳定 `resourceType` 和对应的 `BusinessResourceAccessPolicy`，并可以覆盖对象键前缀及上传入口权限。当前协议不维护 domain、app 或通用能力枚举，避免为尚无第二使用方的元数据提前扩张注册模型。

业务资源注册只定义稳定身份、上传入口权限和对象级授权，**不定义大小上限、扩展名、MIME 或临时有效期**。这些限制由系统管理中的“附件配置”单例统一维护，所有单据和系统资源立即共享同一套限制，禁止单据页面或注册类私自复制全局阈值。

前端业务单据通过通用编辑页的独立“附件”折叠面板接入附件能力，上传、展示、删除和提交字段组装由公共组件负责；具体单据只声明资源类型并提供详情中的附件数据，不重复实现附件控件和请求。已绑定附件删除是独立且立即生效的操作，必须明确提示不能通过取消表单恢复，并且不得把该删除误标为主表单未保存。附件配置中的扩展名与 MIME 白名单使用可自由增删行的表格维护，不提供写死的类型候选项。

### 注册位置和方式

系统公共层定义业务资源协议和注册表，并通过 Spring 收集全部注册 Bean；具体业务模块拥有自己的注册实现。系统内核只依赖注册接口，不反向依赖 SCM 等可选业务模块。当前保留显式注册，不通过扫描 Entity、Controller、表名或注解猜测业务权限。

推荐结构：

```java
public interface BusinessResourceRegistration {
    String resourceType();
    BusinessResourceAccessPolicy accessPolicy();

    default String objectPrefix() { ... }
    default void requireUploadAllowed() { ... }
}
```

例如采购申请模块在自身包内提供：

```java
@Component
@RequiredArgsConstructor
final class PurchaseRequisitionResourceRegistration
        implements BusinessResourceRegistration {

    private final PurchaseRequisitionResourceAccessPolicy accessPolicy;

    @Override
    public String resourceType() {
        return "scm.procurement.purchase-requisition";
    }

    @Override
    public BusinessResourceAccessPolicy accessPolicy() {
        return accessPolicy;
    }
}
```

只有聚合实际使用附件时才需要注册；不使用附件的新单据不增加空注册。新增附件业务聚合的检查清单必须包含业务资源注册、授权策略、上传与越权测试；大小、扩展名、MIME 和临时有效期仅由全局附件配置维护。

应用启动时注册中心执行 fail-fast 校验：resourceType 或对象前缀重复、编码格式非法、缺少访问策略时拒绝启动。运行时遇到未注册 resourceType 默认拒绝，不能回退为登录即可访问。

全局附件配置是单例，保存单文件最大大小、允许扩展名、允许 MIME 和临时附件有效期。业务配置的单文件大小不得超过当前 HTTP 基础设施 100MB 硬上限；提高上限必须同步调整服务端、反向代理和容量评估。修改配置必须经过管理员身份复核和乐观锁校验；上传时服务端读取该配置，同时校验扩展名、声明 MIME 与通用内容探测结果。内容类型识别不得通过业务代码硬编码有限的扩展名分支。`bizType` 身份和对象级授权器仍必须由代码注册，不能通过数据库配置任意创建一个没有业务鉴权实现的类型。

上传接口只接收已注册 `bizType`，但 `bizType` 不能单独构成可信归属。正式单据附件必须由所属业务 Service 创建上传会话，或在服务端已经确认的 `bizType + bizId` 上下文中上传；绑定时再次校验资源存在、操作权限和上传会话。对象键前缀、存储策略和授权器全部由服务端注册项派生，禁止前端提交 domain、app、目录、访问范围或对象键。

`bizType` 一旦投入使用不得随 Java 包名、页面路径或显示名称任意改名；确需替换时必须设计历史数据映射和受控切换方案。

## 生命周期

附件状态使用：

```text
TEMP → ACTIVE → PENDING_DELETE → DELETED
```

- `TEMP`：上传完成但尚未绑定业务对象，访问者必须同时是上传者且持有匹配的上传会话；包含 `upload_session_id` 和 `expires_at`。
- `ACTIVE`：已经通过业务附件关联绑定到业务对象，读取和管理权限继承所属业务对象。
- `PENDING_DELETE`：数据库已解除业务关系，等待事务提交后的对象删除或后台重试。
- `DELETED`：对象已删除，元数据按审计和保留策略清理。

TEMP 转 ACTIVE 只更新数据库归属和状态，不移动对象。过期 TEMP 和 PENDING_DELETE 由 Quartz 集群任务清理；清理操作必须幂等并使用共享存储对象键。

## 核心元数据

附件自身元数据至少覆盖：

```text
对象定位与存储类型
原始文件名、大小、扩展名与 MIME
内容摘要
生命周期状态
上传人、上传会话与临时有效期
创建和更新时间
```

业务类型、业务对象和附件之间的绑定由独立关联维护，不混入附件自身元数据。对象定位只以 `object_key` 为准，生命周期只以 `status` 为准，不维护含义重复的路径、存储文件名或临时标记字段。

## 授权模型

正式附件不采用“仅创建人可读”，也不为每个附件维护用户授权表。附件通过业务资源注册表继承业务对象权限：

```java
interface BusinessResourceAccessPolicy {
    String bizType();
    void requireAllowed(String bizId, BusinessResourceAction action);
}
```

- `READ` 控制预览和下载；业务模块可以综合申请人、审批人、部门负责人、数据范围和审计权限判断。
- `ATTACH`、`DETACH` 和 `DELETE` 分别控制绑定、解除和删除，禁止用含义宽泛的 `canManage` 合并不同状态规则。
- 未注册授权器、业务对象不存在或授权器异常时默认拒绝。
- 功能授权只控制附件能力入口，不能替代对象级归属校验。
- TEMP 只允许同时满足创建人和匹配上传会话的请求访问；前端不能自行把 TEMP 标记成公开。

系统资源文件使用 `SYSTEM_ASSET` 注册项，不使用宽泛的“系统附件允许所有管理员访问”规则。已发布 Logo 等公开资源、未发布配置图片和报表模板分别由所属模块决定访问策略；密钥、证书私钥和存储凭据不得进入附件模块。

## 上传和下载安全

- 上传同时校验声明扩展名、服务端内容探测 MIME 和大小；客户端 Content-Type 必须与探测结果一致，不能作为唯一判断依据。
- 原始文件名必须去除路径、控制字符和响应头危险字符，且只用于展示和 Content-Disposition。
- 对象存储 Bucket 默认私有。Local/FTP 下载由后端鉴权后代理文件流；S3 在对象级授权通过后签发一分钟短时预签名 URL。附件列表不得返回 Local 静态路径或未经本次授权签发的存储地址。
- 响应设置安全的 Content-Disposition、Content-Type、`X-Content-Type-Options: nosniff` 和必要的缓存策略。
- 对象存储凭据在数据库中使用带版本标识的 SM4/GCM 认证密文保存，主密钥只通过部署密钥注入；凭据不进入接口响应、缓存、预览 URL 或日志。生产也可以由后续凭据提供器改为工作负载身份，但不得降级为明文配置。

## 事务和补偿

数据库与对象存储不能原子提交：

- 对象上传成功但元数据写入失败时，按精确 `object_key` 幂等删除；
- 绑定业务对象只改变数据库状态，不执行对象移动；
- 删除先在数据库标记 PENDING_DELETE，事务提交后删除对象；失败由 Quartz 清理任务重试；
- 补偿、重试和清理日志记录附件 ID、对象键摘要、实例 ID 和 Trace ID，不记录存储凭据或预签名 URL。
