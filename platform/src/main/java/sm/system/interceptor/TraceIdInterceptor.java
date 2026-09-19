package sm.system.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import sm.system.util.TraceIdUtil;

/**
 * 公共链路追踪拦截器
 *
 * @author Chekfu
 */
@Component
public class TraceIdInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		try {
			TraceIdUtil.setTraceId(request);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) throws Exception {
		TraceIdUtil.clear();
	}
}
