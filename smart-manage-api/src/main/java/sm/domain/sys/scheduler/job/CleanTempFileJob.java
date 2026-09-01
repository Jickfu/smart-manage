package sm.domain.sys.scheduler.job;

import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.attachment.service.AttachmentService;
import sm.domain.sys.base.fileartifact.service.FileArtifactService;

/** 按数据库生命周期清理共享存储中的过期临时对象和待删除对象。 */
@Component
@RequiredArgsConstructor
@SchedulerJobDefinition(description = "清理过期临时附件和待删除对象", parameterTemplate = "{}")
public class CleanTempFileJob extends QuartzJobBean {
    private final AttachmentService attachmentService;
    private final FileArtifactService fileArtifactService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        int failed = attachmentService.cleanupExpiredAndPending();
        failed += fileArtifactService.cleanupExpiredAndPending();
        if (failed > 0) {
            throw new JobExecutionException("附件清理部分失败: " + failed);
        }
    }
}
