package top.wprop.app.push.exception;

import top.wprop.app.push.common.AjaxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 
 * @author samevillage
 */
@RestControllerAdvice
public class GlobalExceptionHandler
{
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler
    public AjaxResult handleException(Throwable t) {
        t.printStackTrace();
        return AjaxResult.error(t.getMessage());
    }

    /**
     * 运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public AjaxResult runTimeException(RuntimeException e)
    {
        e.printStackTrace();
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(top.wprop.app.push.exception.CustomException.class)
    public AjaxResult runTimeException(top.wprop.app.push.exception.CustomException e)
    {
        e.printStackTrace();
        if (e.getCode() == null)
        {
            return AjaxResult.error(e.getMessage());
        }
        return AjaxResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public AjaxResult handleBindException(BindException bindException) {
        StringBuilder message = new StringBuilder();
        BindingResult bindingResult = bindException.getBindingResult();
        if(bindingResult.hasErrors()) {
            for(ObjectError error : bindingResult.getAllErrors()) {
                message.append(error.getDefaultMessage()).append(", ");
            }
        }
        String messageStr = message.length() >= 2 ?
                message.substring(0, message.length() - 2) :
                message.toString();
        return AjaxResult.error(messageStr);
    }
}
