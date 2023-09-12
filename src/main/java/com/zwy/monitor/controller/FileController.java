package com.zwy.monitor.controller;

import com.zwy.monitor.common.RestResult;
import com.zwy.monitor.service.FileService;
import com.zwy.monitor.web.request.*;
import com.zwy.monitor.web.request.file.CheckUploadRequest;
import com.zwy.monitor.web.request.file.RenameFileRequest;
import com.zwy.monitor.web.request.file.UploadSplitRequest;
import com.zwy.monitor.web.response.file.CheckExistsResponse;
import com.zwy.monitor.web.response.FindDownloadChunkResponse;
import com.zwy.monitor.web.response.FindHistoryFileResponse;
import com.zwy.monitor.web.response.SelectFileResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;

/**
 * @author zwy
 * @date 2022年04月21日 9:16
 */
@RestController
@Slf4j
@RequestMapping("/file")
public class FileController extends BaseController {
    @Resource
    FileService fileService;

    /**
     * 检查文件上传
     *
     * @param req com.zwy.monitor.web.request.file.CheckUploadRequest
     * @return RestResult<CheckExistsResponse>
     */
    @GetMapping("/upload/check")
    public RestResult<CheckExistsResponse> checkExists(@Valid CheckUploadRequest req) {
        return res(() -> {
            req.setUserId(findUserModel().getId());
            return fileService.checkExists(req);
        }, "检查文件是否上传异常");
    }

    @PostMapping(value = "/upload/split", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RestResult<String> uploadSplit(@Valid UploadSplitRequest req) {
        return res(() -> {
            req.setUserId(findUserModel().getId());
            return fileService.uploadSplit(req);
        }, "文件分片上传异常");
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RestResult<String> upload(@Valid UploadRequest req) {
        return res(() -> {
            req.setUserId(findUserModel().getId());
            return fileService.upload(req);
        }, "文件上传异常");
    }


    @PostMapping("/directory")
    public RestResult<String> createDirectory(@Valid @RequestBody CreateDirectoryRequest req) {
        return res(() -> {
            req.setUserId(findUserModel().getId());
            return fileService.createDirectory(req);
        }, "创建目录异常");
    }

    @PutMapping("/rename")
    public RestResult<String> rename(@Valid RenameFileRequest req) {
        return res(() -> {
            req.setUserId(findUserModel().getId());
            return fileService.rename(req);
        }, "重命名异常");
    }

    @GetMapping("/directory")
    public RestResult<List<SelectFileResponse>> directoryList(@Valid SelectFileRequest req) {
        return res(() -> {
            req.setUserId(findUserModel().getId());
            return fileService.selectDirectory(req);
        }, "查询目录异常");
    }

    @DeleteMapping("/directory")
    public RestResult<String> deleteDirectory(@Valid DeleteDirectoryRequest req) {
        return res(() -> {
            req.setUserId(findUserModel().getId());
            return fileService.deleteDirectory(req);
        }, "文件删除异常");
    }


    @GetMapping("/history")
    public RestResult<List<FindHistoryFileResponse>> findHistoryFile() {
        return res(() -> fileService.findHistoryFile(findUserModel().getId()), "查询历史文件异常");
    }

    @DeleteMapping("/history")
    public RestResult<String> delAllHistoryFile() {
        return res(() -> fileService.delAllHistoryFile(findUserModel().getId()), "删除所有历史文件异常");
    }


    @GetMapping("/download/chunk")
    public RestResult<FindDownloadChunkResponse> findDownloadChunk(@Valid FindDownloadChunkRequest req) {
        return res(() -> fileService.findDownloadChunk(req), "查询文件下载总分片数异常");
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@Valid DownloadRequest req) {
        return fileService.download(req);
    }


    @GetMapping("/preview")
    public void test(HttpServletResponse response, @RequestParam int index) {
        String path = "H:\\file\\t2\\preview\\" + index + ".mp4";
        response.setContentType("video/mp4");
        File file = new File(path);
        response.addHeader("Content-Length", String.valueOf(file.length()));
        try (InputStream is = Files.newInputStream(file.toPath());
             OutputStream os = response.getOutputStream();
        ) {
            IOUtils.copy(is, os);
        } catch (Exception e) {
            log.error("播放预览失败", e);
        }
    }

    @GetMapping("/hls/{name}")
    public void hls(HttpServletResponse response, @PathVariable("name") String name) {
        log.info("hls 调用 name {}", name);
        String path = "H:\\t2\\hls\\" + name;
        File file = new File(path);
        response.addHeader("Content-Length", String.valueOf(file.length()));
        try (InputStream is = Files.newInputStream(file.toPath());
             OutputStream os = response.getOutputStream()) {
            IOUtils.copy(is, os);
        } catch (Exception e) {
            log.error("hls 获取异常", e);
        }
    }
}
