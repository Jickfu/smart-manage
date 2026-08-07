package sm.system.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import sm.system.util.ServletUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.regex.Pattern;

/** 按受信代理边界解析客户端地址，禁止无条件信任请求头。 */
@Component
public class ClientIpResolver {
    private static final Pattern IPV6_LITERAL = Pattern.compile("[0-9a-fA-F:.]+");
    private final List<CidrBlock> trustedCidrs;

    public ClientIpResolver(TrustedProxyProperties properties) {
        this.trustedCidrs = properties.getCidrs().stream().map(this::parseCidr).toList();
    }

    public String resolveCurrentRequest() {
        return resolve(ServletUtil.getRequest());
    }

    public String resolve(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr();
        if (!isTrusted(remoteAddress)) {
            return remoteAddress;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return remoteAddress;
        }
        String[] addresses = forwardedFor.split(",");
        for (int index = addresses.length - 1; index >= 0; index--) {
            String address = addresses[index].trim();
            // 整条转发链必须由 IP 字面量组成，避免外部输入触发名称解析或产生歧义。
            if (parseAddressLiteral(address) == null) {
                return remoteAddress;
            }
            if (!address.isBlank() && !isTrusted(address)) {
                return address;
            }
        }
        return addresses[0].trim();
    }

    private boolean isTrusted(String address) {
        if (address == null || address.isBlank()) {
            return false;
        }
        InetAddress candidate = parseAddressLiteral(address);
        if (candidate == null) {
            return false;
        }
        for (CidrBlock cidr : trustedCidrs) {
            if (cidr.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private CidrBlock parseCidr(String cidr) {
        try {
            String[] parts = cidr.trim().split("/", 2);
            InetAddress network = parseAddressLiteral(parts[0]);
            if (network == null) {
                throw new IllegalArgumentException("受信代理 CIDR 必须使用 IP 字面量: " + cidr);
            }
            byte[] networkBytes = network.getAddress();
            int prefixLength = parts.length == 2 ? Integer.parseInt(parts[1]) : networkBytes.length * 8;
            if (prefixLength < 0 || prefixLength > networkBytes.length * 8) {
                throw new IllegalArgumentException("受信代理 CIDR 前缀长度不合法: " + cidr);
            }
            return new CidrBlock(networkBytes, prefixLength);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("受信代理 CIDR 格式不合法: " + cidr, exception);
        }
    }

    private InetAddress parseAddressLiteral(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }
        String candidate = address.trim();
        if (candidate.contains(":")) {
            if (!IPV6_LITERAL.matcher(candidate).matches()) {
                return null;
            }
        } else {
            String[] octets = candidate.split("\\.", -1);
            if (octets.length != 4) {
                return null;
            }
            for (String octet : octets) {
                try {
                    if (octet.isEmpty() || Integer.parseInt(octet) > 255) {
                        return null;
                    }
                } catch (NumberFormatException exception) {
                    return null;
                }
            }
        }
        try {
            return InetAddress.getByName(candidate);
        } catch (UnknownHostException exception) {
            return null;
        }
    }

    private record CidrBlock(byte[] networkBytes, int prefixLength) {
        private boolean contains(InetAddress candidate) {
            byte[] candidateBytes = candidate.getAddress();
            if (networkBytes.length != candidateBytes.length) return false;
            int fullBytes = prefixLength / 8;
            int remainingBits = prefixLength % 8;
            for (int index = 0; index < fullBytes; index++) {
                if (networkBytes[index] != candidateBytes[index]) return false;
            }
            if (remainingBits == 0) return true;
            int mask = 0xff << (8 - remainingBits);
            return (networkBytes[fullBytes] & mask) == (candidateBytes[fullBytes] & mask);
        }
    }
}
