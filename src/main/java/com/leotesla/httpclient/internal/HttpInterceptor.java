package com.leotesla.httpclient.internal;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

/**
 * Http 请求拦截器
 *
 * @version 1.0
 *
 * Created by Tesla on 2016/7/24.
 */

public interface HttpInterceptor {

    /**
     * 请求发送前调用
     * @param request   请求
     * @return          false,执行请求;true，放弃请求，并在请求体指明原因
     */
    @UiThread
    boolean onPreRequest(@NonNull HttpRequest request);

    /**
     * 请求已经在线程中，此时缓存中的部分信息已经加到Response中了，下一步就要调用socket
     * @param request   请求
     * @return          保留，具体使用等后续调整
     */
    @WorkerThread
    boolean onExecuteRequest(@NonNull HttpRequest request);

    /**
     * 响应完成结果处理前调用
     * @param response  响应
     * @return          false,正常处理; true,自己拦截处理不再响应HttpCallback回调
     */
    @WorkerThread
    boolean onPostResponse(@NonNull HttpResponse response);

}
