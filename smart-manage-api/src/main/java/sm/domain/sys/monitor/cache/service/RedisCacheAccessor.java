package sm.domain.sys.monitor.cache.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import sm.domain.sys.monitor.cache.model.vo.CacheRuntimeVO;
import sm.domain.sys.monitor.cache.model.vo.CacheValueItemVO;
import sm.domain.sys.monitor.cache.model.vo.CacheValueVO;
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
import java.util.Collection;
import java.util.LinkedHashSet;

/** 缓存模块内部的 Redis 原始访问器，不作为公开业务入口。 */
@Component
class RedisCacheAccessor {
    private static final int VALUE_ITEM_LIMIT = 100;
    private static final int STRING_BYTE_LIMIT = 64 * 1024;
    private static final Pattern SENSITIVE_FIELD = Pattern.compile(
            "(?i)(\\\"(?:password|token|secret|privateKey|captcha|credential)\\\"\\s*:\\s*)\\\"[^\\\"]*\\\"");
    private static final List<String> SENSITIVE_KEY_MARKERS = List.of(
            "satoken", "sa-token", "smtoken", "sp:login", "session", "captcha", "password-change", "ticket",
            "user-info", "credential", "secret", "private-key");

    private final StringRedisTemplate redisTemplate;
    private volatile boolean memoryUsageSupported = true;

    @Value("${spring.data.redis.database:0}")
    private int database;

    RedisCacheAccessor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    CacheRuntimeVO runtime() {
        return redisTemplate.execute((RedisCallback<CacheRuntimeVO>) connection -> {
            Properties info = connection.info();
            long hits = longProperty(info, "keyspace_hits");
            long misses = longProperty(info, "keyspace_misses");
            long requests = hits + misses;
            Long dbSize = connection.dbSize();
            return CacheRuntimeVO.builder()
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

    /** 只扫描缓存目录登记的受控前缀，禁止退化为全库扫描。 */
    List<RedisEntry> scanEntries(Collection<String> prefixes) {
        return redisTemplate.execute((RedisCallback<List<RedisEntry>>) connection -> {
            List<RedisEntry> records = new ArrayList<>();
            LinkedHashSet<String> visited = new LinkedHashSet<>();
            for (String prefix : prefixes) {
                try (Cursor<byte[]> cursor = connection.scan(
                        ScanOptions.scanOptions().match(prefix + "*").count(200).build())) {
                    while (cursor.hasNext()) {
                        byte[] keyBytes = cursor.next();
                        String key = new String(keyBytes, StandardCharsets.UTF_8);
                        if (!visited.add(key)) continue;
                        DataType dataType = connection.type(keyBytes);
                        Long ttl = connection.ttl(keyBytes);
                        Long memoryBytes = readMemoryUsage(connection, keyBytes);
                    records.add(new RedisEntry(key, dataType == null ? "unknown" : dataType.code(),
                            ttl == null ? -2 : ttl, memoryBytes, !isSensitiveKey(key)));
                    }
                }
            }
            return records;
        });
    }

    /** 应用实例注册通过稳定索引派生具体 Key，无需扫描 Redis DB。 */
    List<RedisEntry> monitorInstanceEntries() {
        String indexKey = "sm:monitor:instances";
        return redisTemplate.execute((RedisCallback<List<RedisEntry>>) connection -> {
            LinkedHashSet<String> keys = new LinkedHashSet<>();
            keys.add(indexKey);
            // 监控索引使用字符串协议，直接读取原始字节以兼容不同 Redis 驱动的命令返回结构。
            var instanceIds = connection.zSetCommands().zRange(bytes(indexKey), 0, -1);
            if (instanceIds != null) {
                instanceIds.forEach(instanceId -> keys.add("sm:monitor:instance:" + text(instanceId)));
            }
            List<RedisEntry> records = new ArrayList<>();
            for (String key : keys) {
                byte[] keyBytes = bytes(key);
                DataType dataType = connection.type(keyBytes);
                if (dataType == null || dataType == DataType.NONE) continue;
                Long ttl = connection.ttl(keyBytes);
                records.add(new RedisEntry(key, dataType.code(), ttl == null ? -2 : ttl,
                        readMemoryUsage(connection, keyBytes), true));
            }
            return records;
        });
    }

    CacheValueVO value(String key) {
        if (key == null || key.isBlank() || key.length() > 1024) {
            throw new BizException(ResultEnum.PARAM_ERROR, "Redis Key 格式不正确");
        }
        if (isSensitiveKey(key)) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "安全敏感 Key 不允许查看 Value");
        }
        return redisTemplate.execute((RedisCallback<CacheValueVO>) connection -> readValue(connection, key));
    }

    long delete(List<String> keys) {
        if (keys == null || keys.isEmpty() || keys.size() > 100) {
            throw new BizException(ResultEnum.PARAM_ERROR, "单次只能删除 1 至 100 个 Redis Key");
        }
        Long deleted = redisTemplate.delete(keys);
        return deleted == null ? 0 : deleted;
    }

    void clearByPrefix(String cacheName) {
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            try (Cursor<byte[]> cursor = connection.scan(
                    ScanOptions.scanOptions().match(cacheName + "*").count(500).build())) {
                List<byte[]> batch = new ArrayList<>(500);
                while (cursor.hasNext()) {
                    batch.add(cursor.next());
                    if (batch.size() == 500) {
                        connection.del(batch.toArray(byte[][]::new));
                        batch.clear();
                    }
                }
                if (!batch.isEmpty()) {
                    connection.del(batch.toArray(byte[][]::new));
                }
            }
            return null;
        });
    }

    private CacheValueVO readValue(RedisConnection connection, String key) {
        byte[] keyBytes = bytes(key);
        DataType dataType = connection.type(keyBytes);
        if (dataType == null || dataType == DataType.NONE) {
            throw new BizException(ResultEnum.NOT_FOUND, "Redis Key 不存在");
        }
        String type = dataType.code();
        List<CacheValueItemVO> items = new ArrayList<>();
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
                    items.add(CacheValueItemVO.builder().name(entryId)
                            .value(streamFields(asList(entry.get(1), "Redis STREAM 字段格式异常")))
                            .base64(false).build());
                }
                Long length = number(connection.execute("XLEN", keyBytes));
                truncated = length != null && length > VALUE_ITEM_LIMIT;
            }
            default -> throw new BizException(ResultEnum.PARAM_ERROR, "暂不支持查看 " + type + " 类型的 Value");
        }
        return CacheValueVO.builder().key(key).type(type).truncated(truncated).items(items).build();
    }

    private CacheValueItemVO valueItem(String name, byte[] value, Double score) {
        DecodedValue decoded = decode(value);
        return CacheValueItemVO.builder().name(name).value(decoded.value()).score(score).base64(decoded.base64()).build();
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

    Long readMemoryUsage(RedisConnection connection, byte[] keyBytes) {
        if (!memoryUsageSupported) {
            return null;
        }
        try {
            return number(connection.execute("MEMORY", bytes("USAGE"), keyBytes));
        } catch (RedisSystemException exception) {
            Throwable current = exception;
            while (current != null) {
                String message = current.getMessage();
                if (message != null && message.toLowerCase(Locale.ROOT).contains("unknown command")
                        && message.toLowerCase(Locale.ROOT).contains("memory")) {
                    memoryUsageSupported = false;
                    return null;
                }
                current = current.getCause();
            }
            throw exception;
        }
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

    record RedisEntry(String key, String type, long ttl, Long memoryBytes, boolean valueReadable) {
    }
}
