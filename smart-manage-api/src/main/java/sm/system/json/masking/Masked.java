package sm.system.json.masking;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import tools.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 标记需要在 JSON 出站边界按权限脱敏的字段。 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = MaskedValueSerializer.class)
public @interface Masked {
    MaskingType type();

    /** 拥有该权限时允许输出明文；留空表示永不放行。 */
    String revealPermission() default "";
}
