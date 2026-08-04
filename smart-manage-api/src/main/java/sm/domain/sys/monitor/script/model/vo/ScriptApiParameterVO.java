package sm.domain.sys.monitor.script.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class ScriptApiParameterVO {
    private String name;
    private String type;
    private boolean required;
    private List<ScriptApiFieldVO> fields;
}
