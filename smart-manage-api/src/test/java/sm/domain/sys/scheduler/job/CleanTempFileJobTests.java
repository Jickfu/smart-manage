package sm.domain.sys.scheduler.job;

import org.junit.jupiter.api.Test;
import org.quartz.JobExecutionException;
import sm.domain.sys.base.attachment.service.AttachmentService;
import sm.domain.sys.base.fileartifact.service.FileArtifactService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CleanTempFileJobTests {

	@Test
    void cleanupJobProvidesCreationMetadata() {
        SchedulerJobDefinition definition =
                CleanTempFileJob.class.getAnnotation(SchedulerJobDefinition.class);
        assertTrue(definition.description().contains("临时附件"));
        assertTrue(definition.parameterTemplate().equals("{}"));
	}

	@Test
	void cleanupDelegatesToAttachmentLifecycleService() throws JobExecutionException {
		AttachmentService attachmentService = mock(AttachmentService.class);
		FileArtifactService fileArtifactService = mock(FileArtifactService.class);
		CleanTempFileJob job = new CleanTempFileJob(attachmentService, fileArtifactService);

		job.executeInternal(null);

		verify(attachmentService).cleanupExpiredAndPending();
		verify(fileArtifactService).cleanupExpiredAndPending();
	}

	@Test
	void cleanupFailureIsVisibleToQuartz() {
		AttachmentService attachmentService = mock(AttachmentService.class);
		when(attachmentService.cleanupExpiredAndPending()).thenReturn(2);
		CleanTempFileJob job = new CleanTempFileJob(attachmentService, mock(FileArtifactService.class));

		assertThrows(JobExecutionException.class, () -> job.executeInternal(null));
	}
}
