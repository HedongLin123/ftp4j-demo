package com.saxo.ftp.enums;

/**
 * @Description FTP认证失败信息枚举
 * @Author donglin.he
 * @Date 2022/11/28 16:12
 */
public enum FtpAuthMsg {

    FTP_AUTH_ERR("CF100000", "FTP认证失败"),
    FTP_UNAME_ERR("CF100001", "FTP用户名错误"),
    FTP_PWD_ERR("CF100002", "FTP密码错误"),
    FTP_HOST_OR_PORT_ERR("CF100003", "FTP连接IP或端口错误"),
    FTP_TYPE_ERR("CF100004", "FTP类型错误"),
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
        for (FtpAuthMsg errorEnum : FtpAuthMsg.values()) {
            if (errorEnum.msg.equals(msg)) {
                return errorEnum.getCode();
            }
        }
        return null;
    }

    FtpAuthMsg(String code, String msg) {
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
