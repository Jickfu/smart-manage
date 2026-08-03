package sm.domain.sys.monitor.redis.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.monitor.redis.constant.RedisPermission;
import sm.domain.sys.monitor.redis.model.form.RedisDeleteForm;
import sm.domain.sys.monitor.redis.model.form.RedisKeyForm;
import sm.domain.sys.monitor.redis.model.form.RedisKeysForm;
import sm.domain.sys.monitor.redis.model.vo.RedisKeysVO;
import sm.domain.sys.monitor.redis.model.vo.RedisRuntimeVO;
import sm.domain.sys.monitor.redis.model.vo.RedisValueVO;
import sm.domain.sys.monitor.redis.service.RedisService;
import sm.system.response.Result;

@RestController
@RequiredArgsConstructor
@Tag(name = "系统监控-Redis 管理")
public class RedisController {
    private final RedisService service;

    @PostMapping("/sys/monitor/redis/runtime")
    @SaCheckPermission(RedisPermission.LIST)
    @Operation(summary = "Redis 实时运行状态")
    public Result<RedisRuntimeVO> runtime() {
        return Result.success(service.runtime());
    }

    @PostMapping("/sys/monitor/redis/keys")
    @SaCheckPermission(RedisPermission.LIST)
    @Operation(summary = "游标扫描 Redis Key")
    public Result<RedisKeysVO> keys(@Valid @RequestBody RedisKeysForm form) {
        return Result.success(service.keys(form));
    }

    @PostMapping("/sys/monitor/redis/value")
    @SaCheckPermission(RedisPermission.VALUE)
    @Operation(summary = "安全预览 Redis Value")
    public Result<RedisValueVO> value(@Valid @RequestBody RedisKeyForm form) {
        return Result.success(service.value(form.getKey()));
    }

    @PostMapping("/sys/monitor/redis/delete")
    @SaCheckPermission(RedisPermission.DELETE)
    @Operation(summary = "批量删除 Redis Key")
    public Result<Long> delete(@Valid @RequestBody RedisDeleteForm form) {
        return Result.success(service.delete(form.getKeys()));
    }
}
