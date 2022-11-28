package com.saxo.ftp.dto.resp;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;

import java.util.Collection;

/***
 * @Descriptions
 * @Create jijun.tang
 * @Date: 2020/12/23 17:27
 * @version V1.0.0
 **/
public class BaseResult<T> {
    @ApiModelProperty(
            name = "status",
            value = "请求状态",
            required = true,
            allowEmptyValue = false
    )
    private boolean status;
    @ApiModelProperty(
            name = "msg",
            value = "返回消息",
            required = false,
            allowEmptyValue = true
    )
    private String msg;
    @ApiModelProperty(
            name = "code",
            value = "状态码",
            required = false,
            allowEmptyValue = true
    )
    private String code;
    @ApiModelProperty(
            name = "data",
            value = "返回数据",
            required = false,
            allowEmptyValue = true
    )
    private T data;

    @JsonIgnore
    @JSONField(
            serialize = false
    )
    public boolean isFailOrNull() {
        return this.status && this.data != null;
    }

    @JsonIgnore
    @JSONField(
            serialize = false
    )
    public boolean isFailOrEmpty() {
        return this.isFailOrNull() && (!(this.data instanceof Collection) || ((Collection)this.data).size() > 0);
    }

    public String toString() {
        return "Result{status=" + this.status + ", message='" + this.msg + '\'' + ", code='" + this.code + '\'' + ",data=" + this.data + '}';
    }

    public BaseResult() {
    }

    public boolean isStatus() {
        return this.status;
    }

    public String getMsg() {
        return this.msg;
    }

    public String getCode() {
        return this.code;
    }

    public T getData() {
        return this.data;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setData(T data) {
        this.data = data;
    }

}