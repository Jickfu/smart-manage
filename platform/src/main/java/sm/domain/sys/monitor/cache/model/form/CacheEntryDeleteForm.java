package sm.domain.sys.monitor.cache.model.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/** 缓存条目批量删除命令。 */
@Data
public class CacheEntryDeleteForm {
    @Valid
    @NotEmpty
    @Size(max = 100)
    private List<CacheEntryKeyForm> entries;
}
