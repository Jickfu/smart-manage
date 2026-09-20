package sm.system.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sm.system.response.Result;


/**
 * @author Chekfu
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public Result<String> handleException(Exception e) {
		return ExceptionResultResolver.resolve(e);
	}

}
