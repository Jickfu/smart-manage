package sm.domain.sys.base.basicdata.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "基础数据编码表单")
public class BasicDataNumberForm {

    @NotBlank(message = "基础数据编码不能为空")
    @Schema(description = "基础数据编码")
    private String number;
}
