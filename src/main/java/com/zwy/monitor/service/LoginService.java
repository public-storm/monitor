package com.zwy.monitor.service;


import com.zwy.monitor.common.RestResult;
import com.zwy.monitor.web.request.UserLoginRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zwy
 * @date 2022年04月18日 13:16
 */
public interface LoginService {
    /**
     * login
     *
     * @param request request
     * @param response response
     * @param req     req
     * @return com.example.myfile.common.RestResult
     * @author zwy
     * @date 2022/4/18 0018 13:18
     */
    RestResult login(HttpServletRequest request, HttpServletResponse response, UserLoginRequest req);
}
