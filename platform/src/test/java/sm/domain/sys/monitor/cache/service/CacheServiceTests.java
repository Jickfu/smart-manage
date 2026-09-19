package sm.domain.sys.monitor.cache.service;

import com.alicp.jetcache.Cache;
import org.junit.jupiter.api.Test;
import sm.domain.sys.base.common.constant.BaseCacheName;
import sm.domain.sys.monitor.cache.model.form.CacheEntryKeyForm;
import sm.domain.sys.monitor.cache.model.form.CacheEntryListForm;
import sm.system.exception.BizException;
import sm.system.helper.CacheHelper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CacheServiceTests {
    private final CacheHelper cacheHelper = mock(CacheHelper.class);
    private final RedisCacheAccessor redisCacheAccessor = mock(RedisCacheAccessor.class);
    private final CacheService service = new CacheService(
            cacheHelper, redisCacheAccessor, JsonMapper.builder().build());

    @Test
    void overviewMustNotCreateCacheInstances() {
        service.overview();

        verify(cacheHelper, times(3)).findCache(any());
        verify(cacheHelper, never()).getCache(any(), any(), anyLong());
    }

    @Test
    void scopeTreeOnlyContainsRegisteredResources() {
        var tree = service.scopeTree();

        assertEquals(List.of("应用缓存", "基础设施缓存"), tree.stream().map(item -> item.getName()).toList());
        assertEquals(List.of("用户信息", "系统参数", "基础数据选项"),
                tree.getFirst().getChildren().stream().map(item -> item.getName()).toList());
    }

    @Test
    void applicationListScansOnlyRegisteredPrefixes() {
        when(redisCacheAccessor.scanEntries(any())).thenReturn(List.of(
                new RedisCacheAccessor.RedisEntry(BaseCacheName.SYS_PARAM + "all", "string", 60, 100L, true)));
        CacheEntryListForm form = form("APPLICATION");

        var result = service.listPage(form);

        assertEquals(List.of(BaseCacheName.SYS_PARAM + "all"),
                result.getRecords().stream().map(item -> item.getKey()).toList());
        verify(redisCacheAccessor).scanEntries(argThat(prefixes -> prefixes.size() == 3 && !prefixes.contains("")));
    }

    @Test
    void sensitiveInfrastructureCategoryDoesNotScanRedis() {
        CacheEntryListForm form = form("INFRASTRUCTURE");
        form.setResourceKey("login-sessions");

        assertEquals(0, service.listPage(form).getTotal());

        verifyNoInteractions(redisCacheAccessor);
    }

    @Test
    @SuppressWarnings("unchecked")
    void managedValueUsesExistingCacheWithoutCreatingIt() {
        Cache<Object, Object> cache = mock(Cache.class);
        when(cacheHelper.findCache(BaseCacheName.SYS_PARAM)).thenReturn(cache);
        when(cache.get("all")).thenReturn(Map.of("timeout", "30"));
        CacheEntryKeyForm form = new CacheEntryKeyForm();
        form.setStorage("REDIS");
        form.setCacheName(BaseCacheName.SYS_PARAM);
        form.setKey(BaseCacheName.SYS_PARAM + "all");

        assertEquals("object", service.value(form).getType());
        verify(cacheHelper, never()).getCache(any(), any(), anyLong());
    }

    @Test
    void infrastructureValueWithoutCacheNameUsesRawRedisAccessor() {
        CacheEntryKeyForm form = new CacheEntryKeyForm();
        form.setStorage("REDIS");
        form.setKey("sm:monitor:instances");

        service.value(form);

        verify(redisCacheAccessor).value("sm:monitor:instances");
    }

    @Test
    void deletingInfrastructureKeyIsRejected() {
        CacheEntryKeyForm form = new CacheEntryKeyForm();
        form.setStorage("REDIS");
        form.setKey("sm:monitor:instances");

        assertThrows(BizException.class, () -> service.delete(List.of(form)));
        verify(redisCacheAccessor, never()).delete(any());
    }

    private CacheEntryListForm form(String scopeType) {
        CacheEntryListForm form = new CacheEntryListForm();
        form.setPageNum(1);
        form.setPageSize(20);
        form.setScopeType(scopeType);
        return form;
    }
}
