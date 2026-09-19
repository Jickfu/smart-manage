package sm.system.net;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

import java.util.regex.Pattern;

/**
 * 只接受 IPv4/IPv6 字面量及单一 CIDR 的不可变地址范围。
 * 第三方库支持的通配符、顺序范围和主机名不属于 Smart Manage 配置协议。
 */
public final class IpAddressRange {
    private static final Pattern IPV4_LITERAL = Pattern.compile(
            "(?:0|[1-9][0-9]{0,2})(?:\\.(?:0|[1-9][0-9]{0,2})){3}");
    private static final Pattern IPV6_LITERAL = Pattern.compile("[0-9a-fA-F:.]+");

    private final IPAddress prefixBlock;
    private final boolean prefixSpecified;

    private IpAddressRange(IPAddress prefixBlock, boolean prefixSpecified) {
        this.prefixBlock = prefixBlock;
        this.prefixSpecified = prefixSpecified;
    }

    public static IpAddressRange parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("IP 地址或 CIDR 不能为空");
        }
        String candidate = value.trim();
        String[] parts = candidate.split("/", -1);
        if (parts.length > 2 || !isAddressLiteralSyntax(parts[0])) {
            throw invalid(value);
        }
        IPAddress address = parseAddress(parts[0], value);
        int prefixLength = address.getBitCount();
        if (parts.length == 2) {
            if (parts[1].isEmpty()) {
                throw invalid(value);
            }
            try {
                prefixLength = Integer.parseInt(parts[1]);
            } catch (NumberFormatException exception) {
                throw invalid(value, exception);
            }
        }
        if (prefixLength < 0 || prefixLength > address.getBitCount()) {
            throw invalid(value);
        }
        return new IpAddressRange(address.setPrefixLength(prefixLength).toPrefixBlock(), parts.length == 2);
    }

    public static boolean isAddressLiteral(String value) {
        if (value == null || value.isBlank() || !isAddressLiteralSyntax(value.trim())) {
            return false;
        }
        try {
            parseAddress(value.trim(), value);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public boolean contains(String addressLiteral) {
        if (addressLiteral == null || addressLiteral.isBlank()
                || !isAddressLiteralSyntax(addressLiteral.trim())) {
            return false;
        }
        try {
            IPAddress address = parseAddress(addressLiteral.trim(), addressLiteral);
            return prefixBlock.contains(address);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public String normalized() {
        return prefixSpecified
                ? prefixBlock.toCanonicalString()
                : prefixBlock.withoutPrefixLength().toCanonicalString();
    }

    private static IPAddress parseAddress(String candidate, String originalValue) {
        IPAddress address = new IPAddressString(candidate).getAddress();
        if (address == null || address.isMultiple()) {
            throw invalid(originalValue);
        }
        return address;
    }

    private static boolean isAddressLiteralSyntax(String candidate) {
        if (candidate.contains(":")) {
            return IPV6_LITERAL.matcher(candidate).matches();
        }
        return IPV4_LITERAL.matcher(candidate).matches();
    }

    private static IllegalArgumentException invalid(String value) {
        return new IllegalArgumentException("IP 地址或 CIDR 不合法: " + value);
    }

    private static IllegalArgumentException invalid(String value, Exception cause) {
        return new IllegalArgumentException("IP 地址或 CIDR 不合法: " + value, cause);
    }
}
