package com.leotesla.httpclient.internal;

import com.leotesla.httpclient.data.KeyValuePair;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * http 配置
 *
 * @version 1.0
 *
 * Created by Tesla on 2016/12/11.
 */

public final class HttpConfig implements Serializable {

    private static final long serialVersionUID = 2270879561424117880L;

    // 客户端UA标识
    private String userAgent = "";
    // 证书
    private Map<String, InputStream> certies = Collections.emptyMap();
    // 响应超时 in millisecond
    private long readTimeout = 8000;
    // 请求超时 in millisecond
    private long writeTimeout = 8000;
    // 允许重定向
    private boolean followRedirect = true;
    // 失败重试
    private boolean retryOnFailed = true;
    // 允许重试最大次数
    private int retryCount = 3;
    // 开启缓存
    private boolean enableCache;
    // 缓存目录
    private String cacheDir = "";
    // 缓存大小 in bytes
    private long cacheSize = 20 * 1024 * 1024;
    // 缓存版本号
    private int cacheVersion = 1;
    // 请求缓存配置
    private CacheConfig cacheConfig = CacheConfig.create(CacheStrategy.JustNow, false);

    /**
     * 设置请求证书
     * @param certies   证书池(key为域名，value为证书流)
     */
    @SafeVarargs
    public final void setCerties(KeyValuePair<String, InputStream>... certies) {
        this.certies = KeyValuePair.convert2Map(certies);
    }

    public final Map<String, InputStream> getCerties() {
        return certies;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public HttpConfig setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public long getWriteTimeout() {
        return writeTimeout;
    }

    public HttpConfig setWriteTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    public boolean isFollowRedirect() {
        return followRedirect;
    }

    public HttpConfig setFollowRedirect(boolean followRedirect) {
        this.followRedirect = followRedirect;
        return this;
    }

    public boolean isRetryOnFailed() {
        return retryOnFailed;
    }

    public HttpConfig setRetryOnFailed(boolean retryOnFailed) {
        this.retryOnFailed = retryOnFailed;
        return this;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public HttpConfig setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    public boolean isEnableCache() {
        return enableCache;
    }

    public HttpConfig setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
        return this;
    }

    public String getCacheDir() {
        return cacheDir;
    }

    public HttpConfig setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
        return this;
    }

    public long getCacheSize() {
        return cacheSize;
    }

    public HttpConfig setCacheSize(long cacheSize) {
        this.cacheSize = cacheSize;
        return this;
    }

    public int getCacheVersion() {
        return cacheVersion;
    }

    public void setCacheVersion(int cacheVersion) {
        this.cacheVersion = cacheVersion;
    }

    public void setCacheConfig(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
    }

    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public String toString() {
        return "HttpConfig{" +
                "readTimeout=" + readTimeout +
                ", writeTimeout=" + writeTimeout +
                ", followRedirect=" + followRedirect +
                ", retryOnFailed=" + retryOnFailed +
                ", retryCount=" + retryCount +
                ", enableCache=" + enableCache +
                ", cacheDir='" + cacheDir + '\'' +
                ", cacheSize=" + cacheSize +
                ", cacheConfig=" + cacheConfig +
                '}';
    }
}
