package com.zwy.monitor.service;

import java.io.IOException;

/**
 * @author zwy
 * @date 2023/6/6 13:55
 */
public interface WebSocketService {
    void sendMessageToUser(String toUserId, String message) throws IOException;
}
