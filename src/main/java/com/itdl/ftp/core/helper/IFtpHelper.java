package com.itdl.ftp.core.helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * FTP操作统一接口
 */
public interface IFtpHelper<T> {

    /**
     * 获取客户端
     * @return 客户端 FTP(S)对应FtpClient, SFTP对应ChannelSftp
     */
    T getClient();


    /**
     * 获取FTP/FTPS/SFTP所处根目录
     * @return 根目录
     */
    String getRootPath();


    /**
     * 创建连接
     * @param ftpServer FTP服务器IP
     * @param ftpPort FTP服务器端口
     * @param ftpUsername FTP连接用户名
     * @param ftpPassword FTP连接密码
     */
    void connect(String ftpServer, int ftpPort, String ftpUsername, String ftpPassword);

    /**
     * 检查目录是否存在
     * @param dirName 目录名称
     */
    boolean checkDirectory(String dirName);


    /**
     * 根据上级目录列出下级目录的名称列表
     * @param dir 上级目录名称
     * @return 下级目录名称列表
     */
    String[] listDir(String dir);

    /**
     * 断开连接
     */
    void disconnect();

    /**
     * 切换目录
     * @param path 目录path
     * @throws Exception
     */
    void changeDirectory(String path);

    /**
     * 下载文件到输出流
     * @param filePath 文件路径
     * @return 输出流（没有关闭，需要调用者自己关闭）
     * @throws Exception
     */
    ByteArrayOutputStream downloadFile(String filePath) throws Exception;

    /**
     * 上传文件
     * @param file 要上传的本地文件
     * @param filePath 上传到FTP服务的所在目录
     * @throws Exception
     */
    void uploadFile(File file, String filePath) throws Exception;

    void uploadFile(InputStream inStream, String filePath) throws Exception;

    /**
     * 删除远程FTP文件
     * @param filePath 远程FTP文件目录
     * @throws Exception
     */
    void deleteFile(String filePath) throws Exception;
}
