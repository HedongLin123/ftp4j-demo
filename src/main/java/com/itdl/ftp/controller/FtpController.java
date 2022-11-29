package com.itdl.ftp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description
 * @Author itdl
 * @Date 2022/11/25 11:22
 */
@RestController
public class FtpController {

    @Value("${commonFtp.version:1.0}")
    private String version;

    @Value("${commonFtp.description:公共Ftp组件，有文件上传下载等功能!}")
    private String description;

    @Value("${spring.application.name:common-ftp}")
    private String applicationName;

    @GetMapping("")
    public Map<String, Object> helloFtp(){
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("applicationName", applicationName);
        result.put("version", version);
        result.put("description", description);
        return result;
    }

}
