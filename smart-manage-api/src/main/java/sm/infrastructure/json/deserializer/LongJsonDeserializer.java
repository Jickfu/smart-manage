package sm.infrastructure.json.deserializer;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * @author Chekfu
 */
public class LongJsonDeserializer extends ValueDeserializer<Long> {
    public static final LongJsonDeserializer INSTANCE = new LongJsonDeserializer();

    @Override
    public Long deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        String value = jsonParser.getText();
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            // 基础设施层不能静默吞掉非法 ID，否则业务层会误判为"未传值"。
            throw DatabindException.from(jsonParser, "Long 字段必须是数字字符串：" + value, exception);
        }
    }
}
