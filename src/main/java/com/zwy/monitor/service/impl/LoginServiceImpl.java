package com.zwy.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zwy.monitor.bean.dbBean.User;
import com.zwy.monitor.beanManager.UserBeanManager;
import com.zwy.monitor.common.ResponseEnum;
import com.zwy.monitor.common.RestResult;
import com.zwy.monitor.common.RestResultBuilder;
import com.zwy.monitor.mapper.UserMapper;
import com.zwy.monitor.service.LoginService;
import com.zwy.monitor.util.PasswordUtil;
import com.zwy.monitor.util.TokenUtil;
import com.zwy.monitor.web.request.UserLoginRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zwy
 * @date 2022年04月18日 13:17
 */
@Service
@Slf4j
public class LoginServiceImpl implements LoginService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public RestResult login(HttpServletRequest request, HttpServletResponse response, UserLoginRequest req) {
        String password = PasswordUtil.generatePassword(req.getPassword());
        req.setPassword(password);
        User user = userMapper.selectOne(new QueryWrapper<User>().setEntity(UserBeanManager.INSTANCE.toUser(req)));
        if (user != null) {
            Map<String, Object> map = new HashMap<>(8);
            map.put(TokenUtil.USER_ID, user.getId());
            map.put(TokenUtil.USER_NAME, user.getUserName());
            String token = TokenUtil.createToken(map, 1);
            redisTemplate.opsForValue().set(token, token, 7, TimeUnit.DAYS);
            response.setHeader(TokenUtil.TOKEN_SIGN, token);
            return RestResultBuilder.success().data(token);
        } else {
            return RestResultBuilder.code(ResponseEnum.USER_NOT_EXISTS);
        }
    }
}
