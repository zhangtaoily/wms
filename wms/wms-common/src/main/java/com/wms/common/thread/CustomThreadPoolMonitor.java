package com.wms.common.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class CustomThreadPoolMonitor {
    private final ThreadPoolExecutor executor;

    public CustomThreadPoolMonitor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public Map<String,Object> monitor() {
        Map<String,Object> map = new HashMap<>();
        map.put("Pool Size", executor.getPoolSize());
        map.put("Active Count", executor.getActiveCount());
        map.put("Completed Tasks", executor.getCompletedTaskCount());
        map.put("Total number of tasks", executor.getTaskCount());
        map.put("Queue Size", executor.getQueue().size());
        return map;
    }
}
