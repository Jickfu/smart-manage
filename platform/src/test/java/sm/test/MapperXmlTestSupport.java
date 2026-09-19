package sm.test;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Mapper XML 测试的资源加载入口，不共享配置或隐式加载公共片段。 */
public final class MapperXmlTestSupport {
    private MapperXmlTestSupport() {
    }

    public static MybatisConfiguration load(String... resources) {
        MybatisConfiguration configuration = new MybatisConfiguration();
        for (String resource : resources) {
            try (InputStream input = MapperXmlTestSupport.class.getClassLoader().getResourceAsStream(resource)) {
                assertNotNull(input, "Mapper XML 不存在：" + resource);
                new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
            } catch (IOException exception) {
                throw new UncheckedIOException("关闭 Mapper XML 失败：" + resource, exception);
            }
        }
        return configuration;
    }
}
