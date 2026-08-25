package sm.domain.sys.monitor.alert.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sm.domain.sys.monitor.alert.constant.MonitorAlertPermission;
import sm.domain.sys.monitor.alert.model.form.MonitorAlertIncidentListForm;
import sm.domain.sys.monitor.alert.model.form.MonitorAlertRuleSaveForm;
import sm.domain.sys.monitor.alert.service.MonitorAlertService;
import sm.system.response.PageData;
import sm.system.response.Result;
import java.util.*;

@RestController
@RequestMapping("/sys/monitor/alert")
@RequiredArgsConstructor
public class MonitorAlertController {
    private final MonitorAlertService service;

    @GetMapping("/rules")
    @SaCheckPermission(MonitorAlertPermission.VIEW)
    public Result<List<Map<String,Object>>> rules() { return Result.success(service.rules()); }

    @PutMapping("/rules")
    @SaCheckPermission(MonitorAlertPermission.MANAGE)
    public Result<Void> saveRule(@Valid @RequestBody MonitorAlertRuleSaveForm form) {
        service.saveRule(form); return Result.success();
    }

    @GetMapping("/incidents")
    @SaCheckPermission(MonitorAlertPermission.VIEW)
    public Result<PageData<Map<String,Object>>> incidents(@Valid MonitorAlertIncidentListForm form) {
        return Result.success(service.incidents(form));
    }
}
