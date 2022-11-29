package com.itdl.ftp.dto.resp;

import com.itdl.ftp.enums.DescriptionEnum;
import org.apache.commons.lang3.StringUtils;

/***
 * @Descriptions
 * @Create jijun.tang
 * @Date: 2020/12/23 17:27
 * @version V1.0.0
 **/
public class JsonResult<T> extends BaseResult<T> {
    JsonResult() {
    }

    public static <T> JsonResult<T> buildSuccess() {
        JsonResult<T> result = new JsonResult();
        result.setCode(DescriptionEnum.PROCESS_STATUS_SUCCESS.getCode());
        result.setMsg("操作成功");
        result.setStatus(true);
        return result;
    }

    public static <T> JsonResult<T> buildSuccess(T data) {
        return buildSuccess((String) null, (String) null, data);
    }

    public static <T> JsonResult<T> buildRows(int rows) {
        return rows > 0 ? buildSuccess() : buildFailure();
    }

    public static <T> JsonResult<T> buildSuccess(String code, String msg) {
        return buildSuccess(code, msg, null);
    }

    public static <T> JsonResult<T> buildSuccess(String code, String msg, T data) {
        JsonResult<T> result = buildSuccess();
        if (data != null) {
            result.setData(data);
        }

        String strCode = code;
        if (StringUtils.isBlank(code)) {
            strCode = DescriptionEnum.PROCESS_STATUS_SUCCESS.getCode();
        }

        String strMsg = msg;
        if (StringUtils.isBlank(msg)) {
            strMsg = DescriptionEnum.PROCESS_STATUS_SUCCESS.getMsg();
        }

        result.setCode(strCode);
        result.setMsg(strMsg);
        return result;
    }

    public static JsonResult buildFailure() {
        JsonResult result = new JsonResult();
        result.setCode(DescriptionEnum.PROCESS_STATUS_FAILURE.getCode());
        result.setMsg("操作失败");
        result.setStatus(false);
        return result;
    }

    public static JsonResult buildFailure(Object data) {
        return buildFailure((String) null, (String) null, data);
    }

    public static JsonResult buildFailure(String code, String msg) {
        return buildFailure(code, msg, (Object) null);
    }

    public static JsonResult buildFailure(String code, String msg, Object data) {
        JsonResult result = buildFailure();
        if (null != data) {
            result.setData(data);
        }

        String strCode = code;
        if (StringUtils.isBlank(code)) {
            strCode = DescriptionEnum.PROCESS_STATUS_FAILURE.getCode();
        }

        String strMsg = msg;
        if (StringUtils.isBlank(msg)) {
            strMsg = DescriptionEnum.PROCESS_STATUS_FAILURE.getMsg();
        }

        result.setCode(strCode);
        result.setMsg(strMsg);
        return result;
    }
}
