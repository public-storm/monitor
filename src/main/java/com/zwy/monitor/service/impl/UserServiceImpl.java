package com.zwy.monitor.service.impl;

import cn.hutool.core.util.IdUtil;
import com.zwy.monitor.bean.dbBean.User;
import com.zwy.monitor.web.request.UserRegisterRequest;
import com.zwy.monitor.common.RestResult;
import com.zwy.monitor.mapper.UserMapper;
import com.zwy.monitor.service.UserService;
import com.zwy.monitor.util.PasswordUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zwy
 * @date 2023/5/27 20:07
 */
@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

    @Override
    public RestResult createUser(UserRegisterRequest req) {
        String pwd = PasswordUtil.generatePassword(req.getPassword());
        User user = new User();
        user.setId(IdUtil.simpleUUID());
        user.setUserName(req.getUserName());
        user.setPassword(pwd);
        user.setRoleId("1");
        userMapper.insert(user);
        return RestResult.success();
    }
}
