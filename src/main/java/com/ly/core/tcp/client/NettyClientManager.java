package com.ly.core.tcp.client;

import com.google.common.collect.Maps;
import com.ly.core.StressRemoteContext;
import com.ly.core.StressResult;
import com.ly.core.tcp.message.Invocation;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentMap;

/**
 * client管理类
 * @Author: luoy
 * @Date: 2020/7/13 10:23.
 */
@Slf4j
public class NettyClientManager {

    private volatile static NettyClientManager MANAGER = null;

    /**
     * Channel-client 映射
     */
    private ConcurrentMap<String, NettyClient> clients = Maps.newConcurrentMap();

    private NettyClientManager() {}

    public static NettyClientManager getInstance() {
        if (MANAGER == null) {
            synchronized (NettyClientManager.class) {
                if (MANAGER == null) {
                    MANAGER = new NettyClientManager();
                }
            }
        }
        return MANAGER;
    }

    public void add(NettyClient client) {
        clients.put(client.getChannel().id().asLongText(), client);
    }

    public void removeAndClose(String channelId) {
        if (clients.containsKey(channelId)) {
            clients.get(channelId).shutdown();
            clients.remove(channelId);
        }
    }

    public void sendAll(Invocation invocation) {
        clients.forEach( (k,v) -> v.send(invocation));
    }

    public void send(String channelId, Invocation invocation) {
        if (clients.containsKey(channelId)) {
            clients.get(channelId).send(invocation);
        }
    }

    public void read(String channelId, Invocation msg) {
        //msg 接收服务端数据
        log.info("接收数据: {}", msg);
        switch (msg.getType()) {
            case BUSINESS:
                doBusiness(msg);
                break;
            case DOWN:
                doDown(channelId);
                break;
            case HEARTBEAT:
                doHeartbeat(msg);
                break;
            case AUTH:
            default:
                break;
        }
    }

    public void reconnect(String channelId) {
        if (clients.containsKey(channelId)) {
            clients.get(channelId).reconnect();
        }
    }

    public boolean isFinish() {
        return clients.size() <= 0;
    }

    private void doDown(String channelId) {
        removeAndClose(channelId);
        log.info("client退出");
        return;
    }

    private void doBusiness(Invocation msg) {
        StressResult remoteResult = (StressResult) msg.getMessage();
        if (remoteResult.getTotalCounter().get() <= 0) {
            return;
        }
        StressRemoteContext.calculateResult(remoteResult);
    }

    private void doHeartbeat(Invocation msg) {
    }
}
