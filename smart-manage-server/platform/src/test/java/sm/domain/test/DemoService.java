package sm.domain.test;

import java.util.Map;

/** 脚本服务网关测试用的公开领域 Service。 */
public class DemoService {
    public Map<String, String> echo(EchoForm form) {
        return Map.of("message", form.name());
    }

    public record EchoForm(String name) {
    }
}
