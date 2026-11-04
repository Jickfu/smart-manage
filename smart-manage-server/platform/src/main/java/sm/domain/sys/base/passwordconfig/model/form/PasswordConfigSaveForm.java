package sm.domain.sys.base.passwordconfig.model.form;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;
import lombok.Data;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

@Schema(description = "密码策略配置保存输入表单")
@Data
public class PasswordConfigSaveForm {
    @Schema(description = "ID")
    @NotNull
    private Long id;
    @Schema(description = "乐观锁版本号；修改已有记录时须提交读取到的版本")
    @NotNull
    private Integer version;
    @Schema(description = "密码最小长度")
    @NotNull
    @Min(4)
    @Max(64)
    @JsonDeserialize(using = MinimumLengthDeserializer.class)
    private Integer minLength;
    @Schema(description = "是否启用四类字符复杂度")
    @NotNull
    private Boolean complexityEnabled;

    /**
     * 安全策略不能将小数静默截断为更短的长度，只接受 JSON 整数。
     */
    public static final class MinimumLengthDeserializer extends ValueDeserializer<Integer> {
        @Override
        public Integer deserialize(JsonParser parser, DeserializationContext context) {
            if (parser.currentToken() != JsonToken.VALUE_NUMBER_INT) {
                throw DatabindException.from(parser, "密码最小长度必须为整数");
            }
            return parser.getIntValue();
        }
    }
}
