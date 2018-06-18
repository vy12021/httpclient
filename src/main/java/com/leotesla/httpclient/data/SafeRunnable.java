package com.leotesla.httpclient.data;

/**
 * 自带异常捕获Runnable
 *
 * @version 1.0
 *
 * Created by LeoTesla on 4/13/2017.
 */

public abstract class SafeRunnable implements Runnable {

    // 需要执行逻辑
    private Runnable runnable;

    public SafeRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public final void run() {
        try {
            if (null != runnable) {
                runnable.run();
            }
        } catch (Exception e) {
            onException(e);
        }
    }

    /**
     * 发生异常
     */
    public abstract void onException(Exception e);

}
