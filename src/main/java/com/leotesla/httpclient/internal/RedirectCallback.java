package com.leotesla.httpclient.internal;

/**
 * 如果发生重定向请求301, 302回调
 *
 * @version 0.1
 *
 * Created by Tesla on 2016/6/12.
 */
public interface RedirectCallback {

    void result(String result, String origin);

    void error(int code, String msg);

}