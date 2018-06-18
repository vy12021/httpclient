package com.leotesla.httpclient.internal;

/**
 * 缓存策略
 * Note: 请求缓存和响应缓存对应存在，如果当前缓存已过期则网络响应数据会以请求缓存配置为缓存过期时间进行缓存
 *
 * @version 1.0
 *
 * Created by Leo on 10/23/2017.
 */

public enum CacheStrategy {

    /**
     * 不使用缓存
     */
    Disable(0),
    /**
     * 刚刚，非常短时间缓存
     */
    JustNow(5000),
    /**
     * 获取一天内的缓存
     */
    OneDay(24 * 60 * 60 * 1000),
    /**
     * 永久不过期
     */
    AllTheTime(Integer.MAX_VALUE),
    /**
     * 自定义时间
     */
    Custom(-1),
    /**
     * 强制使用缓存，否则返回错误
     */
    Must(-1)
    ;

    /**
     * 默认失效时间
     */
    public int defaultExpiredInMs;

    CacheStrategy(int defaultExpiredInMs) {
        this.defaultExpiredInMs = defaultExpiredInMs;
    }

}
