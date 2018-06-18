package com.leotesla.httpclient;

import android.support.annotation.NonNull;

import com.leotesla.httpclient.internal.HttpCallback;
import com.leotesla.httpclient.internal.HttpRequest;
import com.leotesla.httpclient.internal.HttpResponse;

/**
 * 以原数据返回的数据接收器
 *
 * @version 1.0
 *
 * Created by LeoTesla on 2017/10/6.
 */

public abstract class DefaultClientCallback implements HttpCallback {

    private final ClientErrorHandler mErrorHandler;
    private final Object mTag;

    public DefaultClientCallback(@NonNull ClientErrorHandler errorHandler, Object tag) {
        this.mErrorHandler = errorHandler;
        this.mTag = tag;
    }

    @Override
    public final boolean onHttpSuccess(@NonNull HttpResponse response) {
        onSuccess(response.getContent());
        return true;
    }

    @Override
    public final void onHttpFailed(@NonNull HttpResponse response) {
        ClientError clientError = this.mErrorHandler.onHttpFailed(response);
        if (!this.mErrorHandler.onDispatchError(clientError)) {
            onError(clientError);
        }
    }

    @Override
    public final void onHttpCanceled(@NonNull HttpRequest request) {

    }

    public Object getTag() {
        return this.mTag;
    }

    /**
     * 失败
     * @param error 错误信息
     */
    public void onError(ClientError error) {}

    /**
     * 成功
     * @param data  数据
     */
    public abstract void onSuccess(String data);

}
