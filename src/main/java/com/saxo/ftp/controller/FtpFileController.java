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
import com.saxo.ftp.service.FtpFileService;
import com.saxo.ftp.utils.DoubleUtils;
import com.saxo.ftp.utils.HttpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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


    @Autowired
    private FtpFileService ftpFileService;

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

        // 执行文件上传操作
        ftpFileService.upload(files, dto);

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

        // 对path进行处理
        String downloadPath = HttpUtil.replaceFilePath(dto.getFtpDownloadPath());
        final String downloadFileName = downloadPath.substring(downloadPath.lastIndexOf("/") + 1);

        // 下载ftp的文件为输出流
        final ByteArrayOutputStream outputStream = ftpFileService.download(dto);

        // 将输出流转换为输入流
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 执行http下载
        HttpUtil.download(request, response, inputStream, downloadFileName);
        stopWatch.stop();
        log.info("======>>>文件：{}从转换为http下载成功，耗时：{}s", downloadPath, DoubleUtils.formatDouble(stopWatch.getTotalTimeSeconds()));
    }

}
