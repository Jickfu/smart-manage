package sm.domain.sys.base.openapi.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 开放接口发布版本持久化实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_openapi_release")
public class OpenApiReleaseEntity extends BaseEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * API 编码
     */
    private String apiNumber;
    /**
     * API 版本
     */
    private String apiVersion;
    /**
     * API 操作的稳定标识
     */
    private String operationKey;
    /**
     * 名称
     */
    private String name;
    /**
     * HTTP 请求方法
     */
    private String httpMethod;
    /**
     * 路径
     */
    private String path;
    /**
     * API 归属领域稳定编码
     */
    private String domainKey;
    /**
     * 所属领域名称
     */
    private String domainName;
    /**
     * API 归属应用稳定编码
     */
    private String applicationKey;
    /**
     * 应用名称
     */
    private String applicationName;
    /**
     * 稳定功能键
     */
    private String featureKey;
    /**
     * 所属功能名称
     */
    private String featureName;
    /**
     * API 发布状态：PUBLISHED 已发布、OFFLINE 已下线
     */
    private String status;
    /**
     * 说明
     */
    private String description;
    /**
     * 请求参数 JSON Schema
     */
    private String requestSchema;
    /**
     * 响应参数 JSON Schema
     */
    private String responseSchema;
    /**
     * API 版本的补充文档文本
     */
    private String documentation;
    /**
     * API 文档和管理端业务试调使用的显式请求示例
     */
    private String requestExample;
    /**
     * API 文档使用的显式响应 JSON 示例
     */
    private String responseExample;
    /**
     * 是否系统预置
     */
    private Boolean systemPreset;
    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}
