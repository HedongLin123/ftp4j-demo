package com.itdl.ftp.exception;

/**
 * @Description 业务异常
 * @Author itdl
 * @Date 2022/11/28 16:23
 */
public class BizException extends RuntimeException{
    public BizException() {
        super();
    }

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }
}
