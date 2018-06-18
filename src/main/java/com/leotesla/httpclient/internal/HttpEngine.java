package com.leotesla.httpclient.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Closeable;

/**
 * Http 请求核心类
 * 必须有具体http请求框架进行实现，volley,okhttp...
 *
 * @version 1.0
 *
 * Created by Tesla on 2016/5/11.
 */
interface HttpEngine extends Closeable {

    /**
     * 初始化实例对象
     */
    boolean init(@NonNull HttpRequest request) throws HttpException;

    /**
     * 请求准备，比如设置通用的请求属性
     */
    boolean prepare(@NonNull HttpRequest request) throws HttpException;

    /**
     * http get 请求
     * @return                  响应结果
     */
    HttpResponse get(@NonNull HttpRequest request) throws HttpException;

    /**
     * http post 请求
     * @return                  响应结果
     */
    HttpResponse post(@NonNull HttpRequest request) throws HttpException;

    /**
     * http put 请求
     * @return                  响应结果
     */
    HttpResponse put(@NonNull HttpRequest request) throws HttpException;

    /**
     * http delete 请求
     * @return                  响应结果
     */
    HttpResponse delete(@NonNull HttpRequest request) throws HttpException;

    /**
     * 尝试从缓存获取
     * @param request   请求
     * @return 缓存响应结果，过期或者缓存不存在则返回null
     */
    @Nullable
    HttpResponse readCache(@NonNull HttpRequest request) throws HttpException;

    /**
     * 写入缓存
     * @param response   正常响应
     */
    void writeCache(@NonNull HttpResponse response) throws HttpException;

    /**
     * 关闭请求，包括所有打开的数据流和缓存流
     */
    void close();

}
