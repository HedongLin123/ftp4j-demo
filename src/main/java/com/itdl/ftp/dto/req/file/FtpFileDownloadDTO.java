package com.itdl.ftp.dto.req.file;

import com.itdl.ftp.dto.req.IBaseRequest;
import com.itdl.ftp.enums.FtpTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Description 文件下载参数实体
 * @Author itdl
 * @Date 2022/11/25 14:44
 */

@Data
@ApiModel(description = "文件下载参数实体")
public class FtpFileDownloadDTO implements IBaseRequest {

    /**
     * 上传的FTP类型
     */
    @ApiModelProperty(value = "ftp类型 FTP/FTPS/SFTP", required = true)
    private FtpTypeEnum ftpType;

    /**
     * FTP服务器IP地址或域名
     */
    @ApiModelProperty(value = "FTP服务器IP地址或域名", required = true)
    private String ftpServer;

    /**
     * FTP服务器连接端口号
     */
    @ApiModelProperty(value = "FTP服务器连接端口号", required = true)
    private int ftpPort;

    /**
     * FTP连接的用户名
     */
    @ApiModelProperty(value = "FTP连接的用户名", required = true)
    private String ftpUsername;

    /**
     * FTP连接的密码（注意：改密码需要通过对称加密）
     */
    @ApiModelProperty(value = "FTP连接的密码（注意：改密码需要通过对称加密）", required = true)
    private String ftpPassword;

    /**
     * 文件下载的全路径（不需要带ftp服务器的根路径）
     */
    @ApiModelProperty(value = "文件下载的全路径（不需要带ftp服务器的根路径）", required = true)
    private String ftpDownloadPath;

}
