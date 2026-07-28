package sm.domain.sys.base.attachment.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import sm.domain.sys.base.attachment.mapper.AttachmentMapper;
import sm.domain.sys.base.attachment.mapper.BizAttachmentMapper;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;
import sm.domain.sys.base.attachment.model.form.AttachmentPromoteForm;
import sm.system.exception.BizException;
import sm.system.storage.FileStorageService;
import sm.system.storage.FileStorageServiceFactory;
import sm.system.storage.FileStoreResult;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AttachmentTxServiceTests {

	private final AttachmentMapper mapper = mock(AttachmentMapper.class);
	private final BizAttachmentMapper bizMapper = mock(BizAttachmentMapper.class);
	private final FileStorageServiceFactory storageFactory = mock(FileStorageServiceFactory.class);
	private final FileStorageService storage = mock(FileStorageService.class);
	private final AttachmentTxService txService = new AttachmentTxService(mapper, bizMapper, storageFactory);

	@AfterEach
	void clearTransactionSynchronization() {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
		TransactionSynchronizationManager.setActualTransactionActive(false);
	}

	@Test
	void uploadDeletesStoredFileWhenMetadataInsertFails() throws IOException {
		MultipartFile file = mock(MultipartFile.class);
		when(file.getOriginalFilename()).thenReturn("test.txt");
		when(storageFactory.getService()).thenReturn(storage);
		when(storage.store("sys", file)).thenReturn(FileStoreResult.of("stored.txt", "sys/stored.txt", 10));
		when(mapper.insert(any(AttachmentEntity.class))).thenReturn(0);

		assertThrows(BizException.class, () -> txService.upload(file, null));

		verify(storage).delete("sys/stored.txt");
	}

	@Test
	void promoteMovesFileBackToTempWhenDatabaseUpdateFails() throws IOException {
		AttachmentEntity entity = new AttachmentEntity();
		entity.setId(1L);
		entity.setStoredPath("temp/stored.txt");
		entity.setIsTemp(true);
		AttachmentPromoteForm form = new AttachmentPromoteForm();
		form.setAttachmentIds(List.of(1L));
		form.setBizType("purchase-requisition");
		form.setBizId("100");
		when(storageFactory.getService()).thenReturn(storage);
		when(mapper.selectById(1L)).thenReturn(entity);
		when(storage.promote("temp/stored.txt", "biz/purchase-requisition"))
				.thenReturn("biz/purchase-requisition/stored.txt");
		when(mapper.updateById(entity)).thenReturn(0);

		assertThrows(BizException.class, () -> txService.promote(form));

		verify(storage).move("biz/purchase-requisition/stored.txt", "temp");
	}

	@Test
	void deleteRemovesPhysicalFileOnlyAfterDatabaseCommit() throws IOException {
		AttachmentEntity entity = new AttachmentEntity();
		entity.setId(1L);
		entity.setStoredPath("sys/stored.txt");
		when(mapper.selectById(1L)).thenReturn(entity);
		when(mapper.deleteById(1L)).thenReturn(1);
		when(storageFactory.getService()).thenReturn(storage);
		TransactionSynchronizationManager.setActualTransactionActive(true);
		TransactionSynchronizationManager.initSynchronization();

		txService.delete(1L);

		verify(storage, never()).delete("sys/stored.txt");
		for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
			synchronization.afterCommit();
		}
		verify(storage).delete("sys/stored.txt");
	}
}
