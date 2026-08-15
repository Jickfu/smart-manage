package sm.domain.sys.base.numberrule.mapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NumberRuleMapperXmlTests {

    @Test
    void counterAdvanceUsesPostgresqlAtomicUpsertAndReturning() throws IOException {
        String mapperXml = Files.readString(Path.of(
                "src/main/resources/mapper/sys/base/numberrule/NumberRuleMapper.xml"));

        assertTrue(mapperXml.contains("ON CONFLICT (rule_key, scope_key, period_key)"));
        assertTrue(mapperXml.contains("current_value = t_sys_number_counter.current_value + 1"));
        assertTrue(mapperXml.contains("RETURNING current_value"));
    }
}
