package sm.domain.sys.scheduler.service;

import org.junit.jupiter.api.Test;
import org.quartz.Job;
import org.springframework.context.ApplicationContext;
import sm.domain.sys.scheduler.job.CleanTempFileJob;
import sm.domain.sys.scheduler.model.form.JobSaveForm;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JobDefinitionValidatorTests {

    private final ApplicationContext applicationContext = mock(ApplicationContext.class);
    private final JobDefinitionValidator validator =
            new JobDefinitionValidator(applicationContext, mock(JsonMapper.class));

    @Test
    void rejectsInvalidCronExpression() {
        JobSaveForm form = validForm();
        form.setCronExpression("invalid");

        BizException exception = assertThrows(BizException.class, () -> validator.validate(form));

        assertEquals(ResultEnum.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void rejectsJobClassThatIsNotRegisteredAsSpringBean() {
        when(applicationContext.getBeansOfType(Job.class)).thenReturn(Map.of());

        BizException exception = assertThrows(BizException.class, () -> validator.validate(validForm()));

        assertEquals(ResultEnum.CONFIG_ERROR.getCode(), exception.getCode());
    }

    @Test
    void acceptsRegisteredJobAndJsonlessParameters() {
        CleanTempFileJob job = new CleanTempFileJob();
        when(applicationContext.getBeansOfType(Job.class)).thenReturn(Map.of("cleanTempFileJob", job));

        validator.validate(validForm());
    }

    private static JobSaveForm validForm() {
        JobSaveForm form = new JobSaveForm();
        form.setJobClassName(CleanTempFileJob.class.getName());
        form.setCronExpression("0 0 3 * * ?");
        return form;
    }
}
