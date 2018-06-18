package com.leotesla.httpclient;

import android.support.annotation.NonNull;

import com.leotesla.httpclient.internal.HttpDispatcher;
import com.leotesla.httpclient.internal.HttpImpl;
import com.leotesla.httpclient.internal.HttpMethod;
import com.leotesla.httpclient.internal.HttpRequest;
import com.leotesla.httpclient.internal.HttpResponse;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws InterruptedException {
        HttpDispatcher.send(HttpRequest.create(HttpImpl.OkHttp, HttpMethod.GET, "http://www.baidu.com"),
                new HandlerCallback<String>(null) {
                    @Override
                    protected void onPostActionError(Exception e) {

                    }

                    @Override
                    public boolean onHttpSuccess(@NonNull HttpResponse response) {
                        System.out.print(response.toString());
                        return false;
                    }

                    @Override
                    public void onHttpFailed(@NonNull HttpResponse response) {

                    }

                    @Override
                    public void onHttpCanceled(@NonNull HttpRequest request) {

                    }
                });
        Thread.sleep(10000);
    }
}