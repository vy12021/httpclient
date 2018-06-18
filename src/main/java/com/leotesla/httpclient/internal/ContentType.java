package com.leotesla.httpclient.internal;

import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * http 请求类型
 * http request head "Accept"="*\/"
 * http response head "Content-Type"=?
 *
 * @version 1.0
 *
 * Created by Tesla on 2016/12/11.
 */

public enum ContentType {


    // 请求默认响应结果类型
    All("*/*", "utf-8", "gzip, deflate"),

    // 文本流
    Text("text/plain", "utf-8", "gzip, deflate"),
    // 网页类型
    Html("text/html", "utf-8", "gzip, deflate"),
    // 二进制流
    Octet("application/octet-stream", "", "gzip, deflate"),
    // json类型
    Json("application/json", "utf-8", "gzip, deflate"),
    // 表单类型
    Form("application/x-www-form-urlencoded", "", "gzip, deflate"),
    // 多类型混合
    Multipart("multipart/form-data", "", "gzip, deflate");

    // 响应内容类型
    private String type;
    // 字符集
    private String charset;
    // 编码方式
    private String encoding;

    public String getType() {
        return this.type + (TextUtils.isEmpty(charset) ? "" : ("; charset=" + charset));
    }

    public String getAccept() {
        return this.type;
    }

    public String getAcceptCharset() {
        return this.charset;
    }

    public String getAcceptEncoding() {
        return this.encoding;
    }

    ContentType(@NonNull String type, @NonNull String charset, @NonNull String encodings) {
        this.type = type;
        this.charset = charset;
        this.encoding = encodings;
    }

}
