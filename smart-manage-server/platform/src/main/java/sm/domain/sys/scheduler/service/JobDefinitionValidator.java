package sm.domain.sys.scheduler.service;

import lombok.RequiredArgsConstructor;
import org.quartz.CronExpression;
import org.quartz.Job;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import sm.domain.sys.scheduler.model.form.JobSaveForm;
import sm.domain.sys.scheduler.contract.SchedulerJobDefinition;
import sm.domain.sys.base.app.service.AppReferenceService;
import sm.domain.sys.base.app.model.entity.AppEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务定义边界校验器。
 * 仅允许调度 Spring 容器中显式注册的 Job，禁止通过类名加载任意代码。
 */
@Component
@RequiredArgsConstructor
class JobDefinitionValidator {

    private final ApplicationContext applicationContext;
    private final JsonMapper jsonMapper;
    private final AppReferenceService appReferenceService;

    public void validate(JobSaveForm form) {
        if (!CronExpression.isValidExpression(form.getCronExpression().trim())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "Cron 表达式不合法");
        }
        resolveJobClass(form.getJobClassName().trim());
        parseJobData(form.getJobData());
    }

    public Class<? extends Job> resolveJobClass(String className) {
        return jobClasses().stream()
                .filter(jobClass -> jobClass.getName().equals(className))
                .findFirst()
                .orElseThrow(() -> new BizException(ResultEnum.CONFIG_ERROR, "任务类未在系统中注册: " + className));
    }

    /** 裁剪领域后保留其数据库定义，但不得执行该领域的遗留任务；已安装领域中的拼写错误仍然报错。 */
    boolean isFromUnavailableDomain(String className) {
        if (className == null || !className.startsWith("sm.domain.")) return false;
        String[] parts = className.split("\\.");
        if (parts.length < 4 || "sys".equals(parts[2])) return false;
        try {
            var resources = new org.springframework.core.io.support.PathMatchingResourcePatternResolver()
                    .getResources("classpath*:META-INF/smart-manage/migration.properties");
            for (var resource : resources) {
                var properties = new java.util.Properties();
                try (var stream = resource.getInputStream()) { properties.load(stream); }
                if (parts[2].equals(properties.getProperty("id"))) return false;
            }
            return true;
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("无法核实任务所属领域是否装配", exception);
        }
    }

    public List<Map<String, String>> availableJobClasses() {
        return jobClasses().stream()
                .sorted(Comparator.comparing(Class::getName))
                .map(jobClass -> {
                    Map<String, String> item = new LinkedHashMap<>();
                    SchedulerJobDefinition definition = jobClass.getAnnotation(SchedulerJobDefinition.class);
                    item.put("className", jobClass.getName());
                    item.put("simpleName", jobClass.getSimpleName());
                    item.put("description", definition == null ? "" : definition.description());
                    item.put("parameterTemplate", definition == null ? "{}" : definition.parameterTemplate());
                    if (definition != null && !definition.appNumber().isBlank()) {
                        AppEntity app = appReferenceService.findByNumber(definition.appNumber());
                        if (app == null) {
                            throw new BizException(ResultEnum.CONFIG_ERROR,
                                    "任务默认所属应用不存在：" + definition.appNumber());
                        }
                        item.put("appId", app.getId().toString());
                        item.put("appNumber", app.getNumber());
                        item.put("appName", app.getName());
                    }
                    return item;
                })
                .toList();
    }

    public Map<String, Object> parseJobData(String jobDataJson) {
        if (jobDataJson == null || jobDataJson.isBlank()) {
            return Map.of();
        }
        try {
            return jsonMapper.readValue(jobDataJson, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new BizException(ResultEnum.PARAM_ERROR, "任务参数必须是合法的 JSON 对象");
        }
    }

    private List<Class<? extends Job>> jobClasses() {
        return applicationContext.getBeansOfType(Job.class).values().stream()
                .map(AopUtils::getTargetClass)
                .filter(Job.class::isAssignableFrom)
                .filter(jobClass -> jobClass != ManagedJobDispatcher.class)
                .<Class<? extends Job>>map(JobDefinitionValidator::asJobClass)
                .distinct()
                .toList();
    }

    private static Class<? extends Job> asJobClass(Class<?> jobClass) {
        return jobClass.asSubclass(Job.class);
    }
}
