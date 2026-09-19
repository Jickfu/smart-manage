package sm.domain.sys.base.user.constant;

import java.util.List;

/** 用户 Excel 稳定列契约，导入与可回导导出必须共用。 */
public final class UserExcelSchema {
    public static final List<String> IMPORT_HEADERS = List.of("登录账号*", "姓名*", "工号", "邮箱", "手机号",
            "性别(MALE/FEMALE)", "生日(yyyy-MM-dd)", "任职(;分隔，格式=组织编码:职位，首个为主任职)");
    public static final List<String> EXPORT_HEADERS = List.of("登录账号", "姓名", "工号", "邮箱", "手机号", "性别",
            "生日", "账号状态", "任职组织", "职位", "主职组织", "创建时间");

    private UserExcelSchema() { }
}
