package sm.domain.sys.monitor.cache.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.monitor.cache.constant.CachePermission;
import sm.domain.sys.monitor.cache.model.form.CacheClearForm;
import sm.domain.sys.monitor.cache.model.vo.CacheOverviewVO;
import sm.domain.sys.monitor.cache.service.CacheService;
import sm.system.response.Result;

@RestController
@Tag(name = "系统监控-应用缓存")
@RequiredArgsConstructor
public class CacheController {
    private final CacheService service;

    @Operation(summary = "应用缓存概览")
    @SaCheckPermission(CachePermission.LIST)
    @PostMapping("/sys/monitor/cache/overview")
    public Result<CacheOverviewVO> overview() {
        return Result.success(service.overview());
    }

    @Operation(summary = "清除指定应用缓存")
    @SaCheckPermission(CachePermission.CLEAR)
    @PostMapping("/sys/monitor/cache/clear")
    public Result<String> clear(@Valid @RequestBody CacheClearForm form) {
        service.clear(form.getCacheName());
        return Result.success("清除成功");
    }

    @Operation(summary = "清除全部受控应用缓存")
    @SaCheckPermission(CachePermission.CLEAR_ALL)
    @PostMapping("/sys/monitor/cache/clearAll")
    public Result<String> clearAll() {
        service.clearAll();
        return Result.success("全部应用缓存已清除");
    }
}
