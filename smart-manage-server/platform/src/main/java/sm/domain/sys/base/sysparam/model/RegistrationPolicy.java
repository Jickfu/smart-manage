package sm.domain.sys.base.sysparam.model;

import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

/** 单参数同时保存开关和归属，避免分开保存期间产生不完整配置。 */
public record RegistrationPolicy(boolean enabled, Long orgId, String position) {
    public static final String PARAMETER = "SELF_REGISTRATION_POLICY";

    public static RegistrationPolicy parse(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.equals("DISABLED")) return new RegistrationPolicy(false, null, null);
        if (normalized.equals("UNASSIGNED")) return new RegistrationPolicy(true, null, null);
        String[] parts = normalized.split(":", 3);
        if (parts.length == 3 && parts[0].equals("ORGANIZATION")
                && !parts[2].isBlank() && parts[2].trim().length() <= 100) {
            try {
                long orgId = Long.parseLong(parts[1]);
                if (orgId > 0) return new RegistrationPolicy(true, orgId, parts[2].trim());
            } catch (NumberFormatException ignored) {
                // 统一返回可修正的配置提示。
            }
        }
        throw new BizException(ResultEnum.PARAM_ERROR,
                "注册策略须为 DISABLED、UNASSIGNED 或 ORGANIZATION:组织ID:职位");
    }
}
