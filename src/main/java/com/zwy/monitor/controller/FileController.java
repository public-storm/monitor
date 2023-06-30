package com.zwy.monitor.controller;

import com.zwy.monitor.common.RestResult;
import com.zwy.monitor.service.FileService;
import com.zwy.monitor.web.request.*;
import com.zwy.monitor.web.response.CheckExistsResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author zwy
 * @date 2022年04月21日 9:16
 */
@RestController
@Slf4j
public class FileController extends BaseController {
    @Resource
    FileService fileService;

    @GetMapping("/upload")
    @ApiOperation(value = "检查文件是否上传", notes = "检查文件是否上传")
    @ApiResponses({
            @ApiResponse(code = 200, message = "检查文件是否上传", response = CheckExistsResponse.class),
    })
    public RestResult checkExists(@Valid CheckUploadRequest req) {
        return res(() -> fileService.checkExists(req, findUserModel().getId()), "检查文件是否上传异常");
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "文件上传", notes = "文件上传")
    @ApiResponses({
            @ApiResponse(code = 200, message = "上传成功"),
    })
    public RestResult upload(@Valid UploadRequest req) {
        log.debug("当前片 {} 总片数 {}",req.getChunkNumber(),req.getTotalChunks());
        return res(() -> fileService.upload(req, findUserModel().getId()), "文件上传异常");
    }

    @PostMapping("/directory")
    @ApiOperation(value = "创建目录", notes = "创建目录")
    @ApiResponses({
            @ApiResponse(code = 200, message = "创建成功"),
    })
    public RestResult createDirectory(@Valid @RequestBody CreateDirectoryRequest req) {
        return res(() -> fileService.createDirectory(req, findUserModel().getId()), "创建目录异常");
    }

    @PutMapping("/rename")
    @ApiOperation(value = "重命名", notes = "重命名")
    @ApiResponses({
            @ApiResponse(code = 200, message = "重命名成功"),
    })
    public RestResult rename(@Valid RenameFileRequest req) {
        return res(() -> fileService.rename(req, findUserModel().getId()), "重命名异常");
    }

    @GetMapping("/directory")
    @ApiOperation(value = "查看目录", notes = "查看目录")
    @ApiResponses({
            @ApiResponse(code = 200, message = "查询成功"),
    })
    public RestResult directoryList(@Valid SelectFileRequest req) {
        return res(() -> fileService.selectDirectory(req), "查询目录异常");
    }

    @DeleteMapping("/directory")
    @ApiOperation(value = "文件删除", notes = "文件删除")
    @ApiResponses({
            @ApiResponse(code = 200, message = "创建成功"),
    })
    public RestResult deleteDirectory(@Valid DeleteDirectoryRequest req) {
        return res(() -> fileService.deleteDirectory(req, findUserModel().getId()), "文件删除异常");
    }

    @PostMapping("/download")
    @ApiOperation(value = "文件下载", notes = "文件下载")
    @ApiResponses({
            @ApiResponse(code = 200, message = "文件下载成功"),
    })
    public void download(@Valid @RequestBody DownloadRequest req) {
        try {
            fileService.download(req, response);
        } catch (Exception e) {
            log.error("文件下载异常", e);
        }
    }

    @PutMapping("/favorite")
    @ApiOperation(value = "修改收藏", notes = "修改收藏")
    @ApiResponses({
            @ApiResponse(code = 200, message = "修改成功"),
    })
    public RestResult favoriteDirectory(@Valid FavoriteDirectoryRequest req) {
        return res(() -> fileService.favoriteDirectory(req), "修改收藏异常");
    }
}
