package com.ly.core.tcp.client;

import com.google.common.collect.Lists;
import com.ly.core.StressRemoteContext;
import com.ly.core.StressResult;
import com.ly.core.tcp.message.Invocation;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CopyOnWriteArrayList;

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
    }

    public void removeAndClose(String channelId) {
        clients.forEach( client -> {
            if(client.isActive()) {
                if (channelId.equals(client.getChannel().id().asShortText())) {
                    client.shutdown();
                    clients.remove(client);
                }
            }
        });
    }

    public void sendAll(Invocation invocation) {
        int clientSize = clients.size();
        while (true) {
            Long clientActiveSize = clients.stream().filter(NettyClient::isActive).count();

            if(clientSize == clientActiveSize) {
                break;
            }
            try {
                Thread.sleep(RECONNECT_DELAY_SECONDS * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        clients.forEach( v -> v.send(invocation));
    }

    public void send(String channelId, Invocation invocation) {
        clients.forEach( client -> {
            if (client.isActive()) {
                if (channelId.equals(client.getChannel().id().asShortText())) {
                    client.send(invocation);
                }
            }
        });
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
        clients.forEach( client -> {
            if (channelId.equals(client.getChannel().id().asShortText())) {
                client.reconnect();
            }
        });
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
        if (remoteResult.getTotalCounter().get() <= 0) {
            return;
        }
        StressRemoteContext.calculateResult(remoteResult);
    }

    private void doHeartbeat(Invocation msg) {
    }
}
