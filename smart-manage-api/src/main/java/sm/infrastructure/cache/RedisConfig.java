package sm.infrastructure.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Redis 配置（Jackson 3）
 *
 * @author Chekfu
 */
@Configuration
public class RedisConfig {

    /** 日期时间格式：yyyy-MM-dd HH:mm:ss，与 JsonConfig 保持一致 */
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 构建 Jackson 3 JsonMapper：自定义日期格式 + 保留类型信息
        SimpleModule dateModule = new SimpleModule("redisDateModule");
        dateModule.addSerializer(new LocalDateTimeSerializer(DATETIME_FORMATTER));
        dateModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATETIME_FORMATTER));

        JsonMapper jsonMapper = JsonMapper.builder()
                .addModule(dateModule)
                .activateDefaultTyping(
                        BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(),
                        DefaultTyping.NON_FINAL)
                .build();

        // Key 序列化
        template.setKeySerializer(new StringRedisSerializer());
        // Value 序列化（Jackson 3 JSON 格式，保留类型信息）
        template.setValueSerializer(new GenericJacksonJsonRedisSerializer(jsonMapper));

        // Hash Key 序列化
        template.setHashKeySerializer(new StringRedisSerializer());
        // Hash Value 序列化
        template.setHashValueSerializer(new GenericJacksonJsonRedisSerializer(jsonMapper));
        // 默认序列化
        template.setDefaultSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
