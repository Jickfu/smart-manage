package sm.domain.scm.procurement.purchaserequisition.mapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.junit.jupiter.api.Test;
import sm.system.datascope.DataScope;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PurchaseRequisitionMapperXmlTests {
    private static final String MAPPER_RESOURCE =
            "mapper/scm/procurement/purchaserequisition/PurchaseRequisitionMapper.xml";

    @Test
    void statusCountUsesOrganizationAndApplicantUnion() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        InputStream mapperInput = getClass().getClassLoader().getResourceAsStream(MAPPER_RESOURCE);
        assertNotNull(mapperInput, "采购申请 Mapper XML 不存在");
        new XMLMapperBuilder(mapperInput, configuration, MAPPER_RESOURCE, configuration.getSqlFragments()).parse();
        DataScope scope = new DataScope(false, true, Set.of(10L, 11L), 20L);
        BoundSql boundSql = configuration
                .getMappedStatement(PurchaseRequisitionMapper.class.getName() + ".selectStatusCounts")
                .getBoundSql(Map.of("scope", scope));
        String sql = boundSql.getSql().replaceAll("\\s+", " ");

        assertTrue(sql.contains("a.org_id IN"));
        assertTrue(sql.contains("OR a.applicant_id = ?"));
        assertTrue(sql.contains("GROUP BY a.bill_status"));
    }
}
