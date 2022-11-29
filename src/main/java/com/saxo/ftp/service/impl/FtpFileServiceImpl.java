package com.saxo.ftp.service.impl;

import cn.hutool.core.date.StopWatch;
import com.saxo.ftp.core.FtpUtil;
import com.saxo.ftp.core.helper.BaseFtpHelper;
import com.saxo.ftp.dto.req.file.FtpFileDownloadDTO;
import com.saxo.ftp.dto.req.file.FtpFileUploadDTO;
import com.saxo.ftp.enums.FtpAuthMsg;
import com.saxo.ftp.enums.FtpUploadDownloadMsg;
import com.saxo.ftp.exception.BizException;
import com.saxo.ftp.exception.FtpException;
import com.saxo.ftp.service.FtpFileService;
import com.saxo.ftp.utils.DoubleUtils;
import com.saxo.ftp.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description FTP文件上传下载实现
 * @Author donglin.he
 * @Date 2022/11/29 09:55
 */
@Service
@Slf4j
public class FtpFileServiceImpl implements FtpFileService {
    private static final ConcurrentHashMap<String, BaseFtpHelper<?>> ftpHelpers = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> ftpHelperPwds = new ConcurrentHashMap<>();

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    @Override
    public void upload(MultipartFile[] files, FtpFileUploadDTO dto) {
        StopWatch stopWatch = new StopWatch();

        // 实例化FTP客户端
        stopWatch.start();
        BaseFtpHelper<?> ftpHelper;
        try {
            String mapKey =  dto.getFtpType() + "_" + dto.getFtpServer() + "_" + dto.getFtpPort() + "_" + dto.getFtpUsername();
            final BaseFtpHelper<?> baseFtpHelper = ftpHelpers.get(mapKey);
            if (baseFtpHelper != null){
                // 从缓存冲获取连接
                ftpHelper = baseFtpHelper;

                // 校验传入密码与缓存中的密码是否一致
                final String cachePwd = ftpHelperPwds.get(mapKey);
                if (!StringUtils.equals(cachePwd, dto.getFtpPassword().trim())){
                    log.error("密码错误");
                    throw new FtpException(FtpAuthMsg.FTP_PWD_ERR.getMsg());
                }

                // 校验ftpHelper是否健康，健康的直接取出来
                boolean connIsOk = ftpHelper.checkDirectory(ftpHelper.getRootPath());
                if (connIsOk){
                    stopWatch.stop();
                    log.info("========从缓存中获取ftp连接成功，且连接状态正常，耗时：{}s", stopWatch.getTotalTimeSeconds());
                }else {
                    log.info("=====>>>从缓存中获取ftp连接成功，但校验连接失败，即将重新创建连接");
                    // 创建新的连接
                    ftpHelper = createFtpConn(dto, stopWatch, mapKey);
                }
            }else {
                ftpHelper = createFtpConn(dto, stopWatch, mapKey);
            }
        } catch (Exception e) {
            log.error("========>>>创建FTP连接失败：{}", e.getMessage());
            e.printStackTrace();
            throw new FtpException(FtpAuthMsg.FTP_AUTH_ERR.getMsg());
        }

        // TODO：超过8个文件，这里需要改成多线程上传
        for (MultipartFile file : files) {
            stopWatch = new StopWatch();
            stopWatch.start();
            final String originalFilename = file.getOriginalFilename();
            try {
                final String fileName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
                final String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

                String fileFullPath = dto.getFtpUploadPath() + "/" + fileName + "." + suffix;

                ftpHelper.uploadFile(file.getInputStream(), fileFullPath);
                stopWatch.stop();
                log.info("======>>>文件：{}上传成功，耗时：{}s", originalFilename, DoubleUtils.formatDouble(stopWatch.getTotalTimeSeconds()));
            } catch (Exception e) {
                stopWatch.stop();
                log.error("======>>>文件：{}上传失败，耗时：{}s", originalFilename, DoubleUtils.formatDouble(stopWatch.getTotalTimeSeconds()));
                e.printStackTrace();
                throw new FtpException(FtpUploadDownloadMsg.FTP_UPLOAD_FILE_ERR.getMsg());
            }
        }
    }

    @Override
    public ByteArrayOutputStream download(FtpFileDownloadDTO dto) {
        StopWatch stopWatch = new StopWatch();

        // 实例化FTP客户端
        stopWatch.start();
        BaseFtpHelper<?> ftpHelper;
        try {
            String mapKey =  dto.getFtpType() + "_" + dto.getFtpServer() + "_" + dto.getFtpPort() + "_" + dto.getFtpUsername();
            final BaseFtpHelper<?> baseFtpHelper = ftpHelpers.get(mapKey);
            if (baseFtpHelper != null){
                // 从缓存冲获取连接
                ftpHelper = baseFtpHelper;

                // 校验传入密码与缓存中的密码是否一致
                final String cachePwd = ftpHelperPwds.get(mapKey);
                if (!StringUtils.equals(cachePwd, dto.getFtpPassword().trim())){
                    log.error("密码错误");
                    throw new BizException(FtpAuthMsg.FTP_PWD_ERR.getMsg());
                }

                // 校验ftpHelper是否健康，健康的直接取出来
                boolean connIsOk = ftpHelper.checkDirectory(ftpHelper.getRootPath());
                if (connIsOk){
                    stopWatch.stop();
                    log.info("========从缓存中获取ftp连接成功，且连接状态正常，耗时：{}s", DoubleUtils.formatDouble(stopWatch.getTotalTimeSeconds()));
                }else {
                    log.info("=====>>>从缓存中获取ftp连接成功，但校验连接失败，即将重新创建连接");
                    // 创建新的连接
                    final FtpFileUploadDTO ftpFileUploadDTO = new FtpFileUploadDTO();
                    BeanUtils.copyProperties(dto, ftpFileUploadDTO);
                    ftpHelper = createFtpConn(ftpFileUploadDTO, stopWatch, mapKey);
                }
            }else {
                final FtpFileUploadDTO ftpFileUploadDTO = new FtpFileUploadDTO();
                BeanUtils.copyProperties(dto, ftpFileUploadDTO);
                ftpHelper = createFtpConn(ftpFileUploadDTO, stopWatch, mapKey);
            }
        } catch (Exception e) {
            log.error("========>>>创建FTP连接失败：{}", e.getMessage());
            e.printStackTrace();
            throw new BizException(FtpAuthMsg.FTP_AUTH_ERR.getMsg());
        }

        stopWatch = new StopWatch();
        stopWatch.start();
        // 对path进行处理
        String downloadPath = HttpUtil.replaceFilePath(dto.getFtpDownloadPath());
        try {
            final ByteArrayOutputStream outputStream = ftpHelper.downloadFile(downloadPath);
            if (outputStream == null){
                log.error("文件不存在");
                throw new FtpException(FtpUploadDownloadMsg.FTP_DOWNLOAD_FILE_NOT_EXISTS.getMsg());
            }
            stopWatch.stop();
            log.info("======>>>文件：{}从FTP下载成功，耗时：{}s", downloadPath, DoubleUtils.formatDouble(stopWatch.getTotalTimeSeconds()));
            return outputStream;
        } catch (Exception e) {
            stopWatch.stop();
            log.error("======>>>文件：{}下载失败，耗时：{}s", downloadPath, DoubleUtils.formatDouble(stopWatch.getTotalTimeSeconds()));
            e.printStackTrace();
            throw new FtpException(FtpUploadDownloadMsg.FTP_DOWNLOAD_FILE_ERR.getMsg());
        }
    }


    /**
     * 创建一个FTP连接
     * @param dto 连接参数
     * @param stopWatch 耗时统计参数
     * @param mapKey 缓存连接的key
     * @return
     */
    private synchronized BaseFtpHelper<?> createFtpConn(FtpFileUploadDTO dto, StopWatch stopWatch, String mapKey) {
        BaseFtpHelper<?> ftpHelper;// 创建新的连接
        ftpHelper = FtpUtil.createFtpHelper(dto.getFtpType(), dto.getFtpServer(), dto.getFtpPort(), dto.getFtpUsername(), dto.getFtpPassword());
        stopWatch.stop();
        log.info("========创建ftp连接成功，耗时：{}s", DoubleUtils.formatDouble(stopWatch.getTotalTimeSeconds()));
        ftpHelpers.put(mapKey, ftpHelper);
        ftpHelperPwds.put(mapKey, dto.getFtpPassword());
        return ftpHelper;
    }

    /**
     * 服务器停止之前关闭FTP连接
     */
    @PreDestroy
    public void closeFtpConn(){
        for (Map.Entry<String, BaseFtpHelper<?>> entry : ftpHelpers.entrySet()) {
            final String key = entry.getKey();
            log.info("========>>>开始关闭FTP连接：{}", key);
            final BaseFtpHelper<?> ftpHelper = entry.getValue();
            // 关闭ftp连接
            ftpHelper.disconnect();
            log.info("========>>>成功关闭FTP连接：{}", key);
        }
    }
}
