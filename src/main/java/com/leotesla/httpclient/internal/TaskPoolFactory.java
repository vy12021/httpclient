package com.leotesla.httpclient.internal;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池工厂
 *
 * @version 1.0
 *
 * Created by Tesla on 2014/11/12.
 */
public final class TaskPoolFactory {

    private static final RejectedExecutionHandler DEFAULT_HANDLER = new ThreadPoolExecutor.AbortPolicy();

    /** 默认全局线程池
     * corePoolSize: 线程池的核心线程数，默认情况下， 核心线程会在线程池中一直存活， 即使处于闲置状态. 但如果将allowCoreThreadTimeOut设置为true的话, 那么核心线程也会有超时机制， 在keepAliveTime设置的时间过后， 核心线程也会被终止.
     * maximumPoolSize: 最大的线程数， 包括核心线程， 也包括非核心线程， 在线程数达到这个值后，新来的任务将会被阻塞.
     * keepAliveTime: 超时的时间， 闲置的非核心线程超过这个时l长，讲会被销毁回收， 当allowCoreThreadTimeOut为true时，这个值也作用于核心线程.
     * unit：超时时间的时间单位.
     * workQueue：线程池的任务队列， 通过execute方法提交的runnable对象会存储在这个队列中.
     * threadFactory: 线程工厂, 为线程池提供创建新线程的功能.
     * handler: 任务无法执行时，回调handler的rejectedExecution方法来通知调用者.
    **/
    /*private final static ThreadPoolExecutor GLOBAL_POOL = new ThreadPoolExecutor(
            3, 5, 0, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), Executors.defaultThreadFactory(), DEFAULT_HANDLER);*/

    private TaskPoolFactory() {}

    /**
     * 创建新的线程池
     * @param corePoolSize      核心线程数量
     * @param maximumPoolSize   最大同时开启线程数量，存活周期为0
     * @return
     */
    public static synchronized ThreadPoolExecutor create(int corePoolSize, int maximumPoolSize) {
        return new ThreadPoolExecutor(
                corePoolSize, maximumPoolSize, 0,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

}
