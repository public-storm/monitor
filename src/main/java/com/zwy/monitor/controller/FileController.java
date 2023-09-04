package com.zwy.monitor.controller;

import com.zwy.monitor.common.Constants;
import com.zwy.monitor.common.MyRuntimeException;
import com.zwy.monitor.common.RestResult;
import com.zwy.monitor.common.RestResultBuilder;
import com.zwy.monitor.service.FileService;
import com.zwy.monitor.web.request.*;
import com.zwy.monitor.web.response.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author zwy
 * @date 2022年04月21日 9:16
 */
@RestController
@Slf4j
public class FileController extends BaseController {
    @Resource
    FileService fileService;

    /**
     * 检查文件上传
     *
     * @param req com.zwy.monitor.web.request.CheckUploadRequest
     * @return RestResult<CheckExistsResponse>
     */
    @GetMapping("/upload")
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

    @GetMapping("/video1")
    public void test(HttpServletResponse response) {
//        String path = "H:\\file\\6afb389dcc2a45479eb1677d9d8c9294\\f791d703d579470caa9b75e5a371db5c\\f791d703d579470caa9b75e5a371db5c.mp4";
        String path = "H:\\file\\t2\\沙漠往事.mp4";
//        String path = "E:\\ali_download\\沙漠往事 Odnazhdy.v.pustyne.2022.1080P.中文字幕.mp4";
//        String path = "E:\\edge_download\\f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8";
        log.info("文件路径 {}", path);
        InputStream is = null;
        OutputStream os = null;
        try {
            response.setContentType("video/mp4");
            File file = new File(path);
            response.addHeader("Content-Length", "" + file.length());
            is = Files.newInputStream(file.toPath());
            os = response.getOutputStream();
            IOUtils.copy(is, os);
        } catch (Exception e) {
            log.error("播放MP4失败", e);
        } finally {
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @GetMapping("/video2")
    public ResponseEntity<byte[]> test2() throws IOException {
//        String path = "H:\\file\\6afb389dcc2a45479eb1677d9d8c9294\\f791d703d579470caa9b75e5a371db5c\\f791d703d579470caa9b75e5a371db5c.mp4";
//        String path = "H:\\file\\t\\test.mp4";
//        String path = "H:\\file\\t2\\沙漠往事.mp4";
        String path = "H:\\file\\t2\\test.mpd";
        File videoFile = new File(path);
        byte[] videoBytes = Files.readAllBytes(Paths.get(videoFile.getAbsolutePath()));
        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.parseMediaType("video/mp4"));
        headers.setContentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"));
        headers.setContentLength(videoBytes.length);
        return new ResponseEntity<>(videoBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/video3/{path}")
    public ResponseEntity<byte[]> test3(@PathVariable("path") String p) throws IOException {
        String path = "H:\\file\\t\\" + p;
        File videoFile = new File(path);
        byte[] videoBytes = Files.readAllBytes(Paths.get(videoFile.getAbsolutePath()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("video/mp4"));
        headers.setContentLength(videoBytes.length);
        return new ResponseEntity<>(videoBytes, headers, HttpStatus.OK);
    }


    @GetMapping("/video4")
    public ResponseEntity<byte[]> test4(@RequestParam Long index) {
        long chunkSize = 1024 * 1024 * 5L;
//        String path = "H:\\file\\t\\test.mp4";
        String path = "H:\\file\\t2\\沙漠往事.mp4";
        long quotient = findChunkSize(path, chunkSize);
        if (index > quotient || index <= 0) {
            throw new MyRuntimeException("分片索引超出范围 " + index);
        }
        long endPosition = index * chunkSize;
        long startPosition = endPosition - chunkSize;
        try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
            raf.seek(startPosition);
            byte[] buffer = new byte[(int) chunkSize];
            raf.read(buffer);
            return new ResponseEntity<>(buffer, HttpStatus.OK);
        } catch (Exception e) {
            log.error("分片文件读取异常", e);
            return null;
        }
    }

    @GetMapping("/video5")
    public RestResult<Long> test5() {
        long chunkSize = 1024 * 1024 * 5L;
//        String path = "H:\\file\\t\\test.mp4";
        String path = "H:\\file\\t2\\沙漠往事.mp4";
        return RestResultBuilder.<Long>success().data(findChunkSize(path, chunkSize));
    }

    private long findChunkSize(String path, long chunkSize) {
        File file = new File(path);
        long size = file.length();
        long quotient = size / chunkSize;
        long remainder = size % chunkSize;
        if (remainder > 0) {
            quotient++;
        }
        return quotient;
    }

    private final String mediaFolderPath = "H:\\file\\t";

    @GetMapping(value = "/manifest", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<byte[]> getManifest() {
        String manifestPath = mediaFolderPath + "/t.mpd";
        log.info("manifest 接口文件路径 {}", manifestPath);
        return getByteArrayResponse(manifestPath);
    }

    @GetMapping(value = "/{segmentName:.+}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getSegment(@PathVariable String segmentName) {
        log.info("m4s 文件名 {}", segmentName);
        String segmentPath = mediaFolderPath + "/" + segmentName;
        log.info("m4s 文件路径 {}", segmentPath);
        return getByteArrayResponse(segmentPath);
    }

    private ResponseEntity<byte[]> getByteArrayResponse(String filePath) {
        Path path = Paths.get(filePath);
        byte[] content;
        try {
            content = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(content.length);
        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }
}
