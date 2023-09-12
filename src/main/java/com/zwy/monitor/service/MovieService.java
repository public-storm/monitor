package com.zwy.monitor.service;

import com.zwy.monitor.common.RestResult;
import com.zwy.monitor.web.request.movie.CheckUploadRequest;
import com.zwy.monitor.web.request.movie.RenameMovieRequest;
import com.zwy.monitor.web.request.movie.UploadSplitRequest;
import com.zwy.monitor.web.response.movie.CheckExistsResponse;

/**
 * @author zwy
 * @date 2023/9/12 11:49
 */
public interface MovieService {

    RestResult<CheckExistsResponse> checkExists(CheckUploadRequest req);

    RestResult<String> uploadSplit(UploadSplitRequest req);

    RestResult<String> rename(RenameMovieRequest req);
}
