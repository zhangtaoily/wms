package com.wms.common.thread;

public class UserContext {
    public static ThreadLocal<Long> USER_ID = new ThreadLocal<>();
}
