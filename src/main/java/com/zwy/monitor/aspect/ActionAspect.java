package com.zwy.monitor.aspect;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.zwy.monitor.bean.UserModel;
import com.zwy.monitor.bean.dbBean.Action;
import com.zwy.monitor.bean.dbBean.UserActionRecord;
import com.zwy.monitor.common.Constants;
import com.zwy.monitor.common.RestResult;
import com.zwy.monitor.mapper.ActionMapper;
import com.zwy.monitor.mapper.UserActionRecordMapper;
import com.zwy.monitor.util.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author zwy
 * @date 2023/6/27 9:15
 */
@Component
@Slf4j
@Aspect
public class ActionAspect {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private ActionMapper actionMapper;
    @Resource
    private UserActionRecordMapper userActionRecordMapper;

    @Pointcut("@annotation(com.zwy.monitor.aspect.ActionPoint)")
    public void pointCut() {
        //切点表达式
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint point) {
        Object pointObj = null;
        try {
            pointObj = point.proceed();
        } catch (Throwable throwable) {
            log.error("切面逻辑执行异常", throwable);
        }
        addAction(pointObj, point);
        return pointObj;
    }

    private void addAction(Object pointObj, ProceedingJoinPoint point) {
        if (pointObj != null) {
            try {
                MethodSignature methodSignature = (MethodSignature) point.getSignature();
                ActionPoint actionPoint = methodSignature.getMethod().getAnnotation(ActionPoint.class);
                int reqType = actionPoint.reqType();
                String remark = actionPoint.remark();
                HttpServletRequest request = findRequest();
                String token = findToken(pointObj, request);
                UserModel userModel = findUserModelByToken(token);
                String actionParams = findActionParams(reqType, request, point);
                String actionId = findActionId(remark);
                String ip = findIp(request);
                addUserActionRecord(userModel, ip, actionId, actionParams);
            } catch (Exception e) {
                log.error("用户行为记录异常", e);
            }
        }
    }

    /**
     * 获取请求ip
     *
     * @param request request
     * @return string ip
     */
    public String findIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.length() == 0 || Constants.IP_UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || Constants.IP_UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || Constants.IP_UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    /**
     * 添加用户行为记录
     *
     * @param userModel    userModel
     * @param loginIp      ip
     * @param actionId     行为id
     * @param actionParams 行为参数
     */
    private void addUserActionRecord(UserModel userModel, String loginIp, String actionId, String actionParams) {
        if (userModel != null) {
            UserActionRecord userActionRecord = new UserActionRecord();
            userActionRecord.setUserId(userModel.getId());
            userActionRecord.setUserName(userModel.getName());
            userActionRecord.setLoginIp(loginIp);
            userActionRecord.setActionId(actionId);
            userActionRecord.setActionParams(actionParams);
            userActionRecordMapper.insert(userActionRecord);
        }
    }

    /**
     * 获取行为id
     *
     * @param remark 行为名称
     * @return string 行为id
     */
    private String findActionId(String remark) {
        String actionId;
        List<Action> actions = new LambdaQueryChainWrapper<>(actionMapper)
                .eq(Action::getActionName, remark)
                .list();
        if (actions.isEmpty()) {
            actionId = IdUtil.simpleUUID();
            Action action = new Action();
            action.setId(actionId);
            action.setActionName(remark);
            log.debug("添加 action {}", action);
            actionMapper.insert(action);
        } else {
            Action action = actions.get(0);
            actionId = action.getId();
        }
        return actionId;
    }

    /**
     * 获取行为参数
     *
     * @param reqType 请求类型
     * @param request request
     * @param point   point
     * @return string 行为参数
     */
    private String findActionParams(int reqType, HttpServletRequest request, ProceedingJoinPoint point) {
        String actionParams;
        if (reqType == 0) {
            Map<String, String[]> params = request.getParameterMap();
            actionParams = JSONUtil.toJsonStr(params);
        } else if (reqType == 1) {
            actionParams = JSONUtil.toJsonStr(point.getArgs()[0]);
        } else {
            actionParams = "";
            log.warn("未知请求类型 reqType {}", reqType);
        }
        return actionParams;
    }


    /**
     * 获取token
     *
     * @param pointObj 切面返回对象
     * @param request  request
     * @return string token
     */
    private String findToken(Object pointObj, HttpServletRequest request) {
        String token = request.getHeader(TokenUtil.TOKEN_SIGN);
        if (CharSequenceUtil.isBlank(token)) {
            RestResult restResult = (RestResult) pointObj;
            token = (String) restResult.getData();
        }
        log.debug("用户行为获取 token {}", token);
        return token;
    }


    /**
     * 获取 UserModel
     *
     * @param token token
     * @return UserModel
     */
    private UserModel findUserModelByToken(String token) {
        if (CharSequenceUtil.isBlank(token)) {
            log.error("token 空 无法获取用户信息");
            return null;
        }
        String tokenValue = (String) redisTemplate.opsForValue().get(token);
        if (CharSequenceUtil.isBlank(tokenValue)) {
            log.warn("token过期 无法获取用户信息");
            return null;
        }
        JWT jwt = TokenUtil.parseToken(tokenValue);
        JWTPayload jwtPayload = jwt.getPayload();
        JSONObject jsonObject = jwtPayload.getClaimsJson();
        String userId = (String) jsonObject.get(TokenUtil.USER_ID);
        String userName = (String) jsonObject.get(TokenUtil.USER_NAME);
        UserModel userModel = new UserModel();
        userModel.setId(userId);
        userModel.setName(userName);
        log.debug("用户行为获取 UserModel {}", userModel);
        return userModel;
    }

    /**
     * 获取 request
     *
     * @return HttpServletRequest
     */
    private HttpServletRequest findRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) Optional
                .ofNullable(RequestContextHolder.getRequestAttributes())
                .orElseThrow(() -> new RuntimeException("requestAttributes is null"));
        return requestAttributes.getRequest();
    }


}
