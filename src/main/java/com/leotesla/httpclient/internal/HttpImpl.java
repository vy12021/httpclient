package com.leotesla.httpclient.internal;

/**
 * 请求实现
 *
 * @version 1.0
 *
 * Created by Tesla on 2016/12/10.
 */

public enum HttpImpl {

    // jdk HttpUrlConnection 实现
    UrlConnection,
    // github 开源框架 okhttp 实现
    OkHttp,

}
