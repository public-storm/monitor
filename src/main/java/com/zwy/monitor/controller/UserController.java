package com.zwy.monitor.controller;

import com.zwy.monitor.aspect.ActionPoint;
import com.zwy.monitor.common.Constants;
import com.zwy.monitor.common.RestResult;
import com.zwy.monitor.common.RestResultBuilder;
import com.zwy.monitor.service.UserService;
import com.zwy.monitor.web.request.UserRegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author zwy
 * @date 2022年04月11日 15:43
 */
@RestController
@Slf4j
public class UserController extends BaseController {
    @Resource
    private UserService userService;

    @ActionPoint(reqType = 1, remark = Constants.CREATE_USER)
    @PostMapping("/register")
    public RestResult userRegister(@Valid @RequestBody UserRegisterRequest req) {
        return res(() -> userService.createUser(req), "创建用户异常");
    }


    @GetMapping("/user")
    public RestResult findUser() {
        return res(() -> RestResultBuilder.success().data(findUserModel()), "查询用户异常");
    }
}
