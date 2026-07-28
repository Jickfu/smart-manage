package sm.domain.sys.monitor.job.job;

import org.junit.jupiter.api.Test;
import org.quartz.DisallowConcurrentExecution;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CleanTempFileJobTests {

	@Test
	void cleanupJobDisallowsConcurrentExecutionForSameJobKey() {
		assertTrue(CleanTempFileJob.class.isAnnotationPresent(DisallowConcurrentExecution.class));
	}
}
