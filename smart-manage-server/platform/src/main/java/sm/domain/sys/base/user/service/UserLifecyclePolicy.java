package sm.domain.sys.base.user.service;

import sm.domain.sys.base.common.constant.UserConstant;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.form.UserSaveForm;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

/** 管理员用户名即身份边界，通用生命周期命令不得创建或维护该保留账号。 */
final class UserLifecyclePolicy {
    private UserLifecyclePolicy() {
    }

    static void checkSave(UserSaveForm form, UserEntity existing) {
        checkUsername(form.getUsername());
        // 根据数据库目标检查，不能信任请求中可伪造的用户名。
        checkTarget(existing);
    }

    static void checkUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "用户名不能为空");
        }
        if (UserConstant.SUPER_ADMIN.equalsIgnoreCase(username.trim())) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "管理员账号不能通过普通用户保存入口维护");
        }
        if (username.contains("@") || username.length() > 100 || !username.equals(username.trim())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "用户名最长100字符，且不能含有@或首尾空白");
        }
    }

    static void checkTarget(UserEntity target) {
        if (target != null && UserConstant.SUPER_ADMIN.equals(target.getUsername())) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "普通用户管理命令不能操作管理员账号");
        }
    }
}
