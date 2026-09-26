package sm.domain.sys.base.attachment.service;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.attachment.mapper.AttachmentMapper;
import sm.domain.sys.base.attachment.mapper.BizAttachmentMapper;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;
import sm.domain.sys.base.attachment.model.entity.BizAttachmentEntity;
import sm.domain.sys.base.attachment.contract.AttachmentPromoteCommand;
import sm.system.exception.BizException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import sm.system.resource.BusinessResourceAction;
import sm.system.response.ResultEnum;

class AttachmentTxServiceTests {

	private final AttachmentMapper mapper = mock(AttachmentMapper.class);
	private final BizAttachmentMapper bizMapper = mock(BizAttachmentMapper.class);
	private final sm.system.resource.BusinessResourceRegistry resources = mock(sm.system.resource.BusinessResourceRegistry.class);
	private final AttachmentTxService txService = new AttachmentTxService(mapper, bizMapper, resources);

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
		AttachmentPromoteCommand form = new AttachmentPromoteCommand();
		form.setAttachmentIds(List.of(1L));
		form.setBizType("test-document");
		form.setBizId("100");
		when(mapper.selectById(1L)).thenReturn(entity);
		when(mapper.selectForUpdate(1L)).thenReturn(entity);
		BizAttachmentEntity mapping = new BizAttachmentEntity();
		mapping.setBizType("test-document");
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
		AttachmentPromoteCommand form = new AttachmentPromoteCommand();
		form.setAttachmentIds(List.of(1L));
		form.setBizType("sys.base.ui-config");
		form.setBizId("100");
		when(mapper.selectById(1L)).thenReturn(entity);
		when(mapper.selectForUpdate(1L)).thenReturn(entity);

		assertThrows(BizException.class, () -> txService.promote(form));
	}

	@Test
	void promoteRejectsAttachmentWithoutUploadMapping() {
		AttachmentEntity entity = new AttachmentEntity();
		entity.setId(1L);
		entity.setStatus("TEMP");
		AttachmentPromoteCommand form = new AttachmentPromoteCommand();
		form.setAttachmentIds(List.of(1L));
		form.setBizType("sys.base.ui-config");
		form.setBizId("100");
		when(mapper.selectById(1L)).thenReturn(entity);
		when(mapper.selectForUpdate(1L)).thenReturn(entity);
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
		when(mapper.selectForUpdate(1L)).thenReturn(entity);
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
		when(mapper.selectForUpdate(1L)).thenReturn(entity);
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
		when(bizMapper.selectOne(any())).thenReturn(mapping);
		AttachmentEntity lockedAttachment = new AttachmentEntity();
		lockedAttachment.setStatus("ACTIVE");
		when(mapper.selectForUpdate(1L)).thenReturn(lockedAttachment);
		when(bizMapper.updateById(mapping)).thenReturn(1);

		txService.updateRemark(2L, 1L, "  测试合同  ");

		assertEquals("测试合同", mapping.getRemark());
		verify(bizMapper).updateById(mapping);
	}

	@Test
	void updateRemarkRejectsMismatchedBusinessAttachment() {
		BizAttachmentEntity mapping = new BizAttachmentEntity();
		mapping.setId(2L);
		mapping.setAttachmentId(99L);
		when(bizMapper.selectOne(any())).thenReturn(mapping);
		AttachmentEntity lockedAttachment = new AttachmentEntity();
		lockedAttachment.setStatus("ACTIVE");
		when(mapper.selectForUpdate(1L)).thenReturn(lockedAttachment);

		assertThrows(BizException.class, () -> txService.updateRemark(2L, 1L, "备注"));

		verify(bizMapper, never()).updateById(mapping);
	}

	@Test
	void aggregateFreezeRejectsAttachmentWritesBeforeMetadataChanges() {
		AttachmentEntity entity = new AttachmentEntity();
		entity.setId(1L);
		entity.setStatus("TEMP");
		BizAttachmentEntity mapping = new BizAttachmentEntity();
		mapping.setId(2L);
		mapping.setAttachmentId(1L);
		mapping.setBizType("test-document");
		when(mapper.selectById(1L)).thenReturn(entity);
		when(mapper.selectForUpdate(1L)).thenReturn(entity);
		when(bizMapper.selectOne(any())).thenReturn(mapping);
		when(bizMapper.selectOne(any())).thenReturn(mapping);
		AttachmentEntity lockedAttachment = new AttachmentEntity();
		lockedAttachment.setStatus("ACTIVE");
		when(mapper.selectForUpdate(1L)).thenReturn(lockedAttachment);
		AttachmentPromoteCommand command = new AttachmentPromoteCommand();
		command.setAttachmentIds(List.of(1L));
		command.setBizType("test-document");
		command.setBizId("100");
		doThrow(new BizException(ResultEnum.DATA_CONFLICT, "单据已冻结"))
				.when(resources).beforeAttachmentMutation("test-document", "100", 1L, BusinessResourceAction.ATTACH);

		assertThrows(BizException.class, () -> txService.promote(command));
		assertEquals("TEMP", entity.getStatus());
		mapping.setBizId("100");
		assertThrows(BizException.class, () -> txService.updateRemark(2L, 1L, "新备注"));
		doThrow(new BizException(ResultEnum.DATA_CONFLICT, "单据已冻结"))
				.when(resources).beforeAttachmentMutation("test-document", "100", 1L, BusinessResourceAction.DELETE);
		assertThrows(BizException.class, () -> txService.markPendingDelete(1L));
		verify(mapper, never()).updateById(entity);
		verify(bizMapper, never()).updateById(mapping);
		verify(bizMapper, never()).delete(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
	}
}
