package com.itdl;
import org.apache.commons.lang3.StringUtils;
/**
 * Ftp 工具类 基础父类
 */
public abstract class BaseFtpHelper<T> implements IFtpHelper<T> {

    /**
     * 客户端（FTP/FTPS/SFTP）
     */
    protected T ftp;

    /**
     * 根目录
     */
    protected String rootPath;



    /**
     * 初始化Ftp信息
     * @param ftpServer   ftp服务器地址
     * @param ftpPort     Ftp端口号
     * @param ftpUsername ftp 用户名
     * @param ftpPassword ftp 密码
     */
    public BaseFtpHelper(String ftpServer, int ftpPort, String ftpUsername,
                     String ftpPassword) {
        connect(ftpServer, ftpPort, ftpUsername, ftpPassword);
    }


    /**
     * 将文件路径替换为一个正确的路径 windows不处理
     * @param path 文件路径
     */
    public static String replaceFilePath(String path){
        if (StringUtils.isBlank(path)){
            return "";
        }

        if (path.trim().equals("/")){
            return path.trim();
        }

        // 反斜杠转正  双正斜杠去重
        path = path.replaceAll("\\\\", "/");
        while (path.contains("//")){
            path = path.replaceAll("//", "/");
        }

        if (path.endsWith("/")){
            return path.substring(0, path.length() - 1);
        }

        return path;
    }



    @Override
    public T getClient() {
        return ftp;
    }

    @Override
    public String getRootPath() {
        return rootPath;
    }
}
