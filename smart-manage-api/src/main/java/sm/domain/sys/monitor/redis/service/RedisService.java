package sm.domain.sys.monitor.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.monitor.redis.model.form.RedisKeysForm;
import sm.domain.sys.monitor.redis.model.vo.RedisKeyVO;
import sm.domain.sys.monitor.redis.model.vo.RedisKeysVO;
import sm.domain.sys.monitor.redis.model.vo.RedisRuntimeVO;
import sm.domain.sys.monitor.redis.model.vo.RedisValueItemVO;
import sm.domain.sys.monitor.redis.model.vo.RedisValueVO;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;

/** Redis 实例的高风险只读诊断与受限删除服务。 */
@Service
@RequiredArgsConstructor
public class RedisService {
    private static final int VALUE_ITEM_LIMIT = 100;
    private static final int STRING_BYTE_LIMIT = 64 * 1024;
    private static final Pattern SENSITIVE_FIELD = Pattern.compile(
            "(?i)(\\\"(?:password|token|secret|privateKey|captcha|credential)\\\"\\s*:\\s*)\\\"[^\\\"]*\\\"");
    private static final List<String> SENSITIVE_KEY_MARKERS = List.of(
            "satoken", "sa-token", "sp:login", "session", "captcha", "password-change", "ticket",
            "user-info", "credential", "secret", "private-key");

    private final RedisTemplate<String, Object> redisTemplate;
    private final CurrentUserContext currentUserContext;

    @Value("${spring.data.redis.database:0}")
    private int database;

    public RedisRuntimeVO runtime() {
        currentUserContext.checkAdministrator();
        return redisTemplate.execute((RedisCallback<RedisRuntimeVO>) connection -> {
            Properties info = connection.info();
            long hits = longProperty(info, "keyspace_hits");
            long misses = longProperty(info, "keyspace_misses");
            long requests = hits + misses;
            Long dbSize = connection.dbSize();
            return RedisRuntimeVO.builder()
                    .available(true)
                    .version(property(info, "redis_version", "-"))
                    .uptimeSeconds(longProperty(info, "uptime_in_seconds"))
                    .usedMemoryBytes(longProperty(info, "used_memory"))
                    .usedMemoryDisplay(property(info, "used_memory_human", "-"))
                    .maxMemoryBytes(longProperty(info, "maxmemory"))
                    .connectedClients((int) longProperty(info, "connected_clients"))
                    .dbSize(dbSize == null ? 0 : dbSize)
                    .keyspaceHits(hits).keyspaceMisses(misses)
                    .hitRate(requests == 0 ? null : (double) hits / requests)
                    .database(database).collectedAt(LocalDateTime.now()).build();
        });
    }

    public RedisKeysVO keys(RedisKeysForm form) {
        currentUserContext.checkAdministrator();
        String matchPattern = form.getPattern() == null || form.getPattern().isBlank() ? "*" : form.getPattern();
        return redisTemplate.execute((RedisCallback<RedisKeysVO>) connection -> {
            Object raw = connection.execute("SCAN", bytes(form.getCursor()), bytes("MATCH"), bytes(matchPattern),
                    bytes("COUNT"), bytes(String.valueOf(form.getCount())));
            List<?> scanResult = asList(raw, "Redis SCAN 返回格式异常");
            if (scanResult.size() != 2) {
                throw new BizException(ResultEnum.EXTERNAL_SERVICE_ERROR, "Redis SCAN 返回格式异常");
            }
            String nextCursor = text(scanResult.get(0));
            List<RedisKeyVO> records = new ArrayList<>();
            for (Object rawKey : asList(scanResult.get(1), "Redis SCAN Key 列表格式异常")) {
                byte[] keyBytes = rawBytes(rawKey);
                String key = text(rawKey);
                DataType dataType = connection.type(keyBytes);
                Long ttl = connection.ttl(keyBytes);
                Long memoryBytes = number(connection.execute("MEMORY", bytes("USAGE"), keyBytes));
                records.add(RedisKeyVO.builder().key(key)
                        .type(dataType == null ? "unknown" : dataType.code())
                        .ttl(ttl == null ? -2 : ttl).memoryBytes(memoryBytes)
                        .valueReadable(!isSensitiveKey(key)).build());
            }
            return RedisKeysVO.builder().nextCursor(nextCursor).finished("0".equals(nextCursor)).records(records).build();
        });
    }

    public RedisValueVO value(String key) {
        currentUserContext.checkAdministrator();
        if (key == null || key.isBlank() || key.length() > 1024) {
            throw new BizException(ResultEnum.PARAM_ERROR, "Redis Key 格式不正确");
        }
        if (isSensitiveKey(key)) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "安全敏感 Key 不允许查看 Value");
        }
        return redisTemplate.execute((RedisCallback<RedisValueVO>) connection -> readValue(connection, key));
    }

    @BizLog(value = "删除Redis Key", recordRequest = false, recordResponse = false)
    public long delete(List<String> keys) {
        currentUserContext.checkAdministrator();
        if (keys == null || keys.isEmpty() || keys.size() > 100) {
            throw new BizException(ResultEnum.PARAM_ERROR, "单次只能删除 1 至 100 个 Redis Key");
        }
        Long deleted = redisTemplate.delete(keys);
        return deleted == null ? 0 : deleted;
    }

    private RedisValueVO readValue(RedisConnection connection, String key) {
        byte[] keyBytes = bytes(key);
        DataType dataType = connection.type(keyBytes);
        if (dataType == null || dataType == DataType.NONE) {
            throw new BizException(ResultEnum.NOT_FOUND, "Redis Key 不存在");
        }
        String type = dataType.code();
        List<RedisValueItemVO> items = new ArrayList<>();
        boolean truncated;
        switch (type) {
            case "string" -> {
                Long length = connection.stringCommands().strLen(keyBytes);
                byte[] value = connection.stringCommands().getRange(keyBytes, 0, STRING_BYTE_LIMIT - 1L);
                items.add(valueItem(null, value, null));
                truncated = length != null && length > STRING_BYTE_LIMIT;
            }
            case "hash" -> {
                Object result = connection.execute("HSCAN", keyBytes, bytes("0"), bytes("COUNT"), bytes(String.valueOf(VALUE_ITEM_LIMIT)));
                List<?> parts = asList(result, "Redis HSCAN 返回格式异常");
                List<?> values = asList(parts.get(1), "Redis HSCAN 内容格式异常");
                for (int index = 0; index + 1 < values.size(); index += 2) {
                    items.add(valueItem(text(values.get(index)), rawBytes(values.get(index + 1)), null));
                }
                truncated = !"0".equals(text(parts.get(0)));
            }
            case "list" -> {
                List<?> values = asList(connection.execute("LRANGE", keyBytes, bytes("0"), bytes(String.valueOf(VALUE_ITEM_LIMIT - 1))), "Redis LIST 返回格式异常");
                for (int index = 0; index < values.size(); index++) {
                    items.add(valueItem(String.valueOf(index), rawBytes(values.get(index)), null));
                }
                Long length = number(connection.execute("LLEN", keyBytes));
                truncated = length != null && length > VALUE_ITEM_LIMIT;
            }
            case "set" -> {
                Object result = connection.execute("SSCAN", keyBytes, bytes("0"), bytes("COUNT"), bytes(String.valueOf(VALUE_ITEM_LIMIT)));
                List<?> parts = asList(result, "Redis SSCAN 返回格式异常");
                for (Object value : asList(parts.get(1), "Redis SET 内容格式异常")) {
                    items.add(valueItem(null, rawBytes(value), null));
                }
                truncated = !"0".equals(text(parts.get(0)));
            }
            case "zset" -> {
                List<?> values = asList(connection.execute("ZRANGE", keyBytes, bytes("0"), bytes(String.valueOf(VALUE_ITEM_LIMIT - 1)), bytes("WITHSCORES")), "Redis ZSET 返回格式异常");
                for (int index = 0; index + 1 < values.size(); index += 2) {
                    items.add(valueItem(null, rawBytes(values.get(index)), Double.valueOf(text(values.get(index + 1)))));
                }
                Long length = number(connection.execute("ZCARD", keyBytes));
                truncated = length != null && length > VALUE_ITEM_LIMIT;
            }
            case "stream" -> {
                List<?> entries = asList(connection.execute("XRANGE", keyBytes, bytes("-"), bytes("+"),
                        bytes("COUNT"), bytes(String.valueOf(VALUE_ITEM_LIMIT))), "Redis STREAM 返回格式异常");
                for (Object rawEntry : entries) {
                    List<?> entry = asList(rawEntry, "Redis STREAM 条目格式异常");
                    String entryId = text(entry.get(0));
                    items.add(RedisValueItemVO.builder().name(entryId)
                            .value(streamFields(asList(entry.get(1), "Redis STREAM 字段格式异常")))
                            .base64(false).build());
                }
                Long length = number(connection.execute("XLEN", keyBytes));
                truncated = length != null && length > VALUE_ITEM_LIMIT;
            }
            default -> throw new BizException(ResultEnum.PARAM_ERROR, "暂不支持查看 " + type + " 类型的 Value");
        }
        return RedisValueVO.builder().key(key).type(type).truncated(truncated).items(items).build();
    }

    private RedisValueItemVO valueItem(String name, byte[] value, Double score) {
        DecodedValue decoded = decode(value);
        return RedisValueItemVO.builder().name(name).value(decoded.value()).score(score).base64(decoded.base64()).build();
    }

    private DecodedValue decode(byte[] value) {
        if (value == null) {
            return new DecodedValue(null, false);
        }
        try {
            String text = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(value)).toString();
            return new DecodedValue(SENSITIVE_FIELD.matcher(text).replaceAll("$1\"******\""), false);
        } catch (CharacterCodingException exception) {
            return new DecodedValue(Base64.getEncoder().encodeToString(value), true);
        }
    }

    private String streamFields(List<?> fields) {
        StringBuilder builder = new StringBuilder("{");
        for (int index = 0; index + 1 < fields.size(); index += 2) {
            if (builder.length() > 1) builder.append(", ");
            builder.append(text(fields.get(index))).append(": ")
                    .append(decode(rawBytes(fields.get(index + 1))).value());
        }
        return builder.append('}').toString();
    }

    private boolean isSensitiveKey(String key) {
        String normalized = key.toLowerCase(Locale.ROOT);
        return SENSITIVE_KEY_MARKERS.stream().anyMatch(normalized::contains);
    }

    /** 供统一缓存管理列表复用相同的敏感 Key 判定规则。 */
    public boolean isSensitive(String key) {
        return isSensitiveKey(key);
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] rawBytes(Object value) {
        return value instanceof byte[] bytes ? bytes : bytes(String.valueOf(value));
    }

    private static String text(Object value) {
        return value instanceof byte[] bytes ? new String(bytes, StandardCharsets.UTF_8) : String.valueOf(value);
    }

    static List<?> asList(Object value, String message) {
        if (value instanceof List<?> list) {
            return list;
        }
        // Jedis 5 的通用命令接口以 Object[] 返回 SCAN/HSCAN 等嵌套结果。
        if (value instanceof Object[] array) {
            return Arrays.asList(array);
        }
        throw new BizException(ResultEnum.EXTERNAL_SERVICE_ERROR, message);
    }

    private static Long number(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        return Long.valueOf(text(value));
    }

    private static String property(Properties properties, String name, String defaultValue) {
        return properties == null ? defaultValue : properties.getProperty(name, defaultValue);
    }

    private static long longProperty(Properties properties, String name) {
        return Long.parseLong(property(properties, name, "0"));
    }

    private record DecodedValue(String value, boolean base64) {
    }
}
