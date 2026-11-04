package sm.domain.sys.scheduler.singleton.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.scheduler.singleton.converter.SingletonTaskConverter;
import sm.domain.sys.scheduler.singleton.model.form.SingletonListForm;
import sm.domain.sys.scheduler.singleton.model.form.SingletonCommandForm;
import sm.domain.sys.scheduler.singleton.model.vo.SingletonTaskVO;
import sm.domain.sys.scheduler.singleton.model.vo.SingletonEventVO;
import sm.domain.sys.scheduler.singleton.model.entity.SingletonTaskEntity;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.response.PageData;
import sm.system.security.authorization.AdministratorOnly;
import sm.system.query.ListSqlQuery;

/** 常驻任务管理入口；不直接执行或强制抢占业务线程。 */
@Service
@AdministratorOnly
@RequiredArgsConstructor
public class SingletonTaskService {
    private final SingletonTaskRegistry registry;
    private final SingletonTaskAccessor accessor;
    private final SingletonTaskTxService transactions;
    private final SingletonTaskConverter converter;

    public PageData<SingletonTaskVO> listPage(SingletonListForm form) {
        // 首期只开放明确的编码/名称搜索，不接受没有实现语义的表头过滤或排序。
        ListSqlQuery.of(form, java.util.Map.of());
        List<SingletonTaskVO> records = accessor.list(form.getKeyword(),
                (form.getPageNum() - 1) * form.getPageSize(), form.getPageSize()).stream()
                .map(this::assemble).toList();
        return PageData.of(accessor.count(form.getKeyword()), form.getPageNum(), form.getPageSize(), records);
    }
    public SingletonTaskVO detail(String number) {
        SingletonTaskEntity entity = accessor.find(number);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "任务不存在");
        return assemble(entity);
    }
    public List<SingletonEventVO> events(String number) {
        detail(number);
        return accessor.events(number);
    }
    private SingletonTaskVO assemble(SingletonTaskEntity entity) {
        SingletonTaskVO result = converter.toVO(entity);
        result.setAvailable(registry.find(entity.getNumber()) != null);
        return result;
    }
    @BizLog("设置常驻任务运行期望")
    public void control(SingletonCommandForm form) {
        var task = registry.find(form.getNumber());
        if (task == null || !task.definition().controllable()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "当前实例未装配可控制的任务实现");
        }
        if (form.getVersion() == null || form.getRequested() == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "任务版本和运行期望不能为空");
        }
        task.validateControl(form.getRequested());
        transactions.control(form.getNumber(), form.getVersion(), form.getRequested());
    }
}
