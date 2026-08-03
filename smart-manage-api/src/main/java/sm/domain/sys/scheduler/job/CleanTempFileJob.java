package sm.domain.sys.scheduler.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 清理临时文件任务
 *
 * @author Chekfu
 */
@Component
@Slf4j
@SchedulerJobDefinition(
        description = "删除超过保留期限的临时文件",
        parameterTemplate = "{\"tempDir\":\"E:/upload/temp\",\"keepDays\":\"7\"}")
public class CleanTempFileJob extends QuartzJobBean {

    /** 默认临时文件目录 */
    private static final String DEFAULT_TEMP_DIR = "E:/upload/temp";

    /** 默认保留天数 */
    private static final int DEFAULT_KEEP_DAYS = 7;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String tempDir = context.getMergedJobDataMap().getString("tempDir");
        if (tempDir == null || tempDir.isBlank()) {
            tempDir = DEFAULT_TEMP_DIR;
        }
        int keepDays = DEFAULT_KEEP_DAYS;
        try {
            String keepDaysStr = context.getMergedJobDataMap().getString("keepDays");
            if (keepDaysStr != null && !keepDaysStr.isBlank()) {
                keepDays = Integer.parseInt(keepDaysStr);
            }
        } catch (NumberFormatException e) {
            log.warn("keepDays 参数解析失败，使用默认值 {}", DEFAULT_KEEP_DAYS);
        }

        Path dir = Paths.get(tempDir);
        if (!Files.exists(dir)) {
            log.info("临时文件目录不存在，跳过清理: {}", tempDir);
            return;
        }

        Instant cutoff = Instant.now().minus(keepDays, ChronoUnit.DAYS);
        int deleted = 0;
        int failed = 0;
        try (var stream = Files.list(dir)) {
            var files = stream.filter(Files::isRegularFile).toList();
            for (Path file : files) {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
                    if (attrs.lastModifiedTime().toInstant().isBefore(cutoff)) {
                        Files.delete(file);
                        deleted++;
                        log.debug("删除过期临时文件: {}", file.getFileName());
                    }
                } catch (IOException e) {
                    failed++;
                    log.warn("删除文件失败: {}", file.getFileName(), e);
                }
            }
        } catch (IOException e) {
            log.error("扫描临时文件目录失败", e);
            throw new JobExecutionException("扫描临时文件目录失败", e);
        }
        log.info("临时文件清理完成，目录: {}, 保留天数: {}, 删除文件数: {}", tempDir, keepDays, deleted);
        if (failed > 0) {
            throw new JobExecutionException("临时文件清理部分失败，失败文件数: " + failed);
        }
    }
}
