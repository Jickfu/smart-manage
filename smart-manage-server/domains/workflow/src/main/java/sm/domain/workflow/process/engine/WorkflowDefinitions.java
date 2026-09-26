package sm.domain.workflow.process.engine;

import java.util.List;
import java.util.Map;
import sm.domain.workflow.process.runtime.contract.WorkflowBusiness.FieldType;

/** 版本和发布事实由引擎拥有；设计 JSON 只作为官方设计器的透明协议。 */
public interface WorkflowDefinitions {
    record Definition(Long id, String code, String name, String version, boolean published) { }
    record Design(String json, String digest) { }
    Definition describe(Long id);
    List<Definition> list(String code);
    Long create(String code, String name);
    Design design(Long id);
    void save(Long id, String json, String expectedDigest, Map<String, FieldType> conditionFields);
    void publish(Long id, String expectedDigest, Map<String, FieldType> conditionFields);
    Long copy(Long id);
}
