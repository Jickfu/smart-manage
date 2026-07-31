package sm.domain.sys.scheduler.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.scheduler.mapper.JobMapper;
import sm.domain.sys.scheduler.model.entity.JobEntity;
import sm.domain.sys.scheduler.model.form.JobSaveForm;
import sm.domain.sys.scheduler.model.form.JobCommandForm;
import sm.domain.sys.scheduler.constant.JobStatus;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

/** 定时任务数据库事务服务；Quartz 同步由公开 Service 在事务提交后执行。 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class JobTxService {

    private final JobMapper mapper;

    public Long save(JobSaveForm form) {
        JobEntity entity;
        if (form.getId() == null) {
            entity = new JobEntity();
            entity.setNumber(form.getNumber());
        } else {
            entity = mapper.selectById(form.getId());
            if (entity == null) {
                throw new BizException(ResultEnum.NOT_FOUND, "任务不存在");
            }
            requireVersion(entity, form.getVersion());
            if (Boolean.TRUE.equals(entity.getIsSystem())) {
                if (!entity.getJobClassName().equals(form.getJobClassName())) {
                    throw new BizException(ResultEnum.BILL_STATUS_ERROR, "系统内置任务不可修改执行类");
                }
                if (!entity.getJobGroup().equals(form.getJobGroup())) {
                    throw new BizException(ResultEnum.BILL_STATUS_ERROR, "系统内置任务不可修改任务分组");
                }
            }
        }

        String jobGroup = form.getJobGroup() == null || form.getJobGroup().isBlank()
                ? "DEFAULT" : form.getJobGroup().trim();
        requireUnique(form.getId(), form.getNumber().trim(), form.getJobName().trim(), jobGroup);
        entity.setNumber(form.getNumber().trim());
        entity.setJobName(form.getJobName().trim());
        entity.setJobGroup(jobGroup);
        entity.setJobClassName(form.getJobClassName().trim());
        entity.setCronExpression(form.getCronExpression().trim());
        entity.setJobData(form.getJobData());
        entity.setMutexKey(form.getMutexKey() == null || form.getMutexKey().isBlank()
                ? null : form.getMutexKey().trim());
        // 状态只允许通过暂停/恢复命令改变；新增任务必须先以暂停状态完成配置。
        if (form.getId() == null) {
            entity.setStatus(JobStatus.PAUSED.name());
        }
        entity.setRemark(form.getRemark());

        if (form.getId() == null) {
            if (mapper.insert(entity) != 1) {
                throw new BizException(ResultEnum.PERSISTENCE_ERROR, "新增任务失败");
            }
        } else if (mapper.updateById(entity) != 1) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "任务已被其他用户修改");
        }
        return entity.getId();
    }

    public void deleteById(Long id, Integer version) {
        if (id == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "任务ID不能为空");
        }
        JobEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "任务不存在");
        }
        if (Boolean.TRUE.equals(entity.getIsSystem())) {
            throw new BizException(ResultEnum.BILL_STATUS_ERROR, "系统内置任务不可删除");
        }
        requireVersion(entity, version);
        // 执行实例是审计记录，删除任务定义时必须保留。
        int deleted = mapper.delete(new LambdaQueryWrapper<JobEntity>()
                .eq(JobEntity::getId, id)
                .eq(JobEntity::getVersion, version));
        if (deleted != 1) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "任务已被其他用户删除");
        }
    }

    public void pause(java.util.List<JobCommandForm> jobs) {
        jobs.forEach(job -> updateStatus(
                job.getId(), job.getVersion(), JobStatus.ENABLED, JobStatus.PAUSED));
    }

    public void resume(java.util.List<JobCommandForm> jobs) {
        jobs.forEach(job -> updateStatus(
                job.getId(), job.getVersion(), JobStatus.PAUSED, JobStatus.ENABLED));
    }

    private void updateStatus(Long id, Integer version, JobStatus currentStatus, JobStatus targetStatus) {
        JobEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "任务不存在");
        }
        requireVersion(entity, version);
        if (!currentStatus.name().equals(entity.getStatus())) {
            throw new BizException(ResultEnum.BILL_STATUS_ERROR, "当前任务状态不允许执行该操作");
        }
        int updated = mapper.update(null, new LambdaUpdateWrapper<JobEntity>()
                .eq(JobEntity::getId, id)
                .eq(JobEntity::getVersion, version)
                .eq(JobEntity::getStatus, currentStatus.name())
                .set(JobEntity::getStatus, targetStatus.name())
                .set(JobEntity::getVersion, version + 1));
        if (updated != 1) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "任务状态或版本已发生变化，请刷新后重试");
        }
    }

    private void requireUnique(Long id, String number, String jobName, String jobGroup) {
        Long numberCount = mapper.selectCount(new LambdaQueryWrapper<JobEntity>()
                .eq(JobEntity::getNumber, number)
                .ne(id != null, JobEntity::getId, id));
        if (numberCount > 0) {
            throw new BizException(ResultEnum.UNIQUE_CONFLICT, "任务编码已存在");
        }
        Long keyCount = mapper.selectCount(new LambdaQueryWrapper<JobEntity>()
                .eq(JobEntity::getJobName, jobName)
                .eq(JobEntity::getJobGroup, jobGroup)
                .ne(id != null, JobEntity::getId, id));
        if (keyCount > 0) {
            throw new BizException(ResultEnum.UNIQUE_CONFLICT, "同一分组下任务名称已存在");
        }
    }

    private void requireVersion(JobEntity entity, Integer version) {
        if (version == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "修改任务时乐观锁版本号不能为空");
        }
        if (!java.util.Objects.equals(entity.getVersion(), version)) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "任务已被其他用户修改，请刷新后重试");
        }
    }
}
