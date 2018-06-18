package com.leotesla.httpclient.internal;

import android.support.annotation.NonNull;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Http 请求分发器
 *
 * @version 1.0
 *
 * Created by Tesla on 2016/7/24.
 */

public final class HttpDispatcher {

    private static final HttpCallback CALLBACK = new HttpCallback() {
        @Override public boolean onHttpSuccess(@NonNull HttpResponse response) { return false;}
        @Override public void onHttpFailed(@NonNull HttpResponse response) { }
        @Override public void onHttpCanceled(@NonNull HttpRequest request) { }
    };
    public static boolean LOG;
    static boolean LOG_ENABLE;

    private HttpDispatcher() {}

    // http 线程池
    private static final ThreadPoolExecutor HTTP_EXECUTOR = TaskPoolFactory.create(2, 5);

    /**
     * 发送请求
     * @param request   请求实体
     */
    public static void send(@NonNull HttpRequest request, @NonNull HttpCallback callback) {
        // 过滤器
        for (HttpFilter filter : request.getFilters()) {
            if (!filter.filter(request)) {
                request.cancel();
                callback.onHttpCanceled(request);
                return;
            }
        }

        HttpExecutor runnable = new HttpExecutor(request, callback) {
            @Override boolean onPreExecute(@NonNull HttpRequest request) {
                // 拦截器
                for (HttpInterceptor interceptor : request.getInterceptors()) {
                    if (interceptor.onPreRequest(request)) {
                        return true;
                    }
                }
                return false;
            }
            @Override boolean onPostExecute(@NonNull HttpResponse response) {
                // 拦截器
                for (HttpInterceptor interceptor : response.request.getInterceptors()) {
                    if (interceptor.onPostResponse(response)) {
                        return true;
                    }
                }
                return false;
            }
        };
        if (!request.isCanceled()) {
            // 获取具体实现
            HttpEngine engine;
            switch (request.getImpl()) {
                case OkHttp:
                    engine = new OkClient();
                    break;
                case UrlConnection:
                    engine = new UrlClient();
                    break;
                default:
                    engine = new OkClient();
            }
            request.attachEngine(engine);
            HTTP_EXECUTOR.submit(runnable);
        } else {
            callback.onHttpCanceled(request);
        }
    }

    /**
     * 发送请求
     * @param request   请求实体
     */
    public static void send(@NonNull HttpRequest request) {
        // 过滤器
        for (HttpFilter filter : request.getFilters()) {
            if (!filter.filter(request)) {
                request.cancel();
                return;
            }
        }

        HttpExecutor action = new HttpExecutor(request, CALLBACK) {
            @Override boolean onPreExecute(@NonNull HttpRequest request) {
                // 拦截器
                for (HttpInterceptor interceptor : request.getInterceptors()) {
                    if (interceptor.onPreRequest(request)) {
                        return true;
                    }
                }
                return false;
            }
            @Override boolean onPostExecute(@NonNull HttpResponse response) {
                // 拦截器
                for (HttpInterceptor interceptor : response.request.getInterceptors()) {
                    if (interceptor.onPostResponse(response)) {
                        return true;
                    }
                }
                return false;
            }
        };
        if (!request.isCanceled()) {
            // 获取具体实现
            HttpEngine engine;
            switch (request.getImpl()) {
                case OkHttp:
                    engine = new OkClient();
                    break;
                case UrlConnection:
                    engine = new UrlClient();
                    break;
                default:
                    engine = new OkClient();
            }
            request.attachEngine(engine);
            action.execute();
        }
    }

}
