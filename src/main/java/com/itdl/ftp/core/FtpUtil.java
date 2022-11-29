package com.itdl.ftp.core;

import com.itdl.ftp.core.helper.BaseFtpHelper;
import com.itdl.ftp.core.helper.FtpHelper;
import com.itdl.ftp.core.helper.FtpsHelper;
import com.itdl.ftp.core.helper.SftpHelper;
import com.itdl.ftp.enums.FtpTypeEnum;

/**
 * FTP对外工具类
 */
public class FtpUtil {

    /**
     * 根据FTP类型和基本信息创建一个FtpHelper
     * @param ftpServer FTP服务器IP地址
     * @param ftpPort FTP服务器端口
     * @param ftpUsername FTP用户名
     * @param ftpPassword FTP密码
     * @return 目录下的文件列表和DDL关系
     */
    public static BaseFtpHelper<?> createFtpHelper(FtpTypeEnum ftpType,
                                                   String ftpServer,
                                                   int ftpPort,
                                                   String ftpUsername,
                                                   String ftpPassword){
        // 判断使用哪种FTP
        BaseFtpHelper<?> ftpHelper = null;
        switch (ftpType){
            case FTP:
                ftpHelper = new FtpHelper(ftpServer, ftpPort, ftpUsername, ftpPassword);
                break;
            case FTPS:
                ftpHelper = new FtpsHelper(ftpServer, ftpPort, ftpUsername, ftpPassword);
                break;
            case SFTP:
                ftpHelper = new SftpHelper(ftpServer, ftpPort, ftpUsername, ftpPassword);
                break;
            default:
                break;
        }
        if (ftpHelper == null){
            throw new RuntimeException("ftpHelper is not null");
        }

        return ftpHelper;
    }

}
