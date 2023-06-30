package com.zwy.monitor.service;


import com.zwy.monitor.common.RestResult;
import com.zwy.monitor.web.request.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @author zwy
 * @date 2022年04月21日 16:00
 */
public interface FileService {
    /**
     * 检查分片是否存在
     *
     * @param req    req
     * @param userId userId
     * @return com.example.myfile.common.RestResult
     * @author zwy
     * @date 2022/4/21 0021 16:04
     */
    RestResult checkExists(CheckUploadRequest req, String userId);

    /**
     * 文件上传
     *
     * @param req    req
     * @param userId userId
     * @return com.example.myfile.common.RestResult
     * @author zwy
     * @date 2022/4/21 0021 16:04
     */
    RestResult upload(UploadRequest req, String userId);

    /**
     * 文件下载
     *
     * @param req      req
     * @param response response
     * @author zwy
     * @date 2022/6/28 0028 14:17
     */
    void download(DownloadRequest req, HttpServletResponse response);


    /**
     * 创建目录
     *
     * @param req    req
     * @param userId userId
     * @return com.example.myfile.common.RestResult
     * @author zwy
     * @date 2022/5/17 0017 11:21
     */
    RestResult createDirectory(CreateDirectoryRequest req, String userId);

    /**
     * 重命名文件
     *
     * @param req    req
     * @param userId userId
     * @return com.example.myfile.common.RestResult
     * @author zwy
     * @date 2022/11/15 0015 13:21
     */
    RestResult rename(RenameFileRequest req, String userId);

    /**
     * 查询目录
     *
     * @param req req
     * @return com.example.myfile.common.RestResult
     * @author zwy
     * @date 2022/5/17 0017 14:03
     */
    RestResult selectDirectory(SelectFileRequest req);

    /**
     * 文件删除
     *
     * @param req    req
     * @param userId userId
     * @return com.example.myfile.common.RestResult
     * @author zwy
     * @date 2022/6/27 0027 14:38
     */
    RestResult deleteDirectory(DeleteDirectoryRequest req, String userId);


    /**
     * 文件收藏
     *
     * @param req req
     * @return com.example.myfile.common.RestResult
     * @author zwy
     * @date 2022/7/8 0008 11:03
     */
    RestResult favoriteDirectory(FavoriteDirectoryRequest req);

}
