package com.leotesla.httpclient.internal;

import android.support.annotation.NonNull;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

/**
 * Http 异常处理
 *
 * @version 1.0
 *
 * Created by Tesla on 2016/7/24.
 */

public final class HttpException extends Exception {

    private static final long serialVersionUID = -1769649670566410276L;

    private ErrorType type = ErrorType.Connect;

    HttpException(@NonNull ErrorType type) {
        super(new Throwable(type.getError()));
        this.type = type;
    }

    HttpException(@NonNull Exception exception) {
        super(exception);
        if (SocketTimeoutException.class.isInstance(exception)) {
            this.type = ErrorType.Timeout;
        } else if (InvalidParameterException.class.isInstance(exception)) {
            this.type = ErrorType.Params;
        } else if (SSLHandshakeException.class.isInstance(exception) ||
                SSLPeerUnverifiedException.class.isInstance(exception)) {
            this.type = ErrorType.SSL;
        } else if (MalformedURLException.class.isInstance(exception)) {
            this.type = ErrorType.Url;
        } else if (NoRouteToHostException.class.isInstance(exception)) {
            this.type = ErrorType.Route;
        } else if (UnknownHostException.class.isInstance(exception)) {
            this.type = ErrorType.Host;
        } else if (FileNotFoundException.class.isInstance(exception)) {
            this.type = ErrorType.NotFound;
        } else {
            this.type = ErrorType.Connect;
        }
    }

    /**
     * 是否可以重试，比如404这种异常就不需要重试了，一般网络型异常可以重试
     */
    public boolean needRetry() {
        return ErrorType.isRelayNetwork(this.type);
    }

    public ErrorType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "type: " + this.type + "; " + super.toString();
    }

}
