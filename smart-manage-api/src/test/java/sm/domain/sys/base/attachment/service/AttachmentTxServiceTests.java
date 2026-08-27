package sm.domain.sys.base.attachment.service;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.attachment.mapper.AttachmentMapper;
import sm.domain.sys.base.attachment.mapper.BizAttachmentMapper;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;
import sm.domain.sys.base.attachment.model.entity.BizAttachmentEntity;
import sm.domain.sys.base.attachment.contract.model.form.AttachmentPromoteForm;
import sm.system.exception.BizException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AttachmentTxServiceTests {

	private final AttachmentMapper mapper = mock(AttachmentMapper.class);
	private final BizAttachmentMapper bizMapper = mock(BizAttachmentMapper.class);
	private final AttachmentTxService txService = new AttachmentTxService(mapper, bizMapper);

	@Test
	void persistUploadRejectsMetadataInsertFailure() {
		when(mapper.insert(any(AttachmentEntity.class))).thenReturn(0);

		assertThrows(BizException.class, () -> txService.persistUpload(
				"test.txt", "asset/sys/stored.txt", 10, "text/plain", ".txt",
				"LOCAL", "sha256", "sys.base.ui-config", 24));
	}

	@Test
	void persistUploadRejectsTemporaryMappingFailure() {
		when(mapper.insert(any(AttachmentEntity.class))).thenReturn(1);
		when(bizMapper.insert(any(BizAttachmentEntity.class))).thenReturn(0);

		assertThrows(BizException.class, () -> txService.persistUpload(
				"test.txt", "asset/sys/stored.txt", 10, "text/plain", ".txt",
				"LOCAL", "sha256", "sys.base.ui-config", 24));
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
	void deleteOnlyMarksPendingAndReturnsStableStorageTarget() {
		AttachmentEntity entity = new AttachmentEntity();
		entity.setId(1L);
		entity.setObjectKey("sys/stored.txt");
		entity.setObjectKey("sys/stored.txt");
		entity.setStorageType("LOCAL");
		when(mapper.selectById(1L)).thenReturn(entity);
		when(mapper.updateById(entity)).thenReturn(1);

		AttachmentDeletionTarget target = txService.markPendingDelete(1L);

		assertEquals("PENDING_DELETE", entity.getStatus());
		assertEquals("sys/stored.txt", target.objectKey());
		verify(mapper).updateById(entity);
	}

	@Test
	void markDeletedRequiresPendingStateAndChecksUpdateResult() {
		AttachmentEntity entity = new AttachmentEntity();
		entity.setId(1L);
		entity.setStatus("PENDING_DELETE");
		when(mapper.selectById(1L)).thenReturn(entity);
		when(mapper.updateById(entity)).thenReturn(1);

		txService.markDeleted(1L);

		assertEquals("DELETED", entity.getStatus());
		verify(mapper).updateById(entity);
	}

	@Test
	void markDeletedAlwaysUsesIndependentTransaction() throws Exception {
		Transactional transactional = AttachmentTxService.class
				.getDeclaredMethod("markDeleted", Long.class)
				.getAnnotation(Transactional.class);

		assertEquals(Propagation.REQUIRES_NEW, transactional.propagation());
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
