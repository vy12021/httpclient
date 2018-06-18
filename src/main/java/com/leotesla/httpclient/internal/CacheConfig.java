package com.leotesla.httpclient.internal;

import android.support.annotation.IntRange;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置
 *
 * @version 1.0
 *
 * Created by Leo on 10/23/2017.
 */

public class CacheConfig {

    // 缓存加载基础策略
    public final CacheStrategy strategy;

    // 缓存有效期
    public final int expiredMs;

    // 是否允许加载过期数据
    public final boolean loadStale;

    private CacheConfig(CacheStrategy strategy, int expiredMs, boolean loadStale) {
        this.strategy = strategy;
        this.expiredMs = expiredMs;
        this.loadStale = loadStale;
    }

    @Override
    public String toString() {
        return "CacheConfig{" +
                "strategy=" + strategy +
                ", expiredMs=" + expiredMs +
                ", loadStale=" + loadStale +
                '}';
    }

    public static CacheConfig create(CacheStrategy strategy) {
        return new CacheConfig(strategy, strategy.defaultExpiredInMs, false);
    }

    public static CacheConfig create(CacheStrategy strategy, boolean loadStale) {
        return new CacheConfig(strategy, strategy.defaultExpiredInMs, loadStale);
    }

    public static CacheConfig create(@IntRange(from = 0) int maxAge, TimeUnit unit) {
        int ms = (int) unit.toMillis(maxAge);
        return new CacheConfig(CacheStrategy.Custom, ms, false);
    }

    public static CacheConfig create(@IntRange(from = 0) int maxAge, TimeUnit unit, boolean loadStale) {
        int ms = (int) unit.toMillis(maxAge);
        return new CacheConfig(CacheStrategy.Custom, ms, loadStale);
    }

}
