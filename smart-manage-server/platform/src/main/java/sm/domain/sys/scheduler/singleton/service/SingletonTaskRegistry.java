package sm.domain.sys.scheduler.singleton.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import sm.domain.sys.scheduler.singleton.contract.SingletonTask;

/** 只接受 Spring 中显式注册的实现，不按请求中的类名加载代码。 */
@Component
public class SingletonTaskRegistry {
    private final Map<String, SingletonTask> tasks = new LinkedHashMap<>();
    public SingletonTaskRegistry(List<SingletonTask> implementations) {
        for (SingletonTask task : implementations) {
            if (tasks.putIfAbsent(task.definition().number(), task) != null) {
                throw new IllegalStateException("单例任务编码重复");
            }
        }
    }
    public List<SingletonTask> tasks() { return List.copyOf(tasks.values()); }
    public SingletonTask find(String number) { return tasks.get(number); }
}
