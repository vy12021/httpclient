package com.leotesla.httpclient.internal;

import android.support.annotation.NonNull;

/**
 * Http 请求过滤器
 *
 * @version 1.0
 *
 * Created by Tesla on 2016/7/24.
 */

public interface HttpFilter {

    /**
     * 过滤指定请求
     * @param request   请求
     * @return          true,继续发送;false,取消发送
     */
    boolean filter(@NonNull HttpRequest request);

}
