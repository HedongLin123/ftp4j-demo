package com.saxo.ftp.service;

import com.saxo.ftp.dto.req.file.FtpFileDownloadDTO;
import com.saxo.ftp.dto.req.file.FtpFileUploadDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;

/**
 * @Description FTP操作接口 例如，上传下载等
 * @Author donglin.he
 * @Date 2022/11/29 09:50
 */
public interface FtpFileService {

    /**
     * 文件上传
     * @param files 要上传的文件列表
     * @param dto 要上传的FTP服务器认证信息和要上传的目录
     */
    void upload(MultipartFile[] files, FtpFileUploadDTO dto);


    /**
     * 文件下载
     * @param dto 要下载的文件的FTP服务器认证信息和要下载的文件路径
     * @return 文件的输出流
     */
    ByteArrayOutputStream download(FtpFileDownloadDTO dto);

}
