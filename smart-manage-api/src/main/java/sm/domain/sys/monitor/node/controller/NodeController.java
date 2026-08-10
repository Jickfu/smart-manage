package sm.domain.sys.monitor.node.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import sm.domain.sys.monitor.common.model.vo.MonitorInstanceVO;
import sm.domain.sys.monitor.node.constant.NodePermission;
import sm.domain.sys.monitor.node.model.vo.NodeInfoVO;
import sm.domain.sys.monitor.node.service.NodeService;
import sm.system.response.Result;

import java.util.List;

/**
 * 节点监控接口
 */
@RestController
@Tag(name = "系统监控-节点监控", description = "服务器节点状态监控")
@RequiredArgsConstructor
public class NodeController {

    private final NodeService service;

    @GetMapping("/sys/monitor/instances")
    @Operation(summary = "在线实例", description = "查询 Redis 注册表中的在线应用实例")
    @SaCheckPermission(NodePermission.VIEW)
    public Result<List<MonitorInstanceVO>> instances() {
        return Result.success(service.instances());
    }

    @GetMapping("/sys/monitor/node/snapshot")
    @Operation(summary = "节点信息", description = "获取指定在线实例 JVM/OS/CPU/内存/磁盘/线程/GC 聚合信息")
    @SaCheckPermission(NodePermission.VIEW)
    public Result<NodeInfoVO> snapshot(@RequestParam(required = false) String instanceId) {
        return Result.success(service.snapshot(instanceId));
    }

    @GetMapping("/sys/monitor/internal/node/snapshot")
    @Operation(summary = "本机节点信息", description = "供其他已鉴权应用实例定向采集本机运行快照")
    @SaCheckPermission(NodePermission.VIEW)
    public Result<NodeInfoVO> localSnapshot() {
        return Result.success(service.localSnapshot());
    }
}
