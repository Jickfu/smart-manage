package sm.domain.sys.monitor.script.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class ScriptApiServiceVO {
    private String beanName;
    private String className;
    private List<ScriptApiMethodVO> methods;
}
