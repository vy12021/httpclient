package com.leotesla.httpclient;


import android.os.Handler;
import android.support.annotation.MainThread;

import com.leotesla.httpclient.data.ParamTyped;
import com.leotesla.httpclient.data.SafeRunnable;
import com.leotesla.httpclient.internal.HttpCallback;

import java.io.Serializable;

/**
 * 添加Handler回调机制
 *
 * @version 1.0
 *
 * Created by Leo on 2018/06/14
 */
public abstract class HandlerCallback<T extends Serializable>
        extends ParamTyped implements HttpCallback {

    // 回调线程Handler
    private Handler callbackHandler;
    // 标记
    private final Object mTag;
    // 数据解析器
    protected final Parsable<T> parsable;

    @SuppressWarnings("unchecked")
    public HandlerCallback(Handler callbackHandler) {
        this(callbackHandler, null);
    }

    @SuppressWarnings("unchecked")
    public HandlerCallback(Handler callbackHandler, Object mTag) {
        this.callbackHandler = callbackHandler;
        this.mTag = mTag;
        Class<?> type = getParamTypeAt(0);
        if (Void.TYPE != type) {
            this.parsable = new Parsable<>((Class<T>) type);
        } else {
            this.parsable = null;
        }
    }

    /**
     * 绑定回调线程Handler
     * @param callbackHandler   handler
     */
    void bindHandler(Handler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    /**
     * 获取当前请求tag
     */
    public Object getTag() {
        return this.mTag;
    }

    /**
     * 发送到回调线程执行
     * @param action action
     */
    protected void post(Runnable action) {
        if (null != this.callbackHandler) {
            this.callbackHandler.post(action);
        } else {
            action.run();
        }
    }

    /**
     * 发送到受保护回调线程执行
     * @param action action
     */
    protected void postSafe(Runnable action) {
        final SafeRunnable safeRunnable = new SafeRunnable(action) {
            @Override
            public void onException(Exception e) {
                onPostActionError(e);
            }
        };
        if (null != this.callbackHandler) {
            this.callbackHandler.post(safeRunnable);
        } else {
            safeRunnable.run();
        }
    }

    /**
     * 执行post转换线程后执行体发生错误
     * @see #postSafe(Runnable)
     * @param e     异常
     */
    @MainThread
    protected abstract void onPostActionError(Exception e);

}
