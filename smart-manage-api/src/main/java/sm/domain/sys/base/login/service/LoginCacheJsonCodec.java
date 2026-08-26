package sm.domain.sys.base.login.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Set;

/** 登录缓存 JSON 编解码器，只按调用方指定的具体类型解析，不接受 Redis 数据中的 Java 类型信息。 */
@Component
@RequiredArgsConstructor
class LoginCacheJsonCodec {
    private static final Set<String> TYPE_METADATA_FIELDS = Set.of("@class", "@type", "$type");

    private final JsonMapper jsonMapper;

    String write(Object value) {
        try {
            return jsonMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw cacheUnavailable(exception);
        }
    }

    <T> T read(String json, Class<T> targetType) {
        if (json == null) {
            return null;
        }
        try {
            JsonNode root = jsonMapper.readTree(json);
            rejectTypeMetadata(root);
            return jsonMapper.treeToValue(root, targetType);
        } catch (JacksonException exception) {
            throw cacheUnavailable(exception);
        }
    }

    private void rejectTypeMetadata(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            for (var property : node.properties()) {
                if (TYPE_METADATA_FIELDS.contains(property.getKey())) {
                    throw new BizException(ResultEnum.SERVER_ERROR, "认证服务暂不可用");
                }
                rejectTypeMetadata(property.getValue());
            }
            return;
        }
        if (node.isArray()) {
            node.valueStream().forEach(this::rejectTypeMetadata);
        }
    }

    private BizException cacheUnavailable(JacksonException exception) {
        return new BizException(ResultEnum.SERVER_ERROR, "认证服务暂不可用", exception);
    }
}
