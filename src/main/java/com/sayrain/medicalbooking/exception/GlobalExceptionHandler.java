package com.sayrain.medicalbooking.exception;

import com.sayrain.medicalbooking.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseResult<String> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return new ResponseResult<>(400, ex.getMessage(), null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseResult<String> handleNotFoundException(ResourceNotFoundException ex) {
        log.warn("资源未找到: {}", ex.getMessage());
        return new ResponseResult<>(404, ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseResult<String> handleException(Exception ex) {
        log.error("系统异常: ", ex);
        return new ResponseResult<>(500, "服务器发生错误: " + ex.getMessage(), null);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseResult<String> handleRuntimeException(RuntimeException ex) {
        log.error("运行时异常: ", ex);
        return new ResponseResult<>(500, "系统错误: " + ex.getMessage(), null);
    }
}