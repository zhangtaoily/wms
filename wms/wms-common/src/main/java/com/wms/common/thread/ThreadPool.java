package com.wms.common.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池，适用于任务密集型的操作，看情况选择固定线程的线程池
 */
public class ThreadPool {

    /**
     * 5个固定线程的线程池
     */
    public static final ExecutorService fiveThreadExecutor = Executors.newFixedThreadPool(5,new HandleThreadFactory());

    /**
     * 10个固定线程的线程池
     */
    public static final ExecutorService tenThreadExecutor = Executors.newFixedThreadPool(10,new HandleThreadFactory());

    public static final ExecutorService fiftenThreadExecutor = Executors.newFixedThreadPool(15,new HandleThreadFactory());

    /**
     * 固定一个线程的线程池
     */
    public static final  ExecutorService oneThreadExecutor = Executors.newFixedThreadPool(1,new HandleThreadFactory());

}
