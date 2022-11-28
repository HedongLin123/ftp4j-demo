package com.saxo.ftp.test;


import com.saxo.ftp.core.helper.BaseFtpHelper;
import com.saxo.ftp.core.FtpUtil;
import com.saxo.ftp.enums.FtpTypeEnum;

import java.io.OutputStream;

/**
 * FTP/FTPS/SFTP文件下载测试
 */
public class FtpUtilDownloadTest {

    public static void main(String[] args) throws Exception {
        // 测试连接FTP
        final BaseFtpHelper<?> ftpHelper = FtpUtil.createFtpHelper(FtpTypeEnum.FTP, "10.157.5.29", 21, "work", "workftp");
        try (final OutputStream outputStream = ftpHelper.downloadFile("/sit/blood-analysis-service/input/ods/ods_to_bdm_20220616.osql")){
            System.out.println(outputStream.toString().length());
        }

        // 测试连接FTPS
        final BaseFtpHelper<?> ftpsHelper = FtpUtil.createFtpHelper(FtpTypeEnum.FTPS, "10.157.4.183", 21, "ftpsori", "hQP0nFurTuSwoJVbcxU=");
        try (final OutputStream outputStream = ftpsHelper.downloadFile("/sit/blood-analysis-service/input/hive/rate2_dgkhxx.hql")){
            System.out.println(outputStream.toString().length());
        }

        // 测试连接SFTP
        final BaseFtpHelper<?> sftpHelper = FtpUtil.createFtpHelper(FtpTypeEnum.SFTP, "10.157.4.183", 22, "origin", "hQP0nFurTuSwoJVbcxU=");
        try (final OutputStream outputStream = sftpHelper.downloadFile("/upload/sit/blood-analysis-service/input/10m/10m.hql")){
            System.out.println(outputStream.toString().length());
        }

        // 用完之后一定要关闭连接
        ftpHelper.disconnect();
        ftpsHelper.disconnect();
        sftpHelper.disconnect();
    }

}
