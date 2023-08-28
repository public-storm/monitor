package com.zwy.monitor.config;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import com.zwy.monitor.common.Constants;
import com.zwy.monitor.common.ResponseEnum;
import com.zwy.monitor.common.RestResult;
import com.zwy.monitor.util.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * @author zwy
 * @date 2022年04月13日 16:56
 */
//@Component
@Slf4j
public class MyInterceptor implements HandlerInterceptor {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (checkToken(request)) {
            return true;
        } else {
            RestResult result = new RestResult();
            ResponseEnum.NO_LOGIN.toResult(result);
            responseJson(response, result);
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    private void responseJson(HttpServletResponse response, Object obj) {
        try {
            response.setContentType(Constants.CONTENT_TYPE);
            PrintWriter writer = response.getWriter();
            writer.print(JSONUtil.parseObj(obj, false));
            writer.close();
            response.flushBuffer();
        } catch (Exception e) {
            log.error("responseJson error", e);
        }
    }

    /**
     * 检查session
     *
     * @param request request
     * @return boolean
     * @author zwy
     * @date 2022/4/21 0021 17:54
     */
    private boolean checkToken(HttpServletRequest request) {
        boolean auth = false;
        String token = request.getHeader(TokenUtil.TOKEN_SIGN);
        String url = request.getServletPath();
        if (CharSequenceUtil.isNotBlank(token)) {
            String tokenValue = (String) redisTemplate.opsForValue().get(token);
            //token刷新时间未过期
            if (CharSequenceUtil.isNotBlank(tokenValue)) {
                JWT jwt = TokenUtil.parseToken(tokenValue);
                JWTPayload jwtPayload = jwt.getPayload();
                boolean bolVerifyToken = jwt.validate(0);
                //判断token是否过期
                auth = true;
                if (!bolVerifyToken) {
                    //换取新token
                    String newTokenValue = TokenUtil.refreshToken(jwtPayload);
                    redisTemplate.opsForValue().set(token, newTokenValue, 7, TimeUnit.DAYS);
                }
            } else {
                log.info("token刷新时间过期 请重新登录 servletPath {}", url);
            }
        } else {
            log.info("未携带token servletPath {}", url);
        }
        return auth;
    }
}
