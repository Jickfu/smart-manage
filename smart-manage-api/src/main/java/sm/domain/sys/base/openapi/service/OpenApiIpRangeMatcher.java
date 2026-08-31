package sm.domain.sys.base.openapi.service;

import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** IPv4/IPv6 单地址及 CIDR 匹配器，保存和调用校验复用相同语义。 */
final class OpenApiIpRangeMatcher {
    private static final Pattern IPV6_LITERAL = Pattern.compile("[0-9a-fA-F:.]+");
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
            parse(value);
            normalized.add(value);
        }
        return List.copyOf(normalized);
    }

    static boolean matches(String clientIp, List<String> ranges) {
        if (clientIp == null || clientIp.isBlank()) {
            return false;
        }
        ParsedAddress address = parseAddress(clientIp);
        for (String range : ranges) {
            ParsedRange parsedRange = parse(range);
            if (address.bits == parsedRange.bits
                    && address.value.shiftRight(address.bits - parsedRange.prefixLength)
                    .equals(parsedRange.network.shiftRight(address.bits - parsedRange.prefixLength))) {
                return true;
            }
        }
        return false;
    }

    private static ParsedRange parse(String range) {
        String[] parts = range.split("/", -1);
        if (parts.length > 2) {
            invalid(range);
        }
        ParsedAddress address = parseAddress(parts[0]);
        int prefixLength = address.bits;
        if (parts.length == 2) {
            try {
                prefixLength = Integer.parseInt(parts[1]);
            } catch (NumberFormatException exception) {
                invalid(range);
            }
        }
        if (prefixLength < 0 || prefixLength > address.bits) {
            invalid(range);
        }
        return new ParsedRange(address.value, address.bits, prefixLength);
    }

    private static ParsedAddress parseAddress(String value) {
        try {
            String candidate = value.trim();
            if (candidate.contains(":")) {
                if (!IPV6_LITERAL.matcher(candidate).matches()) {
                    invalid(value);
                }
            } else {
                String[] octets = candidate.split("\\.", -1);
                if (octets.length != 4) {
                    invalid(value);
                }
                for (String octet : octets) {
                    if (octet.isEmpty() || Integer.parseInt(octet) > 255) {
                        invalid(value);
                    }
                }
            }
            InetAddress address = InetAddress.getByName(candidate);
            byte[] bytes = address.getAddress();
            return new ParsedAddress(new BigInteger(1, bytes), bytes.length * Byte.SIZE);
        } catch (UnknownHostException | NumberFormatException exception) {
            throw new BizException(ResultEnum.PARAM_ERROR, "IP 地址或 CIDR 不合法: " + value);
        }
    }

    private static void invalid(String value) {
        throw new BizException(ResultEnum.PARAM_ERROR, "IP 地址或 CIDR 不合法: " + value);
    }

    private record ParsedAddress(BigInteger value, int bits) {
    }

    private record ParsedRange(BigInteger network, int bits, int prefixLength) {
    }
}
