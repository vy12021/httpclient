package com.leotesla.httpclient.internal;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;


import com.leotesla.httpclient.data.KeyValuePair;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Http 请求 okhttp 实现
 *
 * @version 1.0
 *          <p>
 *          Created by Tesla on 2016/7/24.
 */

@SuppressWarnings("all")
final class OkClient implements HttpEngine {

    // 缓存管理
    private HttpCache cache;
    // 客户端工厂
    private final OkHttpClient.Builder CLIENT_BUILDER = new OkHttpClient.Builder();
    // 请求工厂
    private final Request.Builder REQUEST_BUILDER = new Request.Builder();
    // 客户端
    private OkHttpClient client;
    // okhttp 调用核心
    private Call call;
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
            CLIENT_BUILDER
                    .connectTimeout(config.getWriteTimeout(), TimeUnit.MILLISECONDS)
                    .writeTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS)
                    .followRedirects(config.isFollowRedirect())
                    .followSslRedirects(config.isFollowRedirect())
                    .retryOnConnectionFailure(config.isRetryOnFailed());
            Uri uri = Uri.parse(request.getBody().getUrl());
            // 获取相关域名初始化证书
            KeyValuePair<SSLManager.DefaultSSL, SSLManager.SimpleSSL> sslFactory
                    = SSLManager.getSSLFactory(uri.getHost(), true);
            if (null != sslFactory) {
                CLIENT_BUILDER.sslSocketFactory(sslFactory.value.factory, sslFactory.value.trustManager);
                // CLIENT_BUILDER.certificatePinner()
            }
            client = CLIENT_BUILDER.build();
            // TODO 关闭okhttp内置缓存策略
            // CLIENT_BUILDER.cache(new Cache(new File(config.getCacheDir()), config.getCacheSize()));
            CacheControl.Builder ccb = new CacheControl.Builder();
            CacheConfig cacheConfig = config.getCacheConfig();
            switch (cacheConfig.strategy) {
                case Disable:
                    ccb.noCache();
                    break;
                case JustNow:
                    ccb.maxStale(CacheStrategy.JustNow.defaultExpiredInMs, TimeUnit.MILLISECONDS);
                    break;
                case OneDay:
                    ccb.maxStale(CacheStrategy.OneDay.defaultExpiredInMs, TimeUnit.MILLISECONDS);
                    break;
                case AllTheTime:
                    ccb.maxStale(CacheStrategy.AllTheTime.defaultExpiredInMs, TimeUnit.MILLISECONDS);
                    break;
                case Custom:
                    ccb.maxStale(cacheConfig.expiredMs, TimeUnit.MILLISECONDS);
                    break;
                case Must:
                    ccb.onlyIfCached();
                    break;
            }
            // REQUEST_BUILDER.cacheControl(ccb.build());
            ArrayMap<String, String> headerParams = request.getHeader().getParams();
            for (String headerKey : headerParams.keySet()) {
                // header 如果重复就被替换，否则添加
                REQUEST_BUILDER.header(headerKey, headerParams.get(headerKey));
            }
            REQUEST_BUILDER.header("Accept", ContentType.All.getAccept());
            REQUEST_BUILDER.header("Accept-Charset", ContentType.All.getAcceptCharset());
            if (!TextUtils.isEmpty(request.getConfig().getUserAgent())) {
                REQUEST_BUILDER.header("User-Agent",
                        "Okhttp/3.8.1; " + request.getConfig().getUserAgent());
            } else {
                REQUEST_BUILDER.header("User-Agent", "Okhttp/3.8.1");
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
            // get 方式无请求体
            REQUEST_BUILDER.url(HttpHelper.generateUrl(request))
                    .method(request.getMethod().getName(), null);
            this.call = client.newCall(REQUEST_BUILDER.build());
            Response okResponse = call.execute();
            response.statusCode = okResponse.code();
            ResponseBody responseBody = okResponse.body();
            response.closeable = responseBody;
            response.headers = okResponse.headers().toMultimap();
            if (null != responseBody.contentType()) {
                response.contentType = responseBody.contentType().toString();
                response.contentLen = responseBody.contentLength();
                response.inputStream = responseBody.byteStream();
                switch (request.getBody().getContentType()) {
                    case Json:
                    case Text:
                    case Form:
                    case Html:
                    case All:
                        response.responseContent = responseBody.string();
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
            RequestBody requestBody = null;
            Map<String, KeyValuePair<ContentType, Serializable>> params = request.getBody().getParams();

            if (!params.isEmpty()) {
                final String content = params.get(params.keySet().iterator().next()).value.toString();
                switch (request.getBody().getContentType()) {
                    case Form:
                        // 提交表单
                        FormBody.Builder formBuilder = new FormBody.Builder();
                        for (String key : params.keySet()) {
                            formBuilder.add(key, params.get(key).value.toString());
                        }
                        requestBody = formBuilder.build();
                        break;
                    case Multipart:
                        MultipartBody.Builder builder = new MultipartBody.Builder();
                        for (String key : params.keySet()) {
                            KeyValuePair<ContentType, Serializable> typeAndContent = params.get(key);
                            builder.addFormDataPart(key, null, RequestBody.create(
                                    MediaType.parse(typeAndContent.key.getType()),
                                    typeAndContent.value.toString()));
                        }
                        requestBody = builder.build();
                        break;
                    case Json:
                        // json 对象
                        String jsonParams = "";
                        if (params.containsKey(HttpBody.KEY_OBJECT)) {
                            // 无参数，直接序列化整个
                            jsonParams = HttpHelper.toJSONString(params.get(HttpBody.KEY_OBJECT));
                        } else {
                            // 多参
                            Map<String, String> map = new HashMap<>(params.size());
                            for (String key : params.keySet()) {
                                map.put(key, params.get(key).value.toString());
                            }
                            jsonParams = HttpHelper.toJSONString(map);
                        }
                        requestBody = RequestBody.create(
                                MediaType.parse(ContentType.Json.getType()),
                                jsonParams);
                        break;
                    case Text:
                        // 文本
                        requestBody = RequestBody.create(
                                MediaType.parse(ContentType.Text.getType()), content);
                        break;
                    case Octet:
                        // 上传二进制流
                        requestBody = FormBody.create(
                                MediaType.parse(ContentType.Octet.getType()), content);
                        break;
                    default: {
                        FormBody.Builder defaultBuilder = new FormBody.Builder();
                        for (String key : params.keySet()) {
                            defaultBuilder.add(key, params.get(key).value.toString());
                        }
                        requestBody = defaultBuilder.build();
                        break;
                    }
                }
            } else {
                requestBody = RequestBody.create(
                        MediaType.parse(request.getBody().getContentType().getType()), "");
            }

            REQUEST_BUILDER.url(HttpHelper.generateUrl(request))
                    .method(request.getMethod().getName(), requestBody);

            this.call = client.newCall(REQUEST_BUILDER.build());
            Response okResponse = call.execute();
            response.statusCode = okResponse.code();
            ResponseBody responseBody = okResponse.body();
            response.closeable = responseBody;
            response.headers = okResponse.headers().toMultimap();
            if (null != responseBody.contentType()) {
                response.contentType = responseBody.contentType().toString();
                response.contentLen = responseBody.contentLength();
                response.inputStream = responseBody.byteStream();
                switch (request.getBody().getContentType()) {
                    case Json:
                    case Text:
                    case Html:
                    case Form:
                    case All:
                        response.responseContent = responseBody.string();
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
            RequestBody requestBody = null;
            Map<String, KeyValuePair<ContentType, Serializable>> params = request.getBody().getParams();
            
            if (!params.isEmpty()) {
                final String content = params.get(params.keySet().iterator().next()).value.toString();
                switch (request.getBody().getContentType()) {
                    case Form: {
                        // 提交表单
                        FormBody.Builder formBuilder = new FormBody.Builder();
                        for (String key : params.keySet()) {
                            formBuilder.add(key, params.get(key).value.toString());
                        }
                        requestBody = formBuilder.build();
                        break;
                    }
                    case Multipart:
                        MultipartBody.Builder builder = new MultipartBody.Builder();
                        for (String key : params.keySet()) {
                            KeyValuePair<ContentType, Serializable> typeAndContent = params.get(key);
                            builder.addFormDataPart(key, null, RequestBody.create(
                                    MediaType.parse(typeAndContent.key.getType()),
                                    typeAndContent.value.toString()));
                        }
                        requestBody = builder.build();
                        break;
                    case Json:
                        // json 对象
                        String jsonParams = "";
                        if (params.containsKey(HttpBody.KEY_OBJECT)) {
                            // 无参数，直接序列化整个
                            jsonParams = HttpHelper.toJSONString(params.get(HttpBody.KEY_OBJECT));
                        } else {
                            // 多参
                            Map<String, String> map = new HashMap<>(params.size());
                            for (String key : params.keySet()) {
                                map.put(key, params.get(key).value.toString());
                            }
                            jsonParams = HttpHelper.toJSONString(map);
                        }
                        requestBody = RequestBody.create(
                                MediaType.parse(ContentType.Json.getType()),
                                jsonParams);
                        break;
                    case Text:
                        // 文本
                        requestBody = RequestBody.create(
                                MediaType.parse(ContentType.Text.getType()), content);
                        break;
                    case Octet:
                        // 上传二进制流
                        requestBody = FormBody.create(
                                MediaType.parse(ContentType.Octet.getType()), content);
                        break;
                    default: {
                        FormBody.Builder defaultBuilder = new FormBody.Builder();
                        for (String key : params.keySet()) {
                            defaultBuilder.add(key, params.get(key).value.toString());
                        }
                        requestBody = defaultBuilder.build();
                        break;
                    }
                }
            } else {
                requestBody = RequestBody.create(
                        MediaType.parse(request.getBody().getContentType().getType()), "");
            }

            REQUEST_BUILDER.url(HttpHelper.generateUrl(request))
                    .method(request.getMethod().getName(), requestBody);

            this.call = client.newCall(REQUEST_BUILDER.build());
            Response okResponse = call.execute();
            response.statusCode = okResponse.code();
            ResponseBody responseBody = okResponse.body();
            response.closeable = responseBody;
            response.headers = okResponse.headers().toMultimap();
            if (null != responseBody.contentType()) {
                response.contentType = responseBody.contentType().toString();
                response.contentLen = responseBody.contentLength();
                response.inputStream = responseBody.byteStream();
                switch (request.getBody().getContentType()) {
                    case Json:
                    case Text:
                    case Html:
                    case Form:
                    case All:
                        response.responseContent = responseBody.string();
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
            REQUEST_BUILDER.url(HttpHelper.generateUrl(request))
                    .method(request.getMethod().getName(), null);

            this.call = client.newCall(REQUEST_BUILDER.build());
            Response okResponse = call.execute();
            response.statusCode = okResponse.code();
            ResponseBody responseBody = okResponse.body();
            response.closeable = responseBody;
            response.headers = okResponse.headers().toMultimap();
            if (null != responseBody.contentType()) {
                response.contentType = responseBody.contentType().toString();
                response.contentLen = responseBody.contentLength();
                response.inputStream = responseBody.byteStream();
                switch (request.getBody().getContentType()) {
                    case Json:
                    case Text:
                    case Html:
                    case Form:
                    case All:
                        response.responseContent = responseBody.string();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            if (null != cache) {
                cache.flush();
            }
            if (null != call) {
                call.cancel();
            }
            if (null != request && null != request.getResponse()) {
                request.getResponse().close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class CacheControlInterceptor implements Interceptor {

        private final HttpRequest request;

        public CacheControlInterceptor(HttpRequest request) {
            this.request = request;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);

            return response;
        }
    }

}
