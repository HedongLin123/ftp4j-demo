package com.saxo.ftp.controller;

import cn.hutool.core.date.StopWatch;
import com.saxo.ftp.core.FtpUtil;
import com.saxo.ftp.core.helper.BaseFtpHelper;
import com.saxo.ftp.dto.req.file.FtpFileDownloadDTO;
import com.saxo.ftp.dto.req.file.FtpFileUploadDTO;
import com.saxo.ftp.dto.resp.JsonResult;
import com.saxo.ftp.enums.DescriptionEnum;
import com.saxo.ftp.enums.FtpAuthMsg;
import com.saxo.ftp.enums.FtpUploadDownloadMsg;
import com.saxo.ftp.exception.BizException;
import com.saxo.ftp.exception.FtpException;
import com.saxo.ftp.utils.HttpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description
 * @Author donglin.he
 * @Date 2022/11/25 11:41
 */
@Controller
@RequestMapping("/fileMgr")
@Slf4j
@Api(tags = "Ftp文件管理")
public class FtpFileController {
    private static final ConcurrentHashMap<String, BaseFtpHelper<?>> ftpHelpers = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> ftpHelperPwds = new ConcurrentHashMap<>();

    @PostMapping(value = "/upload", headers = "content-type=multipart/form-data", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "文件上传",notes = "文件上传",consumes = "multipart/form-data",response = Object.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "待上传文件(可多个)", paramType="form", dataType="file", collectionFormat="array", required = true, allowMultiple = true),
    })
    @ResponseBody
    public JsonResult upload(@RequestPart(value = "files", required = true) MultipartFile[] files, FtpFileUploadDTO dto){
        if (files == null || files.length == 0 || dto == null){
            log.error("上传文件不能为空");
            throw new BizException(DescriptionEnum.PARAMS_EXCEPTION.getMsg());
        }

        if (dto.getFtpType() == null){
            return JsonResult.buildFailure(DescriptionEnum.PARAMS_EXCEPTION.getCode(), "请求参数ftpType不能为空");
        }

        if (StringUtils.isBlank(dto.getFtpUploadPath())){
            return JsonResult.buildFailure(DescriptionEnum.PARAMS_EXCEPTION.getCode(), "请求参数ftpUploadPath不能为空");
        }

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
                log.info("======>>>文件：{}上传成功，耗时：{}s", originalFilename, stopWatch.getTotalTimeSeconds());
            } catch (Exception e) {
                stopWatch.stop();
                log.error("======>>>文件：{}上传失败，耗时：{}s", originalFilename, stopWatch.getTotalTimeSeconds());
                e.printStackTrace();
                throw new FtpException(FtpUploadDownloadMsg.FTP_UPLOAD_FILE_ERR.getMsg());
            }
        }
        return JsonResult.buildSuccess();
    }


    @RequestMapping(value = "/download", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public void download(HttpServletRequest request, HttpServletResponse response, FtpFileDownloadDTO dto){
        if (dto == null){
            log.error("请求参数不能为空");
            throw new BizException(DescriptionEnum.PARAMS_EXCEPTION.getMsg());
        }

        if (dto.getFtpType() == null){
            throw new BizException("请求参数ftpType不能为空");
        }

        if (StringUtils.isBlank(dto.getFtpDownloadPath())){
            throw new BizException("请求参数ftpDownloadPath不能为空");
        }

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
                    log.info("========从缓存中获取ftp连接成功，且连接状态正常，耗时：{}s", stopWatch.getTotalTimeSeconds());
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
        // todo:这里需要对path进行处理
        String downloadPath = dto.getFtpDownloadPath();
        final String downloadFileName = downloadPath.substring(downloadPath.lastIndexOf("/") + 1);
        try(final ByteArrayOutputStream outputStream = ftpHelper.downloadFile(downloadPath);) {
            if (outputStream == null){
                log.error("文件不存在");
                throw new FtpException(FtpUploadDownloadMsg.FTP_DOWNLOAD_FILE_NOT_EXISTS.getMsg());
            }
            // 将输出流转换为输入流
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            // 执行下载
            HttpUtil.download(request, response, inputStream, downloadFileName);
            stopWatch.stop();
            log.info("======>>>文件：{}下载成功，耗时：{}s", downloadPath, stopWatch.getTotalTimeSeconds());
        } catch (Exception e) {
            stopWatch.stop();
            log.error("======>>>文件：{}下载失败，耗时：{}s", downloadPath, stopWatch.getTotalTimeSeconds());
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
        log.info("========创建ftp连接成功，耗时：{}s", stopWatch.getTotalTimeSeconds());
        ftpHelpers.put(mapKey, ftpHelper);
        ftpHelperPwds.put(mapKey, dto.getFtpPassword());
        return ftpHelper;
    }


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
