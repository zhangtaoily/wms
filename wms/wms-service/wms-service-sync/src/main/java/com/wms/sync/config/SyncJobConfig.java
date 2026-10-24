package com.wms.sync.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * M7 同步工程化：启用 Spring 定时任务（同步补偿重试 Job）
 */
@Configuration
@EnableScheduling
public class SyncJobConfig {
}
