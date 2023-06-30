package com.zwy.monitor.controller;

import com.zwy.monitor.aspect.ActionPoint;
import com.zwy.monitor.common.Constants;
import com.zwy.monitor.common.RestResult;
import com.zwy.monitor.service.LoginService;
import com.zwy.monitor.web.request.UserLoginRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author zwy
 * @date 2022年04月11日 15:27
 */
@RestController
@Slf4j
public class LoginController extends BaseController {
    @Resource
    private LoginService loginService;

    @ActionPoint(reqType = 1, remark = Constants.LOGIN)
    @PostMapping("/login")
    public RestResult userLogin(@Valid @RequestBody UserLoginRequest req) {
        return res(() -> loginService.login(request, response, req), "登录异常");
    }

}
