package sm.domain.sys.scheduler.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.scheduler.mapper.JobMapper;
import sm.domain.sys.scheduler.model.entity.JobEntity;
import sm.domain.sys.scheduler.model.form.JobSaveForm;
import sm.domain.sys.scheduler.model.form.JobCommandForm;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

class JobTxServiceTests {

    private final JobMapper mapper = mock(JobMapper.class);
    private final JobTxService txService = new JobTxService(mapper);

    @Test
    void newTaskIsAlwaysCreatedPaused() {
        when(mapper.insert(any(JobEntity.class))).thenReturn(1);

        txService.save(saveForm());

        ArgumentCaptor<JobEntity> captor = ArgumentCaptor.forClass(JobEntity.class);
        verify(mapper).insert(captor.capture());
        assertEquals("PAUSED", captor.getValue().getStatus());
    }

    @Test
    void saveRejectsStaleVersionBeforeWriting() {
        JobEntity entity = task("ENABLED", 2);
        when(mapper.selectById(1L)).thenReturn(entity);
        JobSaveForm form = saveForm();
        form.setId(1L);
        form.setVersion(1);

        BizException exception = assertThrows(BizException.class, () -> txService.save(form));

        assertEquals(ResultEnum.DATA_CONFLICT.getCode(), exception.getCode());
        verify(mapper, never()).updateById(any(JobEntity.class));
    }

    @Test
    void pauseRequiresEnabledState() {
        when(mapper.selectById(1L)).thenReturn(task("PAUSED", 2));

        BizException exception = assertThrows(
                BizException.class, () -> txService.pause(java.util.List.of(command(1L, 2))));

        assertEquals(ResultEnum.BILL_STATUS_ERROR.getCode(), exception.getCode());
        verify(mapper, never()).update(any(), any());
    }

    @Test
    void deleteUsesVersionedAtomicCondition() {
        when(mapper.selectById(1L)).thenReturn(task("ENABLED", 2));
        when(mapper.delete(any())).thenReturn(0);

        BizException exception = assertThrows(BizException.class, () -> txService.deleteById(1L, 2));

        assertEquals(ResultEnum.DATA_CONFLICT.getCode(), exception.getCode());
        verify(mapper).delete(any());
    }

    private static JobEntity task(String status, Integer version) {
        JobEntity entity = new JobEntity();
        entity.setId(1L);
        entity.setStatus(status);
        entity.setVersion(version);
        entity.setIsSystem(false);
        return entity;
    }

    private static JobSaveForm saveForm() {
        JobSaveForm form = new JobSaveForm();
        form.setNumber("JOB-001");
        form.setJobName("测试任务");
        form.setJobGroup("DEFAULT");
        form.setJobClassName("example.Job");
        form.setCronExpression("0 0 * * * ?");
        return form;
    }

    private static JobCommandForm command(Long id, Integer version) {
        JobCommandForm form = new JobCommandForm();
        form.setId(id);
        form.setVersion(version);
        return form;
    }
}
