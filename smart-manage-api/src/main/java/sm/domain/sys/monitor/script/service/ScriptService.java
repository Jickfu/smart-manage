package sm.domain.sys.monitor.script.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.javascript.*;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.helper.UserHelper;
import sm.domain.sys.base.sysparam.service.SysParamService;
import sm.domain.sys.monitor.script.model.entity.ScriptEntity;
import sm.domain.sys.monitor.script.model.form.ScriptExecuteForm;
import sm.domain.sys.monitor.script.model.form.ScriptListForm;
import sm.domain.sys.monitor.script.model.form.ScriptSaveForm;
import sm.domain.sys.monitor.script.model.vo.ScriptDetailVO;
import sm.domain.sys.monitor.script.model.vo.ScriptListVO;
import sm.domain.sys.monitor.script.model.vo.ScriptResultVO;
import sm.domain.sys.monitor.script.mapper.ScriptMapper;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.aop.log.BizLog;
import sm.system.helper.SpringContextHelper;
import sm.system.response.PageData;
import sm.system.util.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 脚本控制台服务——Rhino 执行 JS + 脚本 CRUD
 */
@Slf4j
@Service
public class ScriptService {

    private final ScriptMapper mapper;
    private final SysParamService sysParamService;
    private final ScriptTxService txService;
    private final ScriptConverter converter;

    public ScriptService(
            ScriptMapper mapper,
            SysParamService sysParamService,
            ScriptTxService txService,
            ScriptConverter converter) {
        this.mapper = mapper;
        this.sysParamService = sysParamService;
        this.txService = txService;
        this.converter = converter;
    }

    // ---- 脚本执行 ----

    @BizLog(value = "执行脚本", saveRequest = false, saveResponse = false)
    public ScriptResultVO execute(ScriptExecuteForm form) {
        UserHelper.checkAdmin();
        String content = form.getContent().trim();
        if (content.isEmpty()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "脚本内容不能为空");
        }

        ScriptResultVO result = new ScriptResultVO();
        long start = System.currentTimeMillis();

        // 从系统参数取超时时间，默认 60 秒
        Integer timeoutParam = sysParamService.getInt("SCRIPT_EXECUTE_TIMEOUT");
        int timeoutSeconds = timeoutParam != null && timeoutParam > 0 ? timeoutParam : 60;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<ScriptResultVO> future = executor.submit(() -> executeScript(content));

        try {
            ScriptResultVO r = future.get(timeoutSeconds, TimeUnit.SECONDS);
            result.setOutput(r.getOutput());
        } catch (TimeoutException e) {
            future.cancel(true);
            result.setOutput("Error: 脚本执行超时 (" + timeoutSeconds + "s)");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            log.warn("脚本执行异常: {}", cause.getMessage());
            result.setOutput("Error: " + cause.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.setOutput("Error: 执行被中断");
        } finally {
            executor.shutdownNow();
        }

        result.setExecuteDuration((int) (System.currentTimeMillis() - start));
        return result;
    }

    private ScriptResultVO executeScript(String jsContent) {
        ScriptResultVO result = new ScriptResultVO();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outStream, true, StandardCharsets.UTF_8);

        Context cx = Context.enter();
        try {
            cx.setOptimizationLevel(-1);
            cx.setLanguageVersion(Context.VERSION_ES6);

            Scriptable scope = cx.initStandardObjects();

            // 注入 ctx：Spring 容器，通过 ctx.getBean("xxx") 获取 Bean
            Object wrappedCtx = Context.javaToJS(SpringContextHelper.getApplicationContext(), scope);
            ScriptableObject.putProperty(scope, "ctx", wrappedCtx);

            // 重定向 print() 函数，捕获输出
            ScriptableObject.putProperty(scope, "print", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    printStream.println(Context.toString(args.length == 0 ? Undefined.instance : args[0]));
                    return Undefined.instance;
                }
            });

            // 注入 console.log()
            Scriptable console = cx.newObject(scope);
            ScriptableObject.putProperty(console, "log", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    printStream.println(Context.toString(args.length == 0 ? Undefined.instance : args[0]));
                    return Undefined.instance;
                }
            });
            ScriptableObject.putProperty(scope, "console", console);

            // 执行脚本，最后一行表达式的值作为返回值
            Object evalResult = cx.evaluateString(scope, jsContent, "<script>", 1, null);

            // 收集输出
            printStream.flush();
            String capturedOutput = outStream.toString(StandardCharsets.UTF_8).trim();

            String returnValue = evalResult instanceof Undefined ? "" : Context.toString(evalResult);

            StringBuilder output = new StringBuilder();
            if (!capturedOutput.isEmpty()) {
                output.append(capturedOutput);
            }
            if (!returnValue.isEmpty()) {
                if (!output.isEmpty()) {
                    output.append("\n");
                }
                output.append(returnValue);
            }

            result.setOutput(output.toString());
        } catch (RhinoException e) {
            printStream.flush();
            String capturedOutput = outStream.toString(StandardCharsets.UTF_8).trim();

            StringBuilder output = new StringBuilder();
            if (!capturedOutput.isEmpty()) {
                output.append(capturedOutput).append("\n");
            }
            output.append("Error: ").append(e.getMessage());
            result.setOutput(output.toString());
        } finally {
            Context.exit();
        }

        return result;
    }

    // ---- 脚本 CRUD ----

    public PageData<ScriptListVO> listPage(ScriptListForm form) {
        UserHelper.checkAdmin();
        LambdaQueryWrapper<ScriptEntity> qw = new LambdaQueryWrapper<ScriptEntity>();
        if (StringUtil.isNotBlank(form.getKeyword())) {
            qw.like(ScriptEntity::getNumber, form.getKeyword());
        }
        qw.orderByDesc(ScriptEntity::getCreateTime);

        Page<ScriptEntity> page = mapper.selectPage(new Page<>(form.getPageNum(), form.getPageSize()), qw);
        List<ScriptListVO> vos = page.getRecords().stream().map(converter::toListVO).collect(Collectors.toList());
        return PageData.of(page.getTotal(), form.getPageNum(), form.getPageSize(), vos);
    }

    public ScriptDetailVO detail(Long id) {
        UserHelper.checkAdmin();
        ScriptEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "脚本不存在");
        }
        return converter.toDetailVO(entity);
    }

    @BizLog(value = "保存脚本", saveRequest = false)
    public Long save(ScriptSaveForm form) {
        UserHelper.checkAdmin();
        return txService.save(form);
    }

    @BizLog("删除脚本")
    public void delete(Long id) {
        UserHelper.checkAdmin();
        txService.delete(id);
    }

}
