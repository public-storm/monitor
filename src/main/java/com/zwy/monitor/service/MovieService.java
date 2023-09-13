package com.zwy.monitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zwy.monitor.bean.dbBean.UserMovie;
import com.zwy.monitor.common.PageInfo;
import com.zwy.monitor.common.RestResult;
import com.zwy.monitor.web.request.movie.*;
import com.zwy.monitor.web.response.movie.CheckExistsResponse;
import com.zwy.monitor.web.response.movie.FindUserMovieResponse;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author zwy
 * @date 2023/9/12 11:49
 */
public interface MovieService {

    RestResult<CheckExistsResponse> checkExists(CheckUploadRequest req);

    RestResult<String> uploadSplit(UploadSplitRequest req);

    RestResult<String> rename(RenameMovieRequest req);

    RestResult<PageInfo> findPage(FindUserMovieRequest req);

    RestResult<String> updateTag(UpdateTagRequest req);

    void findHls(String id, String fileName, HttpServletResponse response);
}
