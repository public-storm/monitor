package com.zwy.monitor.service;

import com.zwy.monitor.common.RestResult;
import com.zwy.monitor.web.request.UserRegisterRequest;

/**
 * @author zwy
 * @date 2023/5/27 19:59
 */
public interface UserService {
    /**
     * 创建用户
     *
     * @param req req
     * @return RestResult
     */
    RestResult<String> createUser(UserRegisterRequest req);
}
