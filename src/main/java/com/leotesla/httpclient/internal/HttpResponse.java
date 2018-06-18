package com.leotesla.httpclient.internal;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Http 响应体
 *
 * @version 1.0
 *
 * Created by Tesla on 2016/7/24.
 */

public final class HttpResponse implements Closeable {

    // 请求
    final HttpRequest request;
    // 响应码
    int statusCode;
    // 响应内容
    String responseContent = "";
    // 响应流
    InputStream inputStream;
    // 响应内容长度
    long contentLen;
    // 响应类型
    String contentType = "";
    // 异常
    HttpException exception;
    // 响应流
    Closeable closeable;
    // 是否缓存
    boolean isCache;
    // 返回的头部保存起来，key为头部key; value分两部分
    Map<String, List<String>> headers = Collections.emptyMap();

    /**
     * 从缓存构建
     * @param request   请求对象
     * @param cache     缓存数据
     */
    static HttpResponse fromCache(HttpRequest request, String cache, Map<String, List<String>> head) {
        HttpResponse response = new HttpResponse(request);
        request.setResponse(response);
        response.isCache = true;
        response.statusCode = 200;
        response.responseContent = cache;
        response.contentLen = cache.length();
        response.headers = head;
        return response;
    }

    HttpResponse(HttpRequest request) {
        this.request = request;
        this.request.setResponse(this);
    }

    public HttpRequest getRequest() {
        return request;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getContent() {
        return null == responseContent ? "" : responseContent;
    }

    public String getContentType() {
        return null == contentType ? "" : contentType;
    }

    public long getContentLength() {
        return contentLen;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public HttpException getException() {
        return exception;
    }

    public boolean isCache() {
        return isCache;
    }

    public Map<String, List<String>> getHeader(boolean pure) {
        if (pure && null != headers && !headers.isEmpty()) {
            // 去除一些影响序列化信息
            Map<String, List<String>> pureHead = new HashMap<>(headers);
            pureHead.remove("null");
            pureHead.remove("");
            pureHead.remove(null);
            return pureHead;
        } else {
            if (null != this.headers && !this.headers.isEmpty()) {
                return Collections.unmodifiableMap(this.headers);
            } else {
                return Collections.emptyMap();
            }
        }
    }

    public String getLocation() {
        if (statusCode == 302) {
            if (headers.containsKey("location")) {
                return headers.get("location").get(0);
            } else if (headers.containsKey("Location")) {
                return headers.get("Location").get(0);
            }
        }
        return "";
    }

    @Override
    public final void close() throws IOException {
        if (null != inputStream) inputStream.close();
        if (null != closeable) closeable.close();
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "\n isCache=" + isCache +
                ",\n headers=" + headers +
                ",\n request=" + request +
                ",\n statusCode=" + statusCode +
                ",\n responseContent='" + responseContent + '\'' +
                ",\n contentLen=" + contentLen +
                ",\n contentType='" + contentType + '\'' +
                ",\n exception=" + exception +
                '}';
    }
}
