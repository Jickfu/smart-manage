package sm.domain.sys.monitor.sql.model.form;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SqlCatalogListForm {
    @Size(max = 200)
    private String keyword;
}
