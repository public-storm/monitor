package com.zwy.monitor.service.impl;

import com.zwy.monitor.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zwy
 * @date 2023/6/6 13:57
 */
@Slf4j
@Service
@ServerEndpoint("/webSocket/{userId}")
public class WebSocketServiceImpl implements WebSocketService {
    /**
     * 存放每个客户端对应的 WebSocketServer 对象
     */
    private static final ConcurrentHashMap<String, WebSocketServiceImpl> WEB_SOCKET_MAP = new ConcurrentHashMap<>();

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 接收 userId
     */
    private String userId = "";

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.session = session;
        this.userId = userId;
        // 加入
        WEB_SOCKET_MAP.put(userId, this);
        log.info("websocket 创建连接 {} 当前websocket 连接数 {}", userId, getOnlineCount());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if (!WEB_SOCKET_MAP.containsKey(userId)) {
            return;
        }
        WEB_SOCKET_MAP.remove(userId);
        log.info("websocket 关闭: {} 当前websocket 连接数 {}", userId, getOnlineCount());
    }


    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("websocket 错误", error);
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 单发消息
     */
    public static void sendMessageToUser(String toUserId, String message) throws IOException {
        if (WEB_SOCKET_MAP.containsKey(toUserId)) {
            WEB_SOCKET_MAP.get(toUserId).sendMessage(message);
        } else {
            log.error("请求的 userId {} 不在该服务器上", toUserId);
        }
    }

    /**
     * 获取websocket连接数
     */
    public static int getOnlineCount() {
        return WEB_SOCKET_MAP.size();
    }
}
