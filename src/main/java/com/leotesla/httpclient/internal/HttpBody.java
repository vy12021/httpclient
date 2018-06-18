package com.leotesla.httpclient.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;

import com.leotesla.httpclient.data.KeyValuePair;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Http 请求体
 * post, put 参数
 *
 * @version 1.0
 *
 * Created by Tesla on 2016/7/24.
 */

final class HttpBody {

    public static final String FILE = "file";
    public static final String TEXT = "text";

    // object 参数 key
    static final String KEY_OBJECT = "object";

    // 请求url
    private final String url;
    // 请求方式
    private HttpMethod method = HttpMethod.GET;
    // 请求响应类型
    private ContentType contentType = ContentType.Form;
    // 参数列表
    private final Map<String, KeyValuePair<ContentType, Serializable>> params = new ArrayMap<>();

    private HttpBody(@NonNull String url) {
        this.url = url;
    }

    public static HttpBody create(@NonNull String url) {
        return new HttpBody(url);
    }

    final HttpBody put(@Nullable ContentType type, @NonNull String key, String value) {
        params.put(key, new KeyValuePair<>(type, value));
        return this;
    }

    final HttpBody put(@NonNull String key, String value) {
        params.put(key, new KeyValuePair<>(null, value));
        return this;
    }

    final HttpBody put(@Nullable ContentType type, @NonNull Serializable object) {
        params.put(KEY_OBJECT, new KeyValuePair<>(type, object));
        return this;
    }

    final HttpBody putAll(@NonNull Map<String, String> other) {
        for (String key : other.keySet()) {
            put(key, other.get(key));
        }
        return this;
    }

    final HttpBody remove(@NonNull String key) {
        params.remove(key);
        return this;
    }

    final boolean isEmpty() {
        return params.isEmpty();
    }

    final boolean containKey(@NonNull String key) {
        return params.containsKey(key);
    }

    final int getCount() {
        return params.size();
    }

    public String getUrl() {
        return url;
    }

    public Map<String, KeyValuePair<ContentType, Serializable>> getParams() {
        return Collections.unmodifiableMap(this.params);
    }

    HttpBody setContentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    public ContentType getContentType() {
        return contentType;
    }

    HttpBody setMethod(HttpMethod method) {
        this.method = method;
        return this;
    }

    public HttpMethod getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return "HttpBody{" +
                "url='" + url + '\'' +
                ", method=" + method +
                ", contentType=" + contentType +
                ", params=" + params +
                '}';
    }
}
