package sm.framework.config;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.model.SaCookie;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import sm.system.exception.ExceptionResultResolver;
import sm.system.response.ResultEnum;
import sm.framework.security.BrowserRequestSecurity;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sa-Token 全局过滤器
 *
 * @author Chekfu
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class SaTokenConfig {
	private final RequestMappingHandlerMapping requestMappingHandlerMapping;
	private final JsonMapper jsonMapper;
	private final BrowserRequestSecurity browserRequestSecurity;
	@Value("${smart-manage.framework.no-need-login}")
	private String[] noNeedLogin;
	@Bean
	public SaServletFilter getSaServletFilter() {
		return new SaServletFilter()
				// 拦截所有，放行ico
				.addInclude("/**").addExclude("/favicon.ico")
				// 认证函数: 每次请求执行
				.setAuth(obj -> {
					SaRouter.match("/**")
							.notMatch(noNeedLogin)
							.notMatch(saIgnoreList())
							.check(r -> {
								StpUtil.checkLogin();
								browserRequestSecurity.validateCsrfToken();
							});
				})
				.setBeforeAuth(obj -> {
					browserRequestSecurity.validateOrigin();
					// ---------- 设置一些安全响应头 ----------
					//					SaHolder.getResponse()
					//							// 是否可以在iframe显示视图： DENY=不可以 | SAMEORIGIN=同域下可以 | ALLOW-FROM uri=指定域名下可以
					//							.setHeader("X-Frame-Options", "SAMEORIGIN")
					//							// 是否启用浏览器默认XSS防护： 0=禁用 | 1=启用 | 1; mode=block 启用, 并在检查到XSS攻击时，停止渲染页面
					//							.setHeader("X-XSS-Protection", "1; mode=block")
					//							// 禁用浏览器内容嗅探
					//							.setHeader("X-Content-Type-Options", "nosniff")
					//							// 允许指定域访问跨域资源
					//							.setHeader("Access-Control-Allow-Origin", allowedOrigins)
					//							// 允许所有请求方式
					//							.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
					//							// 有效时间
					//							.setHeader("Access-Control-Max-Age", "3600")
					//							// 允许的header参数
					//							.setHeader("Access-Control-Allow-Headers", "*");
					//					// 如果是预检请求，则立即返回到前端
					//					SaRouter.match(SaHttpMethod.OPTIONS)
					//							// OPTIONS预检请求，不做处理
					//							.free(r -> {
					//							})
					//							.back();
				})
				.setError(e -> {
					// 设置响应头
					SaHolder.getResponse().setHeader("Content-Type", "application/json;charset=UTF-8");
					// 处理异常
					try {
						var result = ExceptionResultResolver.resolve(e);
						if (ResultEnum.UNAUTHORIZED.getCode() == result.getCode()) {
							clearAuthenticationCookie();
						}
						return jsonMapper.writeValueAsString(result);
					} catch (Exception ex) {
						return "{\"code\":500,\"msg\":\"server error\"}";
					}
				});
	}

	/** 认证失败时覆盖同路径 Cookie，避免浏览器持续提交已失效凭证。 */
	private void clearAuthenticationCookie() {
		var cookieConfig = SaManager.getConfig().getCookie();
		SaHolder.getResponse().addCookie(new SaCookie()
				.setName(SaManager.getConfig().getTokenName())
				.setValue(null)
				.setMaxAge(0)
				.setDomain(cookieConfig.getDomain())
				.setPath(cookieConfig.getPath())
				.setSecure(cookieConfig.getSecure())
				.setHttpOnly(cookieConfig.getHttpOnly())
				.setSameSite(cookieConfig.getSameSite()));
	}

	/**
	 * 获取所有带有@SaIgnore注解的方法的路径
	 */
	@Bean
	public List<String> saIgnoreList() {
		List<String> list = new ArrayList<>();
		Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
		for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : map.entrySet()) {
			Method method = entry.getValue().getMethod();
			SaIgnore saIgnore = method.getAnnotation(SaIgnore.class);
			if (saIgnore == null) {
				continue;
			}
			PathPatternsRequestCondition pathPatternsCondition = entry.getKey().getPathPatternsCondition();
			if (pathPatternsCondition == null) {
				continue;
			}
			Set<String> patternValues = pathPatternsCondition.getPatternValues();
			list.addAll(patternValues);
		}
		return list;
	}
}
