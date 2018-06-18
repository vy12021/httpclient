package com.leotesla.httpclient.internal;

/**
 * 请求错误类型
 *
 * @version 1.0
 *
 * Created by Tesla on 2016/12/11.
 */

public enum ErrorType {

    // 未知错误
    Unknown("unknown"),
    // 参数错误
    Params("invalid params"),
    // 超时错误
    Timeout("connect timeout"),
    // ssl 握手异常
    SSL("ssl error"),
    // 编码错误
    Encode("invalid url encode"),
    // url 语法错误
    Url("invalid url format"),
    // 其他连接错误
    Connect("connect failed"),
    // 无法路由到主机
    Route("unreachable host"),
    // 未知主机(一般是ip不存在)
    Host("unknown host"),
    // 40x
    NotFound("unknown url"),
    // 50x
    Server("sever exception"),
    // 自定义错误码
    Service("unknown service");

    private String error;

    public String getError() {
        return this.error;
    }

    ErrorType(String error) {
        this.error = error;
    }

    /**
     * 是否网络相关
     */
    public static boolean isRelayNetwork(ErrorType type) {
        return Timeout == type || SSL == type || Connect == type || Route == type || Host == type;
    }

}
