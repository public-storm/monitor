package com.zwy.monitor.service;

import com.zwy.monitor.bean.FileMargeBean;

/**
 * @author zwy
 * @date 2022年07月22日 14:44
 */
public interface FileMargeService {
    /**
     * 异步合并文件
     *
     * @param bean bean
     * @author zwy
     * @date 2022/7/22 0022 14:46
     */
    void fileMarge(FileMargeBean bean);
}
