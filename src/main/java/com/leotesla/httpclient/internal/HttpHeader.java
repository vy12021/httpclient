package com.leotesla.httpclient.internal;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import java.util.Map;

/**
 * Http 请求头（包装自定义头部列表）
 *
 * @version 1.0
 *
 * Created by Tesla on 2016/7/24.
 */

public final class HttpHeader {

    // 请求头参数
    private final ArrayMap<String, String> headers = new ArrayMap<>();

    final HttpHeader put(@NonNull String key, String value) {
        headers.put(key, value);

        return this;
    }

    final HttpHeader putAll(@NonNull Map<String, String> other) {
        headers.putAll(other);

        return this;
    }

    final HttpHeader remove(@NonNull String key) {
        headers.remove(key);

        return this;
    }

    final boolean isEmpty() {
        return headers.isEmpty();
    }

    final boolean containKey(@NonNull String key) {
        return headers.containsKey(key);
    }

    final int getCount() {
        return headers.size();
    }

    public ArrayMap<String, String> getParams() {
        return new ArrayMap<>(this.headers);
    }

    @Override
    public String toString() {
        return "HttpHeader{" +
                "headers=" + headers +
                '}';
    }
}
