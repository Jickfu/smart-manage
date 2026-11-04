package sm.domain.sys.monitor.sql.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "数据库对象目录列表输入表单")
@Data
public class SqlCatalogListForm {
    @Schema(description = "查询关键字")
    @Size(max = 200)
    private String keyword;
}
