package com.zwy.monitor.service;

import com.zwy.monitor.web.request.UploadSplitRequest;

/**
 * @author zwy
 * @date 2022年07月22日 14:44
 */
public interface FileMargeService {
    /**
     * 异步合并文件
     *
     * @param req req
     * @author zwy
     * @date 2022/7/22 0022 14:46
     */
    void fileMarge(UploadSplitRequest req);

    void updateFile(String id);

    void changeDirSize(String superId, long totalSize,boolean add);

    void changeRedisStatus(String userId, String fileName, String superId);

    void addHistory(String fileName,long size,String userId);
}
