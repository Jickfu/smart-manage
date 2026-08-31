package sm.system.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import sm.system.net.IpAddressRange;
import sm.system.util.ServletUtil;

import java.util.List;

/** 按受信代理边界解析客户端地址，禁止无条件信任请求头。 */
@Component
public class ClientIpResolver {
    private final List<IpAddressRange> trustedCidrs;

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
            if (!IpAddressRange.isAddressLiteral(address)) {
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
        if (!IpAddressRange.isAddressLiteral(address)) {
            return false;
        }
        for (IpAddressRange cidr : trustedCidrs) {
            if (cidr.contains(address)) {
                return true;
            }
        }
        return false;
    }

    private IpAddressRange parseCidr(String cidr) {
        try {
            return IpAddressRange.parse(cidr);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("受信代理 CIDR 格式不合法: " + cidr, exception);
        }
    }
}
