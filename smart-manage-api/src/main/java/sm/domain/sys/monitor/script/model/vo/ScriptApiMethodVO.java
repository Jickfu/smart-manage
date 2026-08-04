package sm.domain.sys.monitor.script.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class ScriptApiMethodVO {
    private String name;
    private String signature;
    private String returnType;
    private List<ScriptApiParameterVO> parameters;
    private String example;
}
