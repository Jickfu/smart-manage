package sm.system.exception;

import cn.dev33.satoken.exception.SaTokenException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import sm.system.response.Result;
import sm.system.response.ResultEnum;
import sm.system.util.ServletUtil;

import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * 统一异常响应转换。
 *
 * @author Chekfu
 */
@Slf4j
public final class ExceptionResultResolver {

    private static final String SQL_STATE_UNIQUE_VIOLATION = "23505";
    private static final String SQL_STATE_FOREIGN_KEY_VIOLATION = "23503";

    private ExceptionResultResolver() {
    }

    public static Result<String> resolve(Throwable exception) {
        if (exception instanceof BizException bizException) {
            log.warn("业务异常: {}", bizException.getMessage());
            return Result.error(bizException.getCode(), bizException.getMessage());
        }
        if (exception instanceof DataIntegrityViolationException dataIntegrityException) {
            return handleDataIntegrityException(dataIntegrityException);
        }
        if (exception instanceof OptimisticLockingFailureException) {
            log.warn("乐观锁冲突: {}", exception.getMessage());
            return Result.error(ResultEnum.DATA_CONFLICT);
        }
        if (exception instanceof HttpMessageNotReadableException) {
            return Result.error(ResultEnum.BAD_REQUEST, "JSON 格式错误或字段类型不匹配");
        }
        if (exception instanceof MethodArgumentNotValidException validException) {
            return Result.error(ResultEnum.PARAM_ERROR.getCode(),
                    bindingMessages(validException.getBindingResult().getAllErrors().stream()
                            .map(DefaultMessageSourceResolvable::getDefaultMessage)
                            .toList()));
        }
        if (exception instanceof BindException bindException) {
            return Result.error(ResultEnum.PARAM_ERROR.getCode(),
                    bindingMessages(bindException.getBindingResult().getAllErrors().stream()
                            .map(DefaultMessageSourceResolvable::getDefaultMessage)
                            .toList()));
        }
        if (exception instanceof ConstraintViolationException violationException) {
            String message = violationException.getConstraintViolations().stream()
                    .map(violation -> violation.getMessage())
                    .collect(Collectors.joining("，"));
            return Result.error(ResultEnum.PARAM_ERROR.getCode(), message);
        }
        if (exception instanceof MethodArgumentTypeMismatchException mismatchException) {
            return Result.error(ResultEnum.PARAM_ERROR,
                    "参数 " + mismatchException.getName() + " 类型不正确");
        }
        if (exception instanceof MissingServletRequestParameterException missingException) {
            return Result.error(ResultEnum.PARAM_ERROR,
                    "缺少参数 " + missingException.getParameterName());
        }
        if (exception instanceof MaxUploadSizeExceededException) {
            return Result.error(ResultEnum.FILE_TOO_LARGE);
        }
        if (exception instanceof SaTokenException saTokenException) {
            return SaTokenExceptionResult.getExceptionResult(saTokenException);
        }
        if (exception instanceof HttpRequestMethodNotSupportedException methodException) {
            return Result.error(ResultEnum.BAD_REQUEST, "不支持当前请求方式：" + methodException.getMethod());
        }
        if (exception instanceof NoResourceFoundException) {
            return Result.error(ResultEnum.NOT_FOUND);
        }
        if (exception instanceof BadSqlGrammarException) {
            log.error("SQL 语法异常", exception);
            return Result.error(ResultEnum.SQL_ERROR);
        }
        if (exception instanceof DataAccessException) {
            log.error("数据持久化异常", exception);
            return Result.error(ResultEnum.PERSISTENCE_ERROR);
        }
        if (exception instanceof SerializationException) {
            log.error("Redis 序列化异常", exception);
            return Result.error(ResultEnum.CONFIG_ERROR);
        }
        if (exception instanceof RedisConnectionFailureException) {
            log.error("Redis 连接失败", exception);
            return Result.error(ResultEnum.CONFIG_ERROR);
        }

        log.error("服务器异常，请求地址：{}", ServletUtil.getRequest().getRequestURL(), exception);
        return Result.error(ResultEnum.SERVER_ERROR);
    }

    private static Result<String> handleDataIntegrityException(DataIntegrityViolationException exception) {
        String sqlState = findSqlState(exception);
        log.warn("数据完整性冲突，SQLState={}", sqlState, exception);
        if (SQL_STATE_UNIQUE_VIOLATION.equals(sqlState)) {
            return Result.error(ResultEnum.UNIQUE_CONFLICT);
        }
        if (SQL_STATE_FOREIGN_KEY_VIOLATION.equals(sqlState)) {
            return Result.error(ResultEnum.FOREIGN_KEY_CONFLICT);
        }
        return Result.error(ResultEnum.PERSISTENCE_ERROR);
    }

    private static String findSqlState(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof SQLException sqlException && sqlException.getSQLState() != null) {
                return sqlException.getSQLState();
            }
            current = current.getCause();
        }
        return null;
    }

    private static String bindingMessages(java.util.List<String> messages) {
        return messages.stream()
                .filter(message -> message != null && !message.isBlank())
                .collect(Collectors.joining("，"));
    }
}
