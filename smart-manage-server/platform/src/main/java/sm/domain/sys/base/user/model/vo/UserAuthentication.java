package sm.domain.sys.base.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户凭据校验结果，仅用于登录服务内部编排。
 */
@Schema(description = "用户认证信息")
public record UserAuthentication(
    @Schema(description = "用户ID")
    Long userId,
    @Schema(description = "用户名")
    String username,
    @Schema(description = "名称")
    String name,
    @Schema(description = "是否必须修改密码")
    boolean passwordReset,
    @Schema(description = "当前会话是否为特殊管理员身份")
    boolean administrator,
    @Schema(description = "组织 ID")
    Long orgId,
    @Schema(description = "凭据安全代际，安全字段变化后旧验证码和会话失效")
    Long credentialGeneration,
    @Schema(description = "提示/错误消息")
    String message) {

    public static UserAuthentication failed(String message) {
        return new UserAuthentication(null, null, null, false, false, null, null, message);
    }

    public boolean successful() {
        return userId != null;
    }
}
