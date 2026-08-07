package sm.domain.sys.monitor.cache.model.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

/** 缓存条目统一分页查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CacheEntryListForm extends PageForm {
    private String keyword;
    private String scopeType;
    private String cloudNumber;
    private String appNumber;
}
