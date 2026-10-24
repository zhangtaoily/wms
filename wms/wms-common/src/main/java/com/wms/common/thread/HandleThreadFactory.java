package com.wms.common.thread;

import java.util.concurrent.ThreadFactory;

public class HandleThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        // java -jar 模式下必须显式绑定 LaunchedURLClassLoader：
        // 池化线程是惰性创建的，若首个任务由 contextClassLoader 为 AppClassLoader 的线程提交，
        // worker 线程将无法加载 BOOT-INF/lib 中的类（MyBatis 反射加载 Mapper 会抛 ClassNotFoundException）。
        thread.setContextClassLoader(HandleThreadFactory.class.getClassLoader());
        thread.setUncaughtExceptionHandler(new UncaughtExceptionHandle());
        return thread;
    }
}
