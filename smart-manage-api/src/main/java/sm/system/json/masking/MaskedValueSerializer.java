package sm.system.json.masking;

import cn.dev33.satoken.stp.StpUtil;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/** 不修改原对象，仅在 JSON 序列化时输出脱敏副本。 */
public final class MaskedValueSerializer extends ValueSerializer<Object> {
    private final MaskingType type;
    private final String revealPermission;

    public MaskedValueSerializer() {
        this(MaskingType.REDACT, "");
    }

    private MaskedValueSerializer(MaskingType type, String revealPermission) {
        this.type = type;
        this.revealPermission = revealPermission;
    }

    @Override
    public ValueSerializer<?> createContextual(SerializationContext context, BeanProperty property) {
        Masked annotation = property == null ? null : property.getAnnotation(Masked.class);
        if (annotation == null) return this;
        return new MaskedValueSerializer(annotation.type(), annotation.revealPermission());
    }

    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContext context) throws JacksonException {
        String text = String.valueOf(value);
        generator.writeString(canReveal() ? text : type.mask(text));
    }

    private boolean canReveal() {
        if (revealPermission == null || revealPermission.isBlank()) return false;
        try {
            return StpUtil.isLogin() && StpUtil.hasPermission(revealPermission);
        } catch (RuntimeException exception) {
            // 安全上下文缺失或异常时必须失败关闭，不能意外返回明文。
            return false;
        }
    }
}
