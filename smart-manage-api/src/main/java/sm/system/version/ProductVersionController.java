package sm.system.version;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import sm.system.response.Result;

/** 关于产品使用的只读入口，沿用全局登录校验，不开放匿名访问。 */
@RestController
@RequiredArgsConstructor
public class ProductVersionController {
    private final ProductVersionService service;

    @GetMapping("/sys/base/product/version")
    @Operation(summary = "获取当前后端版本")
    public Result<ProductVersion> current(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        return Result.success(service.current());
    }
}
