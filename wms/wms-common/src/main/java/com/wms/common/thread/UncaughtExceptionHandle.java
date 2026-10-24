package com.wms.common.thread;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UncaughtExceptionHandle implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("线程异常", e);
        throw new RuntimeException(e);
    }
}
