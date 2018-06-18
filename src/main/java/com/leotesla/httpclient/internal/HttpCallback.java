package com.leotesla.httpclient.internal;

import android.support.annotation.NonNull;

/**
 * http 请求回调
 *
 * @version 1.0
 *
 * Created by Tesla on 2016/12/11.
 */

public interface HttpCallback {

    /**
     * 请求成功
     * @param response  响应
     * @return  true,是否内容可以缓存
     */
    boolean onHttpSuccess(@NonNull HttpResponse response);

    /**
     * 请求失败
     * @param response  响应
     */
    void onHttpFailed(@NonNull HttpResponse response);

    /**
     * 请求取消
     * @param request   请求
     */
    void onHttpCanceled(@NonNull HttpRequest request);

}
