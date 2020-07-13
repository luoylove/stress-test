package com.ly.core.tcp.server;

import com.google.common.collect.Maps;
import com.ly.core.tcp.message.Invocation;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

import java.util.concurrent.ConcurrentMap;

/**
 * @Author: luoy
 * @Date: 2020/7/13 10:23.
 */
public class NettyServerChannelManager {

    public volatile static NettyServerChannelManager MANAGER = null;

    /**
     * Channel 映射
     */
    private ConcurrentMap<ChannelId, Channel> channels = Maps.newConcurrentMap();

    private NettyServerChannelManager() {}

    public static NettyServerChannelManager getInstance() {
        if (MANAGER == null) {
            synchronized (NettyServerChannelManager.class) {
                if (MANAGER == null) {
                    MANAGER = new NettyServerChannelManager();
                }
            }
        }
        return MANAGER;
    }

    public void add(Channel channel) {
        channels.put(channel.id(), channel);
    }

    public void remove(Channel channel) {
        channels.remove(channel.id());
    }

    public void send(Invocation invocation) {

    }
}
