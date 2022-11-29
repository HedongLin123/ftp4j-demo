package com.itdl.ftp.enums;

/**
 * @Description FTP文件上传下载错误消息
 * @Author itdl
 * @Date 2022/11/28 16:12
 */
public enum FtpUploadDownloadMsg {

    FTP_UPLOAD_FILE_ERR("CF200000", "文件上传失败"),
    FTP_DOWNLOAD_FILE_ERR("CF200001", "文件下载失败"),
    FTP_UPLOAD_FILE_SIZE_TO_LARGE("CF200002", "文件大小超过限制"),
    FTP_DOWNLOAD_FILE_NOT_EXISTS("CF200003", "要下载的文件不存在"),
    ;


    /**
     * 信息编码
     **/
    private String code;
    /**
     * 信息数据
     **/
    private String msg;


    public static  String getCodeByMsg(String msg) {
        for (FtpUploadDownloadMsg errorEnum : FtpUploadDownloadMsg.values()) {
            if (errorEnum.msg.equals(msg)) {
                return errorEnum.getCode();
            }
        }
        return null;
    }

    FtpUploadDownloadMsg(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
