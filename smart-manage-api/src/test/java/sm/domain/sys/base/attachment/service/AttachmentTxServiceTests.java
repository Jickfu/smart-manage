package sm.domain.sys.base.attachment.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import sm.domain.sys.base.attachment.mapper.AttachmentMapper;
import sm.domain.sys.base.attachment.mapper.BizAttachmentMapper;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;
import sm.domain.sys.base.attachment.model.entity.BizAttachmentEntity;
import sm.domain.sys.base.attachment.contract.model.form.AttachmentPromoteForm;
import sm.system.exception.BizException;
import sm.system.storage.FileStorageService;
import sm.system.storage.FileStorageServiceFactory;
import sm.system.storage.FileStoreResult;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
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
		when(file.getInputStream()).thenAnswer(ignored -> new ByteArrayInputStream(
				"test".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
		when(storageFactory.getService()).thenReturn(storage);
		when(storage.store("asset/sys/base/ui-config", file)).thenReturn(
				FileStoreResult.of("stored.txt", "asset/sys/stored.txt", 10));
		when(mapper.insert(any(AttachmentEntity.class))).thenReturn(0);

		assertThrows(BizException.class, () -> txService.upload(file, "sys.base.ui-config", "asset/sys/base/ui-config", 24));

		verify(storage).delete("asset/sys/stored.txt");
	}

	@Test
	void uploadDeletesStoredFileWhenHashReadFails() throws IOException {
		MultipartFile file = mock(MultipartFile.class);
		when(file.getOriginalFilename()).thenReturn("broken.txt");
		when(file.getContentType()).thenReturn("text/plain");
		when(file.getInputStream()).thenThrow(new IOException("read failed"));
		when(storageFactory.getService()).thenReturn(storage);
		when(storage.store(anyString(), same(file)))
				.thenReturn(FileStoreResult.of("broken.txt", "biz/scm/broken.txt", 10L));

		assertThrows(IOException.class, () -> txService.upload(file, "scm.procurement.purchase-requisition",
				"biz/scm/procurement", 24));

		verify(storage).delete("biz/scm/broken.txt");
		verify(mapper, never()).insert(any(AttachmentEntity.class));
	}

	@Test
	void promoteKeepsStableObjectKeyWhenDatabaseUpdateFails() {
		AttachmentEntity entity = new AttachmentEntity();
		entity.setId(1L);
		entity.setObjectKey("temp/stored.txt");
		entity.setStorageType("LOCAL");
		entity.setStatus("TEMP");
		AttachmentPromoteForm form = new AttachmentPromoteForm();
		form.setAttachmentIds(List.of(1L));
		form.setBizType("purchase-requisition");
		form.setBizId("100");
		when(mapper.selectById(1L)).thenReturn(entity);
		BizAttachmentEntity mapping = new BizAttachmentEntity();
		mapping.setBizType("purchase-requisition");
		when(bizMapper.selectOne(any())).thenReturn(mapping);
		when(mapper.updateById(entity)).thenReturn(0);

		assertThrows(BizException.class, () -> txService.promote(form));

		assertEquals("temp/stored.txt", entity.getObjectKey());
	}

	@Test
	void promoteRejectsDeletedAttachment() {
		AttachmentEntity entity = new AttachmentEntity();
		entity.setId(1L);
		entity.setStatus("DELETED");
		AttachmentPromoteForm form = new AttachmentPromoteForm();
		form.setAttachmentIds(List.of(1L));
		form.setBizType("sys.base.ui-config");
		form.setBizId("100");
		when(mapper.selectById(1L)).thenReturn(entity);

		assertThrows(BizException.class, () -> txService.promote(form));
	}

	@Test
	void promoteRejectsAttachmentWithoutUploadMapping() {
		AttachmentEntity entity = new AttachmentEntity();
		entity.setId(1L);
		entity.setStatus("TEMP");
		AttachmentPromoteForm form = new AttachmentPromoteForm();
		form.setAttachmentIds(List.of(1L));
		form.setBizType("sys.base.ui-config");
		form.setBizId("100");
		when(mapper.selectById(1L)).thenReturn(entity);
		when(bizMapper.selectOne(any())).thenReturn(null);

		assertThrows(BizException.class, () -> txService.promote(form));
	}

	@Test
	void deleteRemovesPhysicalFileOnlyAfterDatabaseCommit() throws IOException {
		AttachmentEntity entity = new AttachmentEntity();
		entity.setId(1L);
		entity.setObjectKey("sys/stored.txt");
		entity.setObjectKey("sys/stored.txt");
		entity.setStorageType("LOCAL");
		when(mapper.selectById(1L)).thenReturn(entity);
		when(mapper.updateById(entity)).thenReturn(1);
		when(storageFactory.getService("LOCAL")).thenReturn(storage);
		TransactionSynchronizationManager.setActualTransactionActive(true);
		TransactionSynchronizationManager.initSynchronization();

		txService.delete(1L);

		verify(storage, never()).delete("sys/stored.txt");
		for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
			synchronization.afterCommit();
		}
		verify(storage).delete("sys/stored.txt");
	}

	@Test
	void updateRemarkTrimsAndPersistsBusinessAttachmentRemark() {
		BizAttachmentEntity mapping = new BizAttachmentEntity();
		mapping.setId(2L);
		mapping.setAttachmentId(1L);
		when(bizMapper.selectById(2L)).thenReturn(mapping);
		when(bizMapper.updateById(mapping)).thenReturn(1);

		txService.updateRemark(2L, 1L, "  采购合同  ");

		assertEquals("采购合同", mapping.getRemark());
		verify(bizMapper).updateById(mapping);
	}

	@Test
	void updateRemarkRejectsMismatchedBusinessAttachment() {
		BizAttachmentEntity mapping = new BizAttachmentEntity();
		mapping.setId(2L);
		mapping.setAttachmentId(99L);
		when(bizMapper.selectById(2L)).thenReturn(mapping);

		assertThrows(BizException.class, () -> txService.updateRemark(2L, 1L, "备注"));

		verify(bizMapper, never()).updateById(mapping);
	}
}
