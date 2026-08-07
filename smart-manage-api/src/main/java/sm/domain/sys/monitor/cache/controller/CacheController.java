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
import sm.domain.sys.monitor.cache.model.form.CacheEntryDeleteForm;
import sm.domain.sys.monitor.cache.model.form.CacheEntryKeyForm;
import sm.domain.sys.monitor.cache.model.form.CacheEntryListForm;
import sm.domain.sys.monitor.cache.model.vo.CacheEntryVO;
import sm.domain.sys.monitor.cache.model.vo.CacheOverviewVO;
import sm.domain.sys.monitor.cache.model.vo.CacheRuntimeVO;
import sm.domain.sys.monitor.cache.model.vo.CacheScopeVO;
import sm.domain.sys.monitor.cache.model.vo.CacheValueVO;
import sm.domain.sys.monitor.cache.service.CacheService;
import sm.system.response.Result;
import sm.system.response.PageData;

import java.util.List;

@RestController
@Tag(name = "系统监控-缓存监控")
@RequiredArgsConstructor
public class CacheController {
    private final CacheService service;

    @Operation(summary = "应用缓存概览")
    @SaCheckPermission(CachePermission.LIST)
    @PostMapping("/sys/monitor/cache/overview")
    public Result<CacheOverviewVO> overview() {
        return Result.success(service.overview());
    }

    @Operation(summary = "Redis 实时运行状态")
    @SaCheckPermission(CachePermission.LIST)
    @PostMapping("/sys/monitor/cache/runtime")
    public Result<CacheRuntimeVO> runtime() {
        return Result.success(service.runtime());
    }

    @Operation(summary = "缓存所属云与应用树")
    @SaCheckPermission(CachePermission.LIST)
    @PostMapping("/sys/monitor/cache/scopeTree")
    public Result<List<CacheScopeVO>> scopeTree() {
        return Result.success(service.scopeTree());
    }

    @Operation(summary = "统一分页查询本地与 Redis 缓存条目")
    @SaCheckPermission(CachePermission.LIST)
    @PostMapping("/sys/monitor/cache/listPage")
    public Result<PageData<CacheEntryVO>> listPage(@Valid @RequestBody CacheEntryListForm form) {
        return Result.success(service.listPage(form));
    }

    @Operation(summary = "安全查看缓存值")
    @SaCheckPermission(CachePermission.VALUE)
    @PostMapping("/sys/monitor/cache/value")
    public Result<CacheValueVO> value(@Valid @RequestBody CacheEntryKeyForm form) {
        return Result.success(service.value(form));
    }

    @Operation(summary = "批量删除缓存条目")
    @SaCheckPermission(CachePermission.DELETE)
    @PostMapping("/sys/monitor/cache/delete")
    public Result<Long> delete(@Valid @RequestBody CacheEntryDeleteForm form) {
        return Result.success(service.delete(form.getEntries()));
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
