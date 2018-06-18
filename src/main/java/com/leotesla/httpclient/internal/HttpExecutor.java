package com.leotesla.httpclient.internal;


import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import static com.leotesla.httpclient.internal.HttpDispatcher.LOG_ENABLE;

/**
 * http 请求执行实体
 *
 * @version 1.0
 *
 * Created by Tesla on 2016/12/11.
 */

abstract class HttpExecutor implements Runnable {

    private final static String TAG = "HttpExecutor";

    // 请求
    private final HttpRequest request;
    // 回调
    private final HttpCallback callback;

    HttpExecutor(@NonNull HttpRequest request, @NonNull HttpCallback callback) {
        this.request = request;
        this.callback = callback;
        if (onPreExecute(request)) {
            request.cancel();
        }
    }

    @Override
    public void run() {
        // 按照具体的请求方式发送请求
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        try {
            execute();
        } finally {
            request.getEngine().close();
            if (LOG_ENABLE)
                Log.e(TAG, "complete-->" + request.getResponse());
        }
    }

    void execute() {
        HttpResponse response;
        final HttpEngine engine = request.getEngine();
        try {
            engine.init(request);
            request.initTsp = System.currentTimeMillis();
            for (HttpInterceptor interceptor : request.getInterceptors()) {
                interceptor.onExecuteRequest(request);
            }
            engine.prepare(request);
            request.preTsp = System.currentTimeMillis();
            response = engine.readCache(request);
            if (null == response) {
                // 启动引擎执行请求
                response = request.execute();
                request.respTsp = System.currentTimeMillis();
            }
            if (null != response) {
                if (!onPostExecute(response)) {
                    if (response.statusCode == 304 && TextUtils.isEmpty(response.responseContent)) {
                        HttpResponse serverRes = response;
                        CacheConfig cacheConfig = request.getConfig().getCacheConfig();
                        // 强制获取缓存
                        request.cacheStrategy(CacheConfig.create(CacheStrategy.Must));
                        response = engine.readCache(request);
                        request.respTsp = System.currentTimeMillis();
                        if (null != response) {
                            // 还原几个设置
                            response.headers = serverRes.headers;
                            response.statusCode = serverRes.statusCode;
                            request.cacheStrategy(cacheConfig);
                        }
                    }
                    if (null != response) {
                        if (request.isCanceled()) {
                            callback.onHttpCanceled(request);
                        } else {
                            callback.onHttpSuccess(response);
                            request.recTsp = System.currentTimeMillis();
                            engine.writeCache(response);
                        }
                    } else {
                        // 但是缓存没有获取到当做异常处理
                        throw new HttpException(ErrorType.NotFound);
                    }
                }
            } else {
                throw new HttpException(ErrorType.Unknown);
            }
        } catch (HttpException exception) {
            exception.printStackTrace();
            if (exception.needRetry() && request.retry()) {
                // 重新执行逻辑体
                if (LOG_ENABLE) {
                    Log.e(TAG, "Connected failed: retry " + request.getRetriedTimes());
                }
                request.getEngine().close();
                this.execute();
            } else {
                response = request.getResponse();
                response.exception = exception;
                if (!onPostExecute(response)) {
                    if (request.isCanceled()) {
                        callback.onHttpCanceled(request);
                    } else {
                        HttpResponse cache = null;
                        if (ErrorType.NotFound != exception.getType()) {
                            try {
                                cache = engine.readCache(request);
                                request.respTsp = System.currentTimeMillis();
                                request.setResponse(cache);
                            } catch (HttpException e) {
                                e.printStackTrace();
                            }
                        }
                        if (null != cache) {
                            callback.onHttpSuccess(cache);
                            request.recTsp = System.currentTimeMillis();
                        } else {
                            callback.onHttpFailed(response);
                        }
                    }
                }
            }
        }
    }

    // 转移逻辑到分发器中
    abstract boolean onPreExecute(@NonNull HttpRequest request);
    abstract boolean onPostExecute(@NonNull HttpResponse response);

}
