package sm.system.response;

import org.junit.jupiter.api.Test;
import sm.system.exception.BizException;
import sm.system.exception.ExceptionResultResolver;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

class ResultTests {
    @Test
    void allFactoriesUseTheEnumAsFeedbackAuthority() {
        for (ResultEnum result : ResultEnum.values()) {
            if (result == ResultEnum.SUCCESS) continue;
            assertNotNull(result.getFeedbackLevel());
            assertEquals(result.getFeedbackLevel(), Result.error(result).getFeedbackLevel());
            assertEquals(result.getFeedbackLevel(), Result.error(result, "说明").getFeedbackLevel());
            assertEquals(result.getFeedbackLevel(), Result.error(result.getCode(), "自定义说明").getFeedbackLevel());
            assertEquals(result.getFeedbackLevel(),
                    ExceptionResultResolver.resolve(new BizException(result)).getFeedbackLevel());
        }
        assertEquals(FeedbackLevel.ERROR, Result.error(999999, "未知错误").getFeedbackLevel());
        assertEquals(FeedbackLevel.ERROR, Result.error("失败").getFeedbackLevel());
    }

    @Test
    void rejectsSuccessCodesInErrorFactories() {
        assertThrows(IllegalArgumentException.class, () -> Result.error(0, "失败"));
        assertThrows(IllegalArgumentException.class, () -> Result.error((Integer) null, "失败"));
        assertThrows(IllegalArgumentException.class, () -> Result.error(ResultEnum.SUCCESS));
        assertThrows(IllegalArgumentException.class, () -> Result.error(ResultEnum.SUCCESS, "失败"));
    }

    @Test
    void serializesSuccessAndFailureEnvelopeWithoutUiInstructions() {
        var mapper = JsonMapper.builder().build();
        var success = mapper.readTree(mapper.writeValueAsString(Result.success()));
        assertEquals(0, success.get("code").intValue());
        assertTrue(success.get("feedbackLevel").isNull());
        assertTrue(success.has("data"));
        assertTrue(success.has("traceId"));
        var warning = mapper.readTree(mapper.writeValueAsString(Result.error(ResultEnum.PARAM_ERROR)));
        assertEquals("WARNING", warning.get("feedbackLevel").stringValue());
        assertFalse(warning.has("showType"));
    }
}
