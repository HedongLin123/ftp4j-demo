package com.saxo.ftp.core.helper;

import com.saxo.ftp.exception.FtpException;
import it.sauronsoftware.ftp4j.FTPClient;
import org.apache.commons.lang3.StringUtils;

import java.io.*;

/**
 * Ftp 工具类
 */
public class FtpHelper extends BaseFtpHelper<FTPClient>{
    /**
     * 初始化Ftp信息
     *
     * @param ftpServer   ftp服务器地址
     * @param ftpPort     Ftp端口号
     * @param ftpUsername ftp 用户名
     * @param ftpPassword ftp 密码
     */
    public FtpHelper(String ftpServer, int ftpPort, String ftpUsername,
                     String ftpPassword) {
        super(ftpServer, ftpPort, ftpUsername, ftpPassword);
    }

    /**
     * 连接到ftp
     * @param ftpServer   ftp服务器地址
     * @param ftpPort     Ftp端口号
     * @param ftpUsername ftp 用户名
     * @param ftpPassword ftp 密码
     */
    public void connect(String ftpServer, int ftpPort, String ftpUsername, String ftpPassword) {
        ftp = new FTPClient();
        try {
            ftp.connect(ftpServer, ftpPort);
            ftp.login(ftpUsername, ftpPassword);
            ftp.setCharset("UTF-8");
            // 记录根目录
            this.rootPath = ftp.currentDirectory();
            System.out.println(this.rootPath);
        }
        catch (Exception e) {
            ftp =  null;
            throw new FtpException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 更改ftp路径
     * @param dirName
     * @return
     */
    public boolean checkDirectory(String dirName) {
        boolean flag;
        try {
            ftp.changeDirectory(dirName);
            flag = true;
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }


    /**
     * 遍历目录
     * @param dir 目录名称
     * @return 子目录列表
     */
    public String[] listDir(String dir){
        // 不是目录，则直接返回, 校验的同时会进入目录
        if (!checkDirectory(dir)){
            return new String[]{};
        }
        // 查询目录下面有什么
        String[] dirs = new String[]{};
        try {
            dirs = ftp.listNames();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dirs;
    }


    /**
     * 断开ftp链接
     */
    public void disconnect() {
        try {
            if (ftp.isConnected()) {
                ftp.disconnect(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取ftp文件流
     *
     * @param filePath ftp文件路径
     * @return s
     * @throws Exception
     */
    public ByteArrayOutputStream downloadFile(String filePath) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String fileName = "";
        filePath = StringUtils.removeStart(filePath, "/");
        int len = filePath.lastIndexOf("/");
        if (len == -1) {
            if (filePath.length() > 0) {
                fileName = filePath;
            } else {
                throw new Exception("没有输入文件路径");
            }
        } else {
            fileName = filePath.substring(len + 1);
            String type = filePath.substring(0, len);
            String rootStart = rootPath;
            rootStart = StringUtils.removeStart(rootStart, "/");
            if (type.startsWith(rootStart)){
                type = type.substring(rootStart.length() + 1);
            }
            String[] typeArray = type.split("/");
            // 先切换到根目录，预防出错
            ftp.changeDirectory(rootPath);
            for (String s : typeArray) {
                ftp.changeDirectory(s);
            }
        }
        try {
            final String pwd = ftp.currentDirectory();
            ftp.download(fileName, outputStream, 0, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputStream;
    }

    /**
     * 上传文件到ftp
     *
     * @param file     文件对象
     * @param filePath 上传的路径
     * @throws Exception
     */
    public void uploadFile(File file, String filePath) throws Exception {
        InputStream inStream = new FileInputStream(file);
        uploadFile(inStream, filePath);
    }

    /**
     * 上传文件到ftp
     *
     * @param inStream 上传的文件流
     * @param filePath 上传路径
     * @throws Exception
     */
    public void uploadFile(InputStream inStream, String filePath)
            throws Exception {
        if (inStream == null) {
            return;
        }
        String fileName = "";
        filePath = StringUtils.removeStart(filePath, "/");
        int len = filePath.lastIndexOf("/");
        if (len == -1) {
            if (filePath.length() > 0) {
                fileName = filePath;
            } else {
                throw new Exception("没有输入文件路径");
            }
        } else {
            fileName = filePath.substring(len + 1);
            String type = filePath.substring(0, len);
            String rootStart = rootPath;
            rootStart = StringUtils.removeStart(rootStart, "/");
            if (type.startsWith(rootStart)){
                type = type.substring(rootStart.length() + 1);
            }
            String[] typeArray = type.split("/");
            // 先切换到根目录，预防出错
            ftp.changeDirectory(rootPath);
            for (String s : typeArray) {
                if (!checkDirectory(s)) {
                    ftp.createDirectory(s);
                }
            }
        }
        ftp.upload(fileName, inStream, 0, 0, null);
    }

    /**
     * 删除ftp文件
     *
     * @param filePath 文件路径
     * @throws Exception
     */
    public void deleteFile(String filePath) throws Exception {
        String fileName = "";
        filePath = StringUtils.removeStart(filePath, "/");
        int len = filePath.lastIndexOf("/");
        if (len == -1) {
            if (filePath.length() > 0) {
                fileName = filePath;
            } else {
                throw new Exception("没有输入文件路径");
            }
        } else {
            fileName = filePath.substring(len + 1);

            String type = filePath.substring(0, len);
            String[] typeArray = type.split("/");
            for (String s : typeArray) {
                if (checkDirectory(s)) {
                    ftp.changeDirectory(s);
                }
            }
        }
        ftp.deleteFile(fileName);
    }

    /**
     * 切换目录
     *
     * @param path
     * @throws Exception
     */
    public void changeDirectory(String path) {
        if (!StringUtils.isEmpty(path)) {
            try {
                ftp.changeDirectory(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
