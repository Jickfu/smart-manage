package sm.infrastructure.json.serializer;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Long 转 String，防止js丢失精度
 *
 * @author Chekfu
 */
public class LongJsonSerializer extends ValueSerializer<Long> {
    public static final LongJsonSerializer INSTANCE = new LongJsonSerializer();

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializationContext serializationContext) {
        if (null == value) {
            gen.writeNull();
            return;
        }
        // 统一序列化为字符串：避免前端出现 number/string 混用，并彻底规避 JS 精度丢失
        gen.writeString(String.valueOf(value));
    }
}
