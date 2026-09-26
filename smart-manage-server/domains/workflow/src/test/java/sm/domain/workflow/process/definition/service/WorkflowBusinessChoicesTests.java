package sm.domain.workflow.process.definition.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import sm.domain.sys.base.feature.contract.FeatureDirectoryReader;
import sm.domain.sys.base.feature.contract.FeatureDirectoryReference;
import sm.domain.workflow.process.definition.mapper.WorkflowBindingMapper;
import sm.domain.workflow.process.definition.model.entity.WorkflowBindingEntity;
import sm.domain.workflow.process.definition.model.form.DefinitionListForm;
import sm.domain.workflow.process.runtime.contract.WorkflowBusiness;
import sm.domain.workflow.process.runtime.service.WorkflowBusinessRegistry;
import sm.system.exception.BizException;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkflowBusinessChoicesTests {
    @BeforeAll
    static void initializeTableMetadata() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "workflow-binding-test"),
                WorkflowBindingEntity.class);
    }

    @Test
    void explicitFeatureControlsDirectoryEvenWhenBusinessKeyHasDifferentPrefixes() {
        var provider = provider("custom/other/approval", "real/office/document");
        var directory = mock(FeatureDirectoryReader.class);
        when(directory.findByFeatureKeys(List.of("real/office/document"))).thenReturn(List.of(
                new FeatureDirectoryReference("real/office/document", 10L, "真实领域", 20L, "真实应用")));
        var service = new DefinitionService(null, null, null, new WorkflowBusinessRegistry(List.of(provider)), null, directory);
        var choices = service.businessTypes();
        assertEquals(1, choices.size());
        var choice = choices.getFirst();
        assertEquals("custom/other/approval", choice.key());
        assertEquals("测试业务", choice.name());
        assertEquals(10L, choice.domainId());
        assertEquals("真实领域", choice.domainName());
        assertEquals(20L, choice.appId());
        assertEquals("真实应用", choice.appName());
    }

    @Test
    void emptyAssemblyHasNoChoicesAndMissingCatalogIsAnExplicitConfigurationError() {
        var directory = mock(FeatureDirectoryReader.class);
        when(directory.findByFeatureKeys(anyList())).thenReturn(List.of());
        var empty = new DefinitionService(null, null, null, new WorkflowBusinessRegistry(List.of()), null, directory);
        assertTrue(empty.businessTypes().isEmpty());
        var configured = new DefinitionService(null, null, null,
                new WorkflowBusinessRegistry(List.of(provider("approval", "missing-feature"))), null, directory);
        assertThrows(BizException.class, configured::businessTypes);
        assertThrows(IllegalStateException.class, () -> new WorkflowBusinessRegistry(List.of(provider("approval", ""))));
    }

    @Test
    void listScopeUsesExplicitDirectoryAssociationBeforeDatabasePagination() {
        var first = provider("business-one", "feature-one");
        var second = provider("business-two", "feature-two");
        var directory = mock(FeatureDirectoryReader.class);
        when(directory.findByFeatureKeys(List.of("feature-one", "feature-two"))).thenReturn(List.of(
                new FeatureDirectoryReference("feature-one", 10L, "领域", 20L, "应用一"),
                new FeatureDirectoryReference("feature-two", 10L, "领域", 21L, "应用二")));
        var mapper = mock(WorkflowBindingMapper.class);
        var captured = new AtomicReference<LambdaQueryWrapper<WorkflowBindingEntity>>();
        doAnswer(invocation -> {
            captured.set(invocation.getArgument(1));
            return new Page<WorkflowBindingEntity>(1, 20);
        }).when(mapper).selectPage(any(), any());
        var service = new DefinitionService(mapper, null, null,
                new WorkflowBusinessRegistry(List.of(first, second)), null, directory);
        var form = new DefinitionListForm();
        form.setAppId(20L);
        form.setKeyword(" leave ");

        service.listPage(form);

        var query = captured.get();
        assertTrue(query.getSqlSegment().contains("business_type IN"));
        assertTrue(query.getSqlSegment().contains("number LIKE"));
        assertTrue(query.getSqlSegment().contains("name LIKE"));
        assertTrue(query.getParamNameValuePairs().containsValue("business-one"));
        assertFalse(query.getParamNameValuePairs().containsValue("business-two"));
        assertTrue(query.getParamNameValuePairs().containsValue("%leave%"));
    }

    @Test
    void unknownDirectoryScopeCannotFallBackToAllDefinitions() {
        var directory = mock(FeatureDirectoryReader.class);
        when(directory.findByFeatureKeys(List.of("feature-one"))).thenReturn(List.of(
                new FeatureDirectoryReference("feature-one", 10L, "领域", 20L, "应用")));
        var mapper = mock(WorkflowBindingMapper.class);
        var captured = new AtomicReference<LambdaQueryWrapper<WorkflowBindingEntity>>();
        doAnswer(invocation -> {
            captured.set(invocation.getArgument(1));
            return new Page<WorkflowBindingEntity>(1, 20);
        }).when(mapper).selectPage(any(), any());
        var service = new DefinitionService(mapper, null, null,
                new WorkflowBusinessRegistry(List.of(provider("business-one", "feature-one"))), null, directory);
        var form = new DefinitionListForm();
        form.setAppId(999L);

        service.listPage(form);

        assertTrue(captured.get().getSqlSegment().contains("FALSE"));
    }

    private WorkflowBusiness provider(String key, String featureKey) {
        var provider = mock(WorkflowBusiness.class);
        when(provider.key()).thenReturn(key);
        when(provider.name()).thenReturn("测试业务");
        when(provider.featureKey()).thenReturn(featureKey);
        return provider;
    }
}
