package com.ly.core.tcp.serialize;

import com.ly.core.StressResult;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: luoy
 * @Date: 2020/7/2 13:55.
 */
@Data
@Builder
public class MessageRequest<T> implements Serializable {
    private StressResult<T> result;

    /**是否业务已处理完成*/
    private boolean isAvailable;
}
