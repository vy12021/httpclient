package com.leotesla.httpclient.internal;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 完整请求包装对象
 * 包含url，头部，请求体，响应，拦截器，过滤器，请求实现
 *
 * @version 1.0
 *
 * Created by Tesla on 2016/7/24.
 */

public final class HttpRequest {

    // 请求创建时间戳
    private final long timestamp = System.currentTimeMillis();
    // 请求各阶段的时间戳记录
    long initTsp, preTsp, respTsp, recTsp;
    // 请求配置
    private final HttpConfig config = new HttpConfig();
    // 请求头
    private final HttpHeader header = new HttpHeader();
    // 请求体
    private HttpBody body;
    // 请求实现方式
    private final HttpImpl impl;
    // 请求核心引擎
    private HttpEngine engine;
    // 响应
    private HttpResponse response;
    // 请求拦截器
    private final List<HttpInterceptor> interceptors = new ArrayList<>();
    // 请求过滤器
    private final List<HttpFilter> filters = new ArrayList<>();
    // 标记是否执行
    private boolean executed;
    // 是否取消请求
    private boolean canceled;
    // 已重试次数
    private int retriedTimes;

    private HttpRequest(@NonNull HttpImpl impl) {
        this.impl = impl;
    }

    /**
     * 创建请求
     * @param method    请求方法
     * @param url       请求url
     * @return
     */
    public static HttpRequest create(@NonNull HttpMethod method,
                                     @NonNull String url) {
        return create(HttpImpl.UrlConnection, method, url);
    }

    /**
     * 创建请求
     * @param impl      请求实现
     * @param method    请求方法
     * @param url       请求url
     * @return
     */
    public static HttpRequest create(@NonNull HttpImpl impl,
                                     @NonNull HttpMethod method,
                                     @NonNull String url) {
        HttpRequest request = new HttpRequest(impl);
        request.body = HttpBody.create(url);
        request.body.setMethod(method);

        return request;
    }

    /**
     * 关联http引擎
     * @param engine    请求核心实现
     */
    final void attachEngine(@NonNull HttpEngine engine) {
        this.engine = engine;
    }

    /**
     * 取消请求
     */
    public void cancel() {
        this.canceled = true;
        if (null != engine) {
            engine.close();
        }
    }

    public boolean isCanceled() {
        return canceled;
    }

    public HttpRequest addHeader(@NonNull String key, @NonNull String value) {
        this.header.put(key, value);
        return this;
    }

    public HttpRequest addParams(@NonNull String key, @NonNull String value) {
        this.body.put(key, value);
        return this;
    }

    public HttpRequest addParams(@NonNull Serializable value) {
        this.body.put(null, value);
        return this;
    }

    public HttpRequest registerFilter(@NonNull HttpFilter filter) {
        this.filters.add(filter);
        return this;
    }

    public HttpRequest registerInterceptor(@NonNull HttpInterceptor interceptor) {
        this.interceptors.add(interceptor);
        return this;
    }

    public HttpRequest unregisterFilter(@NonNull HttpFilter filter) {
        this.filters.remove(filter);
        return this;
    }

    public HttpRequest unregisterInterceptor(@NonNull HttpInterceptor interceptor) {
        this.interceptors.remove(interceptor);
        return this;
    }

    public HttpRequest clearFilters() {
        this.filters.clear();
        return this;
    }

    public HttpRequest clearInterceptors() {
        this.interceptors.clear();
        return this;
    }

    public HttpBody getBody() {
        return body;
    }

    public HttpEngine getEngine() {
        return engine;
    }

    public HttpHeader getHeader() {
        return header;
    }

    public HttpMethod getMethod() {
        return body.getMethod();
    }

    public HttpResponse getResponse() {
        return response;
    }

    public HttpImpl getImpl() {
        return impl;
    }

    List<HttpFilter> getFilters() {
        return filters;
    }

    List<HttpInterceptor> getInterceptors() {
        return interceptors;
    }

    public HttpRequest timeout(long write, long read) {
        this.config.setWriteTimeout(write);
        this.config.setReadTimeout(read);
        return this;
    }

    public HttpRequest cache(boolean enable, @IntRange(from = 1) int version, long size, String path) {
        this.config.setCacheVersion(version);
        this.config.setEnableCache(enable);
        this.config.setCacheSize(size);
        this.config.setCacheDir(path);
        return this;
    }

    public HttpRequest cacheStrategy(CacheConfig cacheConfig) {
        if (null != cacheConfig) {
            this.config.setCacheConfig(cacheConfig);
        }
        return this;
    }

    public HttpRequest setContentType(ContentType contentType) {
        if (null != contentType) {
            this.body.setContentType(contentType);
        }
        return this;
    }

    public HttpRequest ua(String userAgent, boolean append) {
        if (null != userAgent) {
            this.config.setUserAgent(userAgent);
        }
        return this;
    }

    public HttpConfig getConfig() {
        return config;
    }

    void setResponse(HttpResponse response) {
        this.response = response;
    }

    public boolean isExecuted() {
        return executed;
    }

    public HttpResponse execute() throws HttpException {
        this.executed = true;
        switch (getMethod()) {
            case GET:
                response = engine.get(this);
                break;
            case POST:
                response = engine.post(this);
                break;
            case PUT:
                response = engine.put(this);
                break;
            case DELETE:
                response = engine.delete(this);
                break;
        }
        return this.response;
    }

    /**
     * 重试连接
     * @return  是否可以重试
     */
    boolean retry() {
        if (config.isRetryOnFailed() && config.getRetryCount() > retriedTimes) {
            retriedTimes++;
            return true;
        }
        return false;
    }

    int getRetriedTimes() {
        return this.retriedTimes;
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "\nconfig=" + config +
                ",\n header=" + header +
                ",\n body=" + body +
                ",\n impl=" + impl +
                ",\n canceled=" + canceled +
                ",\n cost=total: " + (System.currentTimeMillis() - timestamp) +
                "[" + (initTsp > timestamp ? initTsp - timestamp : 0) +
                ", " + (preTsp > initTsp ? preTsp - initTsp : 0) +
                ", " + (respTsp > preTsp ? respTsp - preTsp : 0) +
                ", " + (recTsp > respTsp ? recTsp - respTsp : 0) +
                "]" +
                '}';
    }
}
