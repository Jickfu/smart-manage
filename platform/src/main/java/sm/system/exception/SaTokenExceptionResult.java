package sm.system.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.SaTokenException;
import lombok.extern.slf4j.Slf4j;
import sm.system.response.Result;
import sm.system.response.ResultEnum;
import sm.system.util.ServletUtil;

/**
 * @author Chekfu
 */
@Slf4j
public class SaTokenExceptionResult {

	public static Result<String> getExceptionResult(SaTokenException e) {
		Result<String> result;
		if (e instanceof NotLoginException ex) {
			result = Result.error(ResultEnum.UNAUTHORIZED);
		} else if (e instanceof NotPermissionException ex) {
			result = Result.error(ResultEnum.PERMISSION_ERROR.getCode(), ResultEnum.PERMISSION_ERROR.getMsg() + "：" + ex.getPermission());
		} else {
			log.error("认证未知异常，请求地址:{}, 具体信息:{}", ServletUtil.getRequest().getRequestURL(), e.getMessage(), e);
			result = Result.error(ResultEnum.SERVER_ERROR);
		}
		return result;
	}
}
