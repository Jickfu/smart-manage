package sm.system.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.system.util.ServletUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/** 按受信代理边界解析客户端地址，禁止无条件信任请求头。 */
@Component
@RequiredArgsConstructor
public class ClientIpResolver {
    private final TrustedProxyProperties properties;

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
        for (String cidr : properties.getCidrs()) {
            if (contains(cidr, address)) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(String cidr, String address) {
        try {
            String[] parts = cidr.trim().split("/", 2);
            InetAddress network = InetAddress.getByName(parts[0]);
            InetAddress candidate = InetAddress.getByName(address);
            byte[] networkBytes = network.getAddress();
            byte[] candidateBytes = candidate.getAddress();
            if (networkBytes.length != candidateBytes.length) {
                return false;
            }
            int prefixLength = parts.length == 2 ? Integer.parseInt(parts[1]) : networkBytes.length * 8;
            if (prefixLength < 0 || prefixLength > networkBytes.length * 8) {
                return false;
            }
            int fullBytes = prefixLength / 8;
            int remainingBits = prefixLength % 8;
            for (int index = 0; index < fullBytes; index++) {
                if (networkBytes[index] != candidateBytes[index]) {
                    return false;
                }
            }
            if (remainingBits == 0) {
                return true;
            }
            int mask = 0xff << (8 - remainingBits);
            return (networkBytes[fullBytes] & mask) == (candidateBytes[fullBytes] & mask);
        } catch (UnknownHostException | NumberFormatException exception) {
            return false;
        }
    }
}
