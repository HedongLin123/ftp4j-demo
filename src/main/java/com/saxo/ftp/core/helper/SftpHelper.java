package com.saxo.ftp.core.helper;

import com.saxo.ftp.exception.FtpException;
import com.jcraft.jsch.*;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SFTP 工具类
 */
public class SftpHelper extends BaseFtpHelper<ChannelSftp>{
    /**
     * 初始化Ftp信息
     *
     * @param ftpServer   ftp服务器地址
     * @param ftpPort     Ftp端口号
     * @param ftpUsername ftp 用户名
     * @param ftpPassword ftp 密码
     */
    public SftpHelper(String ftpServer, int ftpPort, String ftpUsername,
                      String ftpPassword) {
        super(ftpServer, ftpPort, ftpUsername, ftpPassword);
    }

    /**
     * 连接到ftp
     *
     * @param ftpServer   ftp服务器地址
     * @param ftpPort     Ftp端口号
     * @param ftpUsername ftp 用户名
     * @param ftpPassword ftp 密码
     */
    public void connect(String ftpServer, int ftpPort, String ftpUsername, String ftpPassword) {
        ftp = new ChannelSftp();
        try {
            JSch jsch = new JSch();
            // 获取session
            Session session = jsch.getSession(ftpUsername, ftpServer, ftpPort);
            // 设置密码
            if (!StringUtils.isEmpty(ftpPassword)){
                session.setPassword(ftpPassword);
            }
            Properties properties = new Properties();
            properties.put("StrictHostKeyChecking", "no");
            session.setConfig(properties);
            // 使用会话开启连接
            if (!session.isConnected()){
                session.connect(30000);
            }
            Channel channel = session.openChannel("sftp");
            if (!channel.isConnected()){
                channel.connect(30000);
            }
            ftp = (ChannelSftp) channel;
            // 记录根目录
            this.rootPath = ftp.pwd();
        }
        catch (Exception e) {
            ftp = null;
            throw new FtpException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 更改ftp路径
     *
     * @param dirName
     * @return
     */
    public boolean checkDirectory(String dirName) {
        boolean flag;
        try {
            ftp.cd(dirName);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
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
            final Vector<ChannelSftp.LsEntry> result = (Vector<ChannelSftp.LsEntry>) ftp.ls(dir);
            // 转换为string数组
            return result.stream().map(ChannelSftp.LsEntry::getFilename).collect(Collectors.toList()).toArray(new String[]{});
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
                ftp.getSession().disconnect();
                ftp.disconnect();
                ftp.quit();
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
        InputStream inputStream = null;
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
            if (type.startsWith(rootStart) && !rootPath.equals("/")){
                type = type.substring(rootStart.length() + 1);
            }
            String[] typeArray = type.split("/");
            // 先切换到根目录，预防出错
            ftp.cd(rootPath);
            for (String s : typeArray) {
                ftp.cd(s);
            }
        }
        try {
            final String pwd = ftp.pwd();
            inputStream = ftp.get(fileName);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();//输出流
            byte[] bytes = new byte[1024];
            int outlen;
            while ((outlen = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, outlen); //将读到的字节写入输出流
            }
            return outputStream;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("download file fail");
        }
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
            String[] typeArray = type.split("/");
            for (String s : typeArray) {
                if (!checkDirectory(s)) {
                    ftp.mkdir(s);
                }
            }
        }
        ftp.put(inStream, fileName);
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
                    ftp.cd(s);
                }
            }
        }
        ftp.rm(fileName);
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
                ftp.cd(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
