package com.zwy.monitor.service;


import com.zwy.monitor.common.RestResult;
import com.zwy.monitor.web.request.*;
import com.zwy.monitor.web.response.CheckExistsResponse;
import com.zwy.monitor.web.response.FindDownloadChunkResponse;
import com.zwy.monitor.web.response.FindHistoryFileResponse;
import com.zwy.monitor.web.response.SelectFileResponse;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author zwy
 * @date 2022年04月21日 16:00
 */
public interface FileService {

    RestResult<CheckExistsResponse> checkExists(CheckUploadRequest req);

    RestResult<String> uploadSplit(UploadSplitRequest req);


    RestResult<String> upload(UploadRequest req);


    void download(DownloadRequest req, HttpServletResponse response);



    RestResult<String> createDirectory(CreateDirectoryRequest req);


    RestResult<String> rename(RenameFileRequest req);


    RestResult<List<SelectFileResponse>> selectDirectory(SelectFileRequest req);

    RestResult<String> deleteDirectory(DeleteDirectoryRequest req);


    RestResult<List<FindHistoryFileResponse>> findHistoryFile(String userId);

    RestResult<String> delAllHistoryFile(String userId);

    RestResult<FindDownloadChunkResponse> findDownloadChunk(FindDownloadChunkRequest req);

    ResponseEntity<byte[]> download(DownloadRequest req);

}
