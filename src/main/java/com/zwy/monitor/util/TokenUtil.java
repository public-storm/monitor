package com.zwy.monitor.util;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.zwy.monitor.common.MyRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zwy
 * @date 2022年11月08日 14:11
 */
@Slf4j
public class TokenUtil {
    private TokenUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 签名
     */
    private static final String SIGN = "oJw)3d_v0?>1,<WI";
    /**
     * jwt的过期时间，这个过期时间必须要大于签发时间
     */
    private static final String EXPIRES_AT = "exp";
    /**
     * 生效时间，定义在什么时间之前，该jwt都是不可用的.
     */
    private static final String NOT_BEFORE = "nbf";
    /**
     * jwt的签发时间
     */
    private static final String ISSUED_AT = "iat";
    /**
     * 有效时间
     */
    private static final String EXPIRES_HOUR = "exp_hour";
    /**
     * 用户id
     */
    public static final String USER_ID = "userId";
    /**
     * 用户名
     */
    public static final String USER_NAME = "userName";
    /**
     * token sign
     */
    public static final String TOKEN_SIGN = "token";

    /**
     * 创建token
     *
     * @param payload 携带参数
     * @param exp token过期天数
     * @return java.lang.String
     * @author zwy
     * @date 2022/11/8 0008 16:05
     */
    public static String createToken(Map<String, Object> payload, int exp) {
        if (payload == null) {
            throw new MyRuntimeException("创建token异常 payload不能为null");
        }
        Date now = DateUtil.date();
        Date expTime = DateUtil.offset(now, DateField.MINUTE, exp);
        //签发时间
        payload.put(ISSUED_AT, now);
        //生效时间
        payload.put(NOT_BEFORE, now);
        //过期时间
        payload.put(EXPIRES_AT, expTime);
        payload.put(EXPIRES_HOUR, exp);
        return JWTUtil.createToken(payload, SIGN.getBytes());
    }

    /**
     * token解析和验证
     *
     * @param token token
     * @return cn.hutool.jwt.JWTPayload
     * @author zwy
     * @date 2022/11/8 0008 16:06
     */
    public static JWT parseToken(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        jwt.setKey(SIGN.getBytes());
        return jwt;
    }

    /**
     *  生成新token
     * @author zwy
     * @date 2022/11/9 0009 16:29
     * @param jwtPayload jwtPayload
     * @return java.lang.String
     */
    public static String refreshToken(JWTPayload jwtPayload) {
        JSONObject jsonObject = jwtPayload.getClaimsJson();
        Object userId = jsonObject.get(USER_ID);
        Object userName = jsonObject.get(USER_NAME);
        Integer expHour = (Integer) jsonObject.get(EXPIRES_HOUR);
        Map<String,Object> map = new HashMap<>(8);
        map.put(USER_ID,userId);
        map.put(USER_NAME,userName);
        return createToken(map,expHour);
    }
}
