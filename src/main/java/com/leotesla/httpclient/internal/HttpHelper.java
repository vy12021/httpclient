package com.leotesla.httpclient.internal;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.leotesla.httpclient.data.KeyValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

/**
 * Http 工具类
 *
 * @version 1.0
 *
 * Created by Tesla on 2016/6/12.
 */
public final class HttpHelper {

    private HttpHelper() {}

    static String generateUrl(@NonNull HttpRequest request) {
        final HttpBody body = request.getBody();
        StringBuilder urlBuilder = new StringBuilder(body.getUrl());
        switch (request.getMethod()) {
            case GET:
            case DELETE:
                Map<String, KeyValuePair<ContentType, Serializable>> params = body.getParams();
                if (!params.isEmpty()) {
                    urlBuilder.append("?");
                    urlBuilder.append(serializeParams(params));
                }
                break;
            case POST:
                break;
            case PUT:
                break;
        }

        urlBuilder.trimToSize();

        return urlBuilder.toString();
    }

    /**
     * 序列化参数列表
     * @param params    参数列表
     */
    static String serializeParams(Map<String, KeyValuePair<ContentType, Serializable>> params) {
        StringBuilder content = new StringBuilder();
        Iterator<String> keyIterator = params.keySet().iterator();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            String value;
            if (null == params.get(key).value) {
                value = "";
            } else {
                value = params.get(key).value.toString();
            }
            try {
                if (!TextUtils.isEmpty(value)) {
                    value = URLEncoder.encode(value, "utf-8");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            content.append(key).append("=").append(value);
            if (keyIterator.hasNext()) {
                content.append("&");
            }
        }

        return content.toString();
    }

    /**
     * 从流中读取字符串
     */
    static String getString(@NonNull InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[1024];
        int len;
        while (-1 != (len = reader.read(buffer))) {
            sb.append(buffer, 0, len);
        }
        sb.trimToSize();

        return sb.toString();
    }

    /**
     * 处理请求响应错误
     * @param response      响应体
     */
    static void handleResponseError(@NonNull HttpResponse response) throws HttpException {
        int codeType = response.getStatusCode() / 100;
        if (404 == response.getStatusCode()) {
            // 40x资源找不到
            throw new HttpException(ErrorType.NotFound);
        } else if (5 == codeType) {
            // 50x服务器错误
            throw new HttpException(ErrorType.Server);
        } else if (2 != codeType && 3 != codeType) {
            // 自定义错误
            throw new HttpException(ErrorType.Server);
        }
    }

    /**
     * 任意对象转换为json字符串
     * @param object    对象
     * @return          json字符串
     */
    public static <T> String toJSONString(T object) {
        return JSON.toJSONString(object);
    }

    /**
     * 任意map对象转换为json字符串
     * @param pairs 键值对
     * @return      json字符串
     */
    public static <T> String toJSONString(Map<String, T> pairs) {
        JSONObject jsonObject = new JSONObject();
        for (String key : pairs.keySet()) {
            Object value = pairs.get(key);
            try {
                Object object = JSON.parse(value.toString());
                jsonObject.put(key, object);
            } catch (Exception e) {
                jsonObject.put(key, value);
            }
        }
        return jsonObject.toJSONString();
    }

    /**
     * 获取某个泛型参数
     * @param index     泛型参数位置
     * @return  使用时需要自行转换为类似Class<T>使用
     */
    public static Class<?> getParamTypeAt(@NonNull Class cls, int index) {
        Type type = cls.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] params = ((ParameterizedType) type).getActualTypeArguments();
            if (index < params.length && params[index] instanceof Class) {
                return (Class<?>) params[index];
            }
        }

        return Void.TYPE;
    }

}
