package sm.system.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet工具类
 *
 * @author Chekfu
 */
public class ServletUtil {
	/**
	 * 获取当前请求的HttpServletRequest对象
	 */
	public static HttpServletRequest getRequest() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attributes == null) {
			throw new IllegalStateException("当前不在Web请求上下文中");
		}
		return attributes.getRequest();
	}

	/**
	 * 获取请求头Map
	 */
	public static Map<String, String> getHeaders() {
		HttpServletRequest request = getRequest();
		Map<String, String> headerMap = new HashMap<>();
		Enumeration<String> headerNames = request.getHeaderNames();
		if (headerNames != null) {
			while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement();
				headerMap.put(headerName, request.getHeader(headerName));
			}
		}
		return headerMap;
	}

	/**
	 * 获取请求参数Map
	 */
	public static Map<String, String> getParameters() {
		HttpServletRequest request = getRequest();
		Map<String, String> paramMap = new HashMap<>();
		Enumeration<String> paramNames = request.getParameterNames();
		if (paramNames != null) {
			while (paramNames.hasMoreElements()) {
				String paramName = paramNames.nextElement();
				paramMap.put(paramName, request.getParameter(paramName));
			}
		}
		return paramMap;
	}

}
