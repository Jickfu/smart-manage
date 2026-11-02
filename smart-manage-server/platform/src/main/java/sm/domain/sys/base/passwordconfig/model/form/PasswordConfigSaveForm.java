package sm.domain.sys.base.passwordconfig.model.form;

import jakarta.validation.constraints.*;
import lombok.Data;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

@Data
public class PasswordConfigSaveForm {
    @NotNull
    private Long id;
    @NotNull
    private Integer version;
    @NotNull
    @Min(4)
    @Max(64)
    @JsonDeserialize(using = MinimumLengthDeserializer.class)
    private Integer minLength;
    @NotNull
    private Boolean complexityEnabled;

    /** 安全策略不能将小数静默截断为更短的长度，只接受 JSON 整数。 */
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
