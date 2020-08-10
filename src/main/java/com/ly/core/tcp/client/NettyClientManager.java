package com.ly.core.tcp.client;

import com.google.common.collect.Lists;
import com.ly.core.StressRemoteContext;
import com.ly.core.StressResult;
import com.ly.core.tcp.message.Invocation;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * client管理类
 * @Author: luoy
 * @Date: 2020/7/13 10:23.
 */
@Slf4j
public class NettyClientManager {

    /**重试初始延迟*/
    public static final Long RECONNECT_DELAY_SECONDS = 3L;

    private volatile static NettyClientManager MANAGER = null;

    /** 压力机计数器*/
    private int remoteCount = 0;

    /**
     * client 映射
     */
    private CopyOnWriteArrayList<NettyClient> clients = Lists.newCopyOnWriteArrayList();

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
        clients.add(client);
        remoteCount++;
    }

    public void removeAndClose(String channelId) {
        clients.stream()
                .filter(NettyClient::isActive)
                .filter(client -> channelId.equals(client.getChannel().id().asShortText()))
                .findAny()
                .ifPresent(client -> {
                    client.shutdown();
                    clients.remove(client);
                });
    }

    public void shutdownAll() {
        clients.forEach(NettyClient::shutdown);
    }

    public void syncSendAll(Invocation invocation) {
        int clientSize = clients.size();
        while (true) {
            Long clientActiveSize = clients.stream().filter(NettyClient::isActive).count();

            //所有client都连上了再同步发送
            if(clientSize == clientActiveSize) {
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(RECONNECT_DELAY_SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        clients.forEach( v -> v.send(invocation));
    }

    public void send(String channelId, Invocation invocation) {
        clients.stream()
                .filter(NettyClient::isActive)
                .filter(client -> channelId.equals(client.getChannel().id().asShortText()))
                .findAny()
                .ifPresent(client -> client.send(invocation));
    }

    public void read(String channelId, Invocation msg) {
        //msg 接收服务端数据
        log.info("[{}]接收数据: {}",channelId, msg);
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
        clients.stream()
                .filter(client -> channelId.equals(client.getChannel().id().asShortText()))
                .findAny()
                .ifPresent(NettyClient::reconnect);
    }

    public boolean isFinish() {
        return clients.size() <= 0;
    }

    private void doDown(String channelId) {
        removeAndClose(channelId);
        log.info("[{}]client退出", channelId);
        return;
    }

    private void doBusiness(Invocation msg) {
        StressResult remoteResult = (StressResult) msg.getMessage();
        int threadNumber = remoteCount * remoteResult.getThreadCount();
        remoteResult.setThreadCount(threadNumber);
        if (remoteResult.getTotalCounter().get() <= 0) {
            return;
        }
        StressRemoteContext.calculateResult(remoteResult);
    }

    private void doHeartbeat(Invocation msg) {
    }
}
