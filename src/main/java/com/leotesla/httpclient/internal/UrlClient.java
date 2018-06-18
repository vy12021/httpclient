package com.leotesla.httpclient.internal;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.leotesla.httpclient.data.KeyValuePair;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Http 请求 {@link HttpURLConnection } 实现
 *
 * @version 1.0
 *
 * @see OkClient
 * Created by Tesla on 2016/7/24.
 */

final class UrlClient implements HttpEngine {

    // 缓存管理
    private HttpCache cache;
    // http 请求连接对象
    private HttpURLConnection httpConnection;
    // 客户端请求模型
    private HttpRequest request;

    @Override
    public boolean init(@NonNull HttpRequest request) throws HttpException {
        try {
            this.request = request;
            HttpResponse response = new HttpResponse(request);
            request.setResponse(response);

            HttpConfig config = request.getConfig();

            if (config.getCacheConfig().strategy != CacheStrategy.Disable) {
                this.cache = HttpCache.open(config);
                this.cache.preload(request);
            }

            URL url = new URL(HttpHelper.generateUrl(request));
            httpConnection = (HttpURLConnection) url.openConnection();
            SSLManager.initSSLSocketFactory(config.getCerties());
        } catch (Exception e) {
            e.printStackTrace();
            throw new HttpException(e);
        }
        return true;
    }

    @Override
    public boolean prepare(@NonNull HttpRequest request) throws HttpException {
        try {
            HttpConfig config = request.getConfig();
            httpConnection.setRequestMethod(request.getMethod().getName());
            httpConnection.setConnectTimeout((int) config.getWriteTimeout());
            httpConnection.setReadTimeout((int) config.getReadTimeout());
            if (config.isEnableCache()) {
                httpConnection.setUseCaches(true);
                enableHttpResponseCache(config.getCacheDir(), config.getCacheSize());
            } else {
                httpConnection.setUseCaches(false);
            }
            httpConnection.setInstanceFollowRedirects(config.isFollowRedirect());
            ArrayMap<String, String> headers = request.getHeader().getParams();
            // 设置请求头
            for (String headerKey : headers.keySet()) {
                httpConnection.addRequestProperty(headerKey, headers.get(headerKey));
            }
            httpConnection.setRequestProperty("Accept", ContentType.All.getAccept());
            httpConnection.setRequestProperty("Accept-Charset", ContentType.All.getAcceptCharset());
            if (!TextUtils.isEmpty(request.getConfig().getUserAgent())) {
                String ua = httpConnection.getHeaderField("User-Agent");
                httpConnection.setRequestProperty("User-Agent", request.getConfig().getUserAgent());
            } else {
                httpConnection.setRequestProperty("User-Agent", "UrlConnection/1.1");
            }
            // https 请求
            if (HttpsURLConnection.class.isInstance(httpConnection)) {
                Uri uri = Uri.parse(request.getBody().getUrl());
                // 获取相关域名初始化证书
                KeyValuePair<SSLManager.DefaultSSL, SSLManager.SimpleSSL> sslFactory
                        = SSLManager.getSSLFactory(uri.getHost(), true);
                if (null != sslFactory) {
                    ((HttpsURLConnection) httpConnection).setSSLSocketFactory(sslFactory.value.factory);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new HttpException(e);
        }
        return true;
    }

    @Override
    public HttpResponse get(@NonNull HttpRequest request) throws HttpException {
        HttpResponse response = request.getResponse();
        try {
            httpConnection.setDoInput(true);
            httpConnection.connect();
            response.statusCode = httpConnection.getResponseCode();
            response.headers = httpConnection.getHeaderFields();
            response.inputStream = httpConnection.getInputStream();
            response.contentType = httpConnection.getContentType();
            response.contentLen = httpConnection.getContentLength();
            if (null != response.inputStream) {
                switch (request.getBody().getContentType()) {
                    case Json:
                    case Text:
                    case Form:
                    case Html:
                    case All:
                        response.responseContent = HttpHelper.getString(response.inputStream);
                        break;
                }
            }
        } catch (Exception e) {
            throw new HttpException(e);
        }
        HttpHelper.handleResponseError(response);

        return response;
    }

    @Override
    public HttpResponse post(@NonNull HttpRequest request) throws HttpException {
        HttpResponse response = request.getResponse();
        try {
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);

            Map<String, KeyValuePair<ContentType, Serializable>> params = request.getBody().getParams();

            StringBuilder paramsBuilder = new StringBuilder();

            switch (request.getBody().getContentType()) {
                case Json:
                    // json 对象
                    if (params.containsKey(HttpBody.KEY_OBJECT)) {
                        // 无参数，直接序列化整个
                        paramsBuilder.append(HttpHelper.toJSONString(params.get(HttpBody.KEY_OBJECT)));
                    } else {
                        // 多参
                        Map<String, String> map = new HashMap<>(params.size());
                        for (String key : params.keySet()) {
                            map.put(key, params.get(key).value.toString());
                        }
                        paramsBuilder.append(HttpHelper.toJSONString(map));
                    }
                    break;
                default:
                    paramsBuilder.append(HttpHelper.serializeParams(params));
            }

            byte[] data = paramsBuilder.toString().getBytes();
            httpConnection.setFixedLengthStreamingMode(data.length);
            httpConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            httpConnection.setRequestProperty("Content-Type", request.getBody().getContentType().getType());
            httpConnection.connect();

            httpConnection.getOutputStream().write(data);
            httpConnection.getOutputStream().flush();

            response.statusCode = httpConnection.getResponseCode();
            response.headers = httpConnection.getHeaderFields();
            response.inputStream = httpConnection.getInputStream();
            response.contentType = httpConnection.getContentType();
            response.contentLen = httpConnection.getContentLength();
            if (null != response.inputStream) {
                switch (request.getBody().getContentType()) {
                    case Json:
                    case Text:
                    case Html:
                    case Form:
                    case All:
                        response.responseContent = HttpHelper.getString(response.inputStream);
                        break;
                }
            }
        } catch (Exception e) {
            throw new HttpException(e);
        }
        HttpHelper.handleResponseError(response);

        return response;
    }

    @Override
    public HttpResponse put(@NonNull HttpRequest request) throws HttpException {
        HttpResponse response = request.getResponse();
        try {
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);

            Map<String, KeyValuePair<ContentType, Serializable>> params = request.getBody().getParams();

            StringBuilder paramsBuilder = new StringBuilder();
            switch (request.getBody().getContentType()) {
                case Json:
                    // json 对象
                    if (params.containsKey(HttpBody.KEY_OBJECT)) {
                        // 无参数，直接序列化整个
                        paramsBuilder.append(HttpHelper.toJSONString(params.get(HttpBody.KEY_OBJECT)));
                    } else {
                        // 多参
                        Map<String, String> map = new HashMap<>(params.size());
                        for (String key : params.keySet()) {
                            map.put(key, params.get(key).value.toString());
                        }
                        paramsBuilder.append(HttpHelper.toJSONString(map));
                    }
                    break;
                default:
                    paramsBuilder.append(HttpHelper.serializeParams(params));
            }

            byte[] data = paramsBuilder.toString().getBytes();
            httpConnection.setFixedLengthStreamingMode(data.length);
            httpConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            httpConnection.setRequestProperty("Content-Type", request.getBody().getContentType().getType());
            httpConnection.connect();

            httpConnection.getOutputStream().write(data);
            httpConnection.getOutputStream().flush();

            response.statusCode = httpConnection.getResponseCode();
            response.headers = httpConnection.getHeaderFields();
            response.inputStream = httpConnection.getInputStream();
            response.contentType = httpConnection.getContentType();
            response.contentLen = httpConnection.getContentLength();
            if (null != response.inputStream) {
                switch (request.getBody().getContentType()) {
                    case Json:
                    case Text:
                    case Html:
                    case Form:
                    case All:
                        response.responseContent = HttpHelper.getString(response.inputStream);
                        break;
                }
            }
        } catch (Exception e) {
            throw new HttpException(e);
        }
        HttpHelper.handleResponseError(response);

        return response;
    }

    @Override
    public HttpResponse delete(@NonNull HttpRequest request) throws HttpException {
        HttpResponse response = request.getResponse();
        try {
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);
            httpConnection.connect();
            response.statusCode = httpConnection.getResponseCode();
            response.headers = httpConnection.getHeaderFields();
            response.inputStream = httpConnection.getInputStream();
            response.contentType = httpConnection.getContentType();
            response.contentLen = httpConnection.getContentLength();
            if (null != response.inputStream) {
                switch (request.getBody().getContentType()) {
                    case Json:
                    case Text:
                    case Html:
                    case Form:
                    case All:
                        response.responseContent = HttpHelper.getString(response.inputStream);
                        break;
                }
            }
        } catch (Exception e) {
            throw new HttpException(e);
        }
        HttpHelper.handleResponseError(response);

        return response;
    }

    @Override
    @Nullable
    public HttpResponse readCache(@NonNull HttpRequest request) throws HttpException {
        try {
            if (null != this.cache) {
                return this.cache.read(request);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void writeCache(@NonNull HttpResponse response) throws HttpException {
        try {
            if (null != this.cache) {
                this.cache.write(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            if (null != cache) {
                cache.flush();
            }
            if (null != httpConnection) {
                // 可以关闭所有数据流
                httpConnection.disconnect();
            }
            if (null != request && null != request.getResponse()) {
                request.getResponse().close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * HttpUrlConnection 在api 14 之后加入了 缓存功能，在14 上默认是关闭的，通过反射打开
     * @param cacheDir      缓存目录
     * @param maxSize       最大缓存
     * @throws Exception    初始化失败使用 自己的缓存功能
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void enableHttpResponseCache(@NonNull String cacheDir, long maxSize) throws Exception {
        File httpCacheDir = new File(cacheDir, "http");
        Class.forName("android.net.http.HttpResponseCache")
                .getMethod("install", File.class, long.class)
                .invoke(null, httpCacheDir, maxSize);
    }

}
