package sm.domain.sys.base.sysparam.mapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SysParamMapperXmlTests {

    @Test
    void scopeQueriesDeriveApplicationAndDomainFromFeature() throws IOException {
        String mapperXml = Files.readString(Path.of(
                "src/main/resources/mapper/sys/base/sysparam/SysParamMapper.xml"));

        assertTrue(mapperXml.contains("LEFT JOIN t_sys_feature b ON b.id = a.feature_id"));
        assertTrue(mapperXml.contains("LEFT JOIN t_sys_app c ON c.id = b.app_id"));
        assertTrue(mapperXml.contains("b.app_id = #{form.appId}"));
        assertTrue(mapperXml.contains("c.domain_id = #{form.domainId}"));
        assertTrue(mapperXml.contains("a.feature_id IS NULL"));
    }
}
