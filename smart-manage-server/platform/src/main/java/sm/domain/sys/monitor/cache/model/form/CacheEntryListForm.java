package sm.domain.sys.monitor.cache.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

/**
 * 缓存条目统一分页查询条件。
 */
@Schema(description = "缓存条目查询输入表单")
@Data
@EqualsAndHashCode(callSuper = true)
public class CacheEntryListForm extends PageForm {
    @Schema(description = "查询关键字")
    private String keyword;
    @Schema(description = "缓存查询范围：ALL 全部、APPLICATION 应用、CACHE 指定缓存、INFRASTRUCTURE 基础设施；未指定时为 ALL")
    private String scopeType;
    @Schema(description = "受控缓存资源标识")
    private String resourceKey;
}
