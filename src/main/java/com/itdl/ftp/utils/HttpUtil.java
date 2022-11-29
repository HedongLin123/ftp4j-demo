package com.itdl.ftp.utils;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @Description
 * @Author itdl
 * @Date 2022/11/28 15:09
 */
public class HttpUtil {
    private static final String USER_AGENT = "User-Agent";

    /**
     * http文件下载通用方法
     * @param request http请求
     * @param response http相应
     * @param is 数据输入流
     * @param fileName 文件下载名称
     */
    public static void download(HttpServletRequest request, HttpServletResponse response, ByteArrayInputStream is, String fileName) {
        String userAgent = request.getHeader(USER_AGENT); //获取浏览器内核
        try {
            byte[] bytes = userAgent.contains("MSIE") ? fileName.getBytes() : fileName.getBytes(StandardCharsets.UTF_8);
            // 各浏览器基本都支持ISO编码
            fileName = new String(bytes, StandardCharsets.ISO_8859_1);
            // 设置强制下载不打开
            response.setContentType("application/force-download");
            // 设置文件名
            response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            byte[] buffer = new byte[1024];
            OutputStream os;
            try (BufferedInputStream bis = new BufferedInputStream(is)) {
                os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
            }
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 将文件路径替换为一个正确的路径 windows不处理
     * @param path 文件路径
     * @return
     */
    public static String replaceFilePath(String path){
        if (StringUtils.isBlank(path)){
            return "";
        }

        if (path.trim().equals("/")){
            return path.trim();
        }

        // 反斜杠转正  双正斜杠去重
        path = path.replace("\\\\", "/");
        while (path.contains("//")){
            path = path.replace("//", "/");
        }

        if (path.endsWith("/")){
            return path.substring(0, path.length() - 1);
        }

        return path;
    }
}
