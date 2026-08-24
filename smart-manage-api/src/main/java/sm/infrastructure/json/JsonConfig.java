package sm.infrastructure.json;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sm.infrastructure.json.deserializer.LongJsonDeserializer;
import sm.infrastructure.json.serializer.LongJsonSerializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JSON 全局配置（Jackson 3）
 *
 * @author Chekfu
 */
@Configuration
public class JsonConfig {

    /** 日期格式：yyyy-MM-dd */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /** 日期时间格式：yyyy-MM-dd HH:mm:ss */
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Bean
    public JsonMapperBuilderCustomizer customizer() {
        return builder -> {
            // 日期格式化模块（Jackson 3 内置 JSR-310，无需额外依赖）
            SimpleModule dateModule = new SimpleModule("dateModule");
            dateModule.addSerializer(new LocalDateTimeSerializer(DATETIME_FORMATTER));
            dateModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATETIME_FORMATTER));
            dateModule.addSerializer(new LocalDateSerializer(DATE_FORMATTER));
            dateModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE_FORMATTER));
            builder.addModule(dateModule);

            // Long 转 String 模块，防止JS丢失精度
            SimpleModule longModule = new SimpleModule("longModule");
            longModule.addSerializer(Long.class, LongJsonSerializer.INSTANCE);
            longModule.addDeserializer(Long.class, LongJsonDeserializer.INSTANCE);
            builder.addModule(longModule);
        };
    }

}
