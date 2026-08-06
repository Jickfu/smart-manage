package sm.domain.sys.scheduler.job;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CleanTempFileJobTests {

	@Test
    void cleanupJobProvidesCreationMetadata() {
        SchedulerJobDefinition definition =
                CleanTempFileJob.class.getAnnotation(SchedulerJobDefinition.class);
        assertTrue(definition.description().contains("临时附件"));
        assertTrue(definition.parameterTemplate().equals("{}"));
	}
}
