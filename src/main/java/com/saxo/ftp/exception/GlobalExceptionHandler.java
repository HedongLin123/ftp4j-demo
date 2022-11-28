package com.saxo.ftp.exception;
import com.saxo.ftp.dto.resp.JsonResult;
import com.saxo.ftp.enums.DescriptionEnum;
import com.saxo.ftp.enums.FtpAuthMsg;
import com.saxo.ftp.enums.FtpUploadDownloadMsg;
import it.sauronsoftware.ftp4j.FTPException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;


/***
 * @Descriptions 全局异常
 * @Create jijun.tang
 * @Date: 2020/12/23 17:23
 * @version V1.0.0
 **/
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 422 - UNPROCESSABLE_ENTITY
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public JsonResult handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        String msg = "所上传文件大小超过最大限制，上传失败！";
        log.error(msg, e);
        return JsonResult.buildFailure(FtpUploadDownloadMsg.FTP_UPLOAD_FILE_SIZE_TO_LARGE.getCode(), msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public JsonResult handleIllegalArgumentException(IllegalArgumentException e) {
        String msg = "参数校验异常：" + e.getMessage();
        log.error(msg, e);
        return JsonResult.buildFailure(DescriptionEnum.PARAMS_INVALID_EXCEPTION.getCode(), e.getMessage());
    }

    @ExceptionHandler(BizException.class)
    public JsonResult handleBizException(BizException e) {
        String msg = "业务校验异常：" + e.getMessage();
        log.error(msg, e);
        String code = DescriptionEnum.getCodeByMsg(e.getMessage());
        return JsonResult.buildFailure(code, e.getMessage());
    }


    @ExceptionHandler(FTPException.class)
    public JsonResult handleFtpException(FTPException e) {
        String msg = "FTP认证异常：" + e.getMessage();
        log.error(msg, e);
        String code = FtpAuthMsg.getCodeByMsg(e.getMessage());
        return JsonResult.buildFailure(code, e.getMessage());
    }


    /**
     * 500 - Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public JsonResult handleException(Exception e) {
        String msg = "服务内部异常！" + e.getMessage();
        log.error(msg, e);
        return JsonResult.buildFailure(null, e.getMessage());
    }


}
