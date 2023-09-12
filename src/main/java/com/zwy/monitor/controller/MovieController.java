package com.zwy.monitor.controller;

import com.zwy.monitor.common.RestResult;
import com.zwy.monitor.service.MovieService;
import com.zwy.monitor.web.request.movie.RenameMovieRequest;
import com.zwy.monitor.web.request.movie.UploadSplitRequest;
import com.zwy.monitor.web.request.movie.CheckUploadRequest;
import com.zwy.monitor.web.response.movie.CheckExistsResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author zwy
 * @date 2023/9/12 11:15
 */
@RestController
@RequestMapping("/movie")
public class MovieController extends BaseController {
    @Resource
    private MovieService movieService;

    /**
     * 检查视频是否上传
     *
     * @param req com.zwy.monitor.web.request.movie.CheckUploadRequest
     * @return RestResult<CheckExistsResponse>
     */
    @GetMapping("/upload/check")
    public RestResult<CheckExistsResponse> checkUpload(@Valid CheckUploadRequest req) {
        return res(() -> {
            req.setUserId(findUserModel().getId());
            return movieService.checkExists(req);
        }, "检查视频是否上传异常");
    }

    /**
     * 视频分片上传
     *
     * @param req com.zwy.monitor.web.request.movie.UploadSplitRequest
     * @return RestResult<String>
     */
    @PostMapping(value = "/upload/split", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RestResult<String> uploadSplit(@Valid UploadSplitRequest req) {
        return res(() -> {
            req.setUserId(findUserModel().getId());
            return movieService.uploadSplit(req);
        }, "视频分片上传异常");
    }

    @PutMapping("/rename")
    public RestResult<String> rename(@Valid RenameMovieRequest req) {
        return res(() -> {
            req.setUserId(findUserModel().getId());
            return movieService.rename(req);
        }, "视频重命名异常");
    }


}
