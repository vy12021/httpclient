package com.leotesla.httpclient.internal;

/**
 * http请求方式
 *
 * @version 1.0
 *
 * Created by Tesla on 2016/11/27.
 */

public enum HttpMethod {

    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE");

    private String name;

    public String getName() {
        return this.name;
    }

    HttpMethod(String name) {
        this.name = name;
    }

}
