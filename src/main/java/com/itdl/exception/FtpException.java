package com.itdl.exception;

public class FtpException extends RuntimeException{
    public FtpException(String message) {
        super(message);
    }

    public FtpException(String message, Throwable cause) {
        super(message, cause);
    }
}
