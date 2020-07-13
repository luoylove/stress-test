package com.ly.core.tcp.message;

import lombok.Builder;
import lombok.Data;

/**
 * @Author: luoy
 * @Date: 2020/7/13 11:13.
 */
@Builder
@Data
public class Invocation {

    private Invocation.Type type;

    private Message message;

    public static enum Type {
        /**
         * 业务通讯
         */
        BUSINESS,
        /**
         * 身份认证
         */
        AUTH,
        /**
         * 心跳
         */
        HEARTBEAT,
        /**
         * 结束
         */
        DOWN
    }
}
