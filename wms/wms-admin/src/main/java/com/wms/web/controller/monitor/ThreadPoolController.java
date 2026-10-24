package com.wms.web.controller.monitor;

import com.wms.common.core.domain.R;
import com.wms.common.thread.CustomThreadPoolMonitor;
import com.wms.common.thread.ThreadPool;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/thread/pool")
public class ThreadPoolController {

    @GetMapping()
    public R<Map<String, Object>> getInfo() throws Exception {

        CustomThreadPoolMonitor monitor = new CustomThreadPoolMonitor((ThreadPoolExecutor) ThreadPool.oneThreadExecutor);

        return R.ok(monitor.monitor());
    }
}
