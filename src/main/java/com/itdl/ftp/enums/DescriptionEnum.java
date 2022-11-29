package com.itdl.ftp.enums;

/***
 * @Descriptions 业务通用异常信息返回码定义
 **/
public enum DescriptionEnum {
    PROCESS_STATUS_FAILURE("-000001", "处理失败"),
    PROCESS_STATUS_SUCCESS("000000", "处理成功"),
    PARAMS_EXCEPTION("100010", "参数不能为空"),
    PARAMS_INVALID_EXCEPTION("100011", "参数校验异常"),
    DATA_DOES_NOT_EXIST("100018", "数据不存在")

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
        for (DescriptionEnum errorEnum : DescriptionEnum.values()) {
            if (errorEnum.msg.equals(msg)) {
                return errorEnum.getCode();
            }
        }
        return null;
    }

    DescriptionEnum(String code, String msg) {
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
