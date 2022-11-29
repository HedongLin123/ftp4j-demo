package com.itdl.ftp.test;


import com.itdl.ftp.core.helper.BaseFtpHelper;
import com.itdl.ftp.enums.FtpTypeEnum;
import com.itdl.ftp.core.FtpUtil;

import java.io.File;

/**
 * FTP/FTPS/SFTP文件下载测试
 */
public class FtpUtilUploadTest {

    public static void main(String[] args) throws Exception {
        // 测试连接FTP
        final BaseFtpHelper<?> ftpHelper = FtpUtil.createFtpHelper(FtpTypeEnum.FTP, "10.157.5.29", 21, "work", "workftp");
        ftpHelper.uploadFile(new File("C:\\workspace\\测试文件.txt"), "/sit/blood-analysis-service/input/ods/测试文件.txt");

        // 测试连接FTPS
        final BaseFtpHelper<?> ftpsHelper = FtpUtil.createFtpHelper(FtpTypeEnum.FTPS, "10.157.4.183", 21, "ftpsori", "hQP0nFurTuSwoJVbcxU=");
        ftpsHelper.uploadFile(new File("C:\\workspace\\测试文件.txt"), "/sit/blood-analysis-service/input/hive/测试文件.txt");

        // 测试连接SFTP
        final BaseFtpHelper<?> sftpHelper = FtpUtil.createFtpHelper(FtpTypeEnum.SFTP, "10.157.4.183", 22, "origin", "hQP0nFurTuSwoJVbcxU=");
        sftpHelper.uploadFile(new File("C:\\workspace\\测试文件.txt"), "/upload/sit/blood-analysis-service/input/10m/测试文件.txt");

        // 用完之后一定要关闭连接
        ftpHelper.disconnect();
        ftpsHelper.disconnect();
        sftpHelper.disconnect();
    }

}
