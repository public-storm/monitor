package com.zwy.monitor.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import com.zwy.monitor.bean.UserModel;
import com.zwy.monitor.common.Constants;
import com.zwy.monitor.common.RestResult;
import com.zwy.monitor.common.RestResultBuilder;
import com.zwy.monitor.util.TokenUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.function.Supplier;

/**
 * @author zwy
 * @date 2022年04月11日 15:10
 */
@Slf4j
public class BaseController {
    @Resource
    protected HttpServletRequest request;

    @Resource
    protected HttpServletResponse response;

    /**
     * 返回结果添加异常捕获
     *
     * @param supplier 具体实现
     * @param msg      异常信息
     * @return Result
     */
    protected <T> RestResult<T> res(Supplier<RestResult<T>> supplier, String msg) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.error(msg, e);
            return RestResultBuilder.err(Constants.SERVER_ERROR_CODE, e.getMessage());
        }
    }


    protected UserModel findUserModel() {
        UserModel userModel = new UserModel();
        String token = request.getHeader(TokenUtil.TOKEN_SIGN);
        JWT jwt = TokenUtil.parseToken(token);
        JWTPayload jwtPayload = jwt.getPayload();
        JSONObject jsonObject = jwtPayload.getClaimsJson();
        String userId = (String) jsonObject.get(Constants.USER_ID);
        String userName = (String) jsonObject.get(Constants.USER_NAME);
        userModel.setId(userId);
        userModel.setName(userName);
        return userModel;
    }
}
