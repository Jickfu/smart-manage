package sm.domain.sys.scheduler.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.attachment.mapper.AttachmentMapper;
import sm.domain.sys.base.attachment.mapper.BizAttachmentMapper;
import sm.domain.sys.base.attachment.model.entity.BizAttachmentEntity;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;
import sm.system.storage.FileStorageService;
import sm.system.storage.FileStorageServiceFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/** 按数据库生命周期清理共享存储中的过期临时对象和待删除对象。 */
@Component
@Slf4j
@RequiredArgsConstructor
@SchedulerJobDefinition(description = "清理过期临时附件和待删除对象", parameterTemplate = "{}")
public class CleanTempFileJob extends QuartzJobBean {
    private final AttachmentMapper attachmentMapper;
    private final BizAttachmentMapper bizAttachmentMapper;
    private final FileStorageServiceFactory storageFactory;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        List<AttachmentEntity> candidates = attachmentMapper.selectList(
                new LambdaQueryWrapper<AttachmentEntity>()
                        .and(wrapper -> wrapper
                                .eq(AttachmentEntity::getStatus, "PENDING_DELETE")
                                .or(expired -> expired.eq(AttachmentEntity::getStatus, "TEMP")
                                        .lt(AttachmentEntity::getExpiresAt, LocalDateTime.now())))
                        .orderByAsc(AttachmentEntity::getId));
        int failed = 0;
        for (AttachmentEntity attachment : candidates) {
            try {
                FileStorageService storage = storageFactory.getService(attachment.getStorageType());
                storage.delete(attachment.getObjectKey());
                attachment.setStatus("DELETED");
                if (attachmentMapper.updateById(attachment) != 1) {
                    throw new IllegalStateException("附件清理状态更新失败: " + attachment.getId());
                }
                bizAttachmentMapper.delete(new LambdaQueryWrapper<BizAttachmentEntity>()
                        .eq(BizAttachmentEntity::getAttachmentId, attachment.getId()));
            } catch (IOException | RuntimeException exception) {
                failed++;
                log.warn("附件清理失败，等待下次幂等重试: id={}, storageType={}",
                        attachment.getId(), attachment.getStorageType(), exception);
            }
        }
        log.info("附件清理完成: candidates={}, failed={}", candidates.size(), failed);
        if (failed > 0) {
            throw new JobExecutionException("附件清理部分失败: " + failed);
        }
    }
}
