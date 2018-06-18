package com.leotesla.httpclient;

import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;

import com.leotesla.httpclient.internal.HttpRequest;
import com.leotesla.httpclient.internal.HttpResponse;

/**
 * 框架层到业务层核心错误转换处理器
 *
 * @version 1.0
 *
 * Created by LeoTesla on 2017/10/7.
 */

@WorkerThread
public interface ClientErrorHandler {

    /**
     * http响应失败，由业务层转换为自己想要的ClientError
     * @param response  源生响应
     * @see com.leotesla.httpclient.internal.HttpCallback#onHttpFailed(HttpResponse)
     * @return          客户端错误类型
     */
    @WorkerThread
    ClientError onHttpFailed(HttpResponse response);

    /**
     * 客户端统一错误模型，具体由{@link #onHttpFailed(HttpResponse)}、
     * {@link com.leotesla.httpclient.internal.HttpCallback#onHttpSuccess(HttpResponse)}
     * 数据解析过程或者客户端业务层回调时产生
     * {@link com.leotesla.httpclient.internal.HttpCallback#onHttpCanceled(HttpRequest)}
     * 取消请求也会产生一个取消的异常
     * 最终聚合产生分发给上层处理
     * 参考 CommonDao模块的CallbackBase
     * @param error     错误体
     * @return          true: 框架层处理不返回客户端错误响应；false: 继续发送给客户端
     */
    @WorkerThread
    boolean onDispatchError(ClientError error);

    /**
     * 发送结果ClientError到最终的默认处理器
     * @param error     错误体
     */
    @MainThread
    void onPostError(ClientError error);

}
