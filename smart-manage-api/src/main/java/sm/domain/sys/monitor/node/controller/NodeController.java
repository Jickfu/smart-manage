package sm.domain.sys.monitor.node.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.monitor.node.constant.NodePermission;
import sm.domain.sys.monitor.node.model.vo.NodeInfoVO;
import sm.domain.sys.monitor.node.service.NodeService;
import sm.system.response.Result;

/**
 * 节点监控接口
 */
@RestController
@Tag(name = "系统监控-节点监控", description = "服务器节点状态监控")
@RequiredArgsConstructor
public class NodeController {

    private final NodeService service;

    @PostMapping("/sys/monitor/node/info")
    @Operation(summary = "节点信息", description = "获取当前节点 JVM/OS/CPU/内存/磁盘/线程/GC 聚合信息")
    @SaCheckPermission(NodePermission.LIST)
    public Result<NodeInfoVO> info() {
        return Result.success(service.getNodeInfo());
    }
}
