package sm.domain.sys.base.basicdata.service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import sm.domain.sys.base.basicdata.mapper.BasicDataEntryMapper;
import sm.domain.sys.base.basicdata.mapper.BasicDataMapper;
import sm.domain.sys.base.basicdata.model.entity.BasicDataEntity;
import sm.domain.sys.base.common.constant.CacheConstant;
import sm.system.helper.CacheHelper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BasicDataTxServiceTests {

	private final BasicDataMapper mapper = mock(BasicDataMapper.class);
	private final CacheHelper cacheHelper = mock(CacheHelper.class);
	@SuppressWarnings("unchecked")
	private final Cache<Object, Object> cache = mock(Cache.class);
	private final BasicDataTxService txService =
			new BasicDataTxService(mapper, mock(BasicDataEntryMapper.class), cacheHelper);

	@BeforeAll
	static void initializeMybatisMetadata() {
		TableInfoHelper.initTableInfo(
				new MapperBuilderAssistant(new MybatisConfiguration(), "basic-data-test"),
				BasicDataEntity.class);
	}

	@AfterEach
	void clearTransactionSynchronization() {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
		TransactionSynchronizationManager.setActualTransactionActive(false);
	}

	@Test
	void enabledChangeInvalidatesAffectedLocalCachesOnlyAfterCommit() {
		BasicDataEntity first = new BasicDataEntity();
		first.setNumber("gender");
		BasicDataEntity second = new BasicDataEntity();
		second.setNumber("currency");
		when(mapper.selectByIds(List.of(1L, 2L))).thenReturn(List.of(first, second));
		when(mapper.selectCount(any())).thenReturn(2L);
		when(mapper.update(any())).thenReturn(2);
		when(cacheHelper.getCache(CacheConstant.BASIC_DATA_OPTIONS, CacheType.LOCAL))
				.thenReturn(cache);
		TransactionSynchronizationManager.setActualTransactionActive(true);
		TransactionSynchronizationManager.initSynchronization();

		txService.updateEnabled(List.of(1L, 2L), false);

		verify(cache, never()).remove(any());
		for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
			synchronization.afterCommit();
		}
		verify(cache).remove("gender");
		verify(cache).remove("currency");
	}
}
