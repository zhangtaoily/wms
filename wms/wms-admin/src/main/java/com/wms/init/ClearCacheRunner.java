package com.wms.init;

import com.wms.common.utils.redis.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClearCacheRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        RedisUtils.deleteObject("SERVER_TOKEN");
    }
}
