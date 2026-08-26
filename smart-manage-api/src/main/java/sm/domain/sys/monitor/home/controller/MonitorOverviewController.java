package sm.domain.sys.monitor.home.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sm.domain.sys.monitor.home.model.vo.MonitorOverviewVO;
import sm.domain.sys.monitor.home.service.MonitorOverviewService;
import sm.domain.sys.monitor.runtime.constant.RuntimeMonitorPermission;
import sm.system.response.Result;

@RestController
@RequestMapping("/sys/monitor")
@RequiredArgsConstructor
public class MonitorOverviewController {
  private final MonitorOverviewService service;

  @GetMapping("/overview")
  @SaCheckPermission(RuntimeMonitorPermission.VIEW)
  public Result<MonitorOverviewVO> overview() {
    return Result.success(service.overview());
  }
}
