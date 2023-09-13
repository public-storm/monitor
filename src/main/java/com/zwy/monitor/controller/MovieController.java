package com.zwy.monitor.controller;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.HashUtil;
import com.zwy.monitor.common.MyRuntimeException;
import com.zwy.monitor.common.PageInfo;
import com.zwy.monitor.common.RestResult;
import com.zwy.monitor.service.MovieService;
import com.zwy.monitor.web.request.movie.*;
import com.zwy.monitor.web.response.movie.CheckExistsResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
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

    /**
     * 重命名
     *
     * @param req com.zwy.monitor.web.request.movie.RenameMovieRequest
     * @return RestResult<String>
     */
    @PutMapping("/rename")
    public RestResult<String> rename(@Valid RenameMovieRequest req) {
        return res(() -> {
            req.setUserId(findUserModel().getId());
            return movieService.rename(req);
        }, "视频重命名异常");
    }

    /**
     * 分页查询
     *
     * @param req com.zwy.monitor.web.request.movie.FindUserMovieRequest
     * @return RestResult<PageInfo>
     */
    @GetMapping("/list")
    public RestResult<PageInfo> findPage(@Valid FindUserMovieRequest req) {
        return res(() -> {
            req.setUserId(findUserModel().getId());
            if (CharSequenceUtil.isNotBlank(req.getTag())) {
                req.setHashTag(String.valueOf(HashUtil.fnvHash(req.getTag())));
            }
            return movieService.findPage(req);
        }, "视频列表分页查询异常");
    }

    /**
     * 修改视频标签
     *
     * @param req com.zwy.monitor.web.request.movie.UpdateTagRequest
     * @return RestResult<String>
     */
    @PutMapping("/tag")
    public RestResult<String> updateTag(@Valid UpdateTagRequest req) {
        return res(() -> {
            req.setUserId(findUserModel().getId());
            return movieService.updateTag(req);
        }, "修改视频标签异常");
    }


    /**
     * 获取hls文件
     *
     * @param id       视频id
     * @param fileName 文件名
     */
    @GetMapping("/hls/{id}/{fileName}")
    public void findHls(@PathVariable("id") String id,
                        @PathVariable("fileName") String fileName,
                        HttpServletResponse response) {
        if (CharSequenceUtil.isBlank(id)) {
            throw new MyRuntimeException("视频id不可为空");
        }
        if (CharSequenceUtil.isBlank(fileName)) {
            throw new MyRuntimeException("文件名不可为空");
        }
        movieService.findHls(id, fileName, response);
    }


}
