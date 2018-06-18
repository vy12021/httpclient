package com.leotesla.httpclient;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.leotesla.httpclient.internal.CacheConfig;
import com.leotesla.httpclient.internal.ContentType;
import com.leotesla.httpclient.internal.HttpCallback;
import com.leotesla.httpclient.internal.HttpDispatcher;
import com.leotesla.httpclient.internal.HttpFilter;
import com.leotesla.httpclient.internal.HttpImpl;
import com.leotesla.httpclient.internal.HttpInterceptor;
import com.leotesla.httpclient.internal.HttpMethod;
import com.leotesla.httpclient.internal.HttpRequest;
import com.leotesla.httpclient.internal.HttpResponse;

import java.io.Serializable;
import java.util.Map;

/**
 * 客户端模型，方便调用
 *
 * @version 1.0
 *
 * Created by Leo on 2017/4/16.
 */

public abstract class ClientModule implements HttpInterceptor, HttpFilter {

    // 回调线程Handler
    private final Handler callbackHandler;

    public ClientModule(@NonNull Handler handler) {
        this.callbackHandler = handler;
    }

    /**
     * todo 通用GET请求方式，缓存策略为3秒存活，即3秒内会拉取到同一个响应
     * @param url       请求链接
     * @param params    参数列表
     * @param callback  回调
     */
    public void get(@NonNull String url,
                    @Nullable Map<String, String> params,
                    @Nullable HttpCallback callback) {
        get(null, url, params, callback);
    }

    /**
     * todo GET请求方式
     * @param config    缓存策略
     * @param url       请求url
     * @param params    参数列表
     * @param callback  回调
     */
    public void get(@Nullable CacheConfig config, @NonNull String url,
                    @Nullable Map<String, String> params,
                    @Nullable HttpCallback callback) {
        HttpRequest request = HttpRequest.create(HttpMethod.GET, url);
        request.cacheStrategy(config);
        if (null != params) {
            for (String key : params.keySet()) {
                request.addParams(key, params.get(key));
            }
        }
        dispatch(request, callback);
    }

    /**
     * todo POST请求方式，通用多参数类型post请求
     * @param type      参数类型
     * @param url       请求url
     * @param params    参数列表
     * @param callback  回调
     */
    public void post(@Nullable ContentType type,
                     @NonNull String url,
                     @Nullable Map<String, String> params,
                     @Nullable HttpCallback callback) {
        HttpRequest request = HttpRequest.create(HttpMethod.POST, url).setContentType(type);
        if (null != params) {
            for (String key : params.keySet()) {
                request.addParams(key, params.get(key));
            }
        }
        dispatch(request, callback);
    }

    /**
     * todo POST请求方式，单对象传递参数默认使用json结构
     * @param url       请求url
     * @param object    参数体
     * @param callback  回调
     */
    public void postObject(@NonNull String url,
                           @Nullable Serializable object,
                           @Nullable HttpCallback callback) {
        HttpRequest request = HttpRequest.create(HttpMethod.POST, url)
                .setContentType(ContentType.Json);
        if (null != object) {
            request.addParams(object);
        }
        dispatch(request, callback);
    }

    /**
     * todo POST请求方式，默认请求体使用 form 结构
     * @param url       请求url
     * @param params    参数列表
     * @param callback  回调
     */
    public void post(@NonNull String url,
                     @Nullable Map<String, String> params,
                     @Nullable HttpCallback callback) {
        HttpRequest request = HttpRequest.create(HttpMethod.POST, url);
        if (null != params) {
            for (String key : params.keySet()) {
                request.addParams(key, params.get(key));
            }
        }
        dispatch(request, callback);
    }

    /**
     * todo PUT 请求方式
     * @param url       请求url
     * @param params    请求参数
     * @param callback  回调
     */
    public void put(@NonNull String url,
                    @Nullable Map<String, String> params,
                    @Nullable HttpCallback callback) {
        HttpRequest request = HttpRequest.create(HttpMethod.PUT, url);
        if (null != params) {
            for (String key : params.keySet()) {
                request.addParams(key, params.get(key));
            }
        }
        dispatch(request, callback);
    }

    /**
     * todo DELETE 请求方式
     * @param url       请求url
     * @param params    请求参数
     * @param callback  回调
     */
    public void delete(@NonNull String url,
                       @Nullable Map<String, String> params,
                       @Nullable HttpCallback callback) {
        HttpRequest request = HttpRequest.create(HttpImpl.OkHttp, HttpMethod.DELETE, url);
        if (null != params) {
            for (String key : params.keySet()) {
                request.addParams(key, params.get(key));
            }
        }
        dispatch(request, callback);
    }

    /**
     * 最终发送请求
     * @param request   请求实体
     * @param callback  回调
     */
    private void dispatch(HttpRequest request, HttpCallback callback) {
        request.registerInterceptor(this);
        HttpDispatcher.send(request, new CallbackAdapter(callback));
    }

    /**
     * 响应线程转换
     */
    private class CallbackAdapter implements HttpCallback {

        private final HttpCallback callback;

        private CallbackAdapter(HttpCallback callback) {
            this.callback = callback;
            if (callback instanceof HandlerCallback) {
                // 绑定当前Handler
                ((HandlerCallback) callback).bindHandler(callbackHandler);
            }
        }

        @Override
        public boolean onHttpSuccess(@NonNull final HttpResponse response) {
            if (null != callback) {
                callback.onHttpSuccess(response);
            }
            return true;
        }

        @Override
        public void onHttpFailed(@NonNull final HttpResponse response) {
            if (null != callback) {
                callback.onHttpFailed(response);
            }
        }

        @Override
        public void onHttpCanceled(@NonNull final HttpRequest request) {
            if (null != callback) {
                callback.onHttpCanceled(request);
            }
        }

    }

}
