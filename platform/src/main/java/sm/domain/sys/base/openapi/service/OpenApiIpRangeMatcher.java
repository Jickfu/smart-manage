package sm.domain.sys.base.openapi.service;

import sm.system.exception.BizException;
import sm.system.net.IpAddressRange;
import sm.system.response.ResultEnum;

import java.util.ArrayList;
import java.util.List;

/** IPv4/IPv6 单地址及 CIDR 匹配器，保存和调用校验复用相同语义。 */
final class OpenApiIpRangeMatcher {
    private OpenApiIpRangeMatcher() {
    }

    static List<String> normalize(String ranges) {
        if (ranges == null || ranges.isBlank()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String line : ranges.split("\\R")) {
            String value = line.trim();
            if (value.isEmpty()) {
                continue;
            }
            try {
                normalized.add(IpAddressRange.parse(value).normalized());
            } catch (IllegalArgumentException exception) {
                invalid(value);
            }
        }
        return List.copyOf(normalized);
    }

    static boolean matches(String clientIp, List<String> ranges) {
        if (clientIp == null || clientIp.isBlank()) {
            return false;
        }
        for (String range : ranges) {
            try {
                if (IpAddressRange.parse(range).contains(clientIp)) {
                    return true;
                }
            } catch (IllegalArgumentException exception) {
                invalid(range);
            }
        }
        return false;
    }

    private static void invalid(String value) {
        throw new BizException(ResultEnum.PARAM_ERROR, "IP 地址或 CIDR 不合法: " + value);
    }
}
